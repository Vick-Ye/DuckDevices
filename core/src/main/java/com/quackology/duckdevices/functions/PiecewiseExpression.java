package com.quackology.duckdevices.functions;

import java.util.Arrays;

public class PiecewiseExpression extends Differentiable {

    private final double[] bounds;
    public final Expression[] expressions;

    public PiecewiseExpression(int derivativeCount, double[] bounds, Expression... expressions) {
        super(derivativeCount);
        this.expressions = expressions;
        this.bounds = bounds;
        derivatives[0] = this;
        for(int i = 1; i < derivativeCount; i++) {
            Expression[] derivative = new Expression[expressions.length];
            for(int j = 0; j < derivative.length; j++) {
                derivative[j] = (Expression) new Expression(((PiecewiseExpression) derivatives[i-1]).getExpressions()[j], 1).getDerivative(1);
            }
            derivatives[i] = new PiecewiseExpression(0, bounds, derivative);
        }
    }

    public PiecewiseExpression(int derivativeCount, Double[] bounds, Expression... expressions) {
        this(derivativeCount, Arrays.stream(bounds).mapToDouble(Double::doubleValue).toArray(), expressions);
    }
    @Override
    public Double apply(Double x) {
        for(int i = 0; i < bounds.length; i++) {
            if(x <= bounds[i]) {
                return expressions[i].apply(x);
            }
        }
        return expressions[expressions.length-1].apply(x);
    }

    public Expression[] getExpressions() {
        return this.expressions;
    }
}
