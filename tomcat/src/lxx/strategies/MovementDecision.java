/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.LXXRobotState;
import lxx.utils.LXXCircle;
import lxx.utils.LXXConstants;
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
        final boolean wantToGoFront = LXXUtils.anglesDiff(robot.getHeadingRadians(), desiredHeading) < LXXConstants.RADIANS_90;
        final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LXXConstants.RADIANS_180);
        final boolean isClockwise = Utils.normalRelativeAngle(normalizedDesiredHeading - robot.getHeadingRadians()) >= 0;
        double speedLimit = max(desiredSpeed, robot.getSpeed());

        LXXCircle turnCircle;
        double fromAngle;
        double toAngle;
        do {
            final double turnRadius = LXXUtils.getTurnRadius(speedLimit);
            turnCircle = LXXUtils.getCircle(robot, robot.getHeadingRadians(), turnRadius, isClockwise);
            final double turnDistance = LXXUtils.getTurnDistance(robot.getHeadingRadians(), normalizedDesiredHeading, speedLimit);
            final double turnRadians = turnCircle.toRadians(turnDistance);
            final double alpha = turnCircle.center.angleTo(robot);
            fromAngle = min(alpha, alpha + turnRadians * (isClockwise ? 1 : -1));
            toAngle = max(alpha, alpha + turnRadians * (isClockwise ? 1 : -1));
            speedLimit--;
        } while (!robot.getBattleField().contains(turnCircle, fromAngle, toAngle) && speedLimit > 0);

        final double turnRateRadians =
                LXXUtils.limit(-Rules.getTurnRateRadians(robot.getSpeed()),
                        Utils.normalRelativeAngle(normalizedDesiredHeading - robot.getHeadingRadians()),
                        Rules.getTurnRateRadians(robot.getSpeed()));

        return new MovementDecision(min(desiredSpeed, speedLimit) * (wantToGoFront ? 1 : -1), speedLimit > 0 ? turnRateRadians : 0);
    }

    public String toString() {
        return String.format("(desired velocity = %3.2f, turnRate %3.2f)", desiredVelocity, toDegrees(turnRateRadians));
    }

}
