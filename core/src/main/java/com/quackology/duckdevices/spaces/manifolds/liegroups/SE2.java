package com.quackology.duckdevices.spaces.manifolds.liegroups;

import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.MatReal;

/**
 * Special Euclidean group in 2D
 */
public class SE2 extends MatLieGroup<SE2, MatReal> {

    /**
     * Factory for the SE2 lie group for creating new instances - including exp and log maps or wedge and vee maps
     */
    public static final SE2 FACTORY = new SE2(MatReal.identity(3));

    /**
     * Protected constructor for the SE2 lie group
     * 
     * @param value value of the lie group
     */
    protected SE2(MatReal value) {
        super(value);
    }

    /**
     * Makes a new Manifold with the same structure but the given value
     * <p>
     * TODO: Implement constraints and conditions for lie group SE2
     * 
     * @param value value of the element in the manifold
     * @return a new Manifold with the same structure but the given value
     */
    @Override
    public SE2 make(MatReal value) {
        return new SE2(value);
    }
    

    /**
     * Makes a new SE2 lie group with the given rotation and position
     * 
     * @param rot the rotation of the lie group
     * @param pos the position of the lie group
     * @return a new Manifold with the same structure but the given value
     */
    public SE2 make(SO2 rot, MatReal pos) {
        return SE2.FACTORY.make(MatReal.vertical(MatReal.horizontal(rot.getValue(), pos), new MatReal(new double[][] {{0, 0, 1}})));
    }

    @Override
    public MatReal vee(MatReal lieAlgebra) {
        MatReal pos = lieAlgebra.subMat(0, 2, 2, 1);
        MatReal rot = SO2.FACTORY.vee(lieAlgebra.subMat(0, 0, 2, 1));
        return MatReal.vertical(pos, rot);
    }

    @Override
    public MatReal wedge(MatReal element) {
        return MatReal.vertical(MatReal.horizontal(SO2.FACTORY.wedge(element.get(2, 0)), element.subMat(0, 0, 2, 1)), new MatReal(new double[][] {{0, 0, 0}}));
    }

    @Override
    public SE2 exp(Linear element) {
        MatReal pos = element.toVector().subMat(0, 0, 2, 1);
        double theta = element.toVector().get(2, 0);

        MatReal V;
        if(Math.abs(theta) <= 1e-4) {
            V = MatReal.identity(2);
        } else {
            V = MatReal.identity(2).multiply(Math.sin(theta)/theta).add(SO2.FACTORY.wedge(1).multiply((1-Math.cos(theta))/theta));
        }

        return new SE2(MatReal.vertical(MatReal.horizontal(SO2.FACTORY.exp(theta).getValue(), V.multiply(pos)), new MatReal(new double[][] {{0, 0, 1}})));
    }

    /**
     * Modified exp map for SE2 lie group
     * <P>
     * Represents the pose instead of the twist
     * 
     * @param element the tangent element to map with
     * @return a lie group from the mapping of the given tangent element
     */
    public SE2 pseudo_exp(MatReal element) {
        MatReal pos = element.subMat(0, 0, 2, 1);
        double rot = element.get(2, 0);
        return new SE2(MatReal.vertical(MatReal.horizontal(SO2.FACTORY.exp(rot).getValue(), pos), new MatReal(new double[][] {{0, 0, 1}})));
    }

    @Override
    public MatReal log(SE2 lieGroup) {
        MatReal pos = lieGroup.value.subMat(0, 2, 2, 1);
        double theta = SO2.FACTORY.log(SO2.FACTORY.make(lieGroup.value.subMat(0, 0, 2, 2))).get(0, 0);

        MatReal V;
        if(Math.abs(theta) <= 1e-4) {
            V = MatReal.identity(2);
        } else {
            V = MatReal.identity(2).multiply(Math.sin(theta)/theta).add(SO2.FACTORY.wedge(1).multiply((1-Math.cos(theta))/theta));
        }

        return MatReal.vertical(V.inverse().multiply(pos), new MatReal(theta));
    }

    /**
     * Modified log map for SE2 lie group
     * <p>
     * Calculates the pose instead of the twist
     * <p>
     * Inverse of the exponential map
     * 
     * @param lieGroup the lie group to map with
     * @return a tangent element from the mapping of the given lie group
     */
    public MatReal pseudo_log(SE2 lieGroup) {
        MatReal rot = SO2.FACTORY.log(SO2.FACTORY.make(lieGroup.value.subMat(0, 0, 2, 2)));
        MatReal pos = lieGroup.value.subMat(0, 2, 2, 1);
        return MatReal.vertical(pos, rot);
    }

    @Override
    public SE2 compose(SE2 other) {
        return new SE2(this.value.multiply(other.value));
    }

    @Override
    public SE2 inverse() {
        MatReal rot_inv = SO2.FACTORY.make(this.value.subMat(0, 0, 2, 2)).inverse().getValue();
        MatReal pos = this.value.subMat(0, 2, 2, 1);
        return new SE2(MatReal.vertical(MatReal.horizontal(rot_inv, rot_inv.multiply(pos).multiply(-1)), new MatReal(new double[][] {{0, 0, 1}})));
    }

    @Override
    public MatReal adjoint() {
        MatReal rot = this.value.subMat(0, 0, 2, 2);
        MatReal pos = SO2.FACTORY.wedge(1).multiply(-1).multiply(new MatReal(new double[][] {{this.value.get(0, 2), this.value.get(1, 2)}}).transpose());
        return MatReal.vertical(MatReal.horizontal(rot, pos), new MatReal(new double[][] {{0, 0, 1}}));
    }

    @Override
    public SE2 identity() {
        return new SE2(MatReal.identity(3));
    }
    
}