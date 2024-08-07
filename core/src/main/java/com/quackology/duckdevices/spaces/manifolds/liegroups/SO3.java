package com.quackology.duckdevices.spaces.manifolds.liegroups;

import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.MatReal;

/**
 * Special orthogonal group in 3D
 */
public class SO3 extends MatLieGroup<SO3, MatReal> {
    
    /**
     * Factory for the SO3 lie group for creating new instances - including exp and log maps or wedge and vee maps
     */
    public static final SO3 FACTORY = new SO3(MatReal.identity(3));

    /**
     * Protected constructor for the SO3 lie group
     * 
     * @param value value of the lie group
     */
    protected SO3(MatReal value) {
        super(value);
    }

    /**
     * Makes a new Manifold with the same structure but the given value
     * <p>
     * TODO: Implement constraints and conditions for lie group SO3
     * 
     * @param value value of the element in the manifold
     * @return a new Manifold with the same structure but the given value
     */
    @Override
    public SO3 make(MatReal value) {
        return new SO3(value);
    }
    
    @Override
    public MatReal vee(MatReal lieAlgebra) {
        return new MatReal(new double[][] {
            {lieAlgebra.get(0, 2)},
            {lieAlgebra.get(1, 0)},
            {lieAlgebra.get(2, 1)}
        });
    }

    @Override
    public MatReal wedge(MatReal element) {
        double pitch = element.get(0, 0);
        double yaw = element.get(1, 0);
        double roll = element.get(2, 0);
        return new MatReal(new double[][] {
            {0, -yaw, pitch},
            {yaw, 0, -roll},
            {-pitch, roll, 0}
        });
    }

    /**
     * Gets the lie algebra from the wedge map on the element
     * 
     * @param pitch pitch part of element to map to the lie algebra
     * @param yaw yaw part of element to map to the lie algebra
     * @param roll roll part of element to map to the lie algebra
     * @return the lie algebra from the mapping of the given element
     */
    public MatReal wedge(double pitch, double yaw, double roll) {
        return SO3.FACTORY.wedge(new MatReal(new double[][] {{pitch, yaw, roll}}).transpose());
    }

    @Override
    public SO3 exp(Linear element) {
        double pitch = element.toVector().get(0, 0);
        double yaw = element.toVector().get(1, 0);
        double roll = element.toVector().get(2, 0);
        double theta = Math.sqrt(pitch*pitch + yaw*yaw + roll*roll);

        if (theta <= 1e-4) {
            return new SO3(MatReal.identity(3));
        }

        return new SO3(MatReal.identity(3).add(SO3.FACTORY.wedge(pitch, yaw, roll)).multiply(Math.sin(theta)/theta).add(SO3.FACTORY.wedge(pitch, yaw, roll).multiply(SO3.FACTORY.wedge(pitch, yaw, roll)).multiply((1-Math.cos(theta))/(theta*theta))));
    }

    /**
     * Exponential map from the tangent element to the lie group
     * 
     * @param pitch the pitch part of the tangent element to map with
     * @param yaw the yaw part of the tangent element to map with
     * @param roll the roll part of the tangent element to map with
     * @return a lie group from the mapping of the given tangent element
     */
    public SO3 exp(double pitch, double yaw, double roll) {
        double theta = Math.sqrt(pitch*pitch + yaw*yaw + roll*roll);

        if (theta <= 1e-4) {
            return new SO3(MatReal.identity(3));
        }

        return new SO3(MatReal.identity(3).add(SO3.FACTORY.wedge(pitch, yaw, roll).multiply(Math.sin(theta)/theta)).add(SO3.FACTORY.wedge(pitch, yaw, roll).multiply(SO3.FACTORY.wedge(pitch, yaw, roll)).multiply((1-Math.cos(theta))/(theta*theta))));

    }

    /**
     * Exponential map from the tangent element to the lie group
     * 
     * @param theta the angle of the rotation around the axis to map with
     * @param element the axis of the rotation to map with
     * @return a lie group from the mapping of the given tangent element
     */
    public SO3 exp(double theta, MatReal element) {
        MatReal axis = SO3.FACTORY.wedge(element);

        return new SO3(MatReal.identity(3).add(axis.multiply(Math.sin(theta)).add(axis.multiply(axis).multiply(1-Math.cos(theta)))));
    }

    @Override
    public MatReal log(SO3 lieGroup) {
        double theta = Math.acos((lieGroup.value.trace()-1)/2);

            if (theta == 0) {
                return MatReal.empty(3, 1);
            }

            return vee(lieGroup.value.subtract(lieGroup.value.transpose()).multiply(theta/(2*Math.sin(theta))));
    }

    @Override
    public SO3 compose(SO3 other) {
        return new SO3(this.value.multiply(other.value));
    }

    @Override
    public SO3 inverse() {
        return new SO3(this.value.transpose());
    }

    @Override
    public MatReal adjoint() {
        return this.value;
    }
    
    @Override
    public SO3 identity() {
        return new SO3(MatReal.identity(3));
    }
}