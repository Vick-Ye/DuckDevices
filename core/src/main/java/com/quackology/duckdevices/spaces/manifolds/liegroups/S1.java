package com.quackology.duckdevices.spaces.manifolds.liegroups;

import com.quackology.duckdevices.spaces.Complex;
import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.MatReal;

public class S1 extends LieGroup<S1, Complex> {

    /**
     * Factory for the ComplexUnitCircle lie group for creating new instances - including exp and log maps
     */
    public static final S1 FACTORY = new S1(new Complex(1, 0));

    /**
     * Protected constructor for the unit circle in the complex plane
     * 
     * @param value the value of the complex number
     */
    protected S1(Complex value) {
        super(value);
    }

    @Override
    public S1 exp(Linear element) {
        return new S1(new Complex(Math.cos(element.toVector().get(0, 0)), Math.sin(element.toVector().get(0, 0))));
    }

    /**
     * Exponential map from the tangent element to the lie group
     * <p>
     * Essentially performs exp(rot * i) where i is the imaginary unit
     * 
     * @param rot the tangent element to map with
     * @return a lie group from the mapping of the given tangent element
     */
    public S1 exp(double rot) {
        return new S1(new Complex(Math.cos(rot), Math.sin(rot)));
    }

    @Override
    public Linear log(S1 lieGroup) {
        return new MatReal(Math.atan2(lieGroup.getValue().getImg(), lieGroup.getValue().getReal()));
    }

    @Override
    public S1 compose(S1 lieGroup) {
        return new S1(this.value.multiply(lieGroup.value));
    }

    @Override
    public S1 inverse() {
        return new S1(this.value.conjugate());
    }

    @Override
    public Linear adjoint() {
        return new MatReal(1);
    }

    @Override
    public S1 identity() {
        return new S1(new Complex(1, 0));
    }

    @Override
    public S1 make(Complex value) {
        if(value.abs()-1 > 1e12) {
            throw new IllegalArgumentException("The value must be a unit complex number");
        }
        return new S1(value);
    }
}
