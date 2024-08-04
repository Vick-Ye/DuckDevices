package com.quackology.duckdevices.spaces.manifolds.liegroups;

import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.MatReal;

/**
 * Special Euclidean group in 3D
 */
public class SE3 extends MatLieGroup<SE3, MatReal> {
    
    /**
     * Factory for the SE3 lie group for creating new instances - including exp and log maps or wedge and vee maps
     */
    public static final SE3 FACTORY = new SE3(MatReal.identity(4));

    /**
     * Protected constructor for the SE3 lie group
     * 
     * @param value
     */
    protected SE3(MatReal value) {
        super(value);
    }

    /**
     * Makes a new Manifold with the same structure but the given value
     * <p>
     * TODO: Implement constraints and conditions for lie group SE3
     * 
     * @param value value of the element in the manifold
     * @return a new Manifold with the same structure but the given value
     */
    @Override
    public SE3 make(MatReal value) {
        return new SE3(value);
    }
    
    /**
     * Makes a new SE3 lie group with the given rotation and position
     * 
     * @param rot the rotation of the lie group
     * @param pos the position of the lie group
     * @return a new Manifold with the same structure but the given value
     */
    public SE3 make(SO3 rot, MatReal pos) {
        return SE3.FACTORY.make(MatReal.vertical(MatReal.horizontal(rot.getValue(), pos), new MatReal(new double[][] {{0, 0, 0, 1}})));
    }

    @Override
    public MatReal vee(MatReal lieAlgebra) {
        MatReal pos = lieAlgebra.subMat(0, 3, 3, 1);
        MatReal rot = SO3.FACTORY.vee(lieAlgebra.subMat(0, 0, 3, 1));
        return MatReal.vertical(pos, rot);
    }

    @Override
    public MatReal wedge(MatReal element) {
        return MatReal.vertical(MatReal.horizontal(SO3.FACTORY.wedge(element.subMat(3, 0, 3, 1)), element.subMat(0, 0, 3, 1)), new MatReal(new double[][] {{0, 0, 0, 0}}));
    }

    @Override
    public SE3 exp(Linear element) {
        MatReal pos = element.toVector().subMat(0, 0, 3, 1);
        MatReal rot = element.toVector().subMat(3, 0, 3, 1);

        double pitch = rot.get(0, 0);
        double yaw = rot.get(1, 0);
        double roll = rot.get(2, 0);
        double theta = Math.sqrt(pitch*pitch + yaw*yaw + roll*roll);

        MatReal V;
        if(Math.abs(theta) <= 1e-4) {
            V = MatReal.identity(3);
        } else {
            V = MatReal.identity(3).add(SO3.FACTORY.wedge(rot).multiply((1-Math.cos(theta))/(theta*theta))).add(SO3.FACTORY.wedge(rot).multiply(SO3.FACTORY.wedge(rot)).multiply((theta-Math.sin(theta))/(theta*theta*theta)));
        }

        return new SE3(MatReal.vertical(MatReal.horizontal(SO3.FACTORY.exp(rot).getValue(), V.multiply(pos)), new MatReal(new double[][] {{0, 0, 0, 1}})));
    }

    /**
     * Modified exp map for SE3 lie group
     * <p>
     * Represents the pose instead of the twist
     * 
     * @param element the tangent element to map with
     * @return a lie group from the mapping of the given tangent element
     */
    public SE3 pseudo_exp(MatReal element) {
        MatReal pos = element.subMat(0, 0, 3, 1);
        MatReal rot = element.subMat(3, 0, 3, 1);
        return new SE3(MatReal.vertical(MatReal.horizontal(SO3.FACTORY.exp(rot).getValue(), pos), new MatReal(new double[][] {{0, 0, 0, 1}})));
    }

    @Override
    public MatReal log(SE3 lieGroup) {
        MatReal pos = lieGroup.value.subMat(0, 3, 3, 1);
        MatReal rot = SO3.FACTORY.log(SO3.FACTORY.make(lieGroup.value.subMat(0, 0, 3, 3)));
        
        double pitch = rot.get(0, 0);
        double yaw = rot.get(1, 0);
        double roll = rot.get(2, 0);
        double theta = Math.sqrt(pitch*pitch + yaw*yaw + roll*roll);

        MatReal V;
        if(Math.abs(theta) <= 1e-4) {
            V = MatReal.identity(3);
        } else {
            V = MatReal.identity(3).add(SO3.FACTORY.wedge(rot).multiply((1-Math.cos(theta))/(theta*theta))).add(SO3.FACTORY.wedge(rot).multiply(SO3.FACTORY.wedge(rot)).multiply((theta-Math.sin(theta))/(theta*theta*theta)));
        }

        return MatReal.vertical(V.inverse().multiply(pos), rot);
    }

    /**
     * Modified log map for SE3 lie group
     * <p>
     * Calculates the pose instead of the twist
     * <p>
     * Inverse of the exponential map
     * 
     * @param lieGroup the lie group to map with
     * @return a tangent element from the mapping of the given lie group
     */
    public SE3 pseudo_log(SE3 lieGroup) {
        MatReal pos = lieGroup.value.subMat(0, 3, 3, 1);
        MatReal rot = SO3.FACTORY.log(SO3.FACTORY.make(lieGroup.value.subMat(0, 0, 3, 3)));
        return new SE3(MatReal.vertical(pos, rot));
    }

    @Override
    public SE3 compose(SE3 other) {
        return new SE3(this.value.multiply(other.value));
    }

    @Override
    public SE3 inverse() {
        MatReal rot_inv = SO3.FACTORY.make(this.value.subMat(0, 0, 3, 3)).inverse().getValue();
        MatReal pos = this.value.subMat(0, 3, 3, 1);
        return new SE3(MatReal.vertical(MatReal.horizontal(rot_inv, rot_inv.multiply(pos).multiply(-1)), new MatReal(new double[][] {{0, 0, 0, 1}})));
    }

    @Override
    public MatReal adjoint() {
        MatReal rot = this.value.subMat(0, 0, 3, 3);
        MatReal pos = SO3.FACTORY.wedge(this.value.subMat(3, 0, 3, 1)).multiply(rot);
        return MatReal.vertical(MatReal.horizontal(rot, pos), MatReal.horizontal(MatReal.empty(3, 3), rot));
    }

    @Override
    public SE3 identity() {
        return new SE3(MatReal.identity(4));
    }
    
}