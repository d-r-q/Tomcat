/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.AbstractGFAimingPredictionData;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
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

public class WaveSurfingMovement implements Movement, Painter {

    private final List<OrbitDirection> cmps = new ArrayList<OrbitDirection>();

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final DistanceController distanceController;

    private OrbitDirection minDangerOrbitDirection = OrbitDirection.CLOCKWISE;
    private double distanceToTravel;
    private Target duelOpponent;
    private MovementDirectionPrediction prevPrediction;
    private BattleField battleField;

    public WaveSurfingMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();

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
        final double desiredSpeed = distanceToTravel > LXXUtils.getStopDistance(robot.getSpeed()) ? 8 : 0;

        return getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), desiredSpeed, lxxBullets);
    }

    private boolean needToReselectOrbitDirection(List<LXXBullet> bullets) {
        //return true;
        return prevPrediction == null ||
                !prevPrediction.bullet.equals(bullets.get(0)) ||
                (duelOpponent != null && signum(duelOpponent.getAcceleration()) != prevPrediction.enemyAccelSign) ||
                (duelOpponent != null && duelOpponent.aDistance(robot) < prevPrediction.distanceBetween - 25);
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        final MovementDirectionPrediction clockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE);
        final MovementDirectionPrediction counterClockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
        if (abs(clockwisePrediction.minDanger - counterClockwisePrediction.minDanger) < min(clockwisePrediction.minDanger, counterClockwisePrediction.minDanger) * 0.02) {
            if (minDangerOrbitDirection == OrbitDirection.CLOCKWISE) {
                setMovementParameters(clockwisePrediction);
            } else {
                setMovementParameters(counterClockwisePrediction);
            }
        } else if (clockwisePrediction.minDanger < counterClockwisePrediction.minDanger) {
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
        prediction.orbitDirection = orbitDirection;
        double distance = 0;
        LXXPoint prevPoint = robot.getPosition();
        prediction.points = generatePoints(orbitDirection, lxxBullets, duelOpponent);
        prediction.bullet = lxxBullets.get(0);
        prediction.enemyAccelSign = duelOpponent != null ? signum(duelOpponent.getAcceleration()) : 0;
        prediction.distanceBetween = duelOpponent != null ? duelOpponent.aDistance(robot) : 0;
        for (LXXPoint pnt : prediction.points) {
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
            final AbstractGFAimingPredictionData aimingPredictionData = lxxBullet != null ? (AbstractGFAimingPredictionData) lxxBullet.getAimPredictionData() : null;

            double bulletDanger = 0;
            if (aimingPredictionData != null) {
                bulletDanger = aimingPredictionData.getDangerExt(lxxBullet.getBearingOffsetRadians(pnt),
                        LXXUtils.getRobotWidthInRadians(lxxBullet.getFirePosition(), pnt));
            }

            totalDanger += round(bulletDanger) * 100 * weight;
            weight /= 20;
        }
        final Target opponent = duelOpponent;
        if (opponent != null) {
            totalDanger += getPointDanger(pnt, opponent);
        }
        return totalDanger;
    }

    protected double getPointDanger(APoint pnt, Target opponent) {
        double danger = 0;

        final double distanceToCenterDanger = round(pnt.aDistance(robot.battleField.center) / 10);
        danger += distanceToCenterDanger;
        final double distanceEnemyDanger = round(distanceController.getPreferredDistance() / pnt.aDistance(opponent) * 10);
        danger += distanceEnemyDanger;

        return danger;
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

    private List<LXXPoint> generatePoints(OrbitDirection orbitDirection, List<LXXBullet> bullets, Target enemy) {
        final LXXBullet bullet = bullets.get(0);
        final List<LXXPoint> points = new LinkedList<LXXPoint>();

        final RobotImage robotImg = new RobotImage(robot.getPosition(), robot.getVelocity(), robot.getHeadingRadians(), robot.battleField, 0, robot.getEnergy());
        final RobotImage opponentImg = enemy == null ? null : new RobotImage(enemy.getPosition(), enemy.getVelocity(), enemy.getState().getHeadingRadians(), robot.battleField, 0,
                enemy.getEnergy());
        points.add(new LXXPoint(robotImg));
        int time = 0;
        final APoint surfPoint = getSurfPoint(opponentImg, bullet);
        final double travelledDistance = bullet.getTravelledDistance();
        final APoint firePosition = bullet.getFirePosition();
        while (firePosition.aDistance(robotImg) - travelledDistance > bullet.getSpeed() * time ||
                points.size() < 2) {
            final MovementDecision md = getMovementDecision(surfPoint, orbitDirection, robotImg, Rules.MAX_VELOCITY, bullets);
            if (opponentImg != null) {
                opponentImg.apply(new MovementDecision(Rules.MAX_VELOCITY * signum(opponentImg.getVelocity()), 0));
            }
            robotImg.apply(md);
            points.add(new LXXPoint(robotImg));
            time++;
        }

        return points;
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, double desiredSpeed, List<LXXBullet> bulletsOnAir) {
        double desiredHeading = distanceController.getDesiredHeading(surfPoint, robot, orbitDirection, bulletsOnAir);
        //desiredHeading = battleField.smoothWalls(robot, desiredHeading, orbitDirection == OrbitDirection.CLOCKWISE);
        double smoothedHeadingCW = battleField.smoothWalls(robot, desiredHeading, true);
        double smoothedHeadingCCW = battleField.smoothWalls(robot, desiredHeading, false);
        if (LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCW) <
                LXXUtils.anglesDiff(desiredHeading, smoothedHeadingCCW)) {
            desiredHeading = smoothedHeadingCW;
        } else {
            desiredHeading = smoothedHeadingCCW;
        }

        return MovementDecision.toMovementDecision(robot, desiredSpeed, desiredHeading);
    }

    public void paint(LXXGraphics g) {
        if (prevPrediction == null) {
            return;
        }

        g.setColor(Color.GREEN);
        for (LXXPoint pnt : prevPrediction.points) {
            g.fillCircle(pnt, 3);
        }

        g.drawCircle(prevPrediction.minDangerPoint, 5);
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

        private double minDanger = Integer.MAX_VALUE;
        private LXXPoint minDangerPoint;
        private double distToMinDangerPoint;
        private OrbitDirection orbitDirection;

        public List<LXXPoint> points;
        public LXXBullet bullet;
        public double enemyAccelSign;
        public double distanceBetween;
    }
}
