package com.quackology.duckdevices.controllers;

import com.quackology.duckdevices.functions.Differentiable;
import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Vector;

public class Path {
    private Differentiable[] parametric;

    public Path(Differentiable... parametric) {
        this.parametric = parametric;
    }

    public MatReal getPoint(double t) {
        double[] x = new double[parametric.length];
        for(int i = 0; i < parametric.length; i++) {
            x[i] = parametric[i].apply(t);
        }

        return Vector.build(x);
    }

    public MatReal getTangent(double t) {
        double[] x = new double[parametric.length];
        for(int i = 0; i < parametric.length; i++) {
            x[i] = parametric[i].getDerivative(1).apply(t);
        }

        return Vector.build(x);
    }
}
