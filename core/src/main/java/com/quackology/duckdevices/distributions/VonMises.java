package com.quackology.duckdevices.distributions;

import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.utils.Utils;
import com.quackology.duckdevices.utils.cern.jet.math.Bessel;

/**
 * Von Mises distribution
 */
public class VonMises implements Distribution {
    /**
     * Mean of the distribution
     */
    private double mean;

    /**
     * Concentration of the distribution
     */
    private int k;
    
    /**
     * Constructor for the Von Mises distribution
     * 
     * @param mean mean of the distribution
     * @param k concentration of the distribution
     */
    public VonMises(double mean, int k) {
        this.mean = mean;
        this.k = k;
    }

    /**
     * Get the mean of the distribution
     * 
     * @return the mean of the distribution
     */
    public double getMean() {
        return mean;
    }

    /**
     * Get the concentration of the distribution
     * 
     * @return the concentration of the distribution
     */
    public int getSpread() {
        return k;
    }

    /**
     * Evaluate the probability density function at the given value
     * 
     * @param x value to evaluate the probability density function at
     * @return the value of the probability density function at x
     */
    public double pdf(MatReal x) {
        return Math.exp(this.k * Math.cos(x.get(0, 0) - mean)) / (2*Math.PI*Bessel.i0(this.k));
    }

    /**
     * Nonanalytic integral of the CDF
     * 
     * @param x value to integrate the CDF up to
     * @return the value of the integral of the CDF up to x
     */
    private double cdfIntegral(double x) {
        double current;
        double threshold = 1e-9;
        double sum = 0;
        int n = 1;

        do {
            current = Utils.besseli(n, this.k)*Math.sin(n*(x - mean))/n;
            sum += current;
            n++;
        } while(current > threshold);

        return (2*sum + x* Bessel.i0(this.k)) / (2*Math.PI*Bessel.i0(this.k));
    }

    /**
     * Evaluate the cumulative distribution function at the given value
     * 
     * @param lower lower bound of the integral
     * @param x value to evaluate the cumulative distribution function up to
     * @return the value of the cumulative distribution function from lower to x
     */
    public double cdf(double lower, double x) {
        return cdfIntegral(x) - cdfIntegral(lower);
    }

    /**
     * Evaluate the cumulative distribution function from 0 to the given value
     * 
     * @param x value to evaluate the cumulative distribution function up to
     * @return the value of the cumulative distribution function from 0 to x
     */
    public double cdf(double x) {
        return cdf(0, x);
    }
}
