package com.quackology.duckdevices.controllers;

import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Vector;

public class GVF {
    private Path path;
    private MatReal k;
    private double w;
    private double L;

    public GVF(Path path, MatReal k, double L) {
        this.path = path;
        this.k = k;
        this.L = L;
        w = 0;
    }

    public MatReal getCorrection(MatReal pos) {
        MatReal correction = path.getTangent(w*L).multiply(L*L).subtract(k.hadamardProduct(pos.subtract(path.getPoint(w*L))));
        w += L + Vector.dot(k.hadamardProduct(pos.subtract(path.getPoint(w*L))).hadamardProduct(path.getTangent(w*L)), Vector.build(1, 1).multiply(L*L));
        return correction;
    }
}
