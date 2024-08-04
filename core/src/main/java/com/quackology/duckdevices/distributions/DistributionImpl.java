package com.quackology.duckdevices.distributions;

import java.util.function.BiFunction;

/**
 * Concrete implementation of the Distribution interface
 */
public class DistributionImpl<T extends Object> implements Distribution {

    /**
     * The data describing the distribution
     */
    private T data;

    /**
     * The lambda function that the method pdf will use to evaluate the pdf
     * <p>
     * The first argument is the data describing the distribution
     * <p>
     * The second argument is the value to evaluate the pdf at
     */
    private BiFunction<T, Double, Double> pdf;
    
    /**
     * Constructor of the DistributionImpl using data describing the distribution and a pdf lambda function
     * 
     * @param data the data describing the distribution
     * @param pdf the lambda function to evaluate the pdf where the first argument is the data describing the distribution and the second argument is the value to evaluate the pdf at
     */
    public DistributionImpl(T data, BiFunction<T, Double, Double> pdf) {
        this.data = data;
        this.pdf = pdf;
    }

    /**
     * Gets the data describing the distribution
     * 
     * @return the data describing the distribution
     */
    public T getData() {
        return data;
    }

    /**
     * Sets the data describing the distribution
     * 
     * @param data the data describing the distribution
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Sets the lambda function that the method pdf will use to evaluate the pdf
     * 
     * @param pdf the lambda function to evaluate the pdf where the first argument is the data describing the distribution and the second argument is the value to evaluate the pdf at
     */
    public void setPdf(BiFunction<T, Double, Double> pdf) {
        this.pdf = pdf;
    }

    @Override
    public double pdf(double x) {
        return pdf.apply(data, x);
    }
}
