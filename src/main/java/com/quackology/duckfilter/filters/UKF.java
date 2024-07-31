package com.quackology.duckfilter.filters;

import java.util.function.Function;

import org.ojalgo.matrix.decomposition.Cholesky;

import com.quackology.duckfilter.distributions.MultivariateGaussian;
import com.quackology.duckfilter.functions.TriFunction;
import com.quackology.duckfilter.spaces.MatReal;

/**
 * Unscented Kalman Filter
 * <p>
 * Based on the paper "A New Extension of the Kalman Filter to Nonlinear Systems" by Simon J. Julier and Jeffrey K. Uhlmann
 * <p>
 * Includes sampling methods described in "The Unscented Kalman Filter for Nonlinear Estimation" by Eric A. Wan and Rudolph van der Merwe
 */
public class UKF {

    /**
     * State of the filter
     */
    private MatReal x;

    /**
     * State covariance
     */
    private MatReal p;

    /**
     * State transition function
     * <p>
     * f(x, u, dt), x = state, u = control input, dt = time step
     */
    private TriFunction<MatReal, MatReal, Double, MatReal> f; 
    
    /**
     * Process noise covariance
     */
    private MatReal q;

    /**
     * Control input
     */
    private MatReal u;

    /**
     * Merwe alpha sampling parameter
     */
    private double a;

    /**
     * Merwe beta sampling parameter
     */
    private double b;

    /**
     * Merwe kappa sampling parameter
     */
    private double k;

    /**
     * Julier lambda sampling parameter
     */
    private double l;

    /**
     * Sampling methods
     */
    public static enum Sampling{
        MERWE,
        JULIER
    }

    /**
     * Sampling method to use
     */
    private Sampling sampling = Sampling.MERWE;

    /**
     * Cholesky Solver
     */
    private Cholesky<Double> choleskySolver;

    /**
     * Constructor for the Unscented Kalman Filter
     * <p>
     * Default sampling method is Merwe
     * <p>
     * Must set state transition function using sefF
     * 
     * @param x initial state
     * @param p initial state covariance
     * @param q process noise covariance
     * @param u control input
     */
    public UKF(MatReal x, MatReal p, MatReal q, MatReal u) {
        this.x = x;
        this.p = p;
        this.q = q;
        this.u = u;

        this.a = 0.001;
        this.b = 2;
        this.k = 0;
        this.l = 3-this.x.getRows();

        choleskySolver = Cholesky.R064.make(this.p.getRows(), this.p.getCols());
    }

    /**
     * Constructor for the Unscented Kalman Filter
     * <p>
     * Must set state transition function using sefF
     * 
     * @param sampling sampling method to use
     * @param x initial state
     * @param p initial state covariance
     * @param q process noise covariance
     * @param u control input
     */
    public UKF(Sampling sampling, MatReal x, MatReal p, MatReal q, MatReal u) {
        this(x, p, q, u);
        this.sampling = sampling;
    }

    /**
     * Constructor for the Unscented Kalman Filter
     * <p>
     * Default sampling method is Merwe
     * 
     * @param x initial state
     * @param p initial state covariance
     * @param f state transition function
     * @param q process noise covariance
     * @param u control input
     */
    public UKF(MatReal x, MatReal p, TriFunction<MatReal, MatReal, Double, MatReal> f, MatReal q, MatReal u) {
        this(x, p, q, u);
        this.f = f;
    }

    /**
     * Constructor for the Unscented Kalman Filter
     * 
     * @param sampling sampling method to use
     * @param x initial state
     * @param p initial state covariance
     * @param f state transition function
     * @param q process noise covariance
     * @param u control input
     */
    public UKF(Sampling sampling, MatReal x, MatReal p, TriFunction<MatReal, MatReal, Double, MatReal> f, MatReal q, MatReal u) {
        this(sampling, x, p, q, u);
        this.f = f;
    }

    /**
     * Predicts the next state by propagating the current state through the state transition function
     * <p>
     * Assumes noise is additive so state transition function only takes the state and control input (maybe noise)
     * <p>
     * x = f(x, u) + q
     * 
     * @param dt time step if the state transition function is time dependent
     */
    public void predict(double dt) {
        switch (sampling) {
            case MERWE:
                predict_merwe(dt);
                break;
            case JULIER:
                predict_julier(dt);
                break;
        }
    }

    /**
     * Predicts the next state with augmented state and covariance
     * <p>
     * Takes into account the process noise covariance in the state transition function
     * <p>
     * x = f(x_aug, u) where x_aug is the state on top of the noise
     * 
     * @param dt time step if the state transition function is time dependent
     */
    public void predict_aug(double dt) {
        switch (sampling) {
            case MERWE:
                predict_aug_merwe(dt);
                break;
            case JULIER:
                predict_aug_julier(dt);
                break;
        }
    }

    /**
     * Updates the state using a measurement
     * <p>
     * Measurement function takes the state and returns an element in the measurement space
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    public void update(Function<MatReal, MatReal> h, MatReal z, MatReal r) {
        switch (sampling) {
            case MERWE:
                update_merwe(h, z, r);
                break;
            case JULIER:
                update_julier(h, z, r);
                break;
        }
    }

    /**
     * Predicts the next state using the Merwe sampling method
     * 
     * @param dt time step if the state transition function is time dependent
     */
    private void predict_merwe(double dt) {
        MatReal[] X = new MatReal[this.x.getRows()*2+1];
        double[] weightM = new double[this.x.getRows()*2+1];
        double[] weightC = new double[this.x.getRows()*2+1];
        generateSigmaPoints(X, weightM, weightC, x, p);

        MatReal[] Y = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Y[i] = f.apply(X[i], this.u, dt);
        }
        MultivariateGaussian guassian = unscentedTransform(Y, weightM, weightC);

        this.x = guassian.getMean();
        this.p = guassian.getCovariance().add(this.q);
    }

    /**
     * Predicts the next state with augmented state and covariance using the Merwe sampling method
     * 
     * @param dt time step if the state transition function is time dependent
     */
    private void predict_aug_merwe(double dt) {
        //augment state
        MatReal x_aug = MatReal.vertical(this.x, MatReal.empty(this.q.getRows(), 1));

        //augment covariance
        MatReal p_aug = MatReal.diagonal(this.p, this.q);

        MatReal[] X = new MatReal[x_aug.getRows()*2+1];
        double[] weightM = new double[x_aug.getRows()*2+1];
        double[] weightC = new double[x_aug.getRows()*2+1];
        generateSigmaPoints(X, weightM, weightC, x_aug, p_aug);

        MatReal[] Y = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Y[i] = f.apply(X[i], this.u, dt);
        }
        MultivariateGaussian guassian = unscentedTransform(Y, weightM, weightC);

        this.x = guassian.getMean();
        this.p = guassian.getCovariance();
    }

    /**
     * Updates the state using the Merwe sampling method
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    private void update_merwe(Function<MatReal, MatReal> h, MatReal z, MatReal r) {
        MatReal[] X = new MatReal[this.x.getRows()*2+1];
        double[] weightM = new double[this.x.getRows()*2+1];
        double[] weightC = new double[this.x.getRows()*2+1];
        generateSigmaPoints(X, weightM, weightC, this.x, this.p);

        MatReal[] Z = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Z[i] = h.apply(X[i]);
        }

        MultivariateGaussian gaussian = unscentedTransform(Z, weightM, weightC);

        MatReal t = X[0].subtract(this.x).multiply(Z[0].subtract(gaussian.getMean()).transpose()).multiply(weightC[0]);
        for (int i = 1; i < X.length; i++) {
            t = t.add(X[i].subtract(this.x).multiply(Z[i].subtract(gaussian.getMean()).transpose()).multiply(weightC[i]));
        }

        MatReal k = t.multiply(gaussian.getCovariance().add(r).inverse());
        this.x = this.x.add(k.multiply(z.subtract(gaussian.getMean())));
        this.p = this.p.subtract(k.multiply(gaussian.getCovariance().add(r)).multiply(k.transpose()));
    }

    /**
     * Unscented transform using the Merwe sampling method
     * 
     * @param sigmaPoints sigma points
     * @param weightM weights for the mean
     * @param weightC weights for the covariance
     * @return
     */
    private MultivariateGaussian unscentedTransform(MatReal[] sigmaPoints, double[] weightM, double[] weightC) {
        MatReal meanOut = sigmaPoints[0].multiply(weightM[0]);
        for (int i = 1; i < sigmaPoints.length; i++) {
            meanOut = meanOut.add(sigmaPoints[i].multiply(weightM[i]));
        }
        MatReal covarianceOut = sigmaPoints[0].subtract(meanOut).multiply(sigmaPoints[0].subtract(meanOut).transpose()).multiply(weightC[0]);
        for (int i = 1; i < sigmaPoints.length; i++) {
            covarianceOut = covarianceOut.add(sigmaPoints[i].subtract(meanOut).multiply(sigmaPoints[i].subtract(meanOut).transpose()).multiply(weightC[i]));
        }

        return new MultivariateGaussian(meanOut, covarianceOut);
    }

    /**
     * Generates sigma points using the Merwe sampling method
     * 
     * @param sigmaPoints sigma points to generate
     * @param weightM weights to generate (mean)
     * @param weightC weights to generate (covariance)
     * @param x state
     * @param p state covariance
     */
    private void generateSigmaPoints(MatReal[] sigmaPoints, double[] weightM, double[] weightC, MatReal x, MatReal p) {
        MatReal tolerance = MatReal.identity(p.getRows()).multiply(1e-6);
        p = p.add(tolerance);
        
        int n = sigmaPoints.length/2;
        double l = this.a*this.a*(n+k)-n;

        sigmaPoints[0] = x;
        for (int i = 1; i <= n; i++) {
            sigmaPoints[i] = x.add(p.multiply(n+l).choleskyDecompose(choleskySolver).getCol(i-1));
        }
        for (int i = n+1; i <= 2*n; i++) {
            sigmaPoints[i] = x.subtract(p.multiply(n+l).choleskyDecompose(choleskySolver).getCol(i-n-1));
        }

        weightM[0] = l / (l+n);
        weightC[0] = weightM[0] + 1-this.a*this.a+this.b;
        for (int i = 1; i < n*2+1; i++) {
            weightM[i] = 1 / (2*l+2*n);
            weightC[i] = 1 / (2*l+2*n);
        }
    }

    /**
     * Sets the sampling variables for the Merwe sampling method
     * 
     * @param a alpha
     * @param b beta
     * @param k kappa
     */
    public void setSigmaVariables(double a, double b, double k) {
        this.a = a;
        this.b = b;
        this.k = k;
    }

    /**
     * Predicts the next state using the Julier sampling method
     * 
     * @param dt time step if the state transition function is time dependent
     */
    private void predict_julier(double dt) {
        MatReal[] X = new MatReal[this.x.getRows()*2+1];
        double[] weight = new double[this.x.getRows()*2+1];
        generateSigmaPoints(X, weight, x, p);

        MatReal[] Y = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Y[i] = f.apply(X[i], this.u, dt);
        }
        
        MultivariateGaussian guassian = unscentedTransform(Y, weight);

        this.x = guassian.getMean();
        this.p = guassian.getCovariance().add(this.q);
    }

    /**
     * Predicts the next state with augmented state and covariance using the Julier sampling method
     * 
     * @param dt time step if the state transition function is time dependent
     */
    private void predict_aug_julier(double dt) {
        //augment state
        MatReal x_aug = MatReal.vertical(this.x, MatReal.empty(this.q.getRows(), 1));

        //augment covariance
        MatReal p_aug = MatReal.diagonal(this.p, this.q);

        MatReal[] X = new MatReal[x_aug.getRows()*2+1];
        double[] weight = new double[x_aug.getRows()*2+1];
        generateSigmaPoints(X, weight, x_aug, p_aug);

        MatReal[] Y = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Y[i] = f.apply(X[i], this.u, dt);
        }

        MultivariateGaussian guassian = unscentedTransform(Y, weight);

        this.x = guassian.getMean();
        this.p = guassian.getCovariance();
    }

    /**
     * Updates the state using the Julier sampling method
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    private void update_julier(Function<MatReal, MatReal> h, MatReal z, MatReal r) {
        MatReal[] X = new MatReal[this.x.getRows()*2+1];
        double[] weight = new double[this.x.getRows()*2+1];
        generateSigmaPoints(X, weight, this.x, this.p);

        MatReal[] Z = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Z[i] = h.apply(X[i]);
        }

        MultivariateGaussian gaussian = unscentedTransform(Z, weight);

        MatReal t = X[0].subtract(this.x).multiply(Z[0].subtract(gaussian.getMean()).transpose()).multiply(weight[0]);
        for (int i = 1; i < X.length; i++) {
            t = t.add(X[i].subtract(this.x).multiply(Z[i].subtract(gaussian.getMean()).transpose()).multiply(weight[i]));
        }

        MatReal k = t.multiply(gaussian.getCovariance().add(r).inverse());
        this.x = this.x.add(k.multiply(z.subtract(gaussian.getMean())));
        this.p = this.p.subtract(k.multiply(gaussian.getCovariance().add(r)).multiply(k.transpose()));
    }

    /**
     * Unscented transform using the Julier sampling method
     * 
     * @param sigmaPoints sigma points
     * @param weight weights
     * @return
     */
    private MultivariateGaussian unscentedTransform(MatReal[] sigmaPoints, double[] weight) {
        MatReal meanOut = sigmaPoints[0].multiply(weight[0]);
        for (int i = 1; i < sigmaPoints.length; i++) {
            meanOut = meanOut.add(sigmaPoints[i].multiply(weight[i]));
        }
        MatReal covarianceOut = sigmaPoints[0].subtract(meanOut).multiply(sigmaPoints[0].subtract(meanOut).transpose()).multiply(weight[0]);
        for (int i = 1; i < sigmaPoints.length; i++) {
            covarianceOut = covarianceOut.add(sigmaPoints[i].subtract(meanOut).multiply(sigmaPoints[i].subtract(meanOut).transpose()).multiply(weight[i]));
        }

        return new MultivariateGaussian(meanOut, covarianceOut);
    }

    /**
     * Generates sigma points using the Julier sampling method
     * 
     * @param sigmaPoints sigma points to generate
     * @param weight weights to generate
     * @param x state
     * @param p state covariance
     */
    private void generateSigmaPoints(MatReal[] sigmaPoints, double[] weight, MatReal x, MatReal p) {
        MatReal tolerance = MatReal.identity(p.getRows()).multiply(1e-9);
        p = p.add(tolerance);

        int n = sigmaPoints.length/2;

        sigmaPoints[0] = x;
        for (int i = 1; i <= n; i++) {
            sigmaPoints[i] = x.add(p.multiply(n+this.l).choleskyDecompose(choleskySolver).getCol(i-1));
        }
        for (int i = n+1; i <= 2*n; i++) {
            sigmaPoints[i] = x.subtract(p.multiply(n+this.l).choleskyDecompose(choleskySolver).getCol(i-n-1));
        }

        weight[0] = this.l / (this.l+n);
        for (int i = 1; i < n*2+1; i++) {
            weight[i] = 1 / (2*this.l+2*n);
        }
    }

    /**
     * Sets the sampling variables for the Julier sampling method
     * 
     * @param l lambda
     */
    public void setSigmaVariables(double l) {
        this.l = l;
    }

    /**
     * Gets the current state
     * 
     * @return the current state
     */
    public MatReal getState() {
        return this.x;
    }

    /**
     * Gets the current state covariance
     * 
     * @return the current state covariance
     */
    public MatReal getCovariance() {
        return this.p;
    }

    /**
     * Sets the state transition function
     * 
     * @param f the new state transition function
     */
    public void setF(TriFunction<MatReal, MatReal, Double, MatReal> f) {
        this.f = f;
    }

    /**
     * Sets the the process noise covariance
     * 
     * @param q the new process noise covariance
     */
    public void setQ(MatReal q) {
        this.q = q;
    }

    /**
     * Sets the control input
     * 
     * @param u the new control input
     */
    public void setU(MatReal u) {
        this.u = u;
    }
}
