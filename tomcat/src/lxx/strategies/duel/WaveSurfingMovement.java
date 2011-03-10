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
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public abstract class WaveSurfingMovement implements Movement {

    protected final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    protected final TomcatEyes tomcatEyes;

    private OrbitDirection minDangerOrbitDirection = OrbitDirection.CLOCKWISE;
    private double distanceToTravel;
    private long timeToTravel;
    protected double enemyPreferredDistance;

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
        setEnemyPreferredDistance();
        final MovementDirectionPrediction clockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE);
        final MovementDirectionPrediction counterClockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
        final LXXBullet bullet = lxxBullets.get(0);
        if (abs(clockwisePrediction.minDanger - counterClockwisePrediction.minDanger) < 2) {
            if (minDangerOrbitDirection == OrbitDirection.CLOCKWISE) {
                setMovementParameters(clockwisePrediction, bullet);
            } else {
                setMovementParameters(counterClockwisePrediction, bullet);
            }
        } else if (clockwisePrediction.minDanger < counterClockwisePrediction.minDanger) {
            setMovementParameters(clockwisePrediction, bullet);
        } else {
            setMovementParameters(counterClockwisePrediction, bullet);
        }
    }

    private void setMovementParameters(MovementDirectionPrediction movementDirectionPrediction, LXXBullet bullet) {
        distanceToTravel = movementDirectionPrediction.distToMinDangerPoint;
        timeToTravel = (long) ((bullet.getFirePosition().aDistance(movementDirectionPrediction.minDangerPoint) - bullet.getTravelledDistance()) / bullet.getSpeed());
        minDangerOrbitDirection = movementDirectionPrediction.orbitDirection;
        System.out.println(minDangerOrbitDirection + ": " + movementDirectionPrediction.minDanger + ": " + movementDirectionPrediction.distToMinDangerPoint + ": " +
                timeToTravel);
        robot.getLXXGraphics().setColor(Color.WHITE);
        robot.getLXXGraphics().drawCircle(movementDirectionPrediction.minDangerPoint, 6);
    }

    private void setEnemyPreferredDistance() {
        final Target opponent = targetManager.getDuelOpponent();
        if (opponent == null) {
            return;
        }

        enemyPreferredDistance = getEnemyPreferredDistance(opponent);
    }

    protected abstract double getEnemyPreferredDistance(Target opponent);

    private MovementDirectionPrediction predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection) {
        final MovementDirectionPrediction prediction = new MovementDirectionPrediction();
        prediction.orbitDirection = orbitDirection;
        double distance = 0;
        LXXPoint prevPoint = robot.getPosition();
        for (LXXPoint pnt : generatePoints(orbitDirection, lxxBullets, targetManager.getDuelOpponent())) {
            distance += prevPoint.aDistance(pnt);
            double danger = getPointDanger(lxxBullets, pnt);

            if (danger <= prediction.minDanger) {
                prediction.minDanger = danger;
                prediction.distToMinDangerPoint = distance;
                prediction.minDangerPoint = pnt;
            }
            prevPoint = pnt;
        }

        return prediction;
    }

    private double getPointDanger(List<LXXBullet> lxxBullets, LXXPoint pnt) {
        double totalDanger = 0;
        double weight = 1D;
        for (LXXBullet lxxBullet : lxxBullets) {
            final EnemyAimingPredictionData enemyAimingPredictionData = lxxBullet != null ? (EnemyAimingPredictionData) lxxBullet.getAimPredictionData() : null;

            double bulletDanger;
            if (enemyAimingPredictionData != null) {
                bulletDanger = enemyAimingPredictionData.getDanger(LXXUtils.bearingOffset(lxxBullet.getFirePosition(), lxxBullet.getWave().getTargetStateAtFireTime(), pnt),
                        LXXUtils.getRobotWidthInRadians(lxxBullet.getFirePosition(), pnt));
            } else {
                bulletDanger = 0;
            }

            totalDanger += round(bulletDanger) * 100 * weight;
            weight /= 20;
        }
        final Target opponent = targetManager.getDuelOpponent();
        if (opponent != null) {
            totalDanger += getPointDanger(pnt, opponent);
        }
        return totalDanger;
    }

    protected abstract double getPointDanger(APoint pnt, Target opponent);

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
                opponentImg.apply(new MovementDecision(1, 0, enemy.getVelocity() >= 0 ? MovementDecision.MovementDirection.FORWARD : MovementDecision.MovementDirection.BACKWARD));
            }
        }

        return points;
    }

    private double getTargetHeading(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection) {
        final double distanceBetween = robot.aDistance(surfPoint);
        final Target opponent = targetManager.getDuelOpponent();
        return getTargetHeading(surfPoint, robot, orbitDirection, opponent, distanceBetween);
    }

    protected abstract double getTargetHeading(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection,
                                               Target opponent, double distanceBetween);

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

    protected enum OrbitDirection {

        CLOCKWISE(1),
        COUNTER_CLOCKWISE(-1);

        public final int sign;


        OrbitDirection(int sign) {
            this.sign = sign;
        }
    }

    private class MovementDirectionPrediction {

        private double minDanger = Integer.MAX_VALUE;
        private LXXPoint minDangerPoint;
        private double distToMinDangerPoint;
        private OrbitDirection orbitDirection;

    }

}
