package com.quackology.duckdevices;

import com.quackology.duckdevices.controllers.MotionProfile;
import com.quackology.duckdevices.controllers.ProfileParams;
import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Vector;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MotionProfileTest {
    public static void main(String[] args) {
        ArrayList<Double> time = new ArrayList<>();
        ArrayList<Double> position = new ArrayList<>();
        ArrayList<Double> velocity = new ArrayList<>();
        ArrayList<Double> velocityIntegral = new ArrayList<>();

        MotionProfile profile = new MotionProfile(new ProfileParams(Math.toRadians(110), Math.toRadians(110), -Math.toRadians(40)));

        Function<Double, MatReal> motion = profile.makeProfile(0, Math.toRadians(20));
        double dt = 0.001;
        double integral = 0;
        for(int i = 0; i < 10/dt; i++) {
            time.add(i*dt);
            MatReal state = motion.apply(time.get(time.size()-1));
            position.add(state.get(0, 0));
            velocity.add(state.get(1, 0));
            velocityIntegral.add(integral);

            integral += velocity.get(velocity.size()-1)*dt;
        }

        XYChart chart_ukfX = QuickChart.getChart("Position and Velocity", "Time", "State", "Position", time, position);
        chart_ukfX.addSeries("Velocity", time, velocity);
        chart_ukfX.addSeries("VelocityIntegral", time, velocityIntegral);

        new SwingWrapper<>(chart_ukfX).displayChart();
    }
}
