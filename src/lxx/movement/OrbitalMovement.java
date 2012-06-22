package lxx.movement;

import lxx.LXXRobotState;
import lxx.strategies.MovementDecision;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

/**
 * User: Aleksey Zhidkov
 * Date: 18.06.12
 */
public class OrbitalMovement {

    private final double desiredDistance;

    public OrbitalMovement(double desiredDistance) {
        this.desiredDistance = desiredDistance;
    }

    public MovementDecision makeDecision(APoint center, LXXRobotState robot, OrbitDirection direction) {
        double desiredHeading = getDesiredHeading(center, robot, direction);

        desiredHeading = robot.getBattleField().smoothWalls(robot.getPosition(), desiredHeading, direction == OrbitDirection.CLOCKWISE);

        return toMovementDecision(robot, getDesiredSpeed(direction), desiredHeading);
    }

    public static MovementDecision toMovementDecision(LXXRobotState robot, double desiredSpeed, double desiredHeading) {
        final double headingRadians = robot.getHeadingRadians();
        final boolean wantToGoFront = LXXUtils.anglesDiff(headingRadians, desiredHeading) < LXXConstants.RADIANS_90;
        final double normalizedDesiredHeading = wantToGoFront ? desiredHeading : Utils.normalAbsoluteAngle(desiredHeading + LXXConstants.RADIANS_180);

        final double turnRemaining = Utils.normalRelativeAngle(normalizedDesiredHeading - headingRadians);
        final double speed = robot.getSpeed();
        final double turnRateRadiansLimit = Rules.getTurnRateRadians(speed);
        final double turnRate =
                LXXUtils.limit(-turnRateRadiansLimit,
                        turnRemaining,
                        turnRateRadiansLimit);

        return new MovementDecision(desiredSpeed * (wantToGoFront ? 1 : -1), turnRate);
    }

    private double getDesiredSpeed(OrbitDirection direction) {
        final double desiredVelocity;
        desiredVelocity = Rules.MAX_VELOCITY;

        return desiredVelocity;
    }

    private double getDesiredHeading(APoint center, LXXRobotState robot, OrbitDirection direction) {
        final double distanceBetween = robot.aDistance(center);

        final double distanceDiff = distanceBetween - desiredDistance;
        final double attackAngleKoeff = distanceDiff / desiredDistance;

        final double maxAttackAngle = LXXConstants.RADIANS_100;
        final double minAttackAngle = LXXConstants.RADIANS_80;
        final double attackAngle = LXXConstants.RADIANS_90 + (LXXConstants.RADIANS_30 * attackAngleKoeff);

        final double angleToMe = center.angleTo(robot);

        return Utils.normalAbsoluteAngle(angleToMe +
                LXXUtils.limit(minAttackAngle, attackAngle, maxAttackAngle) * direction.sign);
    }

}
