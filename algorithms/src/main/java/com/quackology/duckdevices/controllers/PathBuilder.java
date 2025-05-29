package com.quackology.duckdevices.controllers;

import java.util.ArrayList;

import com.quackology.duckdevices.functions.Expression;
import com.quackology.duckdevices.functions.PiecewiseExpression;
import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Vector;

public class PathBuilder {
    private MatReal pos;
    private MatReal tan;
    private MatReal basisX;
    private MatReal basisY;
    private final ArrayList<Expression> xExpressions;
    private final ArrayList<Expression> yExpressions;
    private final ArrayList<Double> bounds;
    public PathBuilder(MatReal pos, MatReal tan) {
        this.pos = pos;
        this.tan = tan;
        basisX = null;
        basisY = null;
        xExpressions = new ArrayList<>();
        xExpressions.add(new Expression(""+pos.get(0, 0), 0));
        yExpressions = new ArrayList<>();
        yExpressions.add(new Expression(""+pos.get(1, 0), 0));
        bounds = new ArrayList<>();
        bounds.add(0d);
    }

    public PathBuilder(MatReal pose, double tanMagnitude) {
        this(pose.subMat(0, 0, 2, 1), Vector.build(tanMagnitude * Math.cos(pose.get(2, 0)), tanMagnitude * Math.sin(pose.get(2, 0))));
    }

    public PathBuilder(MatReal pose) {
        this(pose, 1);
    }

    public PathBuilder addHermiteSpline(MatReal targetPos, MatReal targetTan) {
        double a, b, c, d;
        //x
        a = targetTan.get(0, 0) + tan.get(0, 0) - 2 * targetPos.get(0, 0) + 2 * pos.get(0, 0);
        b = 3 * targetPos.get(0, 0) - 3 * pos.get(0, 0) - targetTan.get(0, 0) - 2 * tan.get(0, 0);
        c = tan.get(0, 0);
        d = pos.get(0, 0);
        xExpressions.add(new Expression(a + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 3 + " + b + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 2 + " + c + " * ( x - " + bounds.get(bounds.size()-1) + " ) + " + d, 0));
        basisX = Vector.build(a, b, c, d);

        //y
        a = targetTan.get(1, 0) + tan.get(1, 0) - 2 * targetPos.get(1, 0) + 2 * pos.get(1, 0);
        b = 3 * targetPos.get(1, 0) - 3 * pos.get(1, 0) - targetTan.get(1, 0) - 2 * tan.get(1, 0);
        c = tan.get(1, 0);
        d = pos.get(1, 0);
        yExpressions.add(new Expression(a + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 3 + " + b + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 2 + " + c + " * ( x - " + bounds.get(bounds.size()-1) + " ) + " + d, 0));
        basisY = Vector.build(a, b, c, d);

        bounds.add(bounds.get(bounds.size()-1)+1);
        pos = targetPos;
        tan = targetTan;
        return this;
    }

    public PathBuilder addHermiteSpline(MatReal targetPose, double tanMagnitude) {
        return addHermiteSpline(targetPose.subMat(0, 0, 2, 1), Vector.build(tanMagnitude * Math.cos(targetPose.get(2, 0)), tanMagnitude * Math.sin(targetPose.get(2, 0))));
    }

    public PathBuilder addHermiteSpline(MatReal targetPose) {
        return addHermiteSpline(targetPose, 1);
    }

    public PathBuilder addCubicBezier(MatReal targetPos, MatReal controlA, MatReal controlB) {
        double aX, bX, cX, dX;
        double aY, bY, cY, dY;
        //x
        aX = targetPos.get(0, 0) - pos.get(0, 0)  + 3 * controlA.get(0, 0) - 3 * controlB.get(0, 0);
        bX = 3 * pos.get(0, 0) - 6 * controlA.get(0, 0) + 3 * controlB.get(0, 0);
        cX = 3 * controlA.get(0, 0) - 3 * pos.get(0, 0);
        dX = pos.get(0, 0);
        xExpressions.add(new Expression(aX + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 3 + " + bX + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 2 + " + cX + " * ( x - " + bounds.get(bounds.size()-1) + " ) + " + dX, 0));
        basisX = Vector.build(aX, bX, cX, dX);

        //y
        aY = targetPos.get(1, 0) - pos.get(1, 0)  + 3 * controlA.get(1, 0) - 3 * controlB.get(1, 0);
        bY = 3 * pos.get(1, 0) - 6 * controlA.get(1, 0) + 3 * controlB.get(1, 0);
        cY = 3 * controlA.get(1, 0) - 3 * pos.get(1, 0);
        dY = pos.get(1, 0);
        yExpressions.add(new Expression(aY + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 3 + " + bY + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 2 + " + cY + " * ( x - " + bounds.get(bounds.size()-1) + " ) + " + dY, 0));
        basisY = Vector.build(aY, bY, cY, dY);

        bounds.add(bounds.get(bounds.size()-1)+1);
        pos = targetPos;
        tan = Vector.build(3*aX + 2*bX + cX, 3*aY + 2*bY + cY);
        return this;
    }

    public PathBuilder addBSpline(MatReal controlPt) {
        if(basisX == null) addHermiteSpline(pos.add(Vector.normalize(tan).multiply(1e-9)), tan);
        MatReal basisInv = new MatReal(new double[][] {
            {0, 2.0/3, -1, 1},
            {0, -1.0/3, 0, 1},
            {0, 2.0/3, 1, 1},
            {6, 11.0/3, 2, 1}
        });
        MatReal controlPtX = basisInv.multiply(basisX); 
        MatReal controlPtY = basisInv.multiply(basisY); 
        
        double aX, bX, cX, dX;
        double aY, bY, cY, dY;
        //x
        aX = -controlPtX.get(1, 0)/6 + controlPtX.get(2, 0)/2 - controlPtX.get(3, 0)/2 + controlPt.get(0, 0)/6;
        bX = controlPtX.get(1, 0)/2 - controlPtX.get(2, 0) + controlPtX.get(3, 0)/2;
        cX = -controlPtX.get(1, 0)/2 + controlPtX.get(3, 0)/2;
        dX = controlPtX.get(1, 0)/6 + 2*controlPtX.get(2, 0)/3 + controlPtX.get(3, 0)/6;
        xExpressions.add(new Expression(aX + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 3 + " + bX + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 2 + " + cX + " * ( x - " + bounds.get(bounds.size()-1) + " ) + " + dX, 0));
        basisX = Vector.build(aX, bX, cX, dX);

        //y
        aY = -controlPtY.get(1, 0)/6 + controlPtY.get(2, 0)/2 - controlPtY.get(3, 0)/2 + controlPt.get(1, 0)/6;
        bY = controlPtY.get(1, 0)/2 - controlPtY.get(2, 0) + controlPtY.get(3, 0)/2;
        cY = -controlPtY.get(1, 0)/2 + controlPtY.get(3, 0)/2;
        dY = controlPtY.get(1, 0)/6 + 2*controlPtY.get(2, 0)/3 + controlPtY.get(3, 0)/6;
        yExpressions.add(new Expression(aY + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 3 + " + bY + " * ( x - " + bounds.get(bounds.size()-1) + " ) ^ 2 + " + cY + " * ( x - " + bounds.get(bounds.size()-1) + " ) + " + dY, 0));
        basisY = Vector.build(aY, bY, cY, dY);

        bounds.add(bounds.get(bounds.size()-1)+1);
        pos = Vector.build(aX + bX + cX + dX, aY + bY + cY + dY);
        tan = Vector.build(3*aX + 2*bX + cX, 3*aY + 2*bY + cY);
        return this;
    }

    public PathBuilder addLine(MatReal targetPos) {
        tan = Vector.normalize(targetPos.subtract(pos));
        return addHermiteSpline(targetPos, tan);
    }

    public Path build() {
        xExpressions.add(new Expression(""+xExpressions.get(xExpressions.size()-1).apply(bounds.get(bounds.size()-1)), 0));
        yExpressions.add(new Expression(""+yExpressions.get(yExpressions.size()-1).apply(bounds.get(bounds.size()-1)), 0));
        return new Path(new PiecewiseExpression(1, bounds.toArray(new Double[0]), xExpressions.toArray(new Expression[0])), new PiecewiseExpression(1, bounds.toArray(new Double[0]), yExpressions.toArray(new Expression[0])));
    }
}
