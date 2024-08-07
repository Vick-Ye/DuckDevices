package com.quackology.duckdevices.ukftest;

import java.util.Random;

public class Plane {
    private static Random rand = new Random();
    private double alt;
    private double yVel = 150;
    private double xDist;
    private double xVel = 50;
    private double processNoise;

    public Plane(double alt, double xDist, double processNoise) {
        this.alt = alt;
        this.xDist = xDist;
        this.processNoise = processNoise;
    }

    public void move(double dt) {
        double dx = this.xVel*dt + rand.nextGaussian()*processNoise*dt;
        this.xDist += dx;
        double dy = this.yVel*dt + rand.nextGaussian()*processNoise*dt;
        this.alt += dy;
    }

    public double getAlt() {
        return this.alt;
    }

    public double getXDist() {
        return this.xDist;
    }
}