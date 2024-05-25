package com.quackology.duckfilter.functions;

/**
 * Functional interface for a lambda function that takes three arguments
 */
public interface TriFunction<A, B, C, Out> {

    /**
     * Apply the function to the given arguments
     * 
     * @param a the first argument
     * @param b the second argument
     * @param c the third argument
     * @return output
     */
    public Out apply(A a, B b, C c);
}