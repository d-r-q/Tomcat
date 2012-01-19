/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import robocode.Rules;

public class TurnDistanceTest {

    public static void main(String[] args) {
        QuickMath.init();
        for (double initialSpeed = 0; initialSpeed <= 8; initialSpeed++) {
            for (double acceleration = -2; acceleration <= 1; acceleration++) {
                LXXPoint pnt = new LXXPoint();
                double speed = initialSpeed;
                for (double heading = 0; heading < LXXConstants.RADIANS_90; heading += Rules.getTurnRateRadians(speed)) {
                    speed = LXXUtils.limit(0, speed + acceleration, Rules.MAX_VELOCITY);
                    pnt = pnt.project(heading, speed);
                }

                System.out.printf("f(%3.3f, %3.3f) = %3.3f\n", initialSpeed, acceleration, pnt.getY());
            }
        }
    }

}
