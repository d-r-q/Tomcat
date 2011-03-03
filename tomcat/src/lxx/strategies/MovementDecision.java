/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.utils.LXXConstants;
import lxx.utils.LXXRobotState;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.*;

public class MovementDecision {

    private final double acceleration;
    private final double turnRateRadians;
    private final MovementDirection movementDirection;

    public MovementDecision(double acceleration, double turnRateRadians, MovementDirection movementDirection) {
        this.acceleration = acceleration;
        this.turnRateRadians = turnRateRadians;
        this.movementDirection = movementDirection;
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

    public static MovementDecision toMovementDecision(LXXRobotState robot, double targetHeading,
                                                      MovementDirection movementDirection) {
        final double robotHeading = movementDirection == MovementDirection.FORWARD ? robot.getHeadingRadians() : Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180);
        final double neededTurnRateRadians = Utils.normalRelativeAngle(targetHeading - robotHeading);
        double turnRateRadians = neededTurnRateRadians;
        final double speed = robot.getVelocityModule();
        final double acceleratedSpeed = min(speed + 1, Rules.MAX_VELOCITY);
        if (abs(turnRateRadians) > Rules.getTurnRateRadians(acceleratedSpeed)) {
            turnRateRadians = Rules.getTurnRateRadians(acceleratedSpeed) * signum(turnRateRadians);
        }
        final double acceleration = getAcceleration(robot, turnRateRadians, robotHeading);
        turnRateRadians = min(abs(neededTurnRateRadians),
                abs(Rules.getTurnRateRadians(LXXUtils.limit(0, speed + acceleration, Rules.MAX_VELOCITY)))) * signum(turnRateRadians);

        return new MovementDecision(acceleration, turnRateRadians, movementDirection);
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

    public static enum MovementDirection {

        FORWARD(1),
        BACKWARD(-1);

        public final int sign;

        MovementDirection(int sign) {
            this.sign = sign;
        }
    }

}
