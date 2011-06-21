/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.bullets.enemy.GFAimingPredictionData;
import lxx.office.Office;
import lxx.paint.LXXGraphics;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;

public class WaveSurfingMovement implements Movement {

    protected static final int DEFAULT_DISTANCE_AGAINST_SIMPLE = 400;

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

        distanceController = new DistanceController(office.getRobot(), office.getEnemyBulletManager(), office.getTargetManager());
    }

    public MovementDecision getMovementDecision() {
        duelOpponent = targetManager.getDuelOpponent();
        final List<LXXBullet> lxxBullets = getBullets();
        final Target.TargetState opponent = duelOpponent == null ? null : duelOpponent.getState();
        final APoint surfPoint = getSurfPoint(opponent, lxxBullets);
        selectOrbitDirection(lxxBullets);

        return getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), timeToTravel > 1 ? distanceToTravel / (timeToTravel - 1) : 8);
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        final MovementDirectionPrediction clockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE);
        final MovementDirectionPrediction counterClockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE);
        final LXXBullet bullet = lxxBullets.get(0);
        if (abs(clockwisePrediction.minDanger - counterClockwisePrediction.minDanger) < min(clockwisePrediction.minDanger, counterClockwisePrediction.minDanger) * 0.02) {
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
        for (LXXPoint pnt : generatePoints(orbitDirection, lxxBullets, duelOpponent)) {
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
            final GFAimingPredictionData GFAimingPredictionData = lxxBullet != null ? (GFAimingPredictionData) lxxBullet.getAimPredictionData() : null;

            double bulletDanger = 0;
            if (GFAimingPredictionData != null) {
                bulletDanger = GFAimingPredictionData.getDangerExt(lxxBullet.getBearingOffsetRadians(pnt),
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
        final double distanceEnemyDanger = round(DEFAULT_DISTANCE_AGAINST_SIMPLE / pnt.aDistance(opponent) * 10);
        danger += distanceEnemyDanger;

        return danger;
    }

    private List<LXXBullet> getBullets() {
        final List<LXXBullet> bullets = new ArrayList<LXXBullet>();
        final List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(2);
        for (int i = 0; i < 2; i++) {
            if (bulletsOnAir.size() == i) {
                break;
            }
            bullets.add(bulletsOnAir.get(i));
        }

        final Target duelOpponent = this.duelOpponent;
        if (bullets.size() < 2 && duelOpponent != null) {
            bullets.add(enemyBulletManager.createSafeBullet(duelOpponent));
        }

        return bullets;
    }

    private APoint getSurfPoint(LXXRobotState duelOpponent, List<LXXBullet> bullets) {
        if (duelOpponent == null) {
            return bullets.get(0).getFirePosition();
        }

        return duelOpponent;
    }

    private List<LXXPoint> generatePoints(OrbitDirection orbitDirection, List<LXXBullet> bullets, Target enemy) {
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
        final LXXBullet bullet = bullets.get(0);
        int time = 0;
        while (bullet.getFirePosition().aDistance(robotImg) - bullet.getTravelledDistance() > bullet.getSpeed() * time) {
            final MovementDecision md = getMovementDecision(getSurfPoint(opponentImg, bullets), orbitDirection, robotImg, 8);
            robotImg.apply(md);
            points.add(new LXXPoint(robotImg));
            time++;
            g.fillCircle(robotImg, 3);
        }

        return points;
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, double desiredSpeed) {
        final double desiredHeading = distanceController.getDesiredHeading(surfPoint, robot, orbitDirection);
        final double smoothedHeading = robot.getBattleField().smoothWalls(robot, desiredHeading, orbitDirection == OrbitDirection.CLOCKWISE);

        return MovementDecision.toMovementDecision(robot, desiredSpeed, smoothedHeading);
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

    }

}
