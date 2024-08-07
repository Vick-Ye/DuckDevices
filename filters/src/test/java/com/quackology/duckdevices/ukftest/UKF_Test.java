package com.quackology.duckdevices.ukftest;

import java.util.ArrayList;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import com.quackology.duckdevices.filters.SRUKF;
import com.quackology.duckdevices.filters.UKF;
import com.quackology.duckdevices.spaces.MatReal;

public class UKF_Test {
    public static void main(String[] args) throws Exception {
        double dt = 1;

        // Noise
        double processNoise = Math.sqrt(10);
        double rangeNoise = Math.sqrt(50);
        double bearingNoise = Math.sqrt(Math.toRadians(5));

        // Sensors
        Plane plane = new Plane(1000, 0, processNoise);
        Radar radar = new Radar(0d, 0d, rangeNoise, bearingNoise);
        Radar radar2 = new Radar(1000d, 0d, rangeNoise, bearingNoise);

        // Initial Conditions
        MatReal x = new MatReal(new double[][] {{0d, 0d, 0d, 0d}}).transpose();
        MatReal p = new MatReal(new double[][] {
            {50d*50d, 0d, 0d, 0d},
            {0d, 130d*130d, 0d, 0d},
            {0d, 0d, 750d*750d, 0d},
            {0d, 0d, 0d, 200d*200d}
        });
        MatReal q = new MatReal(new double[][] {
            {0.588, 1.175, 0d, 0d},
            {1.175, 2.35, 0d, 0d},
            {0d, 0d, 0.588, 1.175},
            {0d, 0d, 1.175, 2.35}
        });
        MatReal u = MatReal.empty(4, 0);

        //SRUKF
        SRUKF srukf = new SRUKF(x, p, q, u);
        srukf.setF((MatReal x_, MatReal u_, Double dt_) -> {
            double xPos = x_.get(0, 0) + x_.get(4, 0);
            double xVel = x_.get(1, 0) + x_.get(5, 0);
            double yPos = x_.get(2, 0) + x_.get(6, 0);
            double yVel = x_.get(3, 0) + x_.get(7, 0);
            // double xPos = x_.get(0, 0);
            // double yPos = x_.get(2, 0);
            // double xVel = x_.get(1, 0);
            // double yVel = x_.get(3, 0);
            
            xPos += xVel*dt_;
            yPos += yVel*dt_;

            return new MatReal(new double[][] {{xPos, xVel, yPos, yVel}}).transpose();
        });

        //UKF
        UKF ukf = new UKF(UKF.Sampling.MERWE, x, p, q, u);
        ukf.setF((MatReal x_, MatReal u_, Double dt_) -> {
            double xPos = x_.get(0, 0) + x_.get(4, 0);
            double xVel = x_.get(1, 0) + x_.get(5, 0);
            double yPos = x_.get(2, 0) + x_.get(6, 0);
            double yVel = x_.get(3, 0) + x_.get(7, 0);
            // double xPos = x_.get(0, 0);
            // double xVel = x_.get(1, 0);
            // double yPos = x_.get(2, 0);
            // double yVel = x_.get(3, 0);
            
            xPos += xVel*dt_;
            yPos += yVel*dt_;

            return new MatReal(new double[][] {{xPos, xVel, yPos, yVel}}).transpose();
        });

        // Data
        ArrayList<Double> time = new ArrayList<>();
        ArrayList<Double> srukfX = new ArrayList<>();
        ArrayList<Double> srukfY = new ArrayList<>();
        ArrayList<Double> ukfX = new ArrayList<>();
        ArrayList<Double> ukfY = new ArrayList<>();
        ArrayList<Double> trueX = new ArrayList<>();
        ArrayList<Double> trueY = new ArrayList<>();
        ArrayList<Double> srukfSDX = new ArrayList<>();
        ArrayList<Double> srukfSDY = new ArrayList<>();
        ArrayList<Double> ukfSDX = new ArrayList<>();
        ArrayList<Double> ukfSDY = new ArrayList<>();

        // Simulation
        for (int i = 0; i < 100; i++) {
            time.add(i*dt+dt);
            plane.move(dt);
            
            srukf.predict_aug(dt);
            ukf.predict_aug(dt);

            // Measurement
            for (int j = 0; j < 2; j++) {
                MatReal ping1 = radar.ping(plane);
                MatReal ping2 = radar2.ping(plane);

                srukf.update((MatReal x_) -> {
                    double dist = Math.sqrt(Math.pow(x_.get(0, 0), 2) + Math.pow(x_.get(2, 0), 2));
                    double bearing = Math.atan2(x_.get(2, 0), x_.get(0, 0));
                    return new MatReal(new double[][] {{dist, bearing}}).transpose();
                }, ping1, new MatReal(new double[][] {
                    {rangeNoise*rangeNoise, 0d}, 
                    {0d, bearingNoise*bearingNoise}
                }).choleskyDecompose());
                srukf.update((MatReal x_) -> {
                    double dist = Math.sqrt(Math.pow(x_.get(0, 0)-1000, 2) + Math.pow(x_.get(2, 0), 2));
                    double bearing = Math.atan2(x_.get(2, 0), x_.get(0, 0)-1000);
                    return new MatReal(new double[][] {{dist, bearing}}).transpose();
                }, ping2, new MatReal(new double[][] {
                    {rangeNoise*rangeNoise, 0d}, 
                    {0d, bearingNoise*bearingNoise}
                }).choleskyDecompose());

                ukf.update((MatReal x_) -> {
                    double dist = Math.sqrt(Math.pow(x_.get(0, 0), 2) + Math.pow(x_.get(2, 0), 2));
                    double bearing = Math.atan2(x_.get(2, 0), x_.get(0, 0));
                    return new MatReal(new double[][] {{dist, bearing}}).transpose();
                }, ping1, new MatReal(new double[][] {
                    {rangeNoise*rangeNoise, 0d}, 
                    {0d, bearingNoise*bearingNoise}
                }));
                ukf.update((MatReal x_) -> {
                    double dist = Math.sqrt(Math.pow(x_.get(0, 0)-1000, 2) + Math.pow(x_.get(2, 0), 2));
                    double bearing = Math.atan2(x_.get(2, 0), x_.get(0, 0)-1000);
                    return new MatReal(new double[][] {{dist, bearing}}).transpose();
                }, ping2, new MatReal(new double[][] {
                    {rangeNoise*rangeNoise, 0d}, 
                    {0d, bearingNoise*bearingNoise}
                }));
            }

            // Data
            srukfX.add(srukf.getState().get(0, 0));
            srukfY.add(srukf.getState().get(2, 0));
            srukfSDX.add(Math.sqrt(srukf.getCovariance().get(0, 0)));
            srukfSDY.add(Math.sqrt(srukf.getCovariance().get(2, 2)));
            ukfX.add(ukf.getState().get(0, 0));
            ukfY.add(ukf.getState().get(2, 0));
            ukfSDX.add(Math.sqrt(ukf.getCovariance().get(0, 0)));
            ukfSDY.add(Math.sqrt(ukf.getCovariance().get(2, 2)));
            trueX.add(plane.getXDist());
            trueY.add(plane.getAlt());

        }

        XYChart chart = QuickChart.getChart("UKF vs SRUKF", "X", "Y", "True", trueX, trueY);
        chart.addSeries("UKF", ukfX, ukfY);
        chart.addSeries("SRUKF", srukfX, srukfY);
        new SwingWrapper<>(chart).displayChart();

        // UKF Residuals
        ArrayList<Double> upperBound = new ArrayList<>();
        ArrayList<Double> lowerBound = new ArrayList<>();
        ArrayList<Double> residual = new ArrayList<>();
        for (int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfSDX.get(i));
            lowerBound.add(-3*ukfSDX.get(i));
            residual.add(ukfX.get(i) - trueX.get(i));
        }
        XYChart chart_ukfX = QuickChart.getChart("UKF ResidualX", "Time", "Residual", "X", time, residual);
        chart_ukfX.addSeries("Upper Bound", time, upperBound);
        chart_ukfX.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfX).displayChart();

        upperBound = new ArrayList<>();
        lowerBound = new ArrayList<>();
        residual = new ArrayList<>();
        for (int i = 0; i < time.size(); i++) {
            upperBound.add(3*ukfSDY.get(i));
            lowerBound.add(-3*ukfSDY.get(i));
            residual.add(ukfY.get(i) - trueY.get(i));
        }
        XYChart chart_ukfY = QuickChart.getChart("UKF ResidualY", "Time", "Residual", "Y", time, residual);
        chart_ukfY.addSeries("Upper Bound", time, upperBound);
        chart_ukfY.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_ukfY).displayChart();

        // SRUKF Residuals
        upperBound = new ArrayList<>();
        lowerBound = new ArrayList<>();
        residual = new ArrayList<>();
        for (int i = 0; i < time.size(); i++) {
            upperBound.add(3*srukfSDX.get(i));
            lowerBound.add(-3*srukfSDX.get(i));
            residual.add(srukfX.get(i) - trueX.get(i));
        }
        XYChart chart_srukfX = QuickChart.getChart("SRUKF ResdiualX", "Time", "Residual", "X", time, residual);
        chart_srukfX.addSeries("Upper Bound", time, upperBound);
        chart_srukfX.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_srukfX).displayChart();

        upperBound = new ArrayList<>();
        lowerBound = new ArrayList<>();
        residual = new ArrayList<>();
        for (int i = 0; i < time.size(); i++) {
            upperBound.add(3*srukfSDY.get(i));
            lowerBound.add(-3*srukfSDY.get(i));
            residual.add(srukfY.get(i) - trueY.get(i));
        }
        XYChart chart_srukfY = QuickChart.getChart("SRUKF ResidualY", "Time", "Residual", "Y", time, residual);
        chart_srukfY.addSeries("Upper Bound", time, upperBound);
        chart_srukfY.addSeries("Lower Bound", time, lowerBound);
        new SwingWrapper<>(chart_srukfY).displayChart();
    }
}