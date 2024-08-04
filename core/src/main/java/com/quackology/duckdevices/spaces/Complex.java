package com.quackology.duckdevices.spaces;

import com.quackology.duckdevices.spaces.manifolds.liegroups.S1;

/**
 * Wrapper class for complex
 * <p>
 * Based on apache commons math library for complex numbers
 */
public class Complex implements Linear {

    private org.apache.commons.math3.complex.Complex value;

    /**
     * Constructor of an imaginary number
     * 
     * @param real the real part of the complex number
     * @param img the imaginary part of the complex number
     */
    public Complex(double real, double img) {
        value = new org.apache.commons.math3.complex.Complex(real, img);
    }

    /**
     * Constructor of a complex number based on the apache commons math library complex number
     * 
     * @param value the complex number to be wrapped
     * @return
     */
    public Complex(org.apache.commons.math3.complex.Complex value) {
        this.value = value;
    }

    /**
     * Gets the real part of the complex number
     * 
     * @return a double representing the real part of the complex number
     */
    public double getReal() {
        return this.value.getReal();
    }

    /**
     * Gets the imaginary part of the complex number
     * 
     * @return a double representing the imaginary part of the complex number
     */
    public double getImg() {
        return this.value.getImaginary();
    }

    /**
     * Gets the number of dimensions in the complex space
     * 
     * @Return the number of dimensions in the complex space
     */
    public int getDimensions() {
        return 2;
    }

    /**
     * Gets the vector representation of the complex number
     * 
     * @return the vector representation of the complex number
     */
    public MatReal toVector() {
        return new MatReal(new double[][] {{getReal()}, {getImg()}});
    }

    /**
     * Gets the magnitude of the complex number
     * <p>
     * Same as absolute value
     * 
     * @return the magnitude of the complex number
     */
    public double mag() {
        return this.value.abs();
    }

    /**
     * Gets the absolute value of the complex number
     * <p>
     * Same as magnitude
     * 
     * @return the absolute value of the complex number
     */
    public double abs() {
        return this.value.abs();
    }

    /**
     * Gets the conjugate of the complex number
     * 
     * @return the conjugate of the complex number
     */
    public Complex conjugate() {
        return new Complex(this.value.conjugate());
    }

    /**
     * Gets the negation of the complex number
     * 
     * @return the negation of the complex number
     */
    public Complex negation() {
        return new Complex(this.value.negate());
    }

    /**
     * Adds the complex numbers together
     * 
     * @param complexNum the complex number to add to the current complex number
     * @return a complex number that is sum of the complex numbers
     */
    public Complex add(Complex complexNum) {
        return new Complex(this.value.add(complexNum.value));
    }

    /**
     * Subtracts the complex numbers from each other
     * 
     * @param complexNum the complex number to subtract from the current complex number
     * @return a complex number that is difference of the complex numbers
     */
    public Complex subtract(Complex complexNum) {
        return new Complex(this.value.subtract(complexNum.value));
    }

    /**
     * Multiplies the complex number by a scalar
     * 
     * @param scalar the scalar to multiply against the current complex number
     * @return a complex number that is product of the complex number and the scalar
     */
    public Complex multiply(double scalar) {
        return new Complex(this.value.multiply(scalar));
    }

    /**
     * Multiplies the complex numbers together
     * 
     * @param complexNum the complex number to be multipled against the current complex number
     * @return a complex number that is product of the complex numbers
     */
    public Complex multiply(Complex complexNum) {
        return new Complex(this.value.multiply(complexNum.value));
    }

    /**
     * Multiplies the complex number by a complex number in the unit circle
     * 
     * @param complexNum the complex number in the unit circle to multiply against the current complex number
     * @return a complex number that is product of the complex number and the complex number in the unit circle
     */
    public Complex multiply(S1 complexNum) {
        return this.multiply(complexNum.getValue());
    }

    /**
     * Divides the complex numbers against each other
     * 
     * @param complexNum the complex number to divide from the current complex number (divisor)
     * @return a complex number that is the quotient of the complex numbers
     */
    public Complex divide(Complex complexNum) {
        return new Complex(this.value.divide(complexNum.value));
    }

    public String toString() {
        return this.getReal() + " + " + this.getImg() + "i";
    }
}
