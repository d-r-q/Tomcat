/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.strategies.MovementDecision;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 19.02.11
 */
public final class RobotImage implements LXXRobotState {

    private APoint position;
    private double velocity;
    private double heading;
    private BattleField battleField;
    private double turnRateRadians;

    public RobotImage(APoint position, double velocity, double heading, BattleField battleField, double turnRateRadians) {
        this.position = position;
        this.velocity = velocity;
        this.heading = heading;
        this.battleField = battleField;
        this.turnRateRadians = turnRateRadians;
    }

    // todo(zhidkov): use RobocodeDuelSimulator
    public void apply(MovementDecision movementDecision) {
        heading = Utils.normalAbsoluteAngle(heading + movementDecision.getTurnRateRadians());
        final double maxVelocity = LXXUtils.limit(0, abs(velocity) + movementDecision.getAcceleration(), Rules.MAX_VELOCITY);

        if (signum(velocity) == signum(movementDecision.getMovementDirection().sign) ||
                velocity == 0) {
            velocity = maxVelocity * movementDecision.getMovementDirection().sign;
        } else {
            velocity = max(0, velocity - Rules.DECELERATION);
        }

        position = position.project(velocity >= 0 ? heading : Utils.normalAbsoluteAngle(heading + LXXConstants.RADIANS_180), abs(velocity));
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
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
        if (signum(velocity) >= 0) {
            return heading;
        } else {
            return Utils.normalAbsoluteAngle(heading + Math.PI);
        }
    }

    public double getTurnRateRadians() {
        return turnRateRadians;
    }

    public double getVelocityModule() {
        return abs(velocity);
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

}
