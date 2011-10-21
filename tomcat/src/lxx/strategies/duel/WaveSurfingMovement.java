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
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;

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
    private MovementDirectionPrediction clockwisePrediction;
    private MovementDirectionPrediction counterClockwisePrediction;

    public WaveSurfingMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();
        this.tomcatEyes = tomcatEyes;

        distanceController = new DistanceController(office.getRobot(), office.getEnemyBulletManager(), office.getTargetManager(), tomcatEyes);
        battleField = robot.getState().getBattleField();
    }

    public MovementDecision getMovementDecision() {
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
                (distanceToTravel > LXXUtils.getStopDistance(robot.getSpeed()) + Rules.MAX_VELOCITY ||
                        (duelOpponent != null && tomcatEyes.isRammingNow(duelOpponent)))
                        ? 8
                        : 0;

        return getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), opponent, desiredSpeed, lxxBullets);
    }

    private boolean needToReselectOrbitDirection(List<LXXBullet> bullets) {
        return prevPrediction == null ||
                isBulletsUpdated(bullets) ||
                (duelOpponent != null && signum(duelOpponent.getAcceleration()) != prevPrediction.enemyAccelSign) ||
                (duelOpponent != null && duelOpponent.aDistance(robot) < prevPrediction.distanceBetween - 25);
    }

    private boolean isBulletsUpdated(List<LXXBullet> newBullets) {
        return (newBullets.get(0).getAimPredictionData()).getPredictionTime() !=
                prevPrediction.firstBulletPredictionTime;
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        clockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE);
        counterClockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
        final int cmp = (int) signum(clockwisePrediction.minDanger.danger * (prevPrediction != null && prevPrediction.orbitDirection == OrbitDirection.CLOCKWISE ? 0.9 : 1)
                -
                counterClockwisePrediction.minDanger.danger * (prevPrediction != null && prevPrediction.orbitDirection == OrbitDirection.COUNTER_CLOCKWISE ? 0.9 : 1));
        if (cmp < 0) {
            setMovementParameters(clockwisePrediction);
        } else if (cmp > 0) {
            setMovementParameters(counterClockwisePrediction);
        } else if (prevPrediction != null && prevPrediction.orbitDirection == OrbitDirection.CLOCKWISE) {
            setMovementParameters(clockwisePrediction);
        } else {
            setMovementParameters(counterClockwisePrediction);
        }
    }

    private void setMovementParameters(MovementDirectionPrediction movementDirectionPrediction) {
        distanceToTravel = movementDirectionPrediction.distToMinDangerPoint;
        minDangerOrbitDirection = movementDirectionPrediction.orbitDirection;
        prevPrediction = movementDirectionPrediction;
    }

    private MovementDirectionPrediction predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection) {
        final MovementDirectionPrediction prediction = new MovementDirectionPrediction();
        prediction.enemyPos = duelOpponent != null ? duelOpponent.getPosition() : null;
        prediction.bullets = lxxBullets;
        prediction.firstBulletPredictionTime = lxxBullets.get(0).getAimPredictionData().getPredictionTime();
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
                : LXXUtils.anglesDiff(duelOpponent.angleTo(robot), robot.getHeadingRadians());
        if (enemyAttackAngle > LXXConstants.RADIANS_90) {
            enemyAttackAngle = abs(enemyAttackAngle - LXXConstants.RADIANS_180);
        }
        return new PointDanger(firstWaveDng, secondWaveDng, distToEnemy, battleField.center.aDistance(robot),
                LXXConstants.RADIANS_90 - enemyAttackAngle);
    }

    private PointDangerOnWave getWaveDanger(APoint pnt, LXXBullet bullet) {
        final double bearingOffset = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), pnt);
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), pnt);

        double bulletsDanger = 0;
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        for (PastBearingOffset bo : aimPredictionData.getPredictedBearingOffsets()) {
            final double dist = abs(bearingOffset - bo.bearingOffset);
            if (dist < robotWidthInRadians * 0.75) {
                bulletsDanger += (2 - (dist / (robotWidthInRadians * 0.75))) * bo.danger;
            } else if (dist < robotWidthInRadians * 2.55) {
                bulletsDanger += (1 - (dist / (robotWidthInRadians * 2.55))) * bo.danger;
            }
        }

        return new PointDangerOnWave(robotWidthInRadians, bulletsDanger);
    }

    private List<LXXBullet> getBullets() {
        List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(2);
        if (bulletsOnAir.size() < 2 && duelOpponent != null) {
            bulletsOnAir.add(enemyBulletManager.createFutureBullet(duelOpponent));
        }
        if (bulletsOnAir.size() == 0) {
            bulletsOnAir = enemyBulletManager.getBulletsOnAir(0);
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
            final MovementDecision md = getMovementDecision(surfPoint, orbitDirection, robotImg, opponentImg, 8, bullets);
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(Rules.MAX_VELOCITY * signum(opponentImg.getVelocity()), 0));
            }
            robotImg.apply(md);
            points.add(new WSPoint(robotImg, getPointDanger(bullets, robotImg, opponentImg)));
            time++;
        }

        return points;
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, LXXRobotState opponent, double desiredSpeed, List<LXXBullet> bulletsOnAir) {
        double desiredHeading = distanceController.getDesiredHeading(surfPoint, robot, orbitDirection, bulletsOnAir);
        desiredHeading = battleField.smoothWalls(robot, desiredHeading, orbitDirection == OrbitDirection.CLOCKWISE);

        double direction = robot.getAbsoluteHeadingRadians();
        if (LXXUtils.anglesDiff(direction, desiredHeading) > LXXConstants.RADIANS_90) {
            direction = Utils.normalAbsoluteAngle(direction + LXXConstants.RADIANS_180);
        }
        if (opponent != null &&
                ((LXXUtils.anglesDiff(direction, robot.angleTo(opponent)) < LXXUtils.getRobotWidthInRadians(robot, opponent) * 1.1) ||
                        LXXUtils.getBoundingRectangleAt(robot.project(direction, desiredSpeed), LXXConstants.ROBOT_SIDE_SIZE / 2 - 2).intersects(LXXUtils.getBoundingRectangleAt(opponent)))) {
            desiredSpeed = 0;
        }

        return MovementDecision.toMovementDecision(robot, desiredSpeed, desiredHeading);
    }

    public void paint(LXXGraphics g) {
        if (prevPrediction == null) {
            return;
        }

        drawPath(g, clockwisePrediction.points, new Color(0, 255, 0, 200));
        drawPath(g, counterClockwisePrediction.points, new Color(255, 0, 0, 200));

        g.setColor(new Color(0, 255, 0, 200));
        g.fillCircle(prevPrediction.minDangerPoint, 15);
    }

    private void drawPath(LXXGraphics g, List<WSPoint> points, Color color) {
        g.setColor(color);
        for (WSPoint pnt : points) {
            g.fillCircle(pnt, 3);
        }
    }

    private class WSPoint extends LXXPoint {

        private final PointDanger danger;

        private WSPoint(APoint point, PointDanger danger) {
            super(point);
            this.danger = danger;
        }
    }

    public enum OrbitDirection {

        CLOCKWISE(1),
        COUNTER_CLOCKWISE(-1);

        public final int sign;

        OrbitDirection(int sign) {
            this.sign = sign;
        }
    }

    public class MovementDirectionPrediction {

        private PointDanger MAX_POINT_DANGER = new PointDanger(new PointDangerOnWave(LXXConstants.RADIANS_90, 100),
                new PointDangerOnWave(LXXConstants.RADIANS_90, 100), 0, 1000, LXXConstants.RADIANS_90);

        private PointDanger minDanger = MAX_POINT_DANGER;
        private APoint minDangerPoint;
        private double distToMinDangerPoint;
        private OrbitDirection orbitDirection;
        public LXXPoint enemyPos;
        public List<LXXBullet> bullets;
        public List<WSPoint> points;
        public double enemyAccelSign;
        public double distanceBetween;
        public long firstBulletPredictionTime;
    }

    private class PointDanger {

        public final PointDangerOnWave dangerOnFirstWave;
        public final PointDangerOnWave dangerOnSecondWave;
        public final double distToEnemy;
        public final double distanceToCenter;
        public final double enemyAttackAngle;
        public final double danger;

        private PointDanger(PointDangerOnWave dangerOnFirstWave, PointDangerOnWave dangerOnSecondWave, double distToEnemy, double distanceToWall,
                            double enemyAttackAngle) {
            this.dangerOnFirstWave = dangerOnFirstWave;
            this.dangerOnSecondWave = dangerOnSecondWave;
            this.distToEnemy = distToEnemy;
            this.distanceToCenter = distanceToWall;
            this.enemyAttackAngle = enemyAttackAngle;

            this.danger = dangerOnFirstWave.bulletsDanger * 120 +
                    (dangerOnSecondWave != null ? dangerOnSecondWave.bulletsDanger : 0) * 10 +
                    distanceToCenter / 800 * 3 +
                    enemyAttackAngle * 2 +
                    max(0, (200 - distToEnemy));
        }

        @Override
        public String toString() {
            return String.format("PointDanger (%s #1, %s #2, %3.3f, %3.3f)", dangerOnFirstWave, dangerOnSecondWave, distToEnemy, distanceToCenter);
        }
    }

    private static class PointDangerOnWave {

        public final double robotWidthInRadians;
        public final double bulletsDanger;

        public PointDangerOnWave(double robotWidthInRadians, double bulletsDanger) {
            this.robotWidthInRadians = robotWidthInRadians;
            this.bulletsDanger = bulletsDanger;
        }

        @Override
        public String toString() {
            return String.format("PointDangerOnWave (%3.3f, %3.3f)",
                    Math.toDegrees(robotWidthInRadians), bulletsDanger);
        }
    }

}
