package com.quackology.duckdevices;

import java.util.ArrayList;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import com.quackology.duckdevices.controllers.GVF;
import com.quackology.duckdevices.controllers.Path;
import com.quackology.duckdevices.controllers.PathBuilder;
import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Vector;

public class GVFTest {
    public static void main(String[] args) {
        ArrayList<Double> pathX = new ArrayList<>();
        ArrayList<Double> pathY = new ArrayList<>();
        Path path = new PathBuilder(Vector.build(0, 0, 0))
                .addHermiteSpline(Vector.build(0.3, 1.5, Math.toRadians(40)))
                .addHermiteSpline(Vector.build(0.4, 0.72), Vector.build(0.2 - 0.4, 0.6 - 0.72))
                .addLine(Vector.build(0.2, 0.6))
                .addCubicBezier(Vector.build(0.6, 0.2), Vector.build(0.3, 0.5), Vector.build(0.5, 0.1))
                .addHermiteSpline(Vector.build(0.65, 1.4, Math.toRadians(90)))
                .addBSpline(Vector.build(1, 1))
                .addBSpline(Vector.build(2, 0.5))
                .addLine(Vector.build(2, 0.8))
                .addBSpline(Vector.build(2, 0))
                .addBSpline(Vector.build(1, 0.5))
                .addBSpline(Vector.build(0.6, 0.7))
                .build();

        for(int i = 0; i < 101; i++) {
            MatReal pt = path.getPoint(i/100.0*11);
            pathX.add(pt.get(0, 0));
            pathY.add(pt.get(1, 0));
        }

        GVF gvf = new GVF(path, Vector.build(0.5, 0.5), 0.2);
        MatReal pos = Vector.build(1, 1);
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        x.add(pos.get(0, 0));
        y.add(pos.get(1, 0));
        for(int i = 0; i < 400; i++) {
            pos = pos.add(Vector.normalize(gvf.getCorrection(pos)).multiply(0.05));
            //pos = pos.add(gvf.getCorrection(pos));
            x.add(pos.get(0, 0));
            y.add(pos.get(1, 0));
        }

        XYChart chart_plot = QuickChart.getChart("Path", "X", "Y", "Points", pathX, pathY);
        chart_plot.addSeries("pos", x, y);
        new SwingWrapper<>(chart_plot).displayChart();

    }
}
