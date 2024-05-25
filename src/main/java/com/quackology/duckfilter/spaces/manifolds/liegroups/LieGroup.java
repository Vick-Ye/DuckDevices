package com.quackology.duckfilter.spaces.manifolds.liegroups;

import com.quackology.duckfilter.spaces.Linear;
import com.quackology.duckfilter.spaces.Space;
import com.quackology.duckfilter.spaces.manifolds.Manifold;

/**
 * Abstract lie group superclass described in exp/log maps, composition, inverses, adjoints, and the identity
 * <p>
 * Note: must create a static factory field for the lie group implementations
 * <p>
 * Note: the exp and log mappings should work with the element rather than the lie algebra
 */
public abstract class LieGroup <T extends LieGroup<T, U>, U extends Space> extends Manifold<T, U> {

    /**
     * Protected constructor based on the value
     * 
     * @param value the value of the lie group
     */
    protected LieGroup(U value) {
        super(value);
    }

    /**
     * Exponential map from the tangent element to the lie group
     * 
     * @param element the tangent element to map with
     * @return a lie group from the mapping of the given tangent element
     */
    public abstract T exp(Linear element);

    /**
     * Logarithm map from the lie group to the tangent element
     * <p>
     * Inverse of the exponential map
     * 
     * @param lieGroup the lie group to map with
     * @return a tangent element from the mapping of the given lie group
     */
    public abstract Linear log(T lieGroup);

    /**
     * Composition/Action/Multiplication of the lie group with another lie group
     * 
     * @param lieGroup the other lie group to compose with
     * @return a lie group from the composition of the lie groups
     */
    public abstract T compose(T lieGroup);

    /**
     * Gets the inverse of the lie group
     * 
     * @return a lie group from the inverse of the current lie group
     */
    public abstract T inverse();

    /**
     * Gets the adjoint of the lie group
     * 
     * @return a linear element from the adjoint of the current lie group
     */
    public abstract Linear adjoint();

    /**
     * Gets the identity of the lie group
     * 
     * @return a lie group of the identity of the lie group
     */
    public abstract T identity();
 
    /**
     * Gets the inverse of the lie group
     * 
     * @param lieGroup the lie group to get the inverse of
     * @return a lie group from the inverse of the given lie group
     */
    public T inverse(T lieGroup) {
       return lieGroup.inverse();
    }
 
    /**
     * Phi map that maps the tangent element at the lie group element to a new lie group element
     * <p>
     * M * exp(x) where M is the lie group element and x is the tangent element
     * 
     * @param element the tangent element to map with
     * @return a new lie group element from the mapping of the current lie group element and given linear element
     */
    @Override
    public T phi(Linear element) {
       return this.compose(this.exp(element));
    }
 
    /**
     * Phi_inverse map that maps the given lie group element around the current lie group element to a tangent element
     * <p>
     * log(M^-1 * N) where M is the current lie group element and N is the given lie group element
     * 
     * @param lieGroup the lie group element to map with
     * @return a new tangent element from the mapping of the current lie group element and given lie group element
     */
    @Override
    @SuppressWarnings("unchecked")
    public Linear phi_inverse(T lieGroup) {
         return this.log(lieGroup.inverse().compose((T) this));
     }
}