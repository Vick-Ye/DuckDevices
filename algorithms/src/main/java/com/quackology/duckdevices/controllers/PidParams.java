package com.quackology.duckdevices.controllers;

public class PidParams {
    /**
     * PID parameters
     */
    protected double kp, ki, kd;

    /**
     * Constructor for container for PID parameters
     *
     * @param kp proportional gain
     * @param ki integral gain
     * @param kd derivative gain
     */
    public PidParams(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    /**
     * Set proportional gain
     *
     * @param kp proportional gain
     */
    public void setKp(double kp) {
        this.kp = kp;
    }

    /**
     * Set integral gain
     *
     * @param ki integral gain
     */
    public void setKi(double ki) {
        this.ki = ki;
    }

    /**
     * Set derivative gain
     *
     * @param kd derivative gain
     */
    public void setKd(double kd) {
        this.kd = kd;
    }

}
