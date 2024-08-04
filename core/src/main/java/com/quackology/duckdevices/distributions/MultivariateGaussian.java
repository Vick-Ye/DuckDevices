package com.quackology.duckdevices.distributions;

import com.quackology.duckdevices.spaces.MatReal;

/**
 * Multivariate Gaussian distribution
 */
public class MultivariateGaussian {
    /**
     * Mean of the distribution
     */
    private MatReal mean;

    /**
     * Covariance of the distribution
     */
    private MatReal covariance;

    /**
     * Constructor for the Multivariate Gaussian distribution
     * 
     * @param mean mean of the distribution
     * @param covariance covariance of the distribution
     */
    public MultivariateGaussian(MatReal mean, MatReal covariance) {
        this.mean = mean;
        this.covariance = covariance;
    }

    /**
     * Get the mean of the distribution
     * 
     * @return the mean of the distribution
     */
    public MatReal getMean() {
        return this.mean;
    }

    /**
     * Get the covariance of the distribution
     * 
     * @return the covariance of the distribution
     */
    public MatReal getCovariance() {
        return this.covariance;
    }

    /**
     * Evaluate the probability density function at the given value
     * 
     * @param x value to evaluate the probability density function at
     * @return the value of the probability density function at x
     */
    public double pdf(MatReal x) {
        return Math.exp(-1/2 * x.subtract(mean).transpose().multiply(covariance.inverse()).multiply(x.subtract(mean)).get(0, 0))/Math.sqrt(Math.pow(2*Math.PI, mean.getDimensions())*covariance.determinant());
    } 
}