/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx;

import lxx.utils.APoint;
import lxx.utils.BattleField;
import lxx.utils.LXXPoint;

public interface LXXRobotState extends APoint {

    double getHeadingRadians();

    double getAbsoluteHeadingRadians();

    double getTurnRateRadians();

    double getVelocity();

    double getSpeed();

    LXXRobot getRobot();

    boolean equals(Object another);

    int hashCode();

    BattleField getBattleField();

    double getEnergy();

    LXXPoint getPosition();

}
