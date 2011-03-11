/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

public interface LXXRobotState extends APoint {

    double getHeadingRadians();

    double getAbsoluteHeadingRadians();

    double getTurnRateRadians();

    double getVelocity();

    double getVelocityModule();

    LXXRobot getRobot();

    boolean equals(Object another);

    int hashCode();

    BattleField getBattleField();

    double getEnergy();
}
