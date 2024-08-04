package com.quackology.duckdevices.spaces.manifolds.liegroups;

import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.MatReal;

/**
 * Special orthogonal group in 2D
 */
public class SO2 extends MatLieGroup<SO2, MatReal> {
    
    /**
     * Factory for the SO2 lie group for creating new instances - including exp and log maps or wedge and vee maps
     */
    public static final SO2 FACTORY = new SO2(MatReal.identity(2));

    /**
     * Protected constructor for the SO2 lie group
     * 
     * @param value value of the lie group
     */
    protected SO2(MatReal value) {
        super(value);        
    }

    @Override
    public SO2 make(MatReal value) {
        if(Math.abs(value.determinant()-1) > 1e-4 || value.getDimensions() != 4 || value.getRows() != 2 || Math.abs(value.multiply(value.transpose()).trace()-2) > 1e-4) {
            throw new IllegalArgumentException("Invalid SO2 matrix");
        }
        return new SO2(value);
    }
    
    @Override
    public MatReal vee(MatReal lieAlgebra) {
        return new MatReal(new double[][]{{lieAlgebra.get(1, 0)}});
    }

    @Override
    public MatReal wedge(MatReal element) {
        return new MatReal(new double[][]{{0, -element.get(0, 0)}, {element.get(0, 0), 0}});
    }

    /**
     * Gets the lie algebra from the wedge map on the element
     * 
     * @param rot element to map to the lie algebra
     * @return the lie algebra from the mapping of the given element
     */
    public MatReal wedge(double rot) {
        return SO2.FACTORY.wedge(new MatReal(rot));
    }

    @Override
    public SO2 exp(Linear element) {
        double theta = element.toVector().get(0, 0);
        return new SO2(new MatReal(new double[][]{
            {Math.cos(theta), -Math.sin(theta)},
            {Math.sin(theta), Math.cos(theta)}
        }));
    }

    /**
     * Exponential map from the tangent element to the lie group
     * 
     * @param rot the tangent element to map with
     * @return a lie group from the mapping of the given tangent element
     */
    public SO2 exp(double rot) {
        return SO2.FACTORY.exp(new MatReal(rot));
    }

    @Override
    public MatReal log(SO2 lieGroup) {
        double theta = Math.atan2(lieGroup.value.get(1, 0), lieGroup.value.get(0, 0));
        return new MatReal(theta);
    }

    @Override
    public SO2 compose(SO2 other) {
        return new SO2(this.value.multiply(other.value));
    }

    @Override
    public SO2 inverse() {
        return new SO2(this.value.transpose());
    }

    @Override
    public MatReal adjoint() {
        return MatReal.identity(2);
    }
    
    @Override
    public SO2 identity() {
        return new SO2(MatReal.identity(2));
    }
}