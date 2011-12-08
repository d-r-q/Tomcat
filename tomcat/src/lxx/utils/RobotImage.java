/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.LXXRobot;
import lxx.LXXRobotState;
import lxx.strategies.MovementDecision;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 19.02.11
 */
public final class RobotImage implements LXXRobotState {

    private LXXPoint position;
    private double velocity;
    private double heading;
    private BattleField battleField;
    private double turnRateRadians;
    private double energy;
    private double speed;
    private double absoluteHeadingRadians;

    public RobotImage(LXXPoint position, double velocity, double heading, BattleField battleField, double turnRateRadians, double energy) {
        this.position = position;
        this.velocity = velocity;
        this.speed = abs(velocity);
        this.heading = heading;
        absoluteHeadingRadians = velocity >= 0 ? heading : Utils.normalAbsoluteAngle(heading + LXXConstants.RADIANS_180);
        this.battleField = battleField;
        this.turnRateRadians = turnRateRadians;
        this.energy = energy;
    }

    public RobotImage(LXXRobotState original) {
        this.position = new LXXPoint(original.getX(), original.getY());
        this.velocity = original.getVelocity();
        this.speed = abs(velocity);
        this.heading = original.getHeadingRadians();
        absoluteHeadingRadians = velocity >= 0 ? heading : Utils.normalAbsoluteAngle(heading + LXXConstants.RADIANS_180);
        this.battleField = original.getBattleField();
        this.turnRateRadians = original.getTurnRateRadians();
        this.energy = original.getEnergy();
    }

    public void apply(MovementDecision movementDecision) {
        heading = Utils.normalAbsoluteAngle(heading + movementDecision.getTurnRateRadians());
        final double acceleration;
        final double desiredVelocity = movementDecision.getDesiredVelocity();
        if (abs(signum(velocity) - signum(desiredVelocity)) <= 1) {
            acceleration = LXXUtils.limit(-Rules.DECELERATION, abs(desiredVelocity) - speed, Rules.ACCELERATION);
            speed += acceleration;
            velocity = speed * signum(velocity != 0 ? velocity : desiredVelocity);
        } else {
            // robocode has difficult 2-step rules in this case,
            // but we will keep it simple
            if (speed > Rules.DECELERATION) {
                velocity -= Rules.DECELERATION * signum(velocity);
                speed -= Rules.DECELERATION;
            } else {
                velocity = 0;
                speed = 0;
            }
        }

        absoluteHeadingRadians = velocity >= 0 ? heading : Utils.normalAbsoluteAngle(heading + LXXConstants.RADIANS_180);
        position = position.project(absoluteHeadingRadians, speed);
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

    public double getAbsoluteHeadingRadians() {
        return absoluteHeadingRadians;
    }

    public double getTurnRateRadians() {
        return turnRateRadians;
    }

    public double getSpeed() {
        return speed;
    }

    public LXXRobot getRobot() {
        throw new UnsupportedOperationException();
    }

    public double getHeadingRadians() {
        return heading;
    }

    public BattleField getBattleField() {
        return battleField;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getEnergy() {
        return energy;
    }

    public LXXPoint getPosition() {
        return position;
    }
}
