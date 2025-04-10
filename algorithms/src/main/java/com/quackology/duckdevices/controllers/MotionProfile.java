package com.quackology.duckdevices.controllers;

import com.quackology.duckdevices.spaces.MatReal;
import com.quackology.duckdevices.spaces.Vector;

import java.util.function.Function;

public class MotionProfile {
    ProfileParams params;

    public MotionProfile(ProfileParams params) {
        this.params = params;
    }

    public Function<Double, MatReal> makeProfile(double start, double end) {
        double accel = params.accel;
        double deccel = params.deccel;
        double vel = params.vel;

        double sign = Math.signum(end-start);

        double dist = Math.abs(end-start);

        double maxVel = Math.sqrt(dist/((1/accel - 1/deccel)/2));
        double accelT = maxVel/accel;
        double deccelT = -maxVel/deccel;

        MatReal startState = Vector.build(start, 0);

        if(maxVel <= vel) return time -> {
            if(time < accelT) {
                return Vector.build(accel*time*time*0.5, accel*time).multiply(sign).add(startState);
            } else if(time < accelT + deccelT) {
                return Vector.build(accel*accelT*accelT*0.5 + (time-accelT)*(maxVel+deccel*(time-accelT)*0.5), maxVel+deccel*(time-accelT)).multiply(sign).add(startState);
            } else {
                return Vector.build(dist, 0.0).multiply(sign).add(startState);
            }
        };

        double trueAccelT = vel/accel;
        double trueDeccelT = -vel/deccel;
        double constVelT = (dist-((trueAccelT+trueDeccelT)*0.5*vel))/vel;

        return time -> {
            if(time < trueAccelT) {
                return Vector.build(accel*time*time*0.5, accel*time).multiply(sign).add(startState);
            } else if(time < trueAccelT + constVelT) {
                return Vector.build(accel*trueAccelT*trueAccelT*0.5+vel*(time-trueAccelT), vel).multiply(sign).add(startState);
            } else if(time < trueAccelT + constVelT + trueDeccelT) {
                return Vector.build(accel*trueAccelT*trueAccelT*0.5+vel*constVelT+(time-trueAccelT-constVelT)*(vel+deccel*(time-trueAccelT-constVelT)*0.5), vel + deccel * (time-trueAccelT-constVelT)).multiply(sign).add(startState);
            } else {
                return Vector.build(dist, 0.0).multiply(sign).add(startState);
            }
        };
    }
}