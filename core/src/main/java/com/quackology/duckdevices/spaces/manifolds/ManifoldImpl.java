package com.quackology.duckdevices.spaces.manifolds;

import java.util.function.BiFunction;

import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.Space;

/**
 * Concrete implementation of the Manifold class
 */
public class ManifoldImpl <T extends Space> extends Manifold<ManifoldImpl<T>, T> {

    /**
     * The lambda function phi that the method phi utilizes
     */
    private BiFunction<ManifoldImpl<T>, Linear, ManifoldImpl<T>> phi;

    /**
     * The lambda function phi_inverse that the method phi_inverse utilizes
     */
    private BiFunction<ManifoldImpl<T>, ManifoldImpl<T>, Linear> phi_inverse;

    /**
     * Constructor based on the value and the two maps phi and phi_inverse as lambda functions
     * 
     * @param value value of the element in the manifold
     * @param phi lambda function phi for the map phi (method)
     * @param phi_inverse lambda function phi_inverse for the map phi_inverse (method)
     */
    public ManifoldImpl(T value, BiFunction<ManifoldImpl<T>, Linear, ManifoldImpl<T>> phi, BiFunction<ManifoldImpl<T>, ManifoldImpl<T>, Linear> phi_inverse) {
        super(value);
        this.phi = phi;
        this.phi_inverse = phi_inverse;
    }

    @Override
    public ManifoldImpl<T> make(T value) {
        return new ManifoldImpl<T>(value, phi, phi_inverse);
    }

    @Override
    public ManifoldImpl<T> phi(Linear tangent) {
        return phi.apply(this, tangent);
    }

    @Override
    public Linear phi_inverse(ManifoldImpl<T> manifold) {
        return phi_inverse.apply(this, manifold);
    }
    
}