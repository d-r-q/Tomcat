/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobot;
import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.bullets.enemy.EnemyBulletsPredictionData;
import lxx.office.Office;
import lxx.paint.LXXGraphics;
import lxx.paint.Painter;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.*;
import lxx.utils.simulator.RobocodeDuelSimulator;
import lxx.utils.simulator.RobotProxy;
import robocode.Rules;

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
    private long timeToTravel;
    private Target duelOpponent;
    private MovementDirectionPrediction prevPrediction;
    private BattleField battleField;
    private double preferredDistance;

    public WaveSurfingMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();
        this.tomcatEyes = tomcatEyes;

        distanceController = new DistanceController(office.getRobot().getGunCoolingRate());
        battleField = robot.getState().getBattleField();
    }

    public MovementDecision getMovementDecision() {
        preferredDistance = distanceController.getPreferredDistance();
        duelOpponent = targetManager.getDuelOpponent();
        final List<LXXBullet> lxxBullets = getBullets();
        if (needToReselectOrbitDirection(lxxBullets)) {
            selectOrbitDirection(lxxBullets);
        } else {
            distanceToTravel -= robot.getVelocity();
            timeToTravel--;
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
        final double desiredSpeed = timeToTravel > 0 ? distanceToTravel / timeToTravel : 8;

        return getMovementDecision(surfPoint, minDangerOrbitDirection, robot, duelOpponent, lxxBullets, desiredSpeed);
    }

    private boolean needToReselectOrbitDirection(List<LXXBullet> bullets) {
        return true;
        /*return prevPrediction == null ||
                !prevPrediction.bullet.equals(bullets.get(0)) ||
                bullets.get(0).getFlightTime(robot) < 2 ||
                duelOpponent != null && duelOpponent.aDistance(prevPrediction.enemyPos) > 50 ||
                duelOpponent != null && duelOpponent.aDistance(robot) < 100;*/

    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        final MovementDirectionPrediction clockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE);
        final MovementDirectionPrediction counterClockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
        final LXXBullet bullet = lxxBullets.get(0);
        final int cmp = clockwisePrediction.minDanger.compareTo(counterClockwisePrediction.minDanger);
        if (cmp < 0) {
            setMovementParameters(clockwisePrediction, bullet);
        } else if (cmp > 0) {
            setMovementParameters(counterClockwisePrediction, bullet);
        } else if (prevPrediction != null && prevPrediction.orbitDirection == OrbitDirection.CLOCKWISE) {
            setMovementParameters(clockwisePrediction, bullet);
        } else {
            setMovementParameters(counterClockwisePrediction, bullet);
        }
    }

    private void setMovementParameters(MovementDirectionPrediction movementDirectionPrediction, LXXBullet bullet) {
        distanceToTravel = movementDirectionPrediction.distToMinDangerPoint;
        final double speed = bullet.getSpeed() < LXXConstants.MIN_BULLET_SPEED ? Integer.MAX_VALUE : bullet.getSpeed();
        timeToTravel = round((bullet.getFirePosition().aDistance(movementDirectionPrediction.minDangerPoint) - bullet.getTravelledDistance()) / speed);
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
        for (WSPoint pnt : prediction.points) {
            distance += prevPoint.aDistance(pnt);

            if (pnt.danger.compareTo(prediction.minDanger) <= 0) {
                prediction.minDanger = pnt.danger;
                prediction.distToMinDangerPoint = distance;
                prediction.minDangerPoint = pnt;
            }
            prevPoint = pnt;
        }

        return prediction;
    }

    private PointDanger getPointDanger(List<LXXBullet> lxxBullets, APoint pnt, LXXRobotState duelOpponent) {
        final int bulletsSize = lxxBullets.size();
        final PointDangerOnWave firstWaveDng = bulletsSize == 0 ? null : getWaveDanger(pnt, lxxBullets.get(0));
        final PointDangerOnWave secondWaveDng = bulletsSize == 1 ? null : getWaveDanger(pnt, lxxBullets.get(1));
        final double distToEnemy = duelOpponent != null ? pnt.aDistance(duelOpponent) : 0;
        return new PointDanger(firstWaveDng, secondWaveDng, abs(distToEnemy - preferredDistance), battleField.center.aDistance(pnt));
    }

    private PointDangerOnWave getWaveDanger(APoint pnt, LXXBullet bullet) {
        final double bearingOffset = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), pnt);
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), pnt);

        double bulletsCount = 0;
        for (Double bo : ((EnemyBulletsPredictionData) bullet.getAimPredictionData()).getPredictedBearingOffsets()) {
            final double dist = abs(bearingOffset - bo);
            double closeRange = robotWidthInRadians * 0.51;
            if (dist < closeRange) {
                bulletsCount++;
            } else if (dist < robotWidthInRadians * 3) {
                bulletsCount += 1 - 1 * (dist - closeRange) / (robotWidthInRadians * 3 - closeRange);
            }
        }

        return new PointDangerOnWave(robotWidthInRadians, bulletsCount);
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

        final RobocodeDuelSimulator simulator = new RobocodeDuelSimulator(enemy, robot, robot.getTime(), robot.getRoundNum(),
                new Attribute[0], new ArrayList<LXXBullet>(), bullets);
        int time = 0;
        final APoint surfPoint = getSurfPoint(enemy != null ? enemy.getState() : null, bullet);
        final double travelledDistance = bullet.getTravelledDistance();
        final APoint firePosition = bullet.getFirePosition();
        final LXXRobot myProxy = simulator.getMyProxy();
        final RobotProxy enemyProxy = simulator.getEnemyProxy();
        double enemyDesiredSpeed = 0;
        if (enemyProxy != null) {
            if (enemyProxy.getAcceleration() >= 0) {
                enemyDesiredSpeed = Rules.MAX_VELOCITY * signum(enemyProxy.getState().getVelocity());
            } else {
                enemyDesiredSpeed = -Rules.MAX_VELOCITY * signum(enemyProxy.getState().getVelocity());
            }
        }
        while (firePosition.aDistance(myProxy.getState()) - travelledDistance > bullet.getSpeed() * time) {
            final MovementDecision md = getMovementDecision(surfPoint, orbitDirection, myProxy,
                    enemyProxy, simulator.getEnemyBullets(), 8);
            if (enemyProxy != null) {
                simulator.setEnemyMovementDecision(new MovementDecision(enemyDesiredSpeed, 0));
            }
            simulator.setMyMovementDecision(md);
            simulator.doTurn();
            points.add(new WSPoint(myProxy, getPointDanger(bullets, myProxy, enemyProxy == null ? null : enemyProxy.getState())));
            time++;
        }

        return points;
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobot robot, LXXRobot enemy, List<LXXBullet> bullets, double desiredSpeed) {
        double desiredHeading = distanceController.getDesiredHeading(surfPoint, robot, enemy, orbitDirection, bullets);
        //desiredHeading = battleField.smoothWalls(robot.getState(), desiredHeading, orbitDirection == OrbitDirection.CLOCKWISE);
        double smoothedHeadingCW = battleField.smoothWalls(robot.getState(), desiredHeading, true);
        double smoothedHeadingCCW = battleField.smoothWalls(robot.getState(), desiredHeading, false);
        if (LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCW) <
                LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCCW)) {
            desiredHeading = smoothedHeadingCW;
        } else {
            desiredHeading = smoothedHeadingCCW;
        }

        return MovementDecision.toMovementDecision(robot.getState(), desiredSpeed, desiredHeading);
    }

    public void paint(LXXGraphics g) {
        if (prevPrediction == null) {
            return;
        }

        g.setColor(Color.GREEN);
        for (WSPoint pnt : prevPrediction.points) {
            g.fillCircle(pnt, 3);
        }

        g.drawCircle(prevPrediction.minDangerPoint, 5);
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
                new PointDangerOnWave(LXXConstants.RADIANS_90, 100), 0, 1000);

        private PointDanger minDanger = MAX_POINT_DANGER;
        private APoint minDangerPoint;
        private double distToMinDangerPoint;
        private OrbitDirection orbitDirection;
        public LXXPoint enemyPos;
        public LXXBullet bullet;
        public List<WSPoint> points;
    }

    private class PointDanger implements Comparable<PointDanger> {

        public final PointDangerOnWave dangerOnFirstWave;
        public final PointDangerOnWave dangerOnSecondWave;
        public final double distToEnemyDiff;
        public final double distanceToCenter;

        private PointDanger(PointDangerOnWave dangerOnFirstWave, PointDangerOnWave dangerOnSecondWave, double distToEnemy, double distanceToWall) {
            this.dangerOnFirstWave = dangerOnFirstWave;
            this.dangerOnSecondWave = dangerOnSecondWave;
            this.distToEnemyDiff = distToEnemy;
            this.distanceToCenter = distanceToWall;
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
                res = compareDoubles(thisDng, anotherDng, 0.3);
            }

            return res;
        }

        @Override
        public String toString() {
            return String.format("PointDanger (%s #1, %s #2, %3.3f, %3.3f)", dangerOnFirstWave, dangerOnSecondWave, distToEnemyDiff, distanceToCenter);
        }
    }

    private static class PointDangerOnWave implements Comparable<PointDangerOnWave> {

        //public final double minDistToBulletRadians;
        public final double robotWidthInRadians;
        public final double bulletsCount;

        public PointDangerOnWave(double robotWidthInRadians, double bulletsCount) {
            this.robotWidthInRadians = robotWidthInRadians;
            this.bulletsCount = bulletsCount;
        }

        public int compareTo(PointDangerOnWave o) {
            return compareDoubles(bulletsCount, o.bulletsCount, 0.05);
        }

        @Override
        public String toString() {
            return String.format("PointDangerOnWave (%3.3f, %3.3f)",
                    Math.toDegrees(robotWidthInRadians), bulletsCount);
        }
    }


    public static int compareDoubles(double d1, double d2, double threshold) {
        final double diff = d1 - d2;
        return abs(diff) < max(abs(d1), abs(d2)) * threshold ? 0 : (int) signum(diff);
    }
}
