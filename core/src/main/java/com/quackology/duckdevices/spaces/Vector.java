package com.quackology.duckdevices.spaces;

import com.quackology.duckdevices.spaces.MatReal;

/**
 * Vector operations on MatReal
 */
public class Vector {
    /**
     * unitX vector (1, 0)
     */
    public static final MatReal unitX = Vector.build(1, 0);

    /**
     * unitY vector (0, 1)
     */
    public static final MatReal unitY = Vector.build(0, 1);

    /**
     * Create a vector as a vertical MatReal
     *
     * @param x vector elements
     * @return vector in the form of MatReal
     */
    public static MatReal build(double... x) {
        return new MatReal(new double[][] {x}).transpose();
    }

    /**
     * Get the magnitude of a vector
     *
     * @param vec vector to get magnitude of
     * @return magnitude of vec
     */
    public static double magnitude(MatReal vec) {
        double mag = 0;
        for(int i = 0; i < vec.getRows(); i++) {
            mag += vec.get(i, 0)*vec.get(i, 0);
        }
        return Math.sqrt(mag);
    }

    /**
     * Get the angle between two vectors
     *
     * @param vec1 first vector
     * @param vec2 second vector
     * @return the angle between both vectors [0, Pi]
     */
    public static double angleBetween(MatReal vec1, MatReal vec2) {
        return Math.acos(Vector.dot(vec1, vec2)/Vector.magnitude(vec1)/Vector.magnitude(vec2));
    }

    /**
     * Get the dot product between two vectors
     *
     * @param vec1 first vector
     * @param vec2 second vector
     * @return the dot product between both vectors
     */
    public static double dot(MatReal vec1, MatReal vec2) {
        double result = 0;
        for(int i = 0; i < vec1.getRows(); i++) {
            result += vec1.get(i, 0)*vec2.get(i, 0);
        }
        return result;
    }

    /**
     * Get the normalized vector
     *
     * @param vec vector to be normnalized
     * @return the normalized vector of vec
     */
    public static MatReal normalize(MatReal vec) {
        return vec.multiply(1/Vector.magnitude(vec));
    }
}