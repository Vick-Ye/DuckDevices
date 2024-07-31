package com.quackology.duckfilter.filters;

import java.util.function.Function;

import com.quackology.duckfilter.functions.QuadFunction;
import com.quackology.duckfilter.functions.TriFunction;
import com.quackology.duckfilter.spaces.MatReal;
import com.quackology.duckfilter.spaces.manifolds.CompoundManifold;

/**
 * Square root unscented Kalman filter on Manifolds
 * <p>
 * Modification of the UKFM filter through square root propagation similar to the SRUKF described in the paper "The square-root unscented Kalman filter for state and parameter-estiMatRealion" by Rudolph van der Merwe and Eric A. Wan
 */
public class SRUKFM {
    /**
     * State of the filter
     */
    private CompoundManifold x;

    /**
     * State covariance
     */
    private MatReal p;

    /**
     * State transition function
     * <p>
     * f(x, q, u, dt), x = state, q = noise, u = control input, dt = time step
     */
    private QuadFunction<CompoundManifold, MatReal, MatReal, Double, CompoundManifold> f; 

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
     * Constructor for the Square root Unscented Kalman Filter on Manifolds
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
    public SRUKFM(CompoundManifold x, MatReal p, MatReal q, MatReal u) {
        MatReal tolerance = MatReal.identity(p.getRows()).multiply(1e-6);

        this.x = x;
        this.p = p.add(tolerance).choleskyDecompose();
        this.q = q.add(tolerance).choleskyDecompose();
        this.u = u;

        this.a = 0.001;
        this.b = 2;
        this.k = 0;
        this.l = 3-x.getDimensions();
        
    }

    /**
     * Constructor for the Square root Unscented Kalman Filter on Manifolds
     * <p>
     * Must set state transition function using sefF
     * 
     * @param sampling sampling method to use
     * @param x initial state
     * @param p initial state covariance
     * @param q process noise covariance
     * @param u control input
     */
    public SRUKFM(Sampling sampling, CompoundManifold x, MatReal p, MatReal q, MatReal u) {
        this(x, p, q, u);
        this.sampling = sampling;
    }

    /**
     * Constructor for the Square root Unscented Kalman Filter on Manifolds
     * <p>
     * Default sampling method is Merwe
     * 
     * @param x initial state
     * @param p initial state covariance
     * @param f state transition function
     * @param q process noise covariance
     * @param u control input
     */
    public SRUKFM(CompoundManifold x, MatReal p, QuadFunction<CompoundManifold, MatReal, MatReal, Double, CompoundManifold> f, MatReal q, MatReal u) {
        this(x, p, q, u);
        this.f = f;
    }

    /**
     * Constructor for the Square root Unscented Kalman Filter on Manifolds
     * 
     * @param sampling sampling method to use
     * @param x initial state
     * @param p initial state covariance
     * @param f state transition function
     * @param q process noise covariance
     * @param u control input
     */
    public SRUKFM(Sampling sampling, CompoundManifold x, MatReal p, QuadFunction<CompoundManifold, MatReal, MatReal, Double, CompoundManifold> f, MatReal q, MatReal u) {
        this(sampling, x, p, q, u);
        this.f = f;
    }

    /**
     * Predicts the next state with augmented state and covariance
     * 
     * @param dt time step if state transition function is time dependent
     */
    public void predict(double dt) {
        switch (this.sampling) {
            case MERWE:
                predict_merwe(dt);
                break;
            case JULIER:
                predict_julier(dt);
                break;
        }
    }

    /**
     * Updates the state with noise applied to the state already
     * <p>
     * x = h(x_) where x_ = x.phi(noise)
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    public void update(Function<CompoundManifold, MatReal> h, MatReal z, MatReal r) {
        switch (this.sampling) {
            case MERWE:
                update_merwe(h, z, r);
                break;
            case JULIER:
                update_julier(h, z, r);
                break;
        }
    }

    /**
     * Updates the state given the noise before applying it to the state
     * <p>
     * x = h(x, p, q) where x = state, p = state noise, q = measurement noise 
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    public void update(TriFunction<CompoundManifold, MatReal, MatReal, MatReal> h, MatReal z, MatReal r) {
        switch (this.sampling) {
            case MERWE:
                update_merwe(h, z, r);
                break;
            case JULIER:
                update_julier(h, z, r);
                break;
        }
    }

    /**
     * Predicts the next state with Merwe sampling
     * 
     * @param dt time step if state transition function is time dependent
     */
    private void predict_merwe(double dt) {
        //project mean
        CompoundManifold x = this.f.apply(this.x, MatReal.empty(this.p.getRows(), 1), this.u, dt);

        //generate state covariance noise
        double[] weight = new double[this.p.getRows()*2+1];
        MatReal W = generateNoise_merwe(weight, this.p);

        //generate sigma points in manifold
        CompoundManifold[] X = new CompoundManifold[this.p.getRows()*2];
        for (int i = 0; i < X.length; i++) {
            CompoundManifold x_ = this.x.phi(W.getCol(i));
            X[i] = this.f.apply(x_, MatReal.empty(W.getRows(), 1), this.u, dt);
        }

        //build compound MatRealrix for covariance update
        MatReal c = X[0].phi_inverse_vector(x).multiply(Math.sqrt(weight[1]));
        for (int i = 1; i < X.length; i++) {
            c = MatReal.horizontal(c, X[i].phi_inverse_vector(x).multiply(Math.sqrt(weight[i+1])));
        }
        c = c.transpose();

        //qr
        MatReal p = c.QRDecomposition()[1].transpose();


        //generate white noise
        W = generateNoise_merwe(weight, this.q);

        //generate sigma points
        CompoundManifold[] Y = new CompoundManifold[this.p.getRows()*2];
        for (int i = 0; i < Y.length; i++) {
            Y[i] = this.f.apply(this.x, W.getCol(i), this.u, dt);
        }

        //build compound MatRealrix for covariance update
        c = Y[0].phi_inverse_vector(x).multiply(Math.sqrt(weight[1]));
        for (int i = 1; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].phi_inverse_vector(x).multiply(Math.sqrt(weight[i+1])));
        }
        c = c.transpose();

        //qr
        MatReal p_ = c.QRDecomposition()[1].transpose();

        //p = sqrt(p*p.t + p_*p_.t)
        this.p = MatReal.horizontal(p, p_).transpose().QRDecomposition()[1].transpose();
        this.x = x;
    }

    /**
     * Updates the state
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    private void update_merwe(Function<CompoundManifold, MatReal> h, MatReal z, MatReal r) {
        //generate state covariance noise
        double[] weight = new double[this.p.getRows()*2+1];
        MatReal W = generateNoise_merwe(weight, this.p);

        //generate sigma points in manifold
        MatReal[] Y = new MatReal[this.p.getRows()*2+1];
        Y[0] = h.apply(this.x.phi(MatReal.empty(W.getRows(), 1)));
        for (int i = 1; i < Y.length; i++) {
            CompoundManifold x_ = this.x.phi(W.getCol(i-1));
            Y[i] = h.apply(x_);
        }

        MatReal y = Y[0].multiply(weight[0]);
        for (int i = 1; i < Y.length; i++) {
            y = y.add(Y[i].multiply(weight[i]));
        }

        //covariances
        //innovation covariance
        MatReal c = Y[1].subtract(y).multiply(Math.sqrt(weight[1]));
        for (int i = 2; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].subtract(y).multiply(Math.sqrt(weight[i])));
        }
        c = MatReal.horizontal(c, r).transpose();

        //qr
        MatReal s = c.QRDecomposition()[1].transpose();

        //rank-1 cholesky update
        s = MatReal.cholUpdate(s, Y[0].subtract(y), weight[0]);

        //cross covariance
        MatReal t = W.getCol(0).multiply(Y[1].subtract(y).transpose()).multiply(weight[1]);
        for (int i = 2; i < Y.length; i++) {
            t = t.add(W.getCol(i-1).multiply(Y[i].subtract(y).transpose()).multiply(weight[i]));
        }

        //kalman gain for square root unscented kalman filter through efficient least squares
        MatReal k = MatReal.backwardSub(s.transpose(), MatReal.forwardSub(s, t.transpose())).transpose();

        MatReal u = k.multiply(s);

        this.x = this.x.phi(k.multiply(z.subtract(y)));
        this.p = MatReal.cholUpdate(this.p, u, -1);
    }

    /**
     * Updates the state
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    private void update_merwe(TriFunction<CompoundManifold, MatReal, MatReal, MatReal> h, MatReal z, MatReal r) {
        //augment covariance
        MatReal p_aug = MatReal.diagonal(this.p, r);
        
        //generate state covariance noise
        double[] weight = new double[p_aug.getRows()*2+1];
        MatReal W = generateNoise_merwe(weight, p_aug);

        MatReal Wp = W.subMat(0, 0, this.p.getRows(), W.getCols());
        MatReal Wv = W.subMat(this.p.getRows(), 0, r.getRows(), W.getCols());

        //generate sigma points in manifold
        MatReal[] Y = new MatReal[p_aug.getRows()*2+1];
        Y[0] = h.apply(this.x, MatReal.empty(Wp.getRows(), 1), MatReal.empty(Wv.getRows(), 1));
        for (int i = 1; i < Y.length; i++) {
            Y[i] = h.apply(this.x, Wp.getCol(i-1), Wv.getCol(i-1));
        }

        MatReal y = Y[0].multiply(weight[0]);
        for (int i = 1; i < Y.length; i++) {
            y = y.add(Y[i].multiply(weight[i]));
        }

        //covariances
        //innovation covariance
        MatReal c = Y[1].subtract(y).multiply(Math.sqrt(weight[1]));
        for (int i = 2; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].subtract(y).multiply(Math.sqrt(weight[i])));
        }
        c = c.transpose();

        //qr
        MatReal s = c.QRDecomposition()[1].transpose();

        //rank-1 cholesky update
        s = MatReal.cholUpdate(s, Y[0].subtract(y), weight[0]);

        //cross covariance
        MatReal t = Wp.getCol(0).multiply(Y[1].subtract(y).transpose()).multiply(weight[1]);
        for (int i = 2; i < Y.length; i++) {
            t = t.add(Wp.getCol(i-1).multiply(Y[i].subtract(y).transpose()).multiply(weight[i]));
        }

        //kalman gain for square root unscented kalman filter through efficient least squares
        MatReal k = MatReal.backwardSub(s.transpose(), MatReal.forwardSub(s, t.transpose())).transpose();

        MatReal u = k.multiply(s);

        this.x = this.x.phi(k.multiply(z.subtract(y)));
        this.p = MatReal.cholUpdate(this.p, u, -1);
    }

    /**
     * Generates noise for Merwe sampling
     * 
     * @param weight weights
     * @param p covariance
     * @return noise vectors
     */
    private MatReal generateNoise_merwe(double[] weight, MatReal p) {
        int n = weight.length/2;

        double l = this.a*this.a*(n+this.k)-n;

        MatReal W = p.multiply(Math.sqrt(n+l));
        W = MatReal.horizontal(W, W.multiply(-1));

        weight[0] = l/(l+n) + 1-this.a*this.a+this.b;
        for (int i = 1; i < n*2+1; i++) {
            weight[i] = 1 / (2*l+2*n);
        }

        return W;
    }

    /**
     * Sets the sampling variables for Merwe sampling method
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
     * Predicts the next state with Julier sampling
     * 
     * @param dt time step if state transition function is time dependent
     */
    private void predict_julier(double dt) {
        //project mean
        CompoundManifold x = this.f.apply(this.x, MatReal.empty(this.p.getRows(), 1), this.u, dt);

        //generate state covariance noise
        double[] weight = new double[this.p.getRows()*2+1];
        MatReal W = generateNoise_julier(weight, this.p);

        //generate sigma points in manifold
        CompoundManifold[] X = new CompoundManifold[this.p.getRows()*2];
        for (int i = 0; i < X.length; i++) {
            CompoundManifold x_ = this.x.phi(W.getCol(i));
            X[i] = this.f.apply(x_, MatReal.empty(W.getRows(), 1), this.u, dt);
        }

        //build compound MatRealrix for covariance update
        MatReal c = X[0].phi_inverse_vector(x).multiply(Math.sqrt(weight[1]));
        for (int i = 1; i < X.length; i++) {
            c = MatReal.horizontal(c, X[i].phi_inverse_vector(x).multiply(Math.sqrt(weight[i+1])));
        }
        c = c.transpose();

        //qr
        MatReal p = c.QRDecomposition()[1].transpose();


        //generate white noise
        W = generateNoise_julier(weight, this.q);

        //generate sigma points
        CompoundManifold[] Y = new CompoundManifold[this.p.getRows()*2];
        for (int i = 0; i < Y.length; i++) {
            Y[i] = this.f.apply(this.x, W.getCol(i), this.u, dt);
        }

        //build compound MatRealrix for covariance update
        c = Y[0].phi_inverse_vector(x).multiply(Math.sqrt(weight[1]));
        for (int i = 1; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].phi_inverse_vector(x).multiply(Math.sqrt(weight[i+1])));
        }
        c = c.transpose();

        //qr
        MatReal p_ = c.QRDecomposition()[1].transpose();

        //p = sqrt(p*p.t + p_*p_.t)
        this.p = MatReal.horizontal(p, p_).transpose().QRDecomposition()[1].transpose();
        this.x = x;
    }

    /**
     * Updates the state using Julier sampling method
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    private void update_julier(Function<CompoundManifold, MatReal> h, MatReal z, MatReal r) {
        //generate state covariance noise
        double[] weight = new double[this.p.getRows()*2+1];
        MatReal W = generateNoise_julier(weight, this.p);

        //generate sigma points in manifold
        MatReal[] Y = new MatReal[this.p.getRows()*2+1];
        Y[0] = h.apply(this.x.phi(MatReal.empty(W.getRows(), 1)));
        for (int i = 1; i < Y.length; i++) {
            CompoundManifold x_ = this.x.phi(W.getCol(i-1));
            Y[i] = h.apply(x_);
        }

        MatReal y = Y[0].multiply(weight[0]);
        for (int i = 1; i < Y.length; i++) {
            y = y.add(Y[i].multiply(weight[i]));
        }

        //covariances
        //innovation covariance
        MatReal c = Y[1].subtract(y).multiply(Math.sqrt(weight[1]));
        for (int i = 2; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].subtract(y).multiply(Math.sqrt(weight[i])));
        }
        c = MatReal.horizontal(c, r).transpose();

        //qr
        MatReal s = c.QRDecomposition()[1].transpose();

        //rank-1 cholesky update
        s = MatReal.cholUpdate(s, Y[0].subtract(y), weight[0]);

        //cross covariance
        MatReal t = W.getCol(0).multiply(Y[1].subtract(y).transpose()).multiply(weight[1]);
        for (int i = 2; i < Y.length; i++) {
            t = t.add(W.getCol(i-1).multiply(Y[i].subtract(y).transpose()).multiply(weight[i]));
        }

        //kalman gain for square root unscented kalman filter through efficient least squares
        MatReal k = MatReal.backwardSub(s.transpose(), MatReal.forwardSub(s, t.transpose())).transpose();

        MatReal u = k.multiply(s);

        this.x = this.x.phi(k.multiply(z.subtract(y)));
        this.p = MatReal.cholUpdate(this.p, u, -1);
    }

    /**
     * Updates the state using Julier sampling method
     * 
     * @param h measurement function
     * @param z measurement
     * @param r measurement noise covariance
     */
    private void update_julier(TriFunction<CompoundManifold, MatReal, MatReal, MatReal> h, MatReal z, MatReal r) {
        //augment covariance
        MatReal p_aug = MatReal.diagonal(this.p, r);
        
        //generate state covariance noise
        double[] weight = new double[p_aug.getRows()*2+1];
        MatReal W = generateNoise_julier(weight, p_aug);

        MatReal Wp = W.subMat(0, 0, this.p.getRows(), W.getCols());
        MatReal Wv = W.subMat(this.p.getRows(), 0, r.getRows(), W.getCols());

        //generate sigma points in manifold
        MatReal[] Y = new MatReal[p_aug.getRows()*2+1];
        Y[0] = h.apply(this.x, MatReal.empty(Wp.getRows(), 1), MatReal.empty(Wv.getRows(), 1));
        for (int i = 1; i < Y.length; i++) {
            Y[i] = h.apply(this.x, Wp.getCol(i-1), Wv.getCol(i-1));
        }

        MatReal y = Y[0].multiply(weight[0]);
        for (int i = 1; i < Y.length; i++) {
            y = y.add(Y[i].multiply(weight[i]));
        }

        //covariances
        //innovation covariance
        MatReal c = Y[1].subtract(y).multiply(Math.sqrt(weight[1]));
        for (int i = 2; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].subtract(y).multiply(Math.sqrt(weight[i])));
        }
        c = c.transpose();

        //qr
        MatReal s = c.QRDecomposition()[1].transpose();

        //rank-1 cholesky update
        s = MatReal.cholUpdate(s, Y[0].subtract(y), weight[0]);

        //cross covariance
        MatReal t = Wp.getCol(0).multiply(Y[1].subtract(y).transpose()).multiply(weight[1]);
        for (int i = 2; i < Y.length; i++) {
            t = t.add(Wp.getCol(i-1).multiply(Y[i].subtract(y).transpose()).multiply(weight[i]));
        }

        //kalman gain for square root unscented kalman filter through efficient least squares
        MatReal k = MatReal.backwardSub(s.transpose(), MatReal.forwardSub(s, t.transpose())).transpose();

        MatReal u = k.multiply(s);

        this.x = this.x.phi(k.multiply(z.subtract(y)));
        this.p = MatReal.cholUpdate(this.p, u, -1);
    }

    /**
     * Generates noise for Julier sampling
     * 
     * @param weight weights
     * @param p covariance
     * @return noise vectors
     */
    private MatReal generateNoise_julier(double[] weight, MatReal p) {
        int n = weight.length/2;

        MatReal W = p.multiply(Math.sqrt(n+this.l));
        W = MatReal.horizontal(W, W.multiply(-1));

        weight[0] = this.l / (this.l+n);
        for (int i = 1; i < n*2+1; i++) {
            weight[i] = 1 / (2*this.l+2*n);
        }

        return W;
    }

    /**
     * Sets the sampling variables for Julier sampling method
     * 
     * @param l
     */
    public void setSigmaVariables(double l) {
        this.l = l;
    }
    
    /**
     * Gets the state of the filter
     * 
     * @return the compound manifold
     */
    public CompoundManifold getState() {
        return this.x;
    }

    /**
     * Gets the state covariance
     * 
     * @return the state covariance
     */
    public MatReal getCovariance() {
        return this.p.multiply(this.p.transpose());
    }

    /**
     * Sets the state transition function
     * 
     * @param f new state transition function
     */
    public void setF(QuadFunction<CompoundManifold, MatReal, MatReal, Double, CompoundManifold> f) {
        this.f = f;
    }

    /**
     * Sets the state transition function
     * 
     * @param q new process noise covariance
     */
    public void setQ(MatReal q) {
        this.q = q;
    }

    /**
     * Sets the control input
     * 
     * @param u new control input
     */
    public void setU(MatReal u) {
        this.u = u;
    }
}
