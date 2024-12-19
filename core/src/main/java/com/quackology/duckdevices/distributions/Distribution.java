package com.quackology.duckdevices.distributions;

import com.quackology.duckdevices.spaces.MatReal;

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
    public double pdf(MatReal x);
}
