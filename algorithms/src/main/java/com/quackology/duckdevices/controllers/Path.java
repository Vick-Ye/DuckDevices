package com.quackology.duckdevices.controllers;

import com.quackology.duckdevices.functions.Differentiable;
import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Vector;

public class Path {
    private Differentiable x;
    private Differentiable y;

    public Path(Differentiable x, Differentiable y) {
        this.x = x;
        this.y = y;
    }

    public MatReal getPoint(double t) {
        return Vector.build(x.apply(t), y.apply(t));
    }

    public MatReal getTangent(double t) {
        return Vector.build(x.getDerivative(1).apply(t), y.getDerivative(1).apply(t));
    }
}
