package com.quackology.duckdevices.controllers;

public class ProfileParams {
    protected double accel, vel, deccel;

    public ProfileParams(double accel, double vel, double deccel) {
        this.accel = accel;
        this.vel = vel;
        this.deccel = deccel;
    }

    public void setAccel(double accel) {
        this.accel = accel;
    }

    public void setVel(double vel) {
        this.vel = vel;
    }

    public void setDeccel(double deccel) {
        this.deccel = deccel;
    }
}
