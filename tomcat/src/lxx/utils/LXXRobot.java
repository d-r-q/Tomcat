/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public interface LXXRobot extends APoint {

    long getTime();

    String getName();

    boolean isAlive();

    double getWidth();

    double getHeight();

    LXXRobotState getState();

    LXXRobotState getPrevState();

    boolean equals(Object another);

    int hashCode();

    double getAcceleration();

    LXXPoint getPosition();

    long getLastStopTime();

    long getLastTravelTime();

    long getLastTurnTime();

    long getLastNotTurnTime();
}
