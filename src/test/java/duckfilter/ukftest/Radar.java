package duckfilter.ukftest;
import java.util.Random;

import com.quackology.duckfilter.spaces.MatReal;

public class Radar {
    private static Random rand = new Random();
    private double posX;
    private double posY;
    private double rangeNoise;
    private double bearingNoise;

    public Radar(double posX, double posY, double rangeNoise, double bearingNoise) {
        this.posX = posX;
        this.posY = posY;
        this.rangeNoise = rangeNoise;
        this.bearingNoise = bearingNoise;
    }

    public MatReal ping(Plane plane) {
        double planeX = plane.getXDist();
        double planeY = plane.getAlt();

        double dist = Math.sqrt(Math.pow(planeX-this.posX, 2) + Math.pow(planeY-this.posY, 2)) + rand.nextGaussian()*rangeNoise;
        double bearing = Math.atan2(planeY-this.posY, planeX-this.posX) + rand.nextGaussian()*bearingNoise;
        return new MatReal(new double[][] {{dist, bearing}}).transpose();
    }
}