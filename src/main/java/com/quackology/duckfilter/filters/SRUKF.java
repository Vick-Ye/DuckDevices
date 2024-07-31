package com.quackology.duckfilter.filters;

import java.util.function.Function;

import org.ojalgo.matrix.decomposition.QR;

import com.quackology.duckfilter.functions.TriFunction;
import com.quackology.duckfilter.spaces.MatReal;

/**
 * Square root Unscented Kalman Filter
 * <p>
 * Based on the paper "The square-root unscented Kalman filter for state and parameter-estimation" by Rudolph van der Merwe and Eric A. Wan
 * <p>
 * With modifications for the cholesky update based on the paper "A more efficient rank-one covariance matrix update for evolution strategies" by Oswin Krause and Christian Igel
 */
public class SRUKF {
    /**
     * State of the filter
     */
    private MatReal x;

    /**
     * Square root state covariance
     */
    private MatReal p;

    /**
     * State transition function
     * <p> 
     * f(x, u, dt), x = state, u = control input, dt = time step
     */
    private TriFunction<MatReal, MatReal, Double, MatReal> f;

    /**
     * Square root process noise covariance
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
     * QR Solver
     */
    private QR<Double> qrSolver;

    /**
     * Constructor for the square root unscented Kalman filter
     * <p>
     * Must set state transition function with setF
     * 
     * @param x initial state
     * @param p initial state covariance
     * @param q process noise covariance
     * @param u control input
     */
    public SRUKF(MatReal x, MatReal p, MatReal q, MatReal u) {
        MatReal tolerance = MatReal.identity(p.getRows()).multiply(1e-6);

        this.x = x;
        this.p = p.add(tolerance).choleskyDecompose();
        this.q = q.add(tolerance).choleskyDecompose();
        this.u = u;

        this.a = 0.001;
        this.b = 2;
        this.k = 0;

        qrSolver = QR.R064.make(this.p.getRows()*2+1, this.x.getDimensions());
    }

    /**
     * Constructor for the square root unscented Kalman filter
     * 
     * @param x initial state
     * @param p initial state covariance
     * @param f state transition function
     * @param q process noise covariance
     * @param u control input
     */
    public SRUKF(MatReal x, MatReal p, TriFunction<MatReal, MatReal, Double, MatReal> f, MatReal q, MatReal u) {
        this(x, p, q, u);
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
        MatReal[] X = new MatReal[this.x.getRows()*2+1];
        double[] weightM = new double[this.x.getRows()*2+1];
        double[] weightC = new double[this.x.getRows()*2+1];
        generateSigmaPoints(X, weightM, weightC, this.x, this.p);

        MatReal[] Y = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Y[i] = f.apply(X[i], this.u, dt);
        }

        this.x = Y[0].multiply(weightM[0]);
        for (int i = 1; i < Y.length; i++) {
            this.x = this.x.add(Y[i].multiply(weightM[i]));
        }

        //build compound MatRealrix for covariance update
        MatReal c = Y[1].subtract(this.x).multiply(Math.sqrt(weightC[1]));
        for (int i = 2; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].subtract(this.x).multiply(Math.sqrt(weightC[i])));
        }
        c = MatReal.horizontal(c, this.q).transpose();

        //qr
        MatReal s = c.QRDecompose(qrSolver)[1].transpose();
        
        //rank-1 cholesky update
        s = MatReal.cholUpdate(s, Y[0].subtract(this.x), weightC[0]);

        this.p = s;
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

        this.x = Y[0].multiply(weightM[0]);
        for (int i = 1; i < Y.length; i++) {
            this.x = this.x.add(Y[i].multiply(weightM[i]));
        }

        //build compound MatRealrix for covariance update
        MatReal c = Y[1].subtract(this.x).multiply(Math.sqrt(weightC[1]));
        for (int i = 2; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].subtract(this.x).multiply(Math.sqrt(weightC[i])));
        }
        c = c.transpose(); //no need to include q as it is already incorported by the state augmentation

        //qr
        MatReal s = c.QRDecompose(qrSolver)[1].transpose();
        
        //rank-1 cholesky update
        s = MatReal.cholUpdate(s, Y[0].subtract(this.x), weightC[0]);

        this.p = s;
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
        MatReal[] X = new MatReal[this.x.getRows()*2+1];
        double[] weightM = new double[this.x.getRows()*2+1];
        double[] weightC = new double[this.x.getRows()*2+1];
        generateSigmaPoints(X, weightM, weightC, this.x, this.p);

        MatReal[] Y = new MatReal[X.length];
        for (int i = 0; i < X.length; i++) {
            Y[i] = h.apply(X[i]);
        }

        MatReal y = Y[0].multiply(weightM[0]);
        for (int i = 1; i < Y.length; i++) {
            y = y.add(Y[i].multiply(weightM[i]));
        }
        
        //build compound MatRealrix for innovation covariance
        MatReal c = Y[1].subtract(y).multiply(Math.sqrt(weightC[1]));
        for (int i = 2; i < Y.length; i++) {
            c = MatReal.horizontal(c, Y[i].subtract(y).multiply(Math.sqrt(weightC[i])));
        }
        c = MatReal.horizontal(c, r).transpose();

        //qr
        MatReal s = c.QRDecompose(qrSolver)[1].transpose();

        //rank-1 cholesky update
        s = MatReal.cholUpdate(s, Y[0].subtract(y), weightC[0]);

        //cross covariance
        MatReal t = X[0].subtract(this.x).multiply(Y[0].subtract(y).transpose()).multiply(weightC[0]);
        for (int i = 1; i < X.length; i++) {
            t = t.add(X[i].subtract(this.x).multiply(Y[i].subtract(y).transpose()).multiply(weightC[i]));
        }

        //kalman gain for square root unscented kalman filter through efficent least squares
        MatReal k = MatReal.backwardSub(s.transpose(), MatReal.forwardSub(s, t.transpose())).transpose();

        MatReal u = k.multiply(s);

        this.x = this.x.add(k.multiply(z.subtract(y)));
        this.p = MatReal.cholUpdate(this.p, u, -1);
    }

    /**
     * Generates sigma points and weights for the unscented Kalman filter
     * 
     * @param sigmaPoints sigma points to generate
     * @param weightM weights to generate (mean)
     * @param weightC weights to generate (covariance)
     * @param x state
     * @param p state covariance
     */
    private void generateSigmaPoints(MatReal[] sigmaPoints, double[] weightM, double[] weightC, MatReal x, MatReal p) {
        int n = sigmaPoints.length/2;
        double l = a*a * (n+k) - n;

        sigmaPoints[0] = x;
        for (int i = 1; i <= n; i++) {
            sigmaPoints[i] = x.add(p.multiply(Math.sqrt(n+l)).getCol(i-1));
        }
        for (int i = n+1; i <= 2*n; i++) {
            sigmaPoints[i] = x.subtract(p.multiply(Math.sqrt(n+l)).getCol(i-n-1));
        }

        weightM[0] = l / (l+n);
        weightC[0] = weightM[0] + 1-a*a+b;
        for (int i = 1; i < n*2+1; i++) {
            weightM[i] = 1 / (2*l+2*n);
            weightC[i] = 1 / (2*l+2*n);
        }
    }

    /**
     * Sets the sampling parameters for the unscented Kalman filter (Merwe)
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
     * Sets the state of the filter
     * 
     * @return state of the filter
     */
    public MatReal getState() {
        return this.x;
    }

    /**
     * Sets the covariance of the filter
     * 
     * @return covariance of the filter
     */
    public MatReal getCovariance() {
        return this.p.multiply(this.p.transpose());
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
     * Sets the process noise covariance
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
