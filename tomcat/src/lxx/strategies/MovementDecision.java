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

import static java.lang.Math.*;

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
        final double futureHeading = getFutureHeading(robot, wantToGoFront, turnRateRadians);
        final double distanceToWall = robotPos.distanceToWall(robot.getBattleField(), futureHeading);
        final double futureSpeed = getFutureSpeed(robot.getVelocity(), desiredSpeed, wantToGoFront);
        if (distanceToWall - 4 < LXXUtils.getStopDistance(futureSpeed) + futureSpeed + 2) {
            desiredSpeed = 0;
        }

        return new MovementDecision(desiredSpeed * (wantToGoFront ? 1 : -1), turnRateRadians);
    }

    private static double getFutureSpeed(double velocity, double desiredSpeed, boolean wantToGoFront) {
        if ((velocity > 0 && wantToGoFront) || (velocity < 0 && !wantToGoFront)) {
            return LXXUtils.limit(max(0, abs(velocity) - Rules.DECELERATION),
                    desiredSpeed - abs(velocity), min(Rules.MAX_VELOCITY, abs(velocity) + Rules.ACCELERATION));
        } else if (velocity == 0) {
            return min(desiredSpeed, Rules.ACCELERATION);
        } else {
            return max(0, abs(velocity) - Rules.DECELERATION);
        }
    }

    private static double getFutureHeading(LXXRobotState robot, boolean wantToGoFront, double turnRateRadians) {
        final double futureHeading;
        if (robot.getVelocity() > 0) {
            futureHeading = robot.getHeadingRadians() + turnRateRadians;
        } else if (robot.getVelocity() < 0) {
            futureHeading = Utils.normalAbsoluteAngle(robot.getHeadingRadians() + turnRateRadians + LXXConstants.RADIANS_180);
        } else if (wantToGoFront) {
            futureHeading = robot.getHeadingRadians() + turnRateRadians;
        } else {
            futureHeading = Utils.normalAbsoluteAngle(robot.getHeadingRadians() + turnRateRadians + LXXConstants.RADIANS_180);
        }
        return futureHeading;
    }

    public String toString() {
        return String.format("(desired velocity = %3.2f, turnRate %3.2f)", desiredVelocity, toDegrees(turnRateRadians));
    }

}
