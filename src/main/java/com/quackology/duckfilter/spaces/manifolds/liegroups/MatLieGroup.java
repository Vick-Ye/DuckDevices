package com.quackology.duckfilter.spaces.manifolds.liegroups;

import com.quackology.duckfilter.spaces.Mat;

/**
 * Abstract matrix lie group superclass described with a matrix object and mappings from the element to the lie algebra
 */
@SuppressWarnings("rawtypes")
public abstract class MatLieGroup <T extends MatLieGroup<T, U>, U extends Mat> extends LieGroup<T, U> {

    /**
     * Protected constructor of the matrix lie group
     * 
     * @param value the matrix value of the lie group
     */
    protected MatLieGroup(U value) {
        super(value);
    }

    /**
     * Gets the lie algebra from the wedge map on the element
     * 
     * @param element element to map to the lie algebra
     * @return the lie algebra from the mapping of the given element
     */
    public abstract U wedge(U element);

    /**
     * Gets the element from the vee map on the lie algebra
     * 
     * @param lieAlgebra lie algebra to map to the element
     * @return the element from the mapping of the given lie algebra
     */
    public abstract U vee(U lieAlgebra);
}