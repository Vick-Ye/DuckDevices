package com.quackology.duckdevices.ukfmtest;

import java.util.Random;

import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.manifolds.liegroups.SE2;

public class Robot {
    private final static Random RAND = new Random();
    private SE2 state;
    private MatReal velocity;
    private MatReal variance;
    private MatReal measureVariance;

    public Robot(SE2 state, MatReal velocity, MatReal variance, MatReal measureVariance) {
      this.state = state;
      this.velocity = velocity;
      this.variance = variance;
      this.measureVariance = measureVariance;
    }

    public void move(double dt) {
        SE2 pos = state.phi(variance.subMat(0, 0, 3, 1).multiply(RAND.nextGaussian()));
        MatReal vel = velocity.add(variance.subMat(3, 0, 3, 1).multiply(RAND.nextGaussian()));

        this.state = pos.phi(vel.multiply(dt));
    }

    public MatReal getState() {
        return SE2.FACTORY.pseudo_log(state);
    }

    public MatReal getMeasurement() {
        return SE2.FACTORY.pseudo_log(state.phi(measureVariance.multiply(RAND.nextGaussian())));
    }
}