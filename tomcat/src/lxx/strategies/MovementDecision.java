/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.LXXRobotState;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.io.Serializable;

import static java.lang.Math.toDegrees;
import static java.lang.StrictMath.min;

public class MovementDecision implements Serializable {

    private final double desiredVelocity;
    private final double turnRateRadians;

    public MovementDecision(double desiredVelocity, double turnRateRadians) {
        this.desiredVelocity = desiredVelocity;
        this.turnRateRadians = turnRateRadians;
    }

    public double getTurnRateRadians() {
        return turnRateRadians;
    }

    public double getDesiredVelocity() {
        return desiredVelocity;
    }

    public static MovementDecision toMovementDecision(LXXRobotState robot, double desiredSpeed, double desiredHeading) {
        if (desiredSpeed > Rules.MAX_VELOCITY) {
            desiredSpeed = Rules.MAX_VELOCITY;
        }

        final boolean wantToGoFront = LXXUtils.anglesDiff(robot.getHeadingRadians(), desiredHeading) < LXXConstants.RADIANS_90;
        final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LXXConstants.RADIANS_180);

        final double turnRemaining = Utils.normalRelativeAngle(normalizedDesiredHeading - robot.getHeadingRadians());
        double turnRateRadians =
                LXXUtils.limit(-Rules.getTurnRateRadians(robot.getSpeed()),
                        turnRemaining,
                        Rules.getTurnRateRadians(robot.getSpeed()));

        final LXXPoint robotPos = new LXXPoint(robot);
        final double heading = robot.getSpeed() > 0
                ? robot.getAbsoluteHeadingRadians()
                : wantToGoFront
                  ? robot.getHeadingRadians()
                  : Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180);
        final double distanceToWall = robot.getBattleField().getDistanceToWall(robot.getBattleField().getWall(robotPos, heading + turnRateRadians), robotPos);
        //final double turnDistance = LXXUtils.getMaxTurnDistance(robot.getSpeed(), turnRemaining);
        if (distanceToWall - 4 < LXXUtils.getStopDistance(min(Rules.MAX_VELOCITY, robot.getSpeed() + 1)) + 2) {
            desiredSpeed = 0;
        }

        return new MovementDecision(desiredSpeed * (wantToGoFront ? 1 : -1), turnRateRadians);
    }

    public String toString() {
        return String.format("(desired velocity = %3.2f, turnRate %3.2f)", desiredVelocity, toDegrees(turnRateRadians));
    }

}
