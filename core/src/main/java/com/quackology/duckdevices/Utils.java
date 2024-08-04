package com.quackology.duckdevices;

import java.util.HashMap;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;

import com.quackology.duckdevices.spaces.Complex;

import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.jet.math.tdouble.Bessel;

/**
 * Utility class for various operations
 */
public class Utils {

    /**
     * The precision of the bessel function
     */
    private static double besselPrecision = 10;

    /**
     * General Fast (Discrete) Fourier Transform
     * <p>
     * Computed through parallel colt library
     * 
     * @param values an array of real numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft_general(double[] values) {
        DenseDComplexMatrix1D mat = new DenseDComplexMatrix1D(values.length);
        mat.assignReal(DoubleFactory1D.dense.make(values));

        mat.fft();

        Complex[] out = new Complex[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = new Complex(mat.get(i)[0], mat.get(i)[1]);
        }
        return out;
    }

    /**
     * General Fast (Discrete) Fourier Transform
     * <p>
     * Computed through parallel colt library
     * 
     * @param values an array of complex numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft_general(Complex[] values) {
        double[] real = new double[values.length];
        double[] imag = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            if(values[i] == null) {
                real[i] = 0;
                imag[i] = 0;
            } else {
                real[i] = values[i].getReal();
                imag[i] = values[i].getImg();
            }
        }
        
        DenseDComplexMatrix1D mat = new DenseDComplexMatrix1D(values.length);
        mat.assignReal(DoubleFactory1D.dense.make(real));
        mat.assignImaginary(DoubleFactory1D.dense.make(imag));

        mat.fft();

        Complex[] out = new Complex[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = new Complex(mat.get(i)[0], mat.get(i)[1]);
        }
        return out;
    }

    /**
     * General Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Computed through parallel colt library
     * <p>
     * Scaled
     * 
     * @param values an array of complex numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] ifft_general(Complex[] values) {
        double[] real = new double[values.length];
        double[] imag = new double[values.length];
        for(int i = 0; i < values.length; i++) {
            if(values[i] == null) {
                real[i] = 0;
                imag[i] = 0;
            } else {
                real[i] = values[i].getReal();
                imag[i] = values[i].getImg();
            }
        }
        
        DenseDComplexMatrix1D mat = new DenseDComplexMatrix1D(values.length);
        mat.assignReal(DoubleFactory1D.dense.make(real));
        mat.assignImaginary(DoubleFactory1D.dense.make(imag));

        mat.ifft(true);

        Complex[] out = new Complex[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = new Complex(mat.get(i)[0], mat.get(i)[1]);
        }
        return out;
    }

    /**
     * Transformer object for Fast Fourier Transform through apache commons math library
     */
    private static final FastFourierTransformer FFT_TRANSFORMER = new FastFourierTransformer(DftNormalization.STANDARD);

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through apache commons math library
     * <p>
     * Only accepts sizes that are powers of 2 (but very fast)
     * 
     * @param values an array of real numbers of length 2^n to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft_fast(double[] values) {
        Complex[] out = new Complex[values.length];
        for(int i = 0; i < values.length; i++) {
            out[i] = new Complex(values[i], 0);
        }

        return fft(out);
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through apache commons math library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
     * 
     * @param values an array of real numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft_padded(double[] values, int length) {
        Complex[] padded = new Complex[length];
        for(int i = 0; i < values.length; i++) {
            padded[i] = new Complex(values[i], 0);
        }

        return fft(padded);
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through apache commons math library
     * <p>
     * Only accepts sizes that are powers of 2 (but very fast)
     * 
     * @param values an array of complex numbers of length 2^n to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft_fast(Complex[] values) {
        org.apache.commons.math3.complex.Complex[] mat = new org.apache.commons.math3.complex.Complex[values.length];
        for(int i = 0; i < values.length; i++) {
            if(values[i] == null) {
                mat[i] = new org.apache.commons.math3.complex.Complex(0, 0);
            } else {
                mat[i] = new org.apache.commons.math3.complex.Complex(values[i].getReal(), values[i].getImg());
            }
        }
        org.apache.commons.math3.complex.Complex[] result = FFT_TRANSFORMER.transform(mat, org.apache.commons.math3.transform.TransformType.FORWARD);
        Complex[] out = new Complex[result.length];
        for(int i = 0; i < result.length; i++) {
            out[i] = new Complex(result[i]);
        }

        return out;
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Computed through apache commons math library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
     * 
     * @param values an array of complex numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft_padded(Complex[] values, int length) {
        Complex[] padded = new Complex[length];
        for(int i = 0; i < values.length; i++) {
            padded[i] = values[i];
        }

        return fft(padded);
    }

    /**
     * Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Computed through apache commons math library
     * <p>
     * Only accepts sizes that are powers of 2 (but very fast)
     * <p>
     * Scaled
     * 
     * @param values an array of complex numbers of length 2^n to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] ifft_fast(Complex[] values) {
        org.apache.commons.math3.complex.Complex[] mat = new org.apache.commons.math3.complex.Complex[values.length];
        for(int i = 0; i < values.length; i++) {
            if(values[i] == null) {
                mat[i] = new org.apache.commons.math3.complex.Complex(0, 0);
            } else {
                mat[i] = new org.apache.commons.math3.complex.Complex(values[i].getReal(), values[i].getImg());
            }
        }
        org.apache.commons.math3.complex.Complex[] result = FFT_TRANSFORMER.transform(mat, org.apache.commons.math3.transform.TransformType.INVERSE);
        Complex[] out = new Complex[result.length];
        for(int i = 0; i < result.length; i++) {
            out[i] = new Complex(result[i]);
        }

        return out;
    }

    /**
     * Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Computed through apache commons math library
     * <p>
     * Padded with 0s to the given length (or cull if length is less than values.length)
     * 
     * @param values an array of complex numbers to transform
     * @param length the length of the resulting array
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] ifft_padded(Complex[] values, int length) {
        Complex[] padded = new Complex[length];
        for(int i = 0; i < values.length; i++) {
            padded[i] = values[i];
        }

        return ifft(padded);
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Chooses between general and fast FFT based on size
     * 
     * @param values an array of real numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft(double[] values) {
        int length = values.length;
        Complex[] out;
        if(Math.ceil(Math.log(length)/Math.log(2)) != Math.floor(Math.log(length)/Math.log(2)) || length < values.length) {
            out = fft_general(values);
        } else {
            out = fft_fast(values);
        }
        return out;
    }

    /**
     * Fast (Discrete) Fourier Transform
     * <p>
     * Chooses between general and fast FFT based on size
     * 
     * @param values an array of complex numbers to transform
     * @return an array of complex numbers containing the transformed values
     */
    public static Complex[] fft(Complex[] values) {
        int length = values.length;
        Complex[] out;
        if(Math.ceil(Math.log(length)/Math.log(2)) != Math.floor(Math.log(length)/Math.log(2)) || length < values.length) {
            out = fft_general(values);
        } else {
            out = fft_fast(values);
        }
        return out;
    }

    /**
     * Inverse Fast (Discrete) Fourier Transform
     * <p>
     * Chooses between general and fast IFFT based on size
     * 
     * @param values an array of complex numbers to transform
     * @return
     */
    public static Complex[] ifft(Complex[] values) {
        int length = values.length;
        Complex[] out;
        if(Math.ceil(Math.log(length)/Math.log(2)) != Math.floor(Math.log(length)/Math.log(2)) || length < values.length) {
            out = ifft_general(values);
        } else {
            out = ifft_fast(values);
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
     * @param length the length of the resulting array, 0 < length <= a.length + b.length - 1
     * @return the resulting array of real numbers after convolution truncated to length
     */
    public static double[] convolve(double[] a, double[] b, int length) {
        Complex[] complexA = new Complex[a.length];
        Complex[] complexB = new Complex[b.length];
        for(int i = 0; i < a.length; i++) {
            complexA[i] = new Complex(a[i], 0);
        }
        for(int i = 0; i < b.length; i++) {
            complexB[i] = new Complex(b[i], 0);
        }

        Complex[] result = convolve(complexA, complexB, length);
        double[] out = new double[length];
        for(int i = 0; i < length; i++) {
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
     * @param length the length of the resulting array, 0 < length <= a.length + b.length - 1
     * @return the resulting array of Complex numbers after convolution truncated to length
     */
    public static Complex[] convolve(Complex[] a, Complex[] b, int length) {
        if(0 >= length || length > a.length + b.length - 1) {
            throw new IllegalArgumentException("Length must be between 0 and a.length + b.length - 1");
        }
        int size = a.length + b.length-1;
        Complex[] fftA = fft_padded(a, size);
        Complex[] fftB = fft_padded(b, size);

        Complex[] result = new Complex[size];
        for (int i = 0; i < size; i++) {
            result[i] = fftA[i].multiply(fftB[i]);
        }

        if(Math.ceil(Math.log(size)/Math.log(2)) != Math.floor(Math.log(size)/Math.log(2))) {
            return ifft_general(result);
        } else {
            return ifft(result);
        }
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
        for(int i = 0; i < a.length; i++) {
            complexA[i] = new Complex(a[i], 0);
        }
        for(int i = 0; i < b.length; i++) {
            complexB[i] = new Complex(b[i], 0);
        }

        Complex[] result = cyclicConvolve(complexA, complexB);
        double[] out = new double[result.length];
        for(int i = 0; i < result.length; i++) {
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
        Complex[] fftA = fft_padded(a, size);
        Complex[] fftB = fft_padded(b, size);

        Complex[] out = new Complex[size];
        for (int i = 0; i < size; i++) {
            out[i] = fftA[i].multiply(fftB[i]).multiply(1.0/size);
        }

        return ifft(out);
    }

    /**
     * Map of factorial values for faster computation but higher memory usage
     */
    private static HashMap<Long, Long> factorialTable = new HashMap<Long, Long>();

    /**
     * Factorial computation through dynamic programming (memoization)
     * 
     * @param n the number to compute the factorial of
     * @return the factorial of n
     */
    public static long factorialStored(long n) {
        if(factorialTable.get(n) == null) {
            if(n == 0) return 1;
            return factorialStored(n-1) * n;
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
        for(int i = 1; i <= n; i++) {
            out *= i;
        }
        return out;
    }

    /**
     * Modified Bessel function of the first kind of a generalized order
     * <p>
     * Utilizes Miller's recurrence algorithm
     * <p>
     * https://en.wikipedia.org/wiki/Miller%27s_recurrence_algorithm
     * <p>
     * based on ideas from Numeric Recipes
     * 
     * @param k the order of the Bessel function
     * @param x the argument of the Bessel function
     * @return the value of the Bessel function at the given order and argument
     */
    public static double besseli(int k, int x) {

        if(k == 0) {
            return Bessel.i0(x);
        } else if(k == 1) {
            return Bessel.i1(x);
        }

        double scalar = 2.0/x;

        double bi_plus = 1;
        double bi_n = 0;
        double out = 0;

        // backward recurrence
        // increase starting i for more precision
        for(int i = (int)(x+Math.sqrt(x)*besselPrecision)*2; i > 0; i--) {
            double bi_minus = bi_plus + scalar*i*bi_n;
            bi_plus = bi_n;
            bi_n = bi_minus;

            // overflow
            if(Math.abs(bi_n) > 1e10) {
                //scale everything down a couble magnitudes
                bi_plus *= 1e-10;
                bi_n *= 1e-10;
                out *= 1e-10;
            }
            if(i == k) {
                out = bi_plus;
            }
        }
        return out * Bessel.i0(x)/bi_n;
    }

    public void setBesselPrecision(double precision) {
        besselPrecision = precision;
    }
}
