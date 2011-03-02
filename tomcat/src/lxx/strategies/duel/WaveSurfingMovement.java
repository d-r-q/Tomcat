/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.enemy_bullets.EnemyAimingPredictionData;
import lxx.office.EnemyBulletManager;
import lxx.office.TargetManager;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class WaveSurfingMovement implements Movement {

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;

    private double minDanger;
    private OrbitDirection minDangerOrbitDirection = OrbitDirection.CLOCKWISE;

    public WaveSurfingMovement(Tomcat robot, TargetManager targetManager, EnemyBulletManager enemyBulletManager) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
    }

    public MovementDecision getMovementDecision() {
        final APoint surfPoint = getSurfPoint();
        selectOrbitDirection(surfPoint);

        Target.TargetState opponent = targetManager.getDuelOpponent() == null ? null : targetManager.getDuelOpponent().getState();
        return getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), opponent);
    }

    private void selectOrbitDirection(APoint surfPoint) {
        final List<LXXBullet> lxxBullets = getBullets();

        minDanger = Integer.MAX_VALUE;

        checkPointsInDirection(surfPoint, lxxBullets, OrbitDirection.CLOCKWISE);
        checkPointsInDirection(surfPoint, lxxBullets, OrbitDirection.STOP);
        // todo(zhidkov): reuse stop points
        checkPointsInDirection(surfPoint, lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
    }

    private void checkPointsInDirection(APoint surfPoint, List<LXXBullet> lxxBullets, OrbitDirection orbitDirection) {
        for (LXXPoint pnt : generatePoints(orbitDirection, lxxBullets, surfPoint, targetManager.getDuelOpponent())) {
            double danger = getPointDanger(lxxBullets, pnt);

            if (danger < minDanger) {
                minDanger = danger;
                minDangerOrbitDirection = orbitDirection;
            }
        }
    }

    private double getPointDanger(List<LXXBullet> lxxBullets, LXXPoint pnt) {
        final Target duelOpponent = targetManager.getDuelOpponent();
        double totalDanger = 0;
        double weight = 1D;
        for (LXXBullet lxxBullet : lxxBullets) {
            final EnemyAimingPredictionData enemyAimingPredictionData = lxxBullet != null ? (EnemyAimingPredictionData) lxxBullet.getAimPredictionData() : null;
            double distanceDanger;
            if (duelOpponent != null) {
                distanceDanger = 300 / pnt.aDistance(duelOpponent);
            } else {
                distanceDanger = 0;
            }

            double bulletDanger;
            if (enemyAimingPredictionData != null) {
                bulletDanger = enemyAimingPredictionData.getMaxDanger(LXXUtils.bearingOffset(lxxBullet.getFirePosition(), lxxBullet.getWave().getTargetStateAtFireTime(), pnt),
                        LXXUtils.getRobotWidthInRadians(lxxBullet.getFirePosition(), pnt));
            } else {
                bulletDanger = 0;
            }

            totalDanger += (bulletDanger + distanceDanger) * weight;
            weight /= 25;
        }
        return totalDanger;
    }

    private List<LXXBullet> getBullets() {
        final List<LXXBullet> bullets = new ArrayList<LXXBullet>();
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBullets();
        for (int i = 0; i < 2; i++) {
            if (bulletsOnAir.size() == i) {
                break;
            }
            bullets.add(bulletsOnAir.get(i));
        }

        final Target duelOpponent = targetManager.getDuelOpponent();
        if (bullets.size() == 0 && duelOpponent != null) {
            bullets.add(enemyBulletManager.createSafeBullet(duelOpponent));
        }

        return bullets;
    }

    private APoint getSurfPoint() {
        final Target duelOpponent = targetManager.getDuelOpponent();
        if (duelOpponent == null) {
            return enemyBulletManager.getClosestBullet().getFirePosition();
        }

        return duelOpponent;
    }

    private List<LXXPoint> generatePoints(OrbitDirection orbitDirection, List<LXXBullet> bullets, APoint surfPoint, Target enemy) {
        final List<LXXPoint> points = new ArrayList<LXXPoint>();

        final RobotImage robotImg = new RobotImage(robot.getPosition(), robot.getVelocity(), robot.getHeadingRadians(), robot.battleField, 0);
        final RobotImage opponentImg = enemy == null ? null : new RobotImage(enemy.getPosition(), enemy.getVelocity(), enemy.getState().getHeadingRadians(), robot.battleField, 0);
        final LXXBullet bullet = bullets.get(0);
        int time = 0;
        while (bullet.getFirePosition().aDistance(robotImg) - bullet.getTravelledDistance() > bullet.getSpeed() * time) {
            final MovementDecision md = getMovementDecision(surfPoint, orbitDirection, robotImg, opponentImg);
            robotImg.apply(md);
            points.add(new LXXPoint(robotImg));
            time++;
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(0, 0, enemy.getVelocity() >= 0 ? MovementDecision.MovementDirection.FORWARD : MovementDecision.MovementDirection.BACKWARD));
            }
        }

        return points;
    }

    private double getTargetHeading(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection) {
        return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign -
                LXXConstants.RADIANS_4 * orbitDirection.sign);
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, LXXRobotState opponent) {
        final double targetHeading = getTargetHeading(surfPoint, robot, orbitDirection);

        if (orbitDirection == OrbitDirection.STOP || isGoingOnOpponent(robot, opponent)) {
            double turnRateRadians = Utils.normalRelativeAngle(targetHeading - robot.getHeadingRadians());
            if (abs(turnRateRadians) > LXXConstants.RADIANS_90) {
                turnRateRadians = Utils.normalRelativeAngle(targetHeading - Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180));
            }
            return new MovementDecision(-2,
                    turnRateRadians,
                    MovementDecision.MovementDirection.FORWARD);
        }
        final double smoothedHeading = robot.getBattleField().smoothWalls(robot, targetHeading,
                orbitDirection != OrbitDirection.COUNTER_CLOCKWISE);
        final MovementDecision.MovementDirection md = LXXUtils.anglesDiff(robot.getHeadingRadians(), smoothedHeading) < LXXConstants.RADIANS_90
                ? MovementDecision.MovementDirection.FORWARD
                : MovementDecision.MovementDirection.BACKWARD;
        return MovementDecision.toMovementDecision(robot, smoothedHeading, md);
    }

    private boolean isGoingOnOpponent(LXXRobotState robot, LXXRobotState opponent) {
        return opponent != null &&
                ((robot.aDistance(opponent) < 200 && LXXUtils.anglesDiff(robot.getAbsoluteHeadingRadians(), robot.angleTo(opponent)) < LXXConstants.RADIANS_30) ||
                        (robot.aDistance(opponent) < 100 && LXXUtils.anglesDiff(robot.getAbsoluteHeadingRadians(), robot.angleTo(opponent)) < LXXConstants.RADIANS_90));
    }

    private enum OrbitDirection {

        CLOCKWISE(1),
        STOP(1),
        COUNTER_CLOCKWISE(-1);

        public final int sign;


        OrbitDirection(int sign) {
            this.sign = sign;
        }
    }

}
