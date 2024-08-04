package com.quackology.duckdevices.distributions;

/**
 * Interface for a probability distribution
 */
public interface Distribution {
    /**
     * The probability density function of the distribution
     * 
     * @param x the value to evaluate the pdf at
     * @return the probability density at x
     */
    public double pdf(double x);
}
