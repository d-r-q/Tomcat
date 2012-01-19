/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx;

import lxx.utils.APoint;
import lxx.utils.LXXPoint;

import java.util.List;

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

    long getLastDirChangeTime();

    double getGunHeat();

    double getFirePower();

    int getRound();

    double getLast10TicksDist();

    void addVisit(double guessFactor);

    List<Double> getVisitedGuessFactors();
}
