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

        double futureHeading = robot.getHeadingRadians() + turnRateRadians;
        if (robot.getVelocity() < 0) {
            futureHeading = Utils.normalAbsoluteAngle(futureHeading + LXXConstants.RADIANS_180);
        }
        final LXXPoint robotPos = new LXXPoint(robot);
        final double acceleratedSpeed = min(robot.getSpeed() + 1, Rules.MAX_VELOCITY);
        final double deceleratedSpeed1 = max(robot.getSpeed() - 1, 0);
        final double deceleratedSpeed2 = max(robot.getSpeed() - 2, 0);
        if ((robotPos.distanceToWall(robot.getBattleField(), futureHeading)) / deceleratedSpeed2 <
                abs(turnRemaining) / Rules.getTurnRateRadians(acceleratedSpeed) + 1) {
            desiredSpeed = 0;
        } else if ((robotPos.distanceToWall(robot.getBattleField(), futureHeading)) / deceleratedSpeed1 <
                abs(turnRemaining) / Rules.getTurnRateRadians(acceleratedSpeed) + 1) {
            desiredSpeed = min(deceleratedSpeed2, desiredSpeed);
        } else if ((robotPos.distanceToWall(robot.getBattleField(), futureHeading)) / robot.getSpeed() <
                abs(turnRemaining) / Rules.getTurnRateRadians(acceleratedSpeed) + 1) {
            desiredSpeed = min(deceleratedSpeed1, desiredSpeed);
        } else if ((robotPos.distanceToWall(robot.getBattleField(), futureHeading)) / acceleratedSpeed <
                abs(turnRemaining) / Rules.getTurnRateRadians(acceleratedSpeed) + 1) {
            desiredSpeed = min(robot.getSpeed(), desiredSpeed);
        }

        return new MovementDecision(desiredSpeed * (wantToGoFront ? 1 : -1), turnRateRadians);
    }

    public String toString() {
        return String.format("(desired velocity = %3.2f, turnRate %3.2f)", desiredVelocity, toDegrees(turnRateRadians));
    }

}
