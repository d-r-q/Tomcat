/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.bullets.enemy.EnemyBulletPredictionData;
import lxx.strategies.MovementDecision;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;

public class PointsGenerator {

    private final DistanceController distanceController;
    private final BattleField battleField;

    public PointsGenerator(DistanceController distanceController, BattleField battleField) {
        this.distanceController = distanceController;
        this.battleField = battleField;
    }

    private PointDanger getPointDanger(List<LXXBullet> lxxBullets, LXXRobotState robot, LXXRobotState duelOpponent) {
        final int bulletsSize = lxxBullets.size();
        final double firstWaveDng = bulletsSize == 0 ? 0 : getWaveDanger(robot, lxxBullets.get(0));
        final double secondWaveDng = bulletsSize == 1 ? 0 : getWaveDanger(robot, lxxBullets.get(1));
        final double distToEnemy = duelOpponent != null ? robot.aDistance(duelOpponent) : 5;
        double enemyAttackAngle = duelOpponent == null
                ? LXXConstants.RADIANS_90
                : LXXUtils.anglesDiff(duelOpponent.angleTo(robot), robot.getHeadingRadians());
        if (enemyAttackAngle > LXXConstants.RADIANS_90) {
            enemyAttackAngle = abs(enemyAttackAngle - LXXConstants.RADIANS_180);
        }
        return new PointDanger(firstWaveDng, secondWaveDng, distToEnemy, battleField.center.aDistance(robot),
                LXXConstants.RADIANS_90 - enemyAttackAngle);
    }

    private double getWaveDanger(APoint pnt, LXXBullet bullet) {
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        final List<PastBearingOffset> predictedBearingOffsets = aimPredictionData.getPredictedBearingOffsets();
        if (predictedBearingOffsets.size() == 0) {
            return 0;
        }
        final APoint firePos = bullet.getFirePosition();
        final double alpha = firePos.angleTo(pnt);
        final double bearingOffset = Utils.normalRelativeAngle(alpha - bullet.noBearingOffset());
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(alpha, firePos.aDistance(pnt));

        double bulletsDanger = 0;
        final double hiEffectDist = robotWidthInRadians * 0.75;
        final double lowEffectDist = robotWidthInRadians * 2.55;
        for (PastBearingOffset bo : predictedBearingOffsets) {
            final double dist = abs(bearingOffset - bo.bearingOffset);
            if (dist < hiEffectDist) {
                bulletsDanger += (2 - (dist / hiEffectDist)) * bo.danger;
            } else {
                if (dist < lowEffectDist) {
                    bulletsDanger += (1 - (dist / lowEffectDist)) * bo.danger;
                }
            }
        }

        double intersection = 0;
        final IntervalDouble robotIval = new IntervalDouble(bearingOffset - robotWidthInRadians / 2, bearingOffset + robotWidthInRadians / 2);
        for (IntervalDouble shadow : bullet.getMergedShadows()) {
            if (robotIval.intersects(shadow)) {
                intersection += robotIval.intersection(shadow);
            }
        }
        bulletsDanger *= 1 - intersection / robotWidthInRadians;

        return bulletsDanger;
    }

    public List<WSPoint> generatePoints(APoint dstPoint, List<LXXBullet> bullets, RobotImage robotImg, RobotImage opponentImg, int time) {
        final LXXBullet bullet = bullets.get(0);
        final List<WSPoint> points = new LinkedList<WSPoint>();

        final APoint surfPoint = getSurfPoint(opponentImg, bullet);
        final double travelledDistance = bullet.getTravelledDistance();
        final APoint firePosition = bullet.getFirePosition();
        final double bulletSpeed = bullet.getSpeed();
        final double enemyDesiredVelocity;
        if (opponentImg != null) {
            enemyDesiredVelocity = Rules.MAX_VELOCITY * signum(opponentImg.getVelocity());
        } else {
            enemyDesiredVelocity = 0;
        }
        while (firePosition.aDistance(robotImg) - travelledDistance > bulletSpeed * time || points.size() == 0) {
            final MovementDecision md = getMovementDecision(surfPoint, dstPoint, robotImg, opponentImg);
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(enemyDesiredVelocity, 0));
                for (WSPoint prevPoint : points) {
                    prevPoint.danger.distToEnemy = min(prevPoint.danger.distToEnemy, prevPoint.aDistance(opponentImg));
                    prevPoint.danger.calculateDanger();
                }
            }
            robotImg.apply(md);
            points.add(new WSPoint(robotImg, getPointDanger(bullets, robotImg, opponentImg)));
            time++;
        }

        return points;
    }

    public int playForwardWaveSuring(APoint dstPoint, LXXBullet bullet, RobotImage robotImg, RobotImage opponentImg) {
        int time = 0;
        final APoint surfPoint = getSurfPoint(opponentImg, bullet);
        final double travelledDistance = bullet.getTravelledDistance();
        final APoint firePosition = bullet.getFirePosition();
        final double bulletSpeed = bullet.getSpeed();
        final double enemyDesiredVelocity;
        if (opponentImg != null) {
            enemyDesiredVelocity = Rules.MAX_VELOCITY * signum(opponentImg.getVelocity());
        } else {
            enemyDesiredVelocity = 0;
        }
        while (firePosition.aDistance(robotImg) - travelledDistance > bulletSpeed * time) {
            final MovementDecision md = getMovementDecision(surfPoint, dstPoint, robotImg, opponentImg);
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(enemyDesiredVelocity, 0));
            }
            robotImg.apply(md);
            time++;
        }

        return time;
    }

    public MovementDecision getMovementDecision(APoint surfPoint, APoint dstPoint, LXXRobotState robot, LXXRobotState opponent) {
        final double acceleratedSpeed = min(Rules.MAX_VELOCITY, robot.getSpeed() + Rules.ACCELERATION);
        final double desiredSpeed = (robot.aDistance(dstPoint) > LXXUtils.getStopDistance(acceleratedSpeed) + acceleratedSpeed + 1)
                ? 8
                : 0;

        final double alphaToRobot = surfPoint.angleTo(robot);
        final double alphaToDst = surfPoint.angleTo(dstPoint);
        final OrbitDirection orbitDirection;
        if (Utils.normalRelativeAngle(alphaToDst - alphaToRobot) >= 0) {
            orbitDirection = OrbitDirection.CLOCKWISE;
        } else {
            orbitDirection = OrbitDirection.COUNTER_CLOCKWISE;
        }

        return getMovementDecision(surfPoint, orbitDirection, robot, opponent, desiredSpeed);
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, LXXRobotState opponent, double desiredSpeed) {
        double desiredHeading = distanceController.getDesiredHeading(surfPoint, robot, orbitDirection);
        desiredHeading = battleField.smoothWalls(robot, desiredHeading, orbitDirection == OrbitDirection.CLOCKWISE);

        double direction = robot.getAbsoluteHeadingRadians();
        if (LXXUtils.anglesDiff(direction, desiredHeading) > LXXConstants.RADIANS_90) {
            direction = Utils.normalAbsoluteAngle(direction + LXXConstants.RADIANS_180);
        }
        if (opponent != null) {
            double angleToOpponent = robot.angleTo(opponent);
            if (((LXXUtils.anglesDiff(direction, angleToOpponent) < LXXUtils.getRobotWidthInRadians(angleToOpponent, robot.aDistance(opponent)) * 1.1)) ||
                    LXXUtils.getBoundingRectangleAt(robot.project(direction, desiredSpeed), LXXConstants.ROBOT_SIDE_SIZE / 2 - 2).intersects(LXXUtils.getBoundingRectangleAt(opponent))) {
                desiredSpeed = 0;
            }
        }

        return MovementDecision.toMovementDecision(robot, desiredSpeed, desiredHeading);
    }

    public APoint getSurfPoint(LXXRobotState duelOpponent, LXXBullet bullet) {
        if (duelOpponent == null) {
            return bullet.getFirePosition();
        }

        return duelOpponent;
    }

}
