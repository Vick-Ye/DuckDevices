package duckfilter.ukfmtest;

import java.util.ArrayList;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import com.quackology.duckfilter.filters.SRUKFM;
import com.quackology.duckfilter.filters.UKF;
import com.quackology.duckfilter.filters.UKFM;
import com.quackology.duckfilter.spaces.MatReal;
import com.quackology.duckfilter.spaces.manifolds.CompoundManifold;
import com.quackology.duckfilter.spaces.manifolds.Manifold;
import com.quackology.duckfilter.spaces.manifolds.ManifoldImpl;
import com.quackology.duckfilter.spaces.manifolds.VectorSpaceFactory;
import com.quackology.duckfilter.spaces.manifolds.liegroups.SE2;
import com.quackology.duckfilter.spaces.manifolds.liegroups.SO2;

public class UKFM_Test {
    @SuppressWarnings("unchecked")
    public static void main(String args[] ) throws Exception {
        double dt = 0.5;

        // Noise
        double xNoise = 0;
        double yNoise = 0;
        double rotNoise = Math.toRadians(0);
        double xVelNoise = 2;
        double yVelNoise = 2;
        double rotVelNoise = Math.toRadians(1);
        double measureXNoise = 25;
        double measureYNoise = 25;
        double measureRotNoise = Math.toRadians(10);
        
        // Filter initial conditions
        double x = -1;
        double Y = 3;
        double rot = Math.toRadians(-1);
        double xVel = 20;
        double yVel = 50;
        double rotVel = Math.toRadians(6);
        MatReal P = MatReal.diagonal(new MatReal[] {
            new MatReal(5),
            new MatReal(5),
            new MatReal(Math.toRadians(5)),
            new MatReal(15),
            new MatReal(15),
            new MatReal(Math.toRadians(5))
        });
        P = P.multiply(P);
        MatReal Q = MatReal.diagonal(new MatReal[] {
            new MatReal(xNoise),
            new MatReal(yNoise),
            new MatReal(rotNoise),
            new MatReal(xVelNoise),
            new MatReal(yVelNoise),
            new MatReal(rotVelNoise)
        });
        Q = Q.multiply(Q);
        MatReal R = MatReal.diagonal(new MatReal[] {
            new MatReal(measureXNoise),
            new MatReal(measureYNoise),
            new MatReal(measureRotNoise)
        });
        R = R.multiply(R);

        // Simulation
        Robot robot = new Robot(
            SE2.FACTORY.exp(MatReal.empty(3, 1)),
            new MatReal(new double[][] {
                {25},
                {40},
                {Math.toRadians(3)}
            }), 
            new MatReal(new double[][] {
                {xNoise},
                {yNoise},
                {rotNoise},
                {xVelNoise},
                {yVelNoise},
                {rotVelNoise}
            }),
            new MatReal(new double[][] {
                {measureXNoise},
                {measureYNoise},
                {measureRotNoise},
            })
        );

        // UKF
        UKF ukf = new UKF(
            new MatReal(new double[][] {
                {x},
                {Y},
                {rot},
                {xVel},
                {yVel},
                {rotVel}
            }),
            P,
            Q,
            MatReal.empty(6, 1)
        );

        ukf.setF((x_, u_, dt_) -> {
            SE2 pos_ = SE2.FACTORY.exp(new MatReal(new double[][] {
                {x_.get(0, 0)},
                {x_.get(1, 0)},
                {x_.get(2, 0)}
            }));
            MatReal vel_ = new MatReal(new double[][] {
                {x_.get(3, 0)},
                {x_.get(4, 0)},
                {x_.get(5, 0)}
            });
            return MatReal.vertical(new MatReal[] {
                SE2.FACTORY.pseudo_log(pos_.phi(vel_.multiply(dt_))),
                vel_
            });
        });

        // UKFM
        UKFM ukfm = new UKFM(
            new CompoundManifold(new Manifold[] {
                SE2.FACTORY.exp(new MatReal(new double[][] {
                    {x},
                    {Y},
                    {rot}
                })),
                VectorSpaceFactory.make(new MatReal(new double[][] {
                    {xVel},
                    {yVel},
                    {rotVel}
                }))
            }),
            P,
            Q,
            MatReal.empty(6, 1)
        );

        ukfm.setF((CompoundManifold x_, MatReal w, MatReal u_, Double dt_) -> {
            SE2 pos_ = (SE2) x_.getManifold(0);
            ManifoldImpl<MatReal> vel_ = (ManifoldImpl<MatReal>) x_.getManifold(1);

            MatReal posNoise_ = MatReal.vertical(new MatReal[] {w.getRow(0), w.getRow(1), w.getRow(2)});
            MatReal velNoise_ = MatReal.vertical(new MatReal[] {w.getRow(3), w.getRow(4), w.getRow(5)});

            pos_ = pos_.compose(SE2.FACTORY.pseudo_exp(posNoise_));
            vel_ = vel_.phi(velNoise_);

            pos_ = pos_.phi(vel_.getValue().multiply(dt_));

            return new CompoundManifold(new Manifold[] {
                pos_,
                vel_
            });
        });

        // SRUKFM
        SRUKFM srukfm = new SRUKFM(
            new CompoundManifold(new Manifold[] {
                SE2.FACTORY.exp(new MatReal(new double[][] {
                    {x},
                    {Y},
                    {rot}
                })),
                VectorSpaceFactory.make(new MatReal(new double[][] {
                    {xVel},
                    {yVel},
                    {rotVel}
                }))
            }),
            P,
            Q,
            MatReal.empty(6, 1)
        );

        srukfm.setF((CompoundManifold x_, MatReal w, MatReal u_, Double dt_) -> {
            SE2 pos_ = (SE2) x_.getManifold(0);
            ManifoldImpl<MatReal> vel_ = (ManifoldImpl<MatReal>) x_.getManifold(1);

            MatReal posNoise_ = MatReal.vertical(new MatReal[] {w.getRow(0), w.getRow(1), w.getRow(2)});
            MatReal velNoise_ = MatReal.vertical(new MatReal[] {w.getRow(3), w.getRow(4), w.getRow(5)});

            pos_ = pos_.compose(SE2.FACTORY.pseudo_exp(posNoise_));
            vel_ = vel_.phi(velNoise_);
            pos_ = pos_.phi(vel_.getValue().multiply(dt_));

            return new CompoundManifold(new Manifold[] {
                pos_,
                vel_
            });
        });
       
        // Data
        ArrayList<Double> time = new ArrayList<>();
        ArrayList<Double> trueX = new ArrayList<>();
        ArrayList<Double> trueY = new ArrayList<>();
        ArrayList<Double> trueRot = new ArrayList<>();
        ArrayList<Double> ukfX = new ArrayList<>();
        ArrayList<Double> ukfY = new ArrayList<>();
        ArrayList<Double> ukfRot = new ArrayList<>();
        ArrayList<Double> ukfmX = new ArrayList<>();
        ArrayList<Double> ukfmY = new ArrayList<>();
        ArrayList<Double> ukfmRot = new ArrayList<>();
        ArrayList<Double> srukfmX = new ArrayList<>();
        ArrayList<Double> srukfmY = new ArrayList<>();
        ArrayList<Double> srukfmRot = new ArrayList<>();
        ArrayList<Double> ukfSDX = new ArrayList<>();
        ArrayList<Double> ukfSDY = new ArrayList<>();
        ArrayList<Double> ukfSDRot = new ArrayList<>();
        ArrayList<Double> ukfmSDX = new ArrayList<>();
        ArrayList<Double> ukfmSDY = new ArrayList<>();
        ArrayList<Double> ukfmSDRot = new ArrayList<>();
        ArrayList<Double> srukfmSDX = new ArrayList<>();
        ArrayList<Double> srukfmSDY = new ArrayList<>();
        ArrayList<Double> srukfmSDRot = new ArrayList<>();
        

        for(int i = 0; i < 100; i++) {
            time.add(i*dt+dt);
            robot.move(dt);

            ukf.predict(dt);
            ukfm.predict(dt);
            srukfm.predict(dt);

            // measurement
            MatReal measurement = robot.getMeasurement();
            ukf.update((x_) -> x_.subMat(0, 0, 3, 1), measurement, R);
            ukfm.update(
                (x_, p_, v_) -> SE2.FACTORY.log(SE2.FACTORY.exp(p_.subMat(0, 0, 3, 1)).compose(SE2.FACTORY.exp(v_.subMat(0, 0, 3, 1)))),
                SE2.FACTORY.log(((SE2) ukfm.getState().getManifold(0)).inverse().compose(SE2.FACTORY.pseudo_exp(measurement))), 
                R
            ); 
            srukfm.update(
                (x_, p_, v_) -> SE2.FACTORY.log(SE2.FACTORY.exp(p_.subMat(0, 0, 3, 1)).compose(SE2.FACTORY.exp(v_.subMat(0, 0, 3, 1)))),
                SE2.FACTORY.log(((SE2) srukfm.getState().getManifold(0)).inverse().compose(SE2.FACTORY.pseudo_exp(measurement))), 
                R.choleskyDecompose()
            ); 

            // Data
            ukfX.add(ukf.getState().get(0, 0));
            ukfY.add(ukf.getState().get(1, 0));
            ukfRot.add(ukf.getState().get(2, 0));
            ukfSDY.add(Math.sqrt(ukf.getCovariance().get(1, 1)));
            ukfSDX.add(Math.sqrt(ukf.getCovariance().get(0, 0)));
            ukfSDRot.add(Math.sqrt(ukf.getCovariance().get(2, 2)));
            ukfmX.add(SE2.FACTORY.pseudo_log((SE2) ukfm.getState().getManifold(0)).get(0, 0));
            ukfmY.add(SE2.FACTORY.pseudo_log((SE2) ukfm.getState().getManifold(0)).get(1, 0));
            ukfmRot.add(SE2.FACTORY.pseudo_log((SE2) ukfm.getState().getManifold(0)).get(2, 0));
            ukfmSDX.add(Math.sqrt(ukfm.getCovariance().get(0, 0)));
            ukfmSDY.add(Math.sqrt(ukfm.getCovariance().get(1, 1)));
            ukfmSDRot.add(Math.sqrt(ukfm.getCovariance().get(2, 2)));
            srukfmX.add(SE2.FACTORY.pseudo_log((SE2) srukfm.getState().getManifold(0)).get(0, 0));
            srukfmY.add(SE2.FACTORY.pseudo_log((SE2) srukfm.getState().getManifold(0)).get(1, 0));
            srukfmRot.add(SE2.FACTORY.pseudo_log((SE2) srukfm.getState().getManifold(0)).get(2, 0));
            srukfmSDX.add(Math.sqrt(srukfm.getCovariance().get(0, 0)));
            srukfmSDY.add(Math.sqrt(srukfm.getCovariance().get(1, 1)));
            srukfmSDRot.add(Math.sqrt(srukfm.getCovariance().get(2, 2)));
            trueX.add(robot.getState().get(0, 0));
            trueY.add(robot.getState().get(1, 0));
            trueRot.add(robot.getState().get(2, 0));
        }

        XYChart chart = QuickChart.getChart("Path", "X", "Y", "True Path", trueX, trueY);
        chart.addSeries("UKF", ukfX, ukfY);
        chart.addSeries("UKFM", ukfmX, ukfmY);
        chart.addSeries("SRUKFM", srukfmX, srukfmY);
        new SwingWrapper<>(chart).displayChart();

        // UKF Residuals
        ArrayList<Double> residual = new ArrayList<>();
        ArrayList<Double> lowerBound = new ArrayList<>();
        ArrayList<Double> upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfSDX.get(i));
            lowerBound.add(-3*ukfSDX.get(i));
            residual.add(ukfX.get(i) - trueX.get(i));
        }
        XYChart chart_ukfX = QuickChart.getChart("UKF ResidualX", "Time", "Residual", "X", time, residual);
        chart_ukfX.addSeries("Upper Bound", time, upperBound);
        chart_ukfX.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfX).displayChart();

        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfSDY.get(i));
            lowerBound.add(-3*ukfSDY.get(i));
            residual.add(ukfY.get(i) - trueY.get(i));
        }
        XYChart chart_ukfY = QuickChart.getChart("UKF ResidualY", "Time", "Residual", "Y", time, residual);
        chart_ukfY.addSeries("Upper Bound", time, upperBound);
        chart_ukfY.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfY).displayChart();

        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfSDRot.get(i));
            lowerBound.add(-3*ukfSDRot.get(i));
            residual.add(SO2.FACTORY.log(SO2.FACTORY.exp(ukfRot.get(i)).inverse().compose(SO2.FACTORY.exp(trueRot.get(i)))).get(0, 0));
        }
        XYChart chart_ukfRot = QuickChart.getChart("UKF ResidualRot", "Time", "Residual", "Rot", time, residual);
        chart_ukfRot.addSeries("Upper Bound", time, upperBound);
        chart_ukfRot.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfRot).displayChart();

        // UKFM Residuals
        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfmSDX.get(i));
            lowerBound.add(-3*ukfmSDX.get(i));
            residual.add(ukfmX.get(i) - trueX.get(i));
        }
        XYChart chart_ukfmX = QuickChart.getChart("UKFM ResidualX", "Time", "Residual", "X", time, residual);
        chart_ukfmX.addSeries("Upper Bound", time, upperBound);
        chart_ukfmX.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfmX).displayChart();
        
        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfmSDY.get(i));
            lowerBound.add(-3*ukfmSDY.get(i));
            residual.add(ukfmY.get(i) - trueY.get(i));
        }
        XYChart chart_ukfmY = QuickChart.getChart("UKFM ResidualY", "Time", "Residual", "Y", time, residual);
        chart_ukfmY.addSeries("Upper Bound", time, upperBound);
        chart_ukfmY.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfmY).displayChart();

        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfmSDRot.get(i));
            lowerBound.add(-3*ukfmSDRot.get(i));
            residual.add(SO2.FACTORY.log(SO2.FACTORY.exp(ukfmRot.get(i)).inverse().compose(SO2.FACTORY.exp(trueRot.get(i)))).get(0, 0));
        }
        XYChart chart_ukfmRot = QuickChart.getChart("UKFM ResidualRot", "Time", "Residual", "Rot", time, residual);
        chart_ukfmRot.addSeries("Upper Bound", time, upperBound);
        chart_ukfmRot.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfmRot).displayChart();

        // SRUKFM Residuals
        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*srukfmSDX.get(i));
            lowerBound.add(-3*srukfmSDX.get(i));
            residual.add(srukfmX.get(i) - trueX.get(i));
        }
        XYChart chart_srukfmX = QuickChart.getChart("SRUKFM ResidualX", "Time", "Residual", "X", time, residual);
        chart_srukfmX.addSeries("Upper Bound", time, upperBound);
        chart_srukfmX.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_srukfmX).displayChart();

        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*srukfmSDY.get(i));
            lowerBound.add(-3*srukfmSDY.get(i));
            residual.add(srukfmY.get(i) - trueY.get(i));
        }
        XYChart chart_srukfmY = QuickChart.getChart("SRUKFM ResidualY", "Time", "Residual", "Y", time, residual);
        chart_srukfmY.addSeries("Upper Bound", time, upperBound);
        chart_srukfmY.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_srukfmY).displayChart();

        residual = new ArrayList<>();
        lowerBound = new ArrayList<>();
        upperBound = new ArrayList<>();
        for(int i = 0; i < time.size(); i++) {
            upperBound.add(3*srukfmSDRot.get(i));
            lowerBound.add(-3*srukfmSDRot.get(i));
            residual.add(SO2.FACTORY.log(SO2.FACTORY.exp(srukfmRot.get(i)).inverse().compose(SO2.FACTORY.exp(trueRot.get(i)))).get(0, 0));
        }
        XYChart chart_srukfmRot = QuickChart.getChart("SRUKFM ResidualRot", "Time", "Residual", "Rot", time, residual);
        chart_srukfmRot.addSeries("Upper Bound", time, upperBound);
        chart_srukfmRot.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_srukfmRot).displayChart();
    }
}
