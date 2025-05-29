package com.quackology.duckdevices.utils;

import java.lang.reflect.Array;
import java.util.HashMap;

import org.jtransforms.fft.DoubleFFT_1D;

import com.quackology.duckdevices.spaces.Complex;
import com.quackology.duckdevices.utils.cern.jet.math.Bessel;

/**
 * Utility class for various operations
 */
public class Utils {

    /**
     * The tolerance for double a == double b
     */
    private static double TOLERANCE = 0.0001;

    /**
     * The precision of the bessel function
     */
    private static double besselPrecision = 10;

	/**
	 * Convert double array to complex array
	 * <p>
	 * double[2n] - real
	 * <p>
	 * double[2n+1] - imaginary
	 *
	 * @param arr the double array to convert
	 * @param length the length of the resulting array
	 * @return the complex array
	 */
    private static Complex[] doubleToComplex(double[] arr, int length) {
        Complex[] out = new Complex[length];
        for (int i = 0; i < length; i++) {
            out[i] = new Complex(arr[i*2], arr[i*2+1]);
        }
        return out;
    }

	/**
	 * Convert complex array to double array
	 * <p>
	 * double[2n] - real
	 * <p>
	 * double[2n+1] - imaginary
	 *
	 * @param arr the complex array to convert
	 * @return the double array
	 */
    private static double[] complexToDouble(Complex[] arr) {
        double[] out = new double[arr.length*2];
        for (int i = 0; i < arr.length; i++) {
			if (arr[i] != null) {
                out[i*2] = arr[i].getReal();
                out[i*2+1] = arr[i].getImg();
			} else {
				out[i*2] = 0;
				out[i*2+1] = 0;
			}
        }
        return out;
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * 
     * @param values an array of real numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft(double[] values) {
		DoubleFFT_1D transformer;
		if (transformerTable.get(values.length) == null) {
			transformer = new DoubleFFT_1D(values.length);
		} else {
			transformer = transformerTable.get(values.length);
		}
        double[] formattedValue = new double[values.length*2];
		System.arraycopy(values, 0, formattedValue, 0, values.length);
        transformer.realForwardFull(formattedValue);
        return doubleToComplex(formattedValue, values.length);
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * 
     * @param values an array of complex numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft(Complex[] values) {
		DoubleFFT_1D transformer;
		if (transformerTable.get(values.length) == null) {
			transformer = new DoubleFFT_1D(values.length);
		} else {
			transformer = transformerTable.get(values.length);
		}
        double[] formattedValue = complexToDouble(values);
        transformer.complexForward(formattedValue);
        return doubleToComplex(formattedValue, values.length);
    }

    /**
     * Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
     * Scaled
     * 
     * @param values an array of complex numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] ifft(Complex[] values) {
		DoubleFFT_1D transformer;
		if (transformerTable.get(values.length) == null) {
			transformer = new DoubleFFT_1D(values.length);
		} else {
			transformer = transformerTable.get(values.length);
		}
        double[] formattedValue = complexToDouble(values);
        transformer.complexInverse(formattedValue, true);
        return doubleToComplex(formattedValue, values.length);
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
     * 
     * @param values an array of real numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] paddedFFT(double[] values, int length) {
        double[] padded = new double[length];
		System.arraycopy(values, 0, padded, 0, values.length);

        return fft(padded);
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
     * 
     * @param values an array of complex numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] paddedFFT(Complex[] values, int length) {
        Complex[] padded = new Complex[length];
        System.arraycopy(values, 0, padded, 0, values.length);

        return fft(padded);
    }

    /**
     * Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
     * 
     * @param values an array of complex numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] paddedIFFT(Complex[] values, int length) {
        Complex[] padded = new Complex[length];
        System.arraycopy(values, 0, padded, 0, values.length);

        return ifft(padded);
    }

	/**
	 * Map of transformers for faster computation but higher memory usage
	 */
	private static HashMap<Integer, DoubleFFT_1D> transformerTable = new HashMap<Integer, DoubleFFT_1D>();

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
	 * <p>
	 * Cached transformer for faster computation of the same length
     * 
     * @param values an array of real numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
	public static Complex[] cachedFFT(double[] values) {
		if (transformerTable.get(values.length) == null) {
			transformerTable.put(values.length, new DoubleFFT_1D(values.length));
		}
		return fft(values);
	}

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
	 * <p>
	 * Cached transformer for faster computation of the same length
     * 
     * @param values an array of complex numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
	public static Complex[] cachedFFT(Complex[] values) {
		if (transformerTable.get(values.length) == null) {
			transformerTable.put(values.length, new DoubleFFT_1D(values.length));
		}
		return fft(values);
	}

    /**
     * Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
	 * Cached transformer for faster computation of the same length
     * 
     * @param values an array of complex numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
	public static Complex[] cachedIFFT(Complex[] values) {
		if (transformerTable.get(values.length) == null) {
			transformerTable.put(values.length, new DoubleFFT_1D(values.length));
		}
		return ifft(values);
	}

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
	 * <p>
	 * Cached transformer for faster computation of the same length
     * 
     * @param values an array of real numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
	public static Complex[] cachedPaddedFFT(double[] values, int length) {
		if (transformerTable.get(length) == null) {
			transformerTable.put(length, new DoubleFFT_1D(length));
		}
		return paddedFFT(values, length);
	}
	
    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
	 * <p>
	 * Cached transformer for faster computation of the same length
     * 
     * @param values an array of complex numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
	public static Complex[] cachedPaddedFFT(Complex[] values, int length) {
		if (transformerTable.get(length) == null) {
			transformerTable.put(length, new DoubleFFT_1D(length));
		}
		return paddedFFT(values, length);
	}

    /**
     * Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Computed through JTransforms library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
	 * <p>
	 * Cached transformer for faster computation of the same length
     * 
     * @param values an array of complex numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
	public static Complex[] cachedPaddedIFFT(Complex[] values, int length) {
		if (transformerTable.get(length) == null) {
			transformerTable.put(length, new DoubleFFT_1D(length));
		}
		return paddedIFFT(values, length);
	}

    /**
     * Convolution of two arrays through 0-padding and Convolution Theorem
     * <p>
     * f * g = IFFT(FFT(f) x FFT(g)) such that FFT(f) and FFT(g) are of length f.length + g.length - 1
     * 
     * @param a first array of real numbers to be convolved
     * @param b second array of real numbers to be convolved
     * @param length the length of the resulting array, 0 < length <= a.length + b.length - 1
     * @return the resulting array of real numbers after convolution truncated to length
     */
    public static double[] convolve(double[] a, double[] b, int length) {
        Complex[] complexA = new Complex[a.length];
        Complex[] complexB = new Complex[b.length];
        for (int i = 0; i < a.length; i++) {
            complexA[i] = new Complex(a[i], 0);
        }
        for (int i = 0; i < b.length; i++) {
            complexB[i] = new Complex(b[i], 0);
        }

        Complex[] result = convolve(complexA, complexB, length);
        double[] out = new double[length];
        for (int i = 0; i < length; i++) {
            out[i] = result[i].getReal();
        }
        return out;
    }

    /**
     * Convolution of two arrays through 0-padding and Convolution Theorem
     * <p>
     * f * g = IFFT(FFT(f) x FFT(g)) such that FFT(f) and FFT(g) are of length f.length + g.length - 1
     * 
     * @param a first array of real numbers to be convolved
     * @param b second array of real numbers to be convolved
     * @return the resulting array of real numbers after convolution with length a.length + b.length - 1
     */
    public static double[] convolve(double[] a, double[] b) {
        return convolve(a, b, a.length + b.length - 1);
    }

    /**
     * Convolution of two arrays through 0-padding and Convolution Theorem
     * <p>
     * f * g = IFFT(FFT(f) x FFT(g)) such that FFT(f) and FFT(g) are of length f.length + g.length - 1
     * 
     * @param a first array of complex numbers to be convolved
     * @param b second array of complex numbers to be convolved
     * @param length the length of the resulting array, 0 &lt; length &lt;= a.length + b.length - 1
     * @return the resulting array of Complex numbers after convolution truncated to length
     */
    public static Complex[] convolve(Complex[] a, Complex[] b, int length) {
        if (0 >= length || length > a.length + b.length - 1) {
            throw new IllegalArgumentException("Length must be between 0 and a.length + b.length - 1");
        }
        int size = a.length + b.length-1;
        Complex[] fftA = paddedFFT(a, size);
        Complex[] fftB = paddedFFT(b, size);

        Complex[] result = new Complex[size];
        for (int i = 0; i < size; i++) {
            result[i] = fftA[i].multiply(fftB[i]);
        }

        return ifft(result);
    }

    /**
     * Convolution of two arrays through 0-padding and Convolution Theorem
     * <p>
     * f * g = IFFT(FFT(f) x FFT(g)) such that FFT(f) and FFT(g) are of length f.length + g.length - 1
     * 
     * @param a first array of complex numbers to be convolved
     * @param b second array of complex numbers to be convolved
     * @return the resulting array of Complex numbers after convolution with length a.length + b.length - 1
     */
    public static Complex[] convolve(Complex[] a, Complex[] b) {
        return convolve(a, b, a.length + b.length - 1);
    }

    /**
     * Cyclic convolution of two arrays through Circular Convolution Theorem
     * <p>
     * f * g = IFFT(FFT(f) x FFT(g)) such that FFT(f) and FFT(g) are of length max(f.length, g.length)
     * 
     * @param a first array of real numbers to be convolved
     * @param b second array of real numbers to be convolved
     * @return the resulting array of real numbers after cyclic convolution
     */
    public static double[] cyclicConvolve(double[] a, double[] b) {
        Complex[] complexA = new Complex[a.length];
        Complex[] complexB = new Complex[b.length];
        for (int i = 0; i < a.length; i++) {
            complexA[i] = new Complex(a[i], 0);
        }
        for (int i = 0; i < b.length; i++) {
            complexB[i] = new Complex(b[i], 0);
        }

        Complex[] result = cyclicConvolve(complexA, complexB);
        double[] out = new double[result.length];
        for (int i = 0; i < result.length; i++) {
            out[i] = result[i].getReal();
        }
        return out;
    }

    /**
     * Cyclic convolution of two arrays through Circular Convolution Theorem
     * <p>
     * f * g = IFFT(FFT(f) x FFT(g)) such that FFT(f) and FFT(g) are of length max(f.length, g.length)
     * 
     * @param a first array of complex numbers to be convolved
     * @param b second array of complex numbers to be convolved
     * @return the resulting array of Complex numbers after cyclic convolution
     */
    public static Complex[] cyclicConvolve(Complex[] a, Complex[] b) {
        int size = Math.max(a.length, b.length);
        Complex[] fftA = paddedFFT(a, size);
        Complex[] fftB = paddedFFT(b, size);

        Complex[] out = new Complex[size];
        for (int i = 0; i < size; i++) {
            out[i] = fftA[i].multiply(fftB[i]).multiply(1.0/size);
        }

        return ifft(out);
    }

    /**
     * Map of factorial values for cached computation but higher memory usage
     */
    private static HashMap<Long, Long> factorialTable = new HashMap<>();

    /**
     * Factorial computation through dynamic programming (memoization)
     * 
     * @param n the number to compute the factorial of
     * @return the factorial of n
     */
    public static long factorialCached(long n) {
        if (factorialTable.get(n) == null) {
            if (n == 0) return 1;
            return factorialCached(n-1) * n;
        }
        return factorialTable.get(n);
    }

    /**
     * Standard factorial computation
     * 
     * @param n the number to compute the factorial of
     * @return the factorial of n
     */
    public static long factorial(long n) {
        long out = 1;
        for (int i = 1; i <= n; i++) {
            out *= i;
        }
        return out;
    }

    /**
     * Standard combination computation
     *
     * @param a
     * @param b
     * @return a choose b
     */
    public static long combination(long a, long b) {
        long minimized = Math.min(b, a-b);
        double out = 1;
        for(int i = 0; i < minimized; i++) {
            out *= a-i;
            out /= i+1;
        }
        return Math.round(out);
    }

    /**
     * Modified Bessel function of the first kind of a generalized order
     * <p>
     * Utilizes Miller's recurrence algorithm
     * <p>
     * https://en.wikipedia.org/wiki/Miller%27s_recurrence_algorithm
     * <p>
     * based on Numeric Recipes
     * 
     * @param k the order of the Bessel function
     * @param x the argument of the Bessel function
     * @return the value of the Bessel function at the given order and argument
     */
    public static double besseli(int k, int x) {

        if (k == 0) {
            return Bessel.i0(x);
        } else if (k == 1) {
            return Bessel.i1(x);
        }

        double scalar = 2.0/x;

        double bi_plus = 1;
        double bi_n = 0;
        double out = 0;

        // backward recurrence
        // increase starting i for more precision
        for (int i = (int)(x+Math.sqrt(x)*besselPrecision)*2; i > 0; i--) {
            double bi_minus = bi_plus + scalar*i*bi_n;
            bi_plus = bi_n;
            bi_n = bi_minus;

            // overflow
            if (Math.abs(bi_n) > 1e10) {
                //scale everything down a couble magnitudes
                bi_plus *= 1e-10;
                bi_n *= 1e-10;
                out *= 1e-10;
            }
            if (i == k) {
                out = bi_plus;
            }
        }
        return out * Bessel.i0(x)/bi_n;
    }

    /**
     * returns a &lt; b for doubles with TOLERANCE
     *
     * @param a first number to be compared
     * @param b second number to be compared
     * @return a &lt; b
     */
    public static boolean lessThan(double a, double b) {
        return a < b && !equals(a, b);
    }

    /**
     * returns a &gt; b for doubles with TOLERANCE
     *
     * @param a first number to be compared
     * @param b second number to be compared
     * @return a &gt; b
     */
    public static boolean greaterThan(double a, double b) {
        return a > b && !equals(a, b);
    }

    /**
     * returns a == b for doubles with TOLERANCE
     *
     * @param a first number to be compared
     * @param b second number to be compared
     * @return a == b
     */
    public static boolean equals(double a, double b) {
        return Math.abs(a-b) < TOLERANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[]... arrays) {
        int length = 0;
        for (T[] array : arrays) length += array.length;
        T[] out = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), length);
        int ind = 0;
        for(T[] array : arrays) {
            System.arraycopy(array, 0, out, ind, array.length);
            ind += array.length;
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] single(T element) {
        T[] out = (T[]) Array.newInstance(element.getClass(), 1);
        out[0] = element;
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] array(T... elements) {
        T[] out = (T[]) Array.newInstance(elements[0].getClass(), elements.length);
        System.arraycopy(elements, 0, out, 0, elements.length);
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] fillArray(T element, int len) {
        T[] out = (T[]) Array.newInstance(element.getClass(), len);
        for(int i = 0; i < out.length; i++) {
            out[i] = element;
        }
        return out;
    }

    public void setBesselPrecision(double precision) {
        besselPrecision = precision;
    }

    public void setTolerance(double tolerance) {
        TOLERANCE = tolerance;
    }
}
