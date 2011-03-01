/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.simulator;

import lxx.utils.*;
import robocode.Rules;

public class RobotProxy implements LXXRobot {

    private final LXXRobot original;

    private LXXRobotState prevState;
    private LXXRobotState currentState;

    private long lastTravelTime = 0;
    private long lastStopTime = 0;

    private long time;

    public RobotProxy(LXXRobot original, long time) {
        this.original = original;
        this.time = time;
        currentState = new RobotImage(original.getPosition(), original.getState().getVelocity(),
                original.getState().getHeadingRadians(), original.getState().getBattleField(), original.getState().getTurnRateRadians());
        lastTravelTime = original.getLastTravelTime();
        lastStopTime = original.getLastStopTime();
    }

    void doTurn(LXXRobotState newState) {
        prevState = currentState;
        currentState = newState;

        time++;
        if (newState.getVelocityModule() == 0) {
            lastStopTime = time;
        } else {
            lastTravelTime = time;
        }
    }

    public long getTime() {
        return time;
    }

    public String getName() {
        return original.getName();
    }

    public boolean isAlive() {
        return original.isAlive();
    }

    public double getWidth() {
        return original.getWidth();
    }

    public double getHeight() {
        return original.getHeight();
    }

    public LXXRobotState getState() {
        return currentState;
    }

    public double getAcceleration() {
        if (prevState == null) {
            return original.getAcceleration();
        }
        double acceleration = currentState.getVelocityModule() - prevState.getVelocityModule();

        if (acceleration > Rules.ACCELERATION) {
            acceleration = Rules.ACCELERATION;
        }

        return acceleration;
    }

    public LXXPoint getPosition() {
        return new LXXPoint(currentState);
    }

    public long getLastStopTime() {
        return lastStopTime;
    }

    public long getLastTravelTime() {
        return lastTravelTime;
    }

    public double getX() {
        return currentState.getX();
    }

    public double getY() {
        return currentState.getY();
    }

    public double aDistance(APoint p) {
        return currentState.aDistance(p);
    }

    public double angleTo(APoint pnt) {
        return currentState.angleTo(pnt);
    }

    public APoint project(double alpha, double distance) {
        return currentState.project(alpha, distance);
    }

    public APoint project(DeltaVector dv) {
        return currentState.project(dv);
    }
}
