/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.model.TurnSnapshot;
import lxx.office.AttributesManager;
import lxx.utils.LXXConstants;
import lxx.utils.LXXRobotState;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.io.Serializable;

import static java.lang.Math.*;

public class MovementDecision implements Serializable {

    private final double acceleration;
    private final double turnRateRadians;
    private final MovementDirection movementDirection;

    public final String key;

    public MovementDecision(double acceleration, double turnRateRadians, MovementDirection movementDirection) {
        this.acceleration = acceleration;
        this.turnRateRadians = turnRateRadians;
        this.movementDirection = movementDirection;

        key = movementDirection.sign + ":" + acceleration + ":" + round(toDegrees(turnRateRadians) / 2);
    }

    public static MovementDecision getMovementDecision(TurnSnapshot turnSnapshot) {
        double turnRateRadians = toRadians(turnSnapshot.getRoundedAttrValue(AttributesManager.enemyTurnRate));
        double acceleration = turnSnapshot.getAttrValue(AttributesManager.enemyAcceleration);
        return new MovementDecision(acceleration, turnRateRadians, MovementDirection.get(turnSnapshot.getEnemyVelocity()));
    }

    public double getAcceleration() {
        return acceleration;
    }

    public double getTurnRateRadians() {
        return turnRateRadians;
    }

    public MovementDirection getMovementDirection() {
        return movementDirection;
    }

    public static MovementDecision toMovementDecision(LXXRobotState robot, double targetHeading, MovementDirection movementDirection, double distanceToTravel, long timeToTravel) {
        final double robotHeading = movementDirection == MovementDirection.FORWARD ? robot.getHeadingRadians() : Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180);
        final double neededTurnRateRadians = Utils.normalRelativeAngle(targetHeading - robotHeading);
        double turnRateRadians = neededTurnRateRadians;
        final double speed = robot.getVelocityModule();
        final double acceleratedSpeed = min(speed + 1, Rules.MAX_VELOCITY);
        if (abs(turnRateRadians) > Rules.getTurnRateRadians(acceleratedSpeed)) {
            turnRateRadians = Rules.getTurnRateRadians(acceleratedSpeed) * signum(turnRateRadians);
        }

        final double requiredSpeed = distanceToTravel / timeToTravel;
        double acceleration = getAcceleration(robot, turnRateRadians, robotHeading);
        if (requiredSpeed < speed + acceleration) {
            acceleration = LXXUtils.limit(-Rules.DECELERATION, requiredSpeed - speed, Rules.ACCELERATION);
        }

        turnRateRadians = min(abs(neededTurnRateRadians),
                abs(Rules.getTurnRateRadians(LXXUtils.limit(0, speed + acceleration, Rules.MAX_VELOCITY)))) * signum(turnRateRadians);

        return new MovementDecision(acceleration, turnRateRadians, movementDirection);
    }

    private static double getAcceleration(LXXRobotState robot, double turnRateRadians, double robotHeading) {
        final double speed = min(robot.getVelocityModule(), Rules.MAX_VELOCITY);
        final double acceleratedSpeed = min(speed + 1, Rules.MAX_VELOCITY);
        final double deceleratedSpeed1 = max(speed - 1, 0);
        final double newHeading = Utils.normalAbsoluteAngle(robotHeading + turnRateRadians);
        final int acceleration;
        if (robot.getBattleField().contains(robot.project(newHeading, LXXUtils.getStopDistance(acceleratedSpeed) + 12))) {
            acceleration = 1;
        } else if (robot.getBattleField().contains(robot.project(newHeading, LXXUtils.getStopDistance(speed) + 12))) {
            acceleration = 0;
        } else if (robot.getBattleField().contains(robot.project(newHeading, LXXUtils.getStopDistance(deceleratedSpeed1) + 12))) {
            acceleration = -1;
        } else {
            acceleration = -2;
        }

        if (robot.getVelocityModule() + acceleration > Rules.MAX_VELOCITY) {
            return Rules.MAX_VELOCITY - robot.getVelocityModule();
        }
        if (robot.getVelocityModule() + acceleration < 0) {
            return -robot.getVelocityModule();
        }

        return acceleration;
    }

    public String toString() {
        return String.format("(acceleration = %3.2f, turnRate %3.2f, direction = %s)", acceleration, toDegrees(turnRateRadians), movementDirection);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovementDecision that = (MovementDecision) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    public static enum MovementDirection {

        FORWARD(1),
        BACKWARD(-1);

        public final int sign;

        MovementDirection(int sign) {
            this.sign = sign;
        }

        public static MovementDirection get(double velocity) {
            return velocity < 0 ? BACKWARD : FORWARD;
        }
    }

}
