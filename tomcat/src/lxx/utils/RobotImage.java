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

    private APoint position;
    private double velocity;
    private double heading;
    private BattleField battleField;
    private double turnRateRadians;
    private double energy;

    public RobotImage(LXXRobotState original) {
        this.position = new LXXPoint(original.getX(), original.getY());
        this.velocity = original.getVelocity();
        this.heading = original.getHeadingRadians();
        this.battleField = original.getBattleField();
        this.turnRateRadians = original.getTurnRateRadians();
        this.energy = original.getEnergy();
    }

    public void apply(MovementDecision movementDecision) {
        heading = Utils.normalAbsoluteAngle(heading + movementDecision.getTurnRateRadians());
        final double acceleration;
        if (abs(signum(velocity) - signum(movementDecision.getDesiredVelocity())) <= 1) {
            acceleration = LXXUtils.limit(-Rules.DECELERATION, abs(movementDecision.getDesiredVelocity()) - abs(velocity), Rules.ACCELERATION);
            velocity = (abs(velocity) + acceleration) * signum(velocity != 0 ? velocity : movementDecision.getDesiredVelocity());
        } else {
            // robocode has difficult 2-step rules in this case,
            // but we will keep it simple
            if (abs(velocity) > Rules.DECELERATION) {
                velocity -= Rules.DECELERATION * signum(velocity);
            } else {
                velocity = 0;
            }
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

    public double getSpeed() {
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

    public double getEnergy() {
        return energy;
    }
}
