package com.quackology.duckdevices.functions;

import java.util.function.Function;

public abstract class Differentiable implements Function<Double, Double> {
    protected Differentiable[] derivatives;

    protected Differentiable(int derivativeCount) {
        derivatives = new Differentiable[derivativeCount+1];
        derivatives[0] = this;
        for(int i = 1; i < derivativeCount; i++) {
            int finalI = i;
            derivatives[i] = new Differentiable(0) {
                @Override
                public Double apply(Double x) {
                    return (derivatives[finalI-1].apply(x+1e-6) - derivatives[finalI-1].apply(x)) / 1e-6;
                }
            };
        }
    }

    public Differentiable getDerivative(int order) {
        return derivatives[order];
    }
}
