package com.quackology.duckdevices.spaces.manifolds;

import com.quackology.duckdevices.spaces.Linear;
import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Space;

/**
 * Compound manifold class for combining multiple manifolds
 * <p>
 * Technically can be the value of a manifold :0
 */
@SuppressWarnings("rawtypes")
public class CompoundManifold implements Space {

    /**
     * Array of manifolds that make up the compound manifold
     */
    Manifold[] manifolds;

    /**
     * Constructor based on the array of manifolds
     * 
     * @param manifolds the array of manifolds that make up the compound manifold
     */
    public CompoundManifold(Manifold[] manifolds) {
        this.manifolds = manifolds;
    }

    /**
     * Constructor based on a single manifold
     * 
     * @param manifold the single manifold that makes up the compound manifold
     */
    public CompoundManifold(Manifold manifold) {
        this.manifolds = new Manifold[] {manifold};
    }

     /**
      * Applies the phi map to the entire compound manifold
      *
      * @param tangent an array of tangent elements ordered in the same way as the manifolds in the compound manifold
      * @return a compound manifold containing the manifold elements from the mapping of the current compound manifold and given array
      */
    public CompoundManifold phi(Linear[] tangent) {
        if (this.manifolds.length != tangent.length) {
            throw new IllegalArgumentException("Compound manifolds must be of the same dimensions");
        }

        Manifold[] out = new Manifold[this.manifolds.length];

        for (int i = 0; i < this.manifolds.length; i++) {
            out[i] = this.manifolds[i].phi(tangent[i]);
        }

        return new CompoundManifold(out);
    }

    /**
     * Applies the phi map to the entire compound manifold
     * <p>
     * Only works if the tangent elements are vectors
     * 
     * @param tangent the vector containing every tangent space stacked on top of each other
     * @return a compound manifold containing the manifold elements from the mapping of the current compound manifold and given vector
     */
    public CompoundManifold phi(Linear tangent) {
        if (this.getDimensions() != tangent.getDimensions()) {
            throw new IllegalArgumentException("Compound manifolds must be of the same dimensions");
        }
        
        Manifold[] out = new Manifold[this.manifolds.length];

        int index = 0;
        for (int i = 0; i < this.manifolds.length; i++) {
            MatReal other = tangent.toVector().getRow(index);
            for (int j = 1; j < this.manifolds[i].getDimensions(); j++) {
                other = MatReal.vertical(new MatReal[] {other, tangent.toVector().getRow(index+j)});
            }
            index += this.manifolds[i].getDimensions();

            out[i] = this.manifolds[i].phi(other);
        }

        return new CompoundManifold(out);
    }

    /**
     * Applies the phi_inverse map to the entire compound manifold
     * 
     * @param compoundManifold the compound manifold to map with
     * @return an array of tangent spaces ordered in the same way as the manifolds in the compound manifold
     */
    @SuppressWarnings("unchecked")
    public Linear[] phi_inverse(CompoundManifold compoundManifold) {
        if (this.manifolds.length != compoundManifold.manifolds.length) {
            throw new IllegalArgumentException("Compound manifolds must be of the same dimensions");
        }

        Linear[] out = new Linear[this.manifolds.length];

        for (int i = 0; i < this.manifolds.length; i++) {
            out[i] = this.manifolds[i].phi_inverse(compoundManifold.manifolds[i]);
        }

        return out;
    }

    /**
     * Applies the phi_inverse map to the entire compound manifold
     * <p>
     * Only works if the tangent elements are vectors
     * 
     * @param compoundManifold the compound manifold to map with
     * @return a vector containing every tangent space stacked on top of each other
     */
    @SuppressWarnings("unchecked")
    public MatReal phi_inverse_vector(CompoundManifold compoundManifold) {
        if (this.getDimensions() != compoundManifold.getDimensions()) {
            throw new IllegalArgumentException("Compound manifolds must be of the same dimensions");
        }

        MatReal out = this.manifolds[0].phi_inverse(compoundManifold.manifolds[0]).toVector();

        for (int i = 1; i < this.manifolds.length; i++) {
            out = MatReal.vertical(new MatReal[] {out, this.manifolds[i].phi_inverse(compoundManifold.manifolds[i]).toVector()});
        }

        return out;
    }

    /**
     * Gets the manifold at the given index
     * 
     * @param index the index of the manifold to get
     * @return the manifold at the given index
     */
    public Manifold getManifold(int index) {
        return this.manifolds[index];
    }

    /**
     * Gets an array of the individual dimensions of the manifolds in the compound manifold
     * 
     * @return an array of the individual dimensions of the manifolds in the compound manifold
     */
    public int[] getManifoldDimensions() {
        int[] out = new int[this.manifolds.length];

        for (int i = 0; i < this.manifolds.length; i++) {
            out[i] = this.manifolds[i].getDimensions();
        }

        return out;
    }

    /**
     * Gets the total number of dimensions in the compound manifold
     * 
     * @return the total number of dimensions in the compound manifold
     */
    public int getDimensions() {
        int out = 0;

        for (int i = 0; i < this.manifolds.length; i++) {
            out += this.manifolds[i].getDimensions();
        }

        return out;
    }

    /**
     * Gets the number of manifolds in the compound manifold
     * 
     * @return the number of manifolds in the compound manifold
     */
    public int getManifoldCount() {
        return this.manifolds.length;
    }
}