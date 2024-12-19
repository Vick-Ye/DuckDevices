package com.quackology.duckdevices.filters;

import com.quackology.duckdevices.spaces.MatReal;

/**
 * Kalman filter
 * Can be extended to handle non-linear systems using the extended Kalman filter by setting the state transition function and the measurement function
 */
public class KalmanFilter {

    /**
     * State
     */
    private MatReal x;

    /**
     * State covariance
     */
    private MatReal p;
    
    /**
     * State transition function
     */
    private MatReal f;

    /**
     * Process noise covariance
     */
    private MatReal q;

    /**
     * Control input
     */
    private MatReal u;

    /**
     * Control transformation
     */
    private MatReal b;

    /**
     * Constructor for the Kalman filter
     * 
     * @param x initial state
     * @param p initial state covariance
     * @param f state transition function
     * @param q process noise covariance
     * @param u control input
     * @param b control transformation
     */
    public KalmanFilter(MatReal x, MatReal p, MatReal f, MatReal q, MatReal u, MatReal b) {
        this.x = x;
        this.p = p;
        this.f = f;
        this.q = q;
        this.u = u;
        this.b = b;
    }

    /**
     * Predict the next state
     * To deal with time varying systems, the state transition function and process noise covariance should be updated before calling this method
     */
    public void predict() {
        this.x = this.f.multiply(this.x).add(b.multiply(this.u));
        this.p = this.f.multiply(this.p).multiply(this.f.transpose()).add(this.q);
    }

    /**
     * Update the state based on the measurement
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    public void update(MatReal h, MatReal z, MatReal r) {
        MatReal y = z.subtract(h.multiply(this.x)); //residual

        MatReal s = h.multiply(this.p).multiply(h.transpose()).add(r); //residual covariance
        MatReal k = this.p.multiply(h.transpose()).multiply(s.inverse()); //kalman gain
        this.x = this.x.add(k.multiply(y));
        this.p = MatReal.identity(this.p.getRows()).subtract(k.multiply(h)).multiply(this.p);
        //this.p = this.p.subtract(k.multiply(h).multiply(this.p));
        //this.p = (MatReal.identity(this.p.getR()).subtract(k.multiply(h))).multiply(this.p).multiply(MatReal.identity(this.p.getR()).subtract(k.multiply(h)).transpose()).add(k.multiply(r).multiply(k.transpose()));
    }

    /**
     * Get the current state
     * 
     * @return the current state
     */
    public MatReal getState() {
        return x;
    }

    /**
     * Get the current state covariance
     * 
     * @return the current state covariance
     */
    public MatReal getCovariance() {
        return p;
    }

    /**
     * Set the state transition function
     * 
     * @param f the new state transition function
     */
    public void setF(MatReal f) {
        this.f = f;
    }

    /**
     * Set the the process noise covariance
     * 
     * @param q the new process noise covariance
     */
    public void setQ(MatReal q) {
        this.q = q;
    }

    /**
     * Set the control input
     * 
     * @param u the new control input
     */
    public void setU(MatReal u) {
        this.u = u;
    }

    /**
     * Set the control transformation
     * 
     * @param b the new control transformation
     */
    public void setB(MatReal b) {
        this.b = b;
    }
}