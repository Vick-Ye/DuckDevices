package com.quackology.duckdevices.spaces.manifolds;

import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.Space;

/**
 * Abstract class for the Manifold
 */
public abstract class Manifold<T extends Manifold<T, U>, U extends Space> implements Space {
    protected U value;

    /**
     * Constructor based on the value of the manifold
     * 
     * @param value value of the element in the manifold
     */
    protected Manifold(U value) {
        this.value = value;
    }

    /**
     * Makes a new Manifold with the same structure but the given value
     * <p>
     * Make sure to implement constraints and conditions for manifold
     * 
     * @param value value of the element in the manifold
     * @return a new Manifold with the same structure but the given value
     */
    public abstract T make(U value);

    /**
     * Phi map that maps the tangent element at the manifold element to a new manifold element
     * 
     * @param tangent the tangent element to map with
     * @return a manifold element from the mapping of the current manifold element and given linear element
     */
    public abstract T phi(Linear tangent);

    /**
     * Phi_inverse map that maps the given manifold element around the current manifold element to a tangent element
     * 
     * @param manifold the manifold element to map with
     * @return a tangent element from the mapping of the current manifold element and given manifold element
     */
    public abstract Linear phi_inverse(T manifold);

    /**
     * Gets the value of the manifold
     * 
     * @return the value of the manifold
     */
    public U getValue() {
        return value;
    }

    /**
     * Gets the dimensions of the manifold is embedded in
     * 
     * @return the dimensions of the manifold is embedded in
     */
    public int getEmbeddedDim() {
        return value.getDimensions();
    }

    /**
     * Gets the dimensions of the manifold
     * 
     * @return the dimensions of the manifold
     */
    @SuppressWarnings("unchecked")
    public int getDimensions() {
        return this.phi_inverse((T) this).getDimensions();
    }

    public String toString() {
        return value.toString();
    }
}