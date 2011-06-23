/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.bullets.enemy.EnemyBulletsPredictionData;
import lxx.office.Office;
import lxx.paint.LXXGraphics;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;

public class WaveSurfingMovement implements Movement {

    protected final Tomcat robot;
    protected final TomcatEyes tomcatEyes;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final DistanceController distanceController;

    private OrbitDirection minDangerOrbitDirection = OrbitDirection.CLOCKWISE;
    private double distanceToTravel;
    private long timeToTravel;
    private Target duelOpponent;

    public WaveSurfingMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();
        this.tomcatEyes = tomcatEyes;

        distanceController = new DistanceController(office.getRobot(), office.getEnemyBulletManager(), office.getTargetManager(), tomcatEyes);
    }

    public MovementDecision getMovementDecision() {
        duelOpponent = targetManager.getDuelOpponent();
        final List<LXXBullet> lxxBullets = getBullets();
        final Target.TargetState opponent = duelOpponent == null ? null : duelOpponent.getState();
        final APoint surfPoint = getSurfPoint(opponent, lxxBullets.get(0));
        selectOrbitDirection(lxxBullets);
        final double desiredSpeed = timeToTravel > 0 ? distanceToTravel / timeToTravel : 8;
        final MovementDecision movementDecision = getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), desiredSpeed);

        return movementDecision;
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        final MovementDirectionPrediction clockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE);
        final MovementDirectionPrediction counterClockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
        final LXXBullet bullet = lxxBullets.get(0);
        if (clockwisePrediction.minDanger.compareTo(counterClockwisePrediction.minDanger) < 0) {
            setMovementParameters(clockwisePrediction, bullet);
        } else {
            setMovementParameters(counterClockwisePrediction, bullet);
        }
    }

    private void setMovementParameters(MovementDirectionPrediction movementDirectionPrediction, LXXBullet bullet) {
        distanceToTravel = movementDirectionPrediction.distToMinDangerPoint;
        final double speed = bullet.getSpeed() < LXXConstants.MIN_BULLET_SPEED ? Integer.MAX_VALUE : bullet.getSpeed();
        timeToTravel = (long) ((bullet.getFirePosition().aDistance(movementDirectionPrediction.minDangerPoint) - bullet.getTravelledDistance()) / speed);
        minDangerOrbitDirection = movementDirectionPrediction.orbitDirection;
        robot.getLXXGraphics().setColor(Color.WHITE);
        robot.getLXXGraphics().drawCircle(movementDirectionPrediction.minDangerPoint, 6);
    }

    private MovementDirectionPrediction predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection) {
        final MovementDirectionPrediction prediction = new MovementDirectionPrediction();
        prediction.orbitDirection = orbitDirection;
        double distance = 0;
        LXXPoint prevPoint = robot.getPosition();
        for (LXXPoint pnt : generatePoints(orbitDirection, lxxBullets.get(0), duelOpponent)) {
            distance += prevPoint.aDistance(pnt);
            final PointDanger danger = getPointDanger(lxxBullets, pnt);

            if (danger.compareTo(prediction.minDanger) < 0) {
                prediction.minDanger = danger;
                prediction.distToMinDangerPoint = distance;
                prediction.minDangerPoint = pnt;
            }
            prevPoint = pnt;
        }

        return prediction;
    }

    private PointDanger getPointDanger(List<LXXBullet> lxxBullets, LXXPoint pnt) {
        final PointDangerOnWave firstWaveDng = lxxBullets.size() == 0 ? null : getWaveDanger(pnt, lxxBullets.get(0));
        final PointDangerOnWave secondWaveDng = lxxBullets.size() == 1 ? null : getWaveDanger(pnt, lxxBullets.get(1));
        final double distToEnemy = duelOpponent != null ? pnt.aDistance(duelOpponent) : 0;
        return new PointDanger(firstWaveDng, secondWaveDng, distToEnemy, robot.getState().getBattleField().center.aDistance(pnt));
    }

    private PointDangerOnWave getWaveDanger(LXXPoint pnt, LXXBullet bullet) {
        final double bearingOffset = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), pnt);
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), pnt);

        double minDist = Integer.MAX_VALUE;
        int bulletsCount = 0;
        for (Double bo : ((EnemyBulletsPredictionData) bullet.getAimPredictionData()).getPredictedBearingOffsets()) {
            final double dist = abs(bearingOffset - bo);
            if (dist < robotWidthInRadians / 2 + LXXConstants.RADIANS_1) {
                minDist = 0;
                bulletsCount++;
            }
            minDist = min(minDist, dist);
        }

        return new PointDangerOnWave(minDist, LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), pnt), bulletsCount, bullet.getFlightTime(pnt));
    }

    private List<LXXBullet> getBullets() {
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(1);
        if (bulletsOnAir.size() < 2 && duelOpponent != null) {
            bulletsOnAir.add(enemyBulletManager.createSafeBullet(duelOpponent));
        }
        return bulletsOnAir;
    }

    private APoint getSurfPoint(LXXRobotState duelOpponent, LXXBullet bullet) {
        if (duelOpponent == null) {
            return bullet.getFirePosition();
        }

        return duelOpponent;
    }

    private List<LXXPoint> generatePoints(OrbitDirection orbitDirection, LXXBullet bullet, Target enemy) {
        final List<LXXPoint> points = new LinkedList<LXXPoint>();

        final LXXGraphics g = robot.getLXXGraphics();
        if (orbitDirection == OrbitDirection.CLOCKWISE) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.YELLOW);
        }

        final RobotImage robotImg = new RobotImage(robot.getPosition(), robot.getVelocity(), robot.getHeadingRadians(), robot.battleField, 0, robot.getEnergy());
        final RobotImage opponentImg = enemy == null ? null : new RobotImage(enemy.getPosition(), enemy.getVelocity(), enemy.getState().getHeadingRadians(), robot.battleField, 0,
                enemy.getEnergy());
        int time = 0;
        while (bullet.getFirePosition().aDistance(robotImg) - bullet.getTravelledDistance() - bullet.getSpeed() > bullet.getSpeed() * time) {
            final MovementDecision md = getMovementDecision(getSurfPoint(opponentImg, bullet), orbitDirection, robotImg, 8);
            robotImg.apply(md);
            points.add(new LXXPoint(robotImg));
            time++;
            g.fillCircle(robotImg, 3);
        }

        return points;
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, double desiredSpeed) {
        double desiredHeading = distanceController.getDesiredHeading(surfPoint, robot, orbitDirection);
        final double smoothedHeadingCW = robot.getBattleField().smoothWalls(robot, desiredHeading, true);
        final double smoothedHeadingCCW = robot.getBattleField().smoothWalls(robot, desiredHeading, false);

        final double cwDist = LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCW);
        final double ccwDist = LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCCW);
        if (cwDist < ccwDist * 0.95) {
            desiredHeading = smoothedHeadingCW;
        } else if (ccwDist < cwDist * 0.95) {
            desiredHeading = smoothedHeadingCCW;
        } else {
            desiredHeading = orbitDirection == OrbitDirection.CLOCKWISE ? smoothedHeadingCW : smoothedHeadingCCW;
        }

        return MovementDecision.toMovementDecision(robot, desiredSpeed, desiredHeading);
    }

    public enum OrbitDirection {

        CLOCKWISE(1),
        COUNTER_CLOCKWISE(-1);

        public final int sign;

        OrbitDirection(int sign) {
            this.sign = sign;
        }
    }

    public static class MovementDirectionPrediction {

        private static PointDanger MAX_POINT_DANGER = new PointDanger(new PointDangerOnWave(0, LXXConstants.RADIANS_90, 100, 10),
                new PointDangerOnWave(0, LXXConstants.RADIANS_90, 100, 10), 0, 1000);

        private PointDanger minDanger = MAX_POINT_DANGER;
        private LXXPoint minDangerPoint;
        private double distToMinDangerPoint;
        private OrbitDirection orbitDirection;

    }

    private static class PointDanger implements Comparable<PointDanger> {

        public final PointDangerOnWave dangerOnFirstWave;
        public final PointDangerOnWave dangerOnSecondWave;
        public final double distToEnemy;
        public final double distanceToCenter;

        private PointDanger(PointDangerOnWave dangerOnFirstWave, PointDangerOnWave dangerOnSecondWave, double distToEnemy, double distanceToWall) {
            this.dangerOnFirstWave = dangerOnFirstWave;
            this.dangerOnSecondWave = dangerOnSecondWave;
            this.distToEnemy = distToEnemy;
            this.distanceToCenter = distanceToWall;
        }

        public int compareTo(PointDanger o) {
            int res = 0;
            if (dangerOnFirstWave != null && (dangerOnFirstWave.bulletFlightTime > 5 || o.dangerOnFirstWave.bulletFlightTime > 5)) {
                res = dangerOnFirstWave.compareTo(o.dangerOnFirstWave);
            }
            if (res == 0 && dangerOnSecondWave != null) {
                res = dangerOnSecondWave.compareTo(dangerOnFirstWave);
            }

            if (res == 0) {
                res = (int) round(o.distToEnemy - distToEnemy);

                if (abs(res) < 25) {
                    res = (int) round(distanceToCenter - o.distanceToCenter);
                }
            }

            return res;
        }

    }

    private static class PointDangerOnWave implements Comparable<PointDangerOnWave> {

        public final double minDistToBulletRadians;
        public final double robotWidthInRadians;
        public final int bulletsCount;
        public final double bulletFlightTime;

        public PointDangerOnWave(double minDistToBulletRadians, double robotWidthInRadians, int bulletsCount, double bulletFlightTime) {
            this.minDistToBulletRadians = minDistToBulletRadians;
            this.robotWidthInRadians = robotWidthInRadians;
            this.bulletsCount = bulletsCount;
            this.bulletFlightTime = bulletFlightTime;
        }

        public int compareTo(PointDangerOnWave o) {
            if (!hasCloseBullets() && !o.hasCloseBullets()) {
                return 0;
            } else if (hasCloseBullets() && !o.hasCloseBullets()) {
                return 1;
            } else if (!hasCloseBullets() && o.hasCloseBullets()) {
                return -1;
            } else {
                if (bulletsCount != o.bulletsCount) {
                    return (int) signum(bulletsCount - o.bulletsCount);
                } else {
                    return (int) signum(o.minDistToBulletRadians - minDistToBulletRadians);
                }
            }
        }

        private boolean hasCloseBullets() {
            return minDistToBulletRadians < robotWidthInRadians * 2.5;
        }

    }

}
