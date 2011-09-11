/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.bullets.enemy.EnemyBulletPredictionData;
import lxx.office.Office;
import lxx.paint.LXXGraphics;
import lxx.paint.Painter;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.*;
import robocode.Rules;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;
import static java.lang.Math.abs;

public class WaveSurfingMovement implements Movement, Painter {

    private final List<OrbitDirection> cmps = new ArrayList<OrbitDirection>();

    private final Tomcat robot;
    private final TomcatEyes tomcatEyes;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final DistanceController distanceController;

    private OrbitDirection minDangerOrbitDirection = OrbitDirection.CLOCKWISE;
    private double distanceToTravel;
    private Target duelOpponent;
    private MovementDirectionPrediction prevPrediction;
    private BattleField battleField;
    private double preferredDistance;
    private List<MovementDirectionPrediction> preds;

    public WaveSurfingMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();
        this.tomcatEyes = tomcatEyes;

        distanceController = new DistanceController(office.getRobot(), office.getEnemyBulletManager(), office.getTargetManager(), tomcatEyes);
        battleField = robot.getState().getBattleField();
    }

    public MovementDecision getMovementDecision() {
        preferredDistance = distanceController.getPreferredDistance();
        duelOpponent = targetManager.getDuelOpponent();
        final List<LXXBullet> lxxBullets = getBullets();
        if (needToReselectOrbitDirection(lxxBullets)) {
            selectOrbitDirection(lxxBullets);
        } else {
            distanceToTravel -= robot.getSpeed();
        }

        cmps.add(prevPrediction.orbitDirection);
        if (cmps.size() > 3) {
            if (cmps.get(cmps.size() - 1) != cmps.get(cmps.size() - 2) &&
                    cmps.get(cmps.size() - 1) == cmps.get(cmps.size() - 3)) {
                System.out.println("AAAAAAAAAAAAAAAAAAaa ");
                System.out.println(prevPrediction.minDanger);
            }
        }

        final Target.TargetState opponent = duelOpponent == null ? null : duelOpponent.getState();
        final APoint surfPoint = getSurfPoint(opponent, lxxBullets.get(0));
        final double desiredSpeed =
                (distanceToTravel > LXXUtils.getStopDistance(robot.getSpeed()) ||
                        (duelOpponent != null && tomcatEyes.isRammingNow(duelOpponent)))
                        ? 8
                        : 0;

        return getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), 8 * abs(minDangerOrbitDirection.sign), lxxBullets);
    }

    private boolean needToReselectOrbitDirection(List<LXXBullet> bullets) {
        return true;
        /*return prevPrediction == null ||
                !prevPrediction.bullet.equals(bullets.get(0)) ||
                (duelOpponent != null && signum(duelOpponent.getAcceleration()) != prevPrediction.enemyAccelSign) ||
                (duelOpponent != null && duelOpponent.aDistance(robot) < prevPrediction.distanceBetween - 25);*/
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        preds = new ArrayList<MovementDirectionPrediction>();
        for (OrbitDirection od : OrbitDirection.values()) {
            preds.add(predictMovementInDirection(lxxBullets, od));
        }

        MovementDirectionPrediction bestMd = null;

        for (MovementDirectionPrediction mdp : preds) {
            double bestMdDanger = bestMd == null ? Integer.MAX_VALUE : bestMd.minDanger.danger;
            if (bestMd != null && prevPrediction != null && bestMd.orbitDirection == prevPrediction.orbitDirection) {
                bestMdDanger *= 0.9;
            }
            double mdpDanger = mdp.minDanger.danger;
            if (prevPrediction != null && mdp.orbitDirection == prevPrediction.orbitDirection) {
                mdpDanger *= 0.9;
            }

            if (mdpDanger < bestMdDanger) {
                bestMd = mdp;
            }
        }

        setMovementParameters(bestMd);
    }

    private void setMovementParameters(MovementDirectionPrediction movementDirectionPrediction) {
        distanceToTravel = movementDirectionPrediction.distToMinDangerPoint;
        minDangerOrbitDirection = movementDirectionPrediction.orbitDirection;
        prevPrediction = movementDirectionPrediction;
    }

    private MovementDirectionPrediction predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection) {
        final MovementDirectionPrediction prediction = new MovementDirectionPrediction();
        prediction.enemyPos = duelOpponent != null ? duelOpponent.getPosition() : null;
        prediction.bullet = lxxBullets.get(0);
        prediction.orbitDirection = orbitDirection;
        double distance = 0;
        APoint prevPoint = robot.getPosition();
        prediction.points = generatePoints(orbitDirection, lxxBullets, duelOpponent);
        prediction.enemyAccelSign = duelOpponent != null ? signum(duelOpponent.getAcceleration()) : 0;
        prediction.distanceBetween = duelOpponent != null ? duelOpponent.aDistance(robot) : 0;
        for (WSPoint pnt : prediction.points) {
            distance += prevPoint.aDistance(pnt);

            if (pnt.danger.danger < prediction.minDanger.danger) {
                prediction.minDanger = pnt.danger;
                prediction.distToMinDangerPoint = distance;
                prediction.minDangerPoint = pnt;
            }
            prevPoint = pnt;
        }

        return prediction;
    }

    private PointDanger getPointDanger(List<LXXBullet> lxxBullets, LXXRobotState robot, LXXRobotState duelOpponent) {
        final int bulletsSize = lxxBullets.size();
        final PointDangerOnWave firstWaveDng = bulletsSize == 0 ? null : getWaveDanger(robot, lxxBullets.get(0));
        final PointDangerOnWave secondWaveDng = bulletsSize == 1 ? null : getWaveDanger(robot, lxxBullets.get(1));
        final double distToEnemy = duelOpponent != null ? robot.aDistance(duelOpponent) : 0;
        double enemyAttackAngle = duelOpponent == null
                ? LXXConstants.RADIANS_90
                : LXXUtils.anglesDiff(duelOpponent.angleTo(robot), robot.getAbsoluteHeadingRadians());
        if (enemyAttackAngle > LXXConstants.RADIANS_90) {
            enemyAttackAngle = abs(enemyAttackAngle - LXXConstants.RADIANS_180);
        }
        return new PointDanger(firstWaveDng, secondWaveDng, abs(distToEnemy - preferredDistance), battleField.center.aDistance(robot),
                LXXConstants.RADIANS_90 - enemyAttackAngle);
    }

    private PointDangerOnWave getWaveDanger(APoint pnt, LXXBullet bullet) {
        final double bearingOffset = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), pnt);
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), pnt);

        double minDist = Integer.MAX_VALUE;
        double bulletsDanger = 0;
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        for (PastBearingOffset bo : aimPredictionData.getPredictedBearingOffsets()) {
            final double dist = abs(bearingOffset - bo.bearingOffset);
            if (dist < robotWidthInRadians * 0.45) {
                bulletsDanger += (2 - (dist / (robotWidthInRadians * 0.45))) * bo.danger;
            } else if (dist < robotWidthInRadians * 1.55) {
                bulletsDanger += (1 - (dist / (robotWidthInRadians * 1.55))) * bo.danger;
            }
            minDist = min(minDist, dist);
        }

        return new PointDangerOnWave(robotWidthInRadians, bulletsDanger);
    }

    private List<LXXBullet> getBullets() {
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(0);
        if (bulletsOnAir.size() < 2 && duelOpponent != null) {
            bulletsOnAir.add(enemyBulletManager.createFutureBullet(duelOpponent));
        }
        return bulletsOnAir;
    }

    private APoint getSurfPoint(LXXRobotState duelOpponent, LXXBullet bullet) {
        if (duelOpponent == null) {
            return bullet.getFirePosition();
        }

        return duelOpponent;
    }

    private List<WSPoint> generatePoints(OrbitDirection orbitDirection, List<LXXBullet> bullets, Target enemy) {
        final LXXBullet bullet = bullets.get(0);
        final List<WSPoint> points = new LinkedList<WSPoint>();

        final RobotImage robotImg = new RobotImage(robot.getPosition(), robot.getVelocity(), robot.getHeadingRadians(), robot.battleField, 0, robot.getEnergy());
        final RobotImage opponentImg = enemy == null ? null : new RobotImage(enemy.getPosition(), enemy.getVelocity(), enemy.getState().getHeadingRadians(), robot.battleField, 0,
                enemy.getEnergy());
        int time = 0;
        final APoint surfPoint = getSurfPoint(opponentImg, bullet);
        final double travelledDistance = bullet.getTravelledDistance();
        final APoint firePosition = bullet.getFirePosition();
        while (firePosition.aDistance(robotImg) - travelledDistance > bullet.getSpeed() * time) {
            final MovementDecision md = getMovementDecision(surfPoint, orbitDirection, robotImg, 8 * abs(orbitDirection.sign), bullets);
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(Rules.MAX_VELOCITY * signum(opponentImg.getVelocity()), 0));
            }
            robotImg.apply(md);
            points.add(new WSPoint(robotImg, getPointDanger(bullets, robotImg, opponentImg)));
            if (orbitDirection == OrbitDirection.STOP && robotImg.getVelocity() == 0) {
                break;
            }
            time++;
        }

        return points;
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, double desiredSpeed, List<LXXBullet> bulletsOnAir) {
        double desiredHeading = orbitDirection != OrbitDirection.STOP
                ? distanceController.getDesiredHeading(surfPoint, robot, orbitDirection, bulletsOnAir)
                : robot.getAbsoluteHeadingRadians();
        desiredHeading = battleField.smoothWalls(robot, desiredHeading, orbitDirection == OrbitDirection.CLOCKWISE);
        /*double smoothedHeadingCW = battleField.smoothWalls(robot, desiredHeading, true);
        double smoothedHeadingCCW = battleField.smoothWalls(robot, desiredHeading, false);
        if (LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCW) <
                LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCCW)) {
            desiredHeading = smoothedHeadingCW;
        } else {
            desiredHeading = smoothedHeadingCCW;
        }*/

        return MovementDecision.toMovementDecision(robot, desiredSpeed, desiredHeading);
    }

    public void paint(LXXGraphics g) {
        if (prevPrediction == null) {
            return;
        }

        final Color[] colors = {new Color(255, 255, 255, 100),
                new Color(255, 0, 0, 200),
                new Color(0, 255, 0, 200)};

        for (int i = 0; i < preds.size(); i++) {
            g.setColor(colors[i]);
            for (WSPoint pnt : preds.get(i).points) {
                g.fillCircle(pnt, 3);
            }
        }

        g.drawCircle(prevPrediction.minDangerPoint, 5);
        g.drawString(0, 0, prevPrediction.orbitDirection.toString() + ": " + prevPrediction.minDanger.danger);
    }

    private class WSPoint extends LXXPoint {

        private final PointDanger danger;

        private WSPoint(APoint point, PointDanger danger) {
            super(point);
            this.danger = danger;
        }
    }

    public enum OrbitDirection {

        STOP(0),
        CLOCKWISE(1),
        COUNTER_CLOCKWISE(-1);

        public final int sign;

        OrbitDirection(int sign) {
            this.sign = sign;
        }
    }

    public class MovementDirectionPrediction {

        private PointDanger MAX_POINT_DANGER = new PointDanger(new PointDangerOnWave(LXXConstants.RADIANS_90, 100),
                new PointDangerOnWave(LXXConstants.RADIANS_90, 100), 0, 1000, 0);

        private PointDanger minDanger = MAX_POINT_DANGER;
        private APoint minDangerPoint;
        private double distToMinDangerPoint;
        private OrbitDirection orbitDirection;
        public LXXPoint enemyPos;
        public LXXBullet bullet;
        public List<WSPoint> points;
        public double enemyAccelSign;
        public double distanceBetween;
    }

    private class PointDanger implements Comparable<PointDanger> {

        public final PointDangerOnWave dangerOnFirstWave;
        public final PointDangerOnWave dangerOnSecondWave;
        public final double distToEnemyDiff;
        public final double distanceToCenter;
        public final double enemyAttackAngle;
        public final double danger;

        private PointDanger(PointDangerOnWave dangerOnFirstWave, PointDangerOnWave dangerOnSecondWave, double distToEnemy, double distanceToWall,
                            double enemyAttackAngle) {
            this.dangerOnFirstWave = dangerOnFirstWave;
            this.dangerOnSecondWave = dangerOnSecondWave;
            this.distToEnemyDiff = distToEnemy;
            this.distanceToCenter = distanceToWall;
            this.enemyAttackAngle = enemyAttackAngle;

            this.danger = dangerOnFirstWave.bulletsDanger * 50 +
                    (dangerOnSecondWave != null ? dangerOnSecondWave.bulletsDanger : 0) * 10 +
                    abs(distToEnemyDiff) / preferredDistance * 8 +
                    abs(distanceToCenter - 200) / 400 * 3 +
                    enemyAttackAngle * 2;

        }

        public int compareTo(PointDanger o) {
            int res = 0;

            if (dangerOnFirstWave != null) {
                res = dangerOnFirstWave.compareTo(o.dangerOnFirstWave);
            }

            if (res == 0 && dangerOnSecondWave != null) {
                res = dangerOnSecondWave.compareTo(o.dangerOnSecondWave);
            }

            double thisDng = abs(distToEnemyDiff) / preferredDistance * 3 +
                    abs(distanceToCenter - 200) / 400;

            double anotherDng = abs(o.distToEnemyDiff) / preferredDistance * 3 +
                    abs(o.distanceToCenter - 200) / 400;

            if (res == 0) {
                res = compareDoubles(thisDng, anotherDng, 0.1);
            }

            return res;
        }

        @Override
        public String toString() {
            return String.format("PointDanger (%s #1, %s #2, %3.3f, %3.3f)", dangerOnFirstWave, dangerOnSecondWave, distToEnemyDiff, distanceToCenter);
        }
    }

    private static class PointDangerOnWave implements Comparable<PointDangerOnWave> {

        public final double robotWidthInRadians;
        public final double bulletsDanger;

        public PointDangerOnWave(double robotWidthInRadians, double bulletsDanger) {
            this.robotWidthInRadians = robotWidthInRadians;
            this.bulletsDanger = bulletsDanger;
        }

        public int compareTo(PointDangerOnWave o) {
            return compareDoubles(bulletsDanger, o.bulletsDanger, 0.001);
        }

        @Override
        public String toString() {
            return String.format("PointDangerOnWave (%3.3f, %3.3f)",
                    Math.toDegrees(robotWidthInRadians), bulletsDanger);
        }
    }


    public static int compareDoubles(double d1, double d2, double threshold) {
        final double diff = d1 - d2;
        return abs(diff) < max(abs(d1), abs(d2)) * threshold ? 0 : (int) signum(diff);
    }
}
