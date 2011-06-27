/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.simulator;

import lxx.LXXRobot;
import lxx.LXXRobotState;
import lxx.utils.*;
import robocode.util.Utils;

import static java.lang.Math.signum;

public class RobotProxy implements LXXRobot {

    private final LXXRobot original;
    private final double gunCoolingRate;

    private LXXRobotState prevState;
    private LXXRobotState currentState;

    private long lastTravelTime = 0;
    private long lastStopTime = 0;
    private long lastTurnTime = 0;
    private long lastNotTurnTime = 0;
    private long lastDirChangeTime = 0;
    private double gunHeat;
    private long time;
    private double firePower;

    public RobotProxy(LXXRobot original, long time, double gunCoolingRate) {
        this.original = original;
        this.gunCoolingRate = gunCoolingRate;
        this.time = time;
        currentState = new RobotImage(original.getPosition(), original.getState().getVelocity(),
                original.getState().getHeadingRadians(), original.getState().getBattleField(), original.getState().getTurnRateRadians(),
                original.getState().getEnergy());
        lastTravelTime = original.getLastTravelTime();
        lastStopTime = original.getLastStopTime();
        lastTurnTime = original.getLastTurnTime();
        lastNotTurnTime = original.getLastNotTurnTime();
        lastDirChangeTime = original.getLastDirChangeTime();
        gunHeat = original.getGunHeat();
        firePower = original.getFirePower();
    }

    void doTurn(LXXRobotState newState) {
        final double prevAcceleration = getAcceleration();
        prevState = currentState;
        currentState = newState;
        if (signum(prevAcceleration) != signum(getAcceleration()) && currentState.getSpeed() > 0.1 && currentState.getSpeed() < 7.9) {
            lastDirChangeTime = time;
        }

        time++;
        if (Utils.isNear(newState.getSpeed(), 0) || signum(currentState.getVelocity()) != signum(prevState.getVelocity())) {
            lastStopTime = time;
        } else {
            lastTravelTime = time;
        }

        if (newState.getTurnRateRadians() == 0 || signum(newState.getTurnRateRadians()) != signum(prevState.getTurnRateRadians())) {
            lastNotTurnTime = time - 1;
        } else {
            lastTurnTime = time - 1;
        }

        gunHeat -= gunCoolingRate;
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
        return LXXUtils.calculateAcceleration(prevState, currentState);
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

    public long getLastNotTurnTime() {
        return lastNotTurnTime;
    }

    public long getLastDirChangeTime() {
        return lastDirChangeTime;
    }

    public double getGunHeat() {
        return gunHeat;
    }

    public LXXRobotState getPrevState() {
        return prevState;
    }

    public long getLastTurnTime() {
        return lastTurnTime;
    }

    public double getFirePower() {
        return firePower;
    }
}
