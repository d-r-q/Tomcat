/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.BasicRobot;
import lxx.LXXRobot;
import lxx.LXXRobotState;

import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 13.09.2010
 */
public class RobotSnapshot implements LXXRobotState {

    private final LXXPoint position;
    private final double headingRadians;
    private final double absoluteHeadingRadians;
    private final double turnRateRadians;
    private final double velocity;
    private final double speed;
    private final long time;
    private final LXXRobot robot;
    private final BattleField battleField;
    private final double energy;

    public RobotSnapshot(BasicRobot source) {
        robot = source;
        time = source.getTime();
        headingRadians = source.getHeadingRadians();
        absoluteHeadingRadians = source.getAbsoluteHeadingRadians();
        turnRateRadians = source.getTurnRateRadians();
        speed = abs(source.getVelocity());
        velocity = source.getVelocity();
        position = new LXXPoint(source);
        battleField = source.battleField;
        energy = source.getEnergy();
    }

    public double getAbsoluteHeadingRadians() {
        return absoluteHeadingRadians;
    }

    public double getTurnRateRadians() {
        return turnRateRadians;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getSpeed() {
        return speed;
    }

    public LXXRobot getRobot() {
        return robot;
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public double aDistance(APoint p) {
        return position.aDistance(p);
    }

    public double angleTo(APoint pnt) {
        return position.angleTo(pnt);
    }

    public APoint project(double alpha, double distance) {
        return position.project(alpha, distance);
    }

    public APoint project(DeltaVector dv) {
        return position.project(dv);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobotSnapshot that = (RobotSnapshot) o;

        if (time != that.time) return false;
        if (robot != null ? !robot.equals(that.robot) : that.robot != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + (robot != null ? robot.hashCode() : 0);
        return result;
    }

    public double getHeadingRadians() {
        return headingRadians;
    }

    public BattleField getBattleField() {
        return battleField;
    }

    public double getEnergy() {
        return energy;
    }

    @Override
    public LXXPoint getPosition() {
        return position;
    }
}
