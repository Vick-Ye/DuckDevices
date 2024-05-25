package com.quackology.duckfilter.spaces;

/**
 * Interface for linear spaces
 */
public interface Linear extends Space {

    /**
     * Gets the vectorized form of the matrix
     * 
     * @return the vectorized form of the matrix
     */
    public MatReal toVector();
}
