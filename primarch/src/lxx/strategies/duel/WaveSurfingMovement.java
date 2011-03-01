/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Primarch;
import lxx.enemy_bullets.EnemyAimingPredictionData;
import lxx.office.EnemyBulletManager;
import lxx.office.TargetManager;
import lxx.strategies.Movement;
import lxx.strategies.MovementDecision;
import lxx.strategies.duel.point_selecting.PointsSelector;
import lxx.targeting.Target;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.abs;

public class WaveSurfingMovement implements Movement {

    private final PointsSelector pointsSelector = new PointsSelector();

    private final Primarch robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;

    private double minDanger;
    private OrbitDirection minDangerOrbitDirection;

    public WaveSurfingMovement(Primarch robot, TargetManager targetManager, EnemyBulletManager enemyBulletManager) {
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
        final LXXBullet lxxBullet = getBullet();

        minDanger = Integer.MAX_VALUE;

        checkPointsInDirection(surfPoint, lxxBullet, OrbitDirection.CLOCKWISE);
        checkPointsInDirection(surfPoint, lxxBullet, OrbitDirection.STOP);
        // todo(zhidkov): reuse stop points
        checkPointsInDirection(surfPoint, lxxBullet, OrbitDirection.COUNTER_CLOCKWISE);
    }

    private void checkPointsInDirection(APoint surfPoint, LXXBullet lxxBullet, OrbitDirection orbitDirection) {
        for (LXXPoint pnt : generatePoints(orbitDirection, lxxBullet, surfPoint, targetManager.getDuelOpponent())) {
            double danger = getPointDanger(lxxBullet, pnt);

            if (danger < minDanger) {
                minDanger = danger;
                minDangerOrbitDirection = orbitDirection;
            }
        }
    }

    private double getPointDanger(LXXBullet lxxBullet, LXXPoint pnt) {
        final Target duelOpponent = targetManager.getDuelOpponent();
        final EnemyAimingPredictionData enemyAimingPredictionData = lxxBullet != null ? (EnemyAimingPredictionData) lxxBullet.getAimPredictionData() : null;
        double distanceDanger;
        if (duelOpponent != null) {
            distanceDanger = 100 / pnt.aDistance(duelOpponent);
        } else {
            distanceDanger = 0;
        }

        double bulletDanger;
        if (enemyAimingPredictionData != null) {
            bulletDanger = enemyAimingPredictionData.getDanger(LXXUtils.bearingOffset(lxxBullet.getFirePosition(), lxxBullet.getWave().getTargetStateAtFireTime(), pnt));
        } else {
            bulletDanger = 0;
        }

        return bulletDanger + distanceDanger;
    }

    private LXXBullet getBullet() {
        if (enemyBulletManager.hasBulletsOnAir()) {
            return enemyBulletManager.getClosestBullet();
        }

        Target duelOpponent = targetManager.getDuelOpponent();
        if (duelOpponent.getGunHeat() < 0.2) {
            return enemyBulletManager.getImaginaryBullet(duelOpponent);
        }

        return null;
    }

    private APoint getSurfPoint() {
        if (enemyBulletManager.hasBulletsOnAir()) {
            return enemyBulletManager.getClosestBullet().getFirePosition();
        }

        return targetManager.getDuelOpponent();
    }

    private List<LXXPoint> generatePoints(OrbitDirection orbitDirection, LXXBullet bullet, APoint surfPoint, Target enemy) {
        final List<LXXPoint> points = new ArrayList<LXXPoint>();

        final LXXGraphics g = robot.getLXXGraphics();
        if (orbitDirection == OrbitDirection.CLOCKWISE) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.YELLOW);
        }

        final RobotImage robotImg = new RobotImage(robot.getPosition(), robot.getVelocity(), robot.getHeadingRadians(), robot.battleField, 0);
        final RobotImage opponentImg = enemy == null ? null : new RobotImage(enemy.getPosition(), enemy.getVelocity(), enemy.getState().getHeadingRadians(), robot.battleField, 0);
        int time = 0;
        while ((bullet != null && (bullet.getFirePosition().aDistance(robotImg) - bullet.getTravelledDistance()) > bullet.getSpeed() * time) ||
                (bullet == null && robot.aDistance(enemy) > Rules.getBulletSpeed(enemy.getFirePower()) * time)) {
            final MovementDecision md = getMovementDecision(surfPoint, orbitDirection, robotImg, opponentImg);
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

    private double getTargetHeading(APoint surfPoint, LXXRobotState robot, LXXRobotState opponent, OrbitDirection orbitDirection) {
        return robot.angleTo(pointsSelector.selectPoint(getPossiblePoints(surfPoint, robot, orbitDirection), robot, opponent));
    }

    private Collection<APoint> getPossiblePoints(APoint surfPoint, LXXRobotState robot, OrbitDirection orbitDirection) {
        final double orbitHeading = Utils.normalAbsoluteAngle(surfPoint.angleTo(robot) + LXXConstants.RADIANS_90 * orbitDirection.sign);
        final double heading = LXXUtils.anglesDiff(orbitHeading, robot.getHeadingRadians()) <
                LXXUtils.anglesDiff(orbitHeading, Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180))
                ? robot.getHeadingRadians()
                : Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180);
        final double deceleratedSpeed = LXXUtils.limit(0, robot.getVelocityModule() - Rules.DECELERATION, Rules.MAX_VELOCITY);
        final double maxPossibleTurnRate = Rules.getTurnRateRadians(deceleratedSpeed);

        final Set<APoint> possiblePoints = new HashSet<APoint>();
        double turnRateStep = maxPossibleTurnRate / 10;
        for (double turnRate = -maxPossibleTurnRate; turnRate <= maxPossibleTurnRate + 0.001; turnRate += turnRateStep) {
            possiblePoints.add(robot.project(heading + turnRate, Rules.MAX_VELOCITY));
        }

        return possiblePoints;
    }

    private MovementDecision getMovementDecision(APoint surfPoint, OrbitDirection orbitDirection,
                                                 LXXRobotState robot, LXXRobotState opponent) {
        double targetHeading = getTargetHeading(surfPoint, robot, opponent, orbitDirection);

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
