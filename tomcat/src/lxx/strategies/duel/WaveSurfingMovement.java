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

        final double dst = robot.aDistance(prevPrediction.minDangerPoint);
        if (dst < LXXConstants.ROBOT_SIDE_SIZE / 4) {
            return new MovementDecision(0, 0);
        }
        final double desiredSpeed =
                (dst > LXXUtils.getStopDistance(robot.getSpeed()) ||
                        (duelOpponent != null && tomcatEyes.isRammingNow(duelOpponent)))
                        ? 8
                        : 0;

        return getMovementDecision(robot.angleTo(prevPrediction.minDangerPoint), prevPrediction.wallSmoothDir, robot.getState(), desiredSpeed);
    }

    private boolean needToReselectOrbitDirection(List<LXXBullet> bullets) {
        return true;
        /*return prevPrediction == null ||
                !prevPrediction.bullet.equals(bullets.get(0)) ||
                (duelOpponent != null && signum(duelOpponent.getVelocity()) != prevPrediction.enemyVelocitySign) ||
                robot.aDistance(prevPrediction.minDangerPoint) < LXXUtils.getStopDistance(robot.getSpeed()) + Rules.MAX_VELOCITY;*/
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        preds = new ArrayList<MovementDirectionPrediction>();
        for (int velocity = -8; velocity <= 8; velocity += 16) {
            for (int turnDirection = -1; turnDirection <= 1; turnDirection += 2) {
                for (int i = 0; i <= 1; i++) {
                    preds.add(predictMovementInDirection(lxxBullets, velocity, turnDirection, i == 0));
                }
            }
        }

        MovementDirectionPrediction bestDir = null;
        for (MovementDirectionPrediction pred : preds) {
            if (bestDir == null || pred.minDangerPoint.danger.danger < bestDir.minDangerPoint.danger.danger) {
                bestDir = pred;
            }
        }
        setMovementParameters(bestDir);
    }

    private void setMovementParameters(MovementDirectionPrediction movementDirectionPrediction) {
        distanceToTravel = movementDirectionPrediction.distToMinDangerPoint;
        minDangerOrbitDirection = movementDirectionPrediction.orbitDirection;
        prevPrediction = movementDirectionPrediction;
    }

    private MovementDirectionPrediction predictMovementInDirection(List<LXXBullet> lxxBullets, double desiredVelocity, double turnSign, boolean wallSmoothDirection) {
        final MovementDirectionPrediction prediction = new MovementDirectionPrediction();
        prediction.enemyPos = duelOpponent != null ? duelOpponent.getPosition() : null;
        prediction.bullet = lxxBullets.get(0);
        prediction.wallSmoothDir = wallSmoothDirection;
        double distance = 0;
        APoint prevPoint = robot.getPosition();
        prediction.points = generatePoints(desiredVelocity, turnSign, wallSmoothDirection, lxxBullets, duelOpponent);
        prediction.enemyVelocitySign = duelOpponent != null ? signum(duelOpponent.getVelocity()) : 0;
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
        double attackAngle = duelOpponent != null ? LXXUtils.anglesDiff(duelOpponent.angleTo(robot), robot.getAbsoluteHeadingRadians()) : LXXConstants.RADIANS_90;
        if (attackAngle > LXXConstants.RADIANS_90) {
            attackAngle = (LXXConstants.RADIANS_180 - attackAngle) * 0.9;
        }
        return new PointDanger(firstWaveDng, secondWaveDng, abs(distToEnemy - preferredDistance), battleField.center.aDistance(robot), attackAngle);
    }

    private PointDangerOnWave getWaveDanger(APoint pnt, LXXBullet bullet) {
        final double bearingOffset = bullet.getBearingOffsetRadians(pnt);
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), pnt);

        double bulletsDanger = 0;
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        for (PastBearingOffset bo : aimPredictionData.getPredictedBearingOffsets()) {
            final double dist = abs(bearingOffset - bo.bearingOffset) * abs(bearingOffset - bo.bearingOffset);
            if (dist < robotWidthInRadians * 0.75) {
                bulletsDanger += (2 - (dist / (robotWidthInRadians * 0.45))) * bo.danger;
            } else if (dist < robotWidthInRadians * 2.55) {
                bulletsDanger += (1 - (dist / (robotWidthInRadians * 1.55))) * bo.danger;
            }
        }

        return new PointDangerOnWave(robotWidthInRadians, bulletsDanger);
    }

    private List<LXXBullet> getBullets() {
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(2);
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

    private List<WSPoint> generatePoints(double desiredVelocity, double turnSign, boolean wallSmoothDirection, List<LXXBullet> bullets, Target enemy) {
        final LXXBullet bullet = bullets.get(0);
        final List<WSPoint> points = new LinkedList<WSPoint>();

        final RobotImage robotImg = new RobotImage(robot.getState());
        final RobotImage opponentImg = enemy == null ? null : new RobotImage(enemy.getState());
        int time = 0;
        final double travelledDistance = bullet.getTravelledDistance();
        final APoint firePosition = bullet.getFirePosition();
        while (firePosition.aDistance(robotImg) - travelledDistance > bullet.getSpeed() * time || points.size() < 2) {
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(Rules.MAX_VELOCITY * signum(opponentImg.getVelocity()), 0));
            }

            final double maxTurnRate = Rules.getTurnRateRadians(robotImg.getSpeed());
            final double turnStep = (maxTurnRate - LXXConstants.RADIANS_1) / 3;

            WSPoint bestPoint = null;
            MovementDecision bestDecision = null;
            for (double turnRate = LXXConstants.RADIANS_1; turnRate <= maxTurnRate + LXXConstants.RADIANS_0_1; turnRate += turnStep) {
                final RobotImage tmpImg = new RobotImage(robotImg);
                double desiredHeading = Utils.normalAbsoluteAngle(robotImg.getHeadingRadians() + turnRate * turnSign);
                if (desiredVelocity < 0) {
                    desiredHeading = Utils.normalAbsoluteAngle(desiredHeading + LXXConstants.RADIANS_180);
                }
                boolean stop = false;
                if (opponentImg != null && (LXXUtils.anglesDiff(desiredHeading, robotImg.angleTo(opponentImg)) <
                        LXXUtils.getRobotWidthInRadians(robotImg, opponentImg))) {
                    stop = true;
                }
                final MovementDecision tmpMd = getMovementDecision(desiredHeading, wallSmoothDirection, tmpImg, abs(stop ? 0 : desiredVelocity));
                tmpImg.apply(tmpMd);

                final WSPoint tmpPoint = new WSPoint(tmpImg, getPointDanger(bullets, tmpImg, opponentImg));
                if (bestPoint == null || tmpPoint.danger.danger < bestPoint.danger.danger) {
                    bestPoint = tmpPoint;
                    bestDecision = tmpMd;
                }
            }

            if (bestDecision == null) {
                bestDecision = new MovementDecision(0, 0);
            }
            robotImg.apply(bestDecision);
            points.add(new WSPoint(robotImg, getPointDanger(bullets, robotImg, opponentImg)));
            time++;
        }

        return points;
    }

    private MovementDecision getMovementDecision(double desiredHeading, boolean wallSmoothDirection,
                                                 LXXRobotState robot, double desiredSpeed) {
        final double smoothedHeading = battleField.smoothWalls(robot, desiredHeading, wallSmoothDirection);
        return MovementDecision.toMovementDecision(robot, desiredSpeed, smoothedHeading);
    }

    public void paint(LXXGraphics g) {
        if (prevPrediction == null) {
            return;
        }

        g.setColor(Color.GREEN);
        for (WSPoint pnt : prevPrediction.points) {
            g.fillCircle(pnt, 3);
        }
        robot.getLXXGraphics().setColor(new Color(255, 255, 255, 155));

        for (MovementDirectionPrediction pred : preds) {
            for (WSPoint pnt : pred.points) {
                robot.getLXXGraphics().drawCircle(pnt, 5);
            }
        }

        robot.getLXXGraphics().setColor(new Color(255, 0, 0, 155));
        g.fillCircle(prevPrediction.minDangerPoint, 15);
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
                new PointDangerOnWave(LXXConstants.RADIANS_90, 100), 0, 1000, 0);

        private PointDanger minDanger = MAX_POINT_DANGER;
        private WSPoint minDangerPoint;
        private double distToMinDangerPoint;
        private OrbitDirection orbitDirection;
        public LXXPoint enemyPos;
        public LXXBullet bullet;
        public List<WSPoint> points;
        public double enemyVelocitySign;
        public double distanceBetween;
        public boolean wallSmoothDir;
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

            this.danger = dangerOnFirstWave.bulletsDanger * 55 +
                    (dangerOnSecondWave != null ? dangerOnSecondWave.bulletsDanger : 0) * 10 +
                    abs(distToEnemyDiff) / preferredDistance * 18 +
                    distanceToCenter / 800 * 18 +
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
