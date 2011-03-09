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
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.bullets.LXXBullet;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.*;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.min;

public class WaveSurfingMovement implements Movement {

    private static final DecreasingActivator distanceActivator = new DecreasingActivator(200, 30, 0);
    private static final int DEFAULT_DISTANCE_AGAINST_ADVANCED = 500;
    private static final int DEFAULT_DISTANCE_AGAINST_SIMPLE = 400;

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final TomcatEyes tomcatEyes;

    private double minDanger;
    private OrbitDirection minDangerOrbitDirection = OrbitDirection.CLOCKWISE;
    private double distanceToTravel;
    private long timeToTravel;
    private int enemyPreferredDistance;

    public WaveSurfingMovement(Tomcat robot, TargetManager targetManager,
                               EnemyBulletManager enemyBulletManager, TomcatEyes tomcatEyes) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
        this.tomcatEyes = tomcatEyes;
    }

    public MovementDecision getMovementDecision() {
        final List<LXXBullet> lxxBullets = getBullets();
        final Target.TargetState opponent = targetManager.getDuelOpponent() == null ? null : targetManager.getDuelOpponent().getState();
        final APoint surfPoint = getSurfPoint(opponent, lxxBullets);
        selectOrbitDirection(lxxBullets);

        return getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), distanceToTravel, timeToTravel);
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        minDanger = Integer.MAX_VALUE;
        setEnemyPreferredDistance();
        checkPointsInDirection(lxxBullets, OrbitDirection.CLOCKWISE);
        checkPointsInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
    }

    private void setEnemyPreferredDistance() {
        final Target opponent = targetManager.getDuelOpponent();
        if (opponent == null) {
            return;
        }

        if (tomcatEyes.getEnemyGunType(opponent) == GunType.ADVANCED) {
            enemyPreferredDistance = tomcatEyes.getEnemyPreferredDistance(opponent);
            if (enemyPreferredDistance == -1) {
                enemyPreferredDistance = DEFAULT_DISTANCE_AGAINST_ADVANCED;
            }
            enemyPreferredDistance = (int) min(enemyPreferredDistance - 75, opponent.getPosition().distanceToWall(robot.battleField, opponent.angleTo(robot)) - 75);
        } else {
            enemyPreferredDistance = (int) min(DEFAULT_DISTANCE_AGAINST_SIMPLE, opponent.getPosition().distanceToWall(robot.battleField, opponent.angleTo(robot)) - 75);
        }
    }

    private void checkPointsInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection) {
        double distance = 0;
        long time = 0;
        LXXPoint prevPoint = robot.getPosition();
        for (LXXPoint pnt : generatePoints(orbitDirection, lxxBullets, targetManager.getDuelOpponent())) {
            distance += prevPoint.aDistance(pnt);
            time++;
            double danger = getPointDanger(lxxBullets, pnt);

            if (danger < minDanger) {
                minDanger = danger;
                minDangerOrbitDirection = orbitDirection;
                distanceToTravel = distance;
            }
            prevPoint = pnt;
        }
        if (minDangerOrbitDirection == orbitDirection) {
            timeToTravel = time;
        }
    }

    private double getPointDanger(List<LXXBullet> lxxBullets, LXXPoint pnt) {
        double totalDanger = 0;
        double weight = 1D;
        for (LXXBullet lxxBullet : lxxBullets) {
            final EnemyAimingPredictionData enemyAimingPredictionData = lxxBullet != null ? (EnemyAimingPredictionData) lxxBullet.getAimPredictionData() : null;

            double bulletDanger;
            if (enemyAimingPredictionData != null) {
                bulletDanger = enemyAimingPredictionData.getMaxDanger(LXXUtils.bearingOffset(lxxBullet.getFirePosition(), lxxBullet.getWave().getTargetStateAtFireTime(), pnt),
                        LXXUtils.getRobotWidthInRadians(lxxBullet.getFirePosition(), pnt));
            } else {
                bulletDanger = 0;
            }

            totalDanger += bulletDanger * bulletDanger * weight;
            weight /= 10;
        }
        final Target opponent = targetManager.getDuelOpponent();
        if (opponent != null && tomcatEyes.getEnemyGunType(opponent) == GunType.ADVANCED) {
            final double distanceToCenterDanger = pnt.aDistance(robot.battleField.center) / 600;
            totalDanger += distanceToCenterDanger * distanceToCenterDanger;
        } else if (opponent != null) {
            final double distanceEnemyDanger = DEFAULT_DISTANCE_AGAINST_SIMPLE / pnt.aDistance(opponent);
            totalDanger += distanceEnemyDanger * distanceEnemyDanger / 10;
            final double distanceToCenterDanger = pnt.aDistance(robot.battleField.center) / 1000;
            totalDanger += distanceToCenterDanger * distanceToCenterDanger / 10;
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
        if (bullets.size() < 2 && duelOpponent != null) {
            bullets.add(enemyBulletManager.createSafeBullet(duelOpponent));
        }

        return bullets;
    }

    private APoint getSurfPoint(LXXRobotState duelOpponent, List<LXXBullet> bullets) {
        if (duelOpponent == null) {
            return bullets.get(0).getFirePosition();
        }

        return duelOpponent.project(duelOpponent.getAbsoluteHeadingRadians(), duelOpponent.getVelocityModule());
    }

    private List<LXXPoint> generatePoints(OrbitDirection orbitDirection, List<LXXBullet> bullets, Target enemy) {
        final List<LXXPoint> points = new LinkedList<LXXPoint>();

        final LXXGraphics g = robot.getLXXGraphics();
        if (orbitDirection == OrbitDirection.CLOCKWISE) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.YELLOW);
        }

        final RobotImage robotImg = new RobotImage(robot.getPosition(), robot.getVelocity(), robot.getHeadingRadians(), robot.battleField, 0);
        final RobotImage opponentImg = enemy == null ? null : new RobotImage(enemy.getPosition(), enemy.getVelocity(), enemy.getState().getHeadingRadians(), robot.battleField, 0);
        final LXXBullet bullet = bullets.get(0);
        int time = 0;
        while (bullet.getFirePosition().aDistance(robotImg) - bullet.getTravelledDistance() > bullet.getSpeed() * time) {
            final MovementDecision md = getMovementDecision(getSurfPoint(opponentImg, bullets), orbitDirection, robotImg, Integer.MAX_VALUE, 0);
            robotImg.apply(md);
            points.add(new LXXPoint(robotImg));
            time++;
            g.fillCircle(robotImg, 3);
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(0, 0, enemy.getVelocity() >= 0 ? MovementDecision.MovementDirection.FORWARD : MovementDecision.MovementDirection.BACKWARD));
            }
        }

        return points;
    }

    private double getTargetHeading(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection) {
        final double distanceBetween = robot.aDistance(surfPoint);
        final Target opponent = targetManager.getDuelOpponent();
        if (opponent != null && tomcatEyes.getEnemyGunType(opponent) == GunType.ADVANCED) {
            if (distanceBetween > enemyPreferredDistance + 10) {
                return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_110 * orbitDirection.sign);
            } else if (distanceBetween < enemyPreferredDistance - 10) {
                return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_60 * orbitDirection.sign);
            } else {
                return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign);
            }
        } else {
            if (distanceBetween > DEFAULT_DISTANCE_AGAINST_SIMPLE + 10) {
                return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_100 * orbitDirection.sign);
            } else if (distanceBetween < DEFAULT_DISTANCE_AGAINST_SIMPLE - 10) {
                return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + (LXXConstants.RADIANS_75 -
                        LXXConstants.RADIANS_45 * distanceActivator.activate(surfPoint.aDistance(robot))) * orbitDirection.sign);
            } else {
                return Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign);
            }
        }
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, double distanceToTravel, long timeToTravel) {
        final double targetHeading = getTargetHeading(surfPoint, robot, orbitDirection);
        final double smoothedHeading = robot.getBattleField().smoothWalls(robot, targetHeading,
                orbitDirection == OrbitDirection.CLOCKWISE);
        final MovementDecision.MovementDirection md = LXXUtils.anglesDiff(robot.getHeadingRadians(), smoothedHeading) < LXXConstants.RADIANS_90
                ? MovementDecision.MovementDirection.FORWARD
                : MovementDecision.MovementDirection.BACKWARD;
        return MovementDecision.toMovementDecision(robot, smoothedHeading, md, distanceToTravel, timeToTravel);
    }

    private enum OrbitDirection {

        CLOCKWISE(1),
        COUNTER_CLOCKWISE(-1);

        public final int sign;


        OrbitDirection(int sign) {
            this.sign = sign;
        }
    }

}
