package com.quackology.duckdevices.spaces.manifolds;

import com.quackology.duckdevices.spaces.MatReal;

/**
 * Factory for generating vector space manifold implementations
 */
public class VectorSpaceFactory {

    /**
     * The identity element of the generic vector space to generate new vector space elements
     */
    public static ManifoldImpl<MatReal> element = new ManifoldImpl<MatReal>(new MatReal(0), (a_, b_) -> a_.make(a_.value.add(b_.toVector())), (a_, b_) -> a_.value.subtract(b_.value));

    /**
     * Makes a new vector space element with the given value
     * 
     * @param value the value of the new vector space element
     * @return a new vector space element with the given value
     */
    public static ManifoldImpl<MatReal> make(MatReal value) {
        return element.make(value);
    }
}