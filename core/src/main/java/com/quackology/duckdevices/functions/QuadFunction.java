package com.quackology.duckdevices.functions;

/**
 * Functional interface for a lambda function that takes four arguments
 */
public interface QuadFunction<A, B, C, D, Out> {

    /**
     * Apply the function to the given arguments
     * 
     * @param a first argument
     * @param b second argument
     * @param c third argument
     * @param d fourth argument
     * @return output
     */
    public Out apply(A a, B b, C c, D d);
}