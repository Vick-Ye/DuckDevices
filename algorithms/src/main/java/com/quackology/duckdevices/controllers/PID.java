package com.quackology.duckdevices.controllers;

public class PID {
    /**
     * PID parameters
     */
    private final PidParams params;

    /**
     * Integral sum of error with respect to time
     */
    private double integral;

    /**
     * Previous error
     */
    private double prevError;

    /**
     * Previous time to calculate change in time
     */
    private long pastTime;

    /**
     * Constructor for a PID controller
     *
     * @param params PID parameters
     */
    public PID(PidParams params) {
        this.params = params;
    }

    /**
     * Reset PID controller for a new target
     */
    public void reset() {
        pastTime = System.nanoTime();
        integral = 0;
        prevError = 0;
    }

    /**
     * Get PID corrective input
     * <p>
     * kp * e + ki * integral(e dt) + kd * de/dt
     *
     * @param error current error
     * @return corrective input
     */
    public double getCorrection(double error) {
        double dt = (System.nanoTime() - pastTime) * 1e-9;

        integral += (error + prevError) / 2 * dt;
        double derivative = (error - prevError) / dt;

        //if (Math.signum(error) != Math.signum(prevError)) integral = 0;

        prevError = error;
        return params.kp * error + params.ki * integral + params.kd * derivative;
    }
}
