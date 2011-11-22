/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.Tomcat;
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
import java.util.List;

import static java.lang.Math.signum;

public class WaveSurfingMovement implements Movement, Painter {

    private final Tomcat robot;
    private final TomcatEyes tomcatEyes;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final PointsGenerator pointsGenerator;

    private OrbitDirection minDangerOrbitDirection = OrbitDirection.CLOCKWISE;
    private double distanceToTravel;
    private Target duelOpponent;
    private MovementDirectionPrediction prevPrediction;
    private MovementDirectionPrediction clockwisePrediction;
    private MovementDirectionPrediction counterClockwisePrediction;

    public WaveSurfingMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();
        this.tomcatEyes = tomcatEyes;

        pointsGenerator = new PointsGenerator(new DistanceController(office.getTargetManager(), tomcatEyes), robot.getState().getBattleField());
    }

    public MovementDecision getMovementDecision() {
        duelOpponent = targetManager.getDuelOpponent();
        final List<LXXBullet> lxxBullets = getBullets();
        if (needToReselectOrbitDirection(lxxBullets)) {
            selectOrbitDirection(lxxBullets);
        } else {
            distanceToTravel -= robot.getSpeed();
        }

        final Target.TargetState opponent = duelOpponent == null ? null : duelOpponent.getState();
        final APoint surfPoint = pointsGenerator.getSurfPoint(opponent, lxxBullets.get(0));
        final double desiredSpeed =
                (robot.aDistance(prevPrediction.minDangerPoint) > LXXUtils.getStopDistance(robot.getSpeed()) + Rules.MAX_VELOCITY ||
                        (duelOpponent != null && tomcatEyes.isRammingNow(duelOpponent)))
                        ? 8
                        : 0;

        return pointsGenerator.getMovementDecision(surfPoint, minDangerOrbitDirection, robot.getState(), opponent, desiredSpeed);
    }

    private boolean needToReselectOrbitDirection(List<LXXBullet> bullets) {
        return prevPrediction == null ||
                isBulletsUpdated(bullets) ||
                (duelOpponent != null && signum(duelOpponent.getAcceleration()) != prevPrediction.enemyAccelSign) ||
                (duelOpponent != null && duelOpponent.aDistance(robot) < prevPrediction.distanceBetween - 25) ||
                distanceToTravel <= LXXUtils.getStopDistance(robot.getSpeed()) + Rules.MAX_VELOCITY;
    }

    private boolean isBulletsUpdated(List<LXXBullet> newBullets) {
        return (newBullets.get(0).getAimPredictionData()).getPredictionRoundTime() !=
                prevPrediction.firstBulletPredictionTime;
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        clockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE, new RobotImage(robot.getState()), duelOpponent == null ? null : new RobotImage(duelOpponent.getState()));
        counterClockwisePrediction = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE, new RobotImage(robot.getState()), duelOpponent == null ? null : new RobotImage(duelOpponent.getState()));
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

    private MovementDirectionPrediction predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection, RobotImage robotImage, RobotImage opponentImg) {
        final MovementDirectionPrediction prediction = new MovementDirectionPrediction();
        prediction.enemyPos = duelOpponent != null ? duelOpponent.getPosition() : null;
        prediction.bullets = lxxBullets;
        prediction.firstBulletPredictionTime = lxxBullets.get(0).getAimPredictionData().getPredictionRoundTime();
        prediction.orbitDirection = orbitDirection;
        double distance = 0;
        APoint prevPoint = robot.getPosition();
        prediction.points = pointsGenerator.generatePoints(orbitDirection, lxxBullets, new RobotImage(robotImage), opponentImg != null ? new RobotImage(opponentImg) : opponentImg,
                new PointsGenerator.MaxDesiredSpeedSelector(), 0);
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

        final RobotImage meImg = new RobotImage(robotImage);
        final RobotImage oppImg = opponentImg != null ? new RobotImage(opponentImg) : opponentImg;
        int time = pointsGenerator.playForwardWaveSuring(orbitDirection, lxxBullets.get(0), meImg, oppImg,
                new StopableDesiredSpeedSelector(prediction.minDangerPoint, meImg), 0);

        prediction.pifImg = new RobotImage(meImg);

        if (lxxBullets.size() >= 2) {
            List<LXXBullet> secondBullets = lxxBullets.subList(1, lxxBullets.size());
            final List<WSPoint> secondWavePoints = new ArrayList<WSPoint>();
            final List<WSPoint> cwPoints = pointsGenerator.generatePoints(OrbitDirection.CLOCKWISE, secondBullets, new RobotImage(meImg), oppImg != null ? new RobotImage(oppImg) : oppImg, new PointsGenerator.MaxDesiredSpeedSelector(), time);
            final List<WSPoint> ccwPoints = pointsGenerator.generatePoints(OrbitDirection.COUNTER_CLOCKWISE, secondBullets, new RobotImage(meImg), oppImg != null ? new RobotImage(oppImg) : oppImg, new PointsGenerator.MaxDesiredSpeedSelector(), time);
            prediction.secondCWPoint = cwPoints;
            prediction.secondCCWPoint = ccwPoints;
            secondWavePoints.addAll(cwPoints);
            secondWavePoints.addAll(ccwPoints);
            WSPoint minDangerPoint = null;
            for (WSPoint pnt : secondWavePoints) {
                if (minDangerPoint == null || minDangerPoint.danger.danger > pnt.danger.danger) {
                    minDangerPoint = pnt;
                }
            }
            if (minDangerPoint != null) {
                minDangerPoint.danger.calculateDanger();
                prediction.minDanger.minDangerOnSecondWave = minDangerPoint.danger.danger;
                prediction.minDanger.calculateDanger();
            }
        }

        return prediction;
    }

    private List<LXXBullet> getBullets() {
        List<LXXBullet> bulletsOnAir = enemyBulletManager.getBulletsOnAir(2);
        if (bulletsOnAir.size() < 2 && duelOpponent != null) {
            bulletsOnAir.add(enemyBulletManager.createFutureBullet(duelOpponent));
        }
        if (bulletsOnAir.size() == 0) {
            bulletsOnAir = enemyBulletManager.getAllBulletsOnAir();
        }
        return bulletsOnAir;
    }

    public void paint(LXXGraphics g) {
        if (prevPrediction == null) {
            return;
        }

        drawPath(g, clockwisePrediction.points, new Color(0, 255, 0, 200));
        drawPath(g, counterClockwisePrediction.points, new Color(255, 0, 0, 200));

        if (prevPrediction.secondCWPoint != null && prevPrediction.secondCCWPoint != null) {
            drawPath(g, prevPrediction.secondCWPoint, new Color(0, 255, 255, 200));
            drawPath(g, prevPrediction.secondCCWPoint, new Color(255, 255, 0, 200));
        }

        g.setColor(new Color(0, 255, 0, 200));
        g.fillCircle(prevPrediction.minDangerPoint, 15);

        robot.getLXXGraphics().setColor(new Color(255, 0, 0, 100));
        robot.getLXXGraphics().fillSquare(prevPrediction.pifImg, 25);
    }

    private void drawPath(LXXGraphics g, List<WSPoint> points, Color color) {
        g.setColor(color);
        for (WSPoint pnt : points) {
            g.fillCircle(pnt, 3);
        }
    }

    public class MovementDirectionPrediction {

        private PointDanger MAX_POINT_DANGER = new PointDanger(100, 100, 5, 1000, LXXConstants.RADIANS_90);

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
        public LXXRobotState pifImg;
        public List<WSPoint> secondCWPoint;
        public List<WSPoint> secondCCWPoint;
    }

    private class StopableDesiredSpeedSelector implements PointsGenerator.DesiredSpeedSelector {

        private final LXXRobotState robot;
        private APoint destination;

        public StopableDesiredSpeedSelector(APoint destination, LXXRobotState robot) {
            this.destination = destination;
            this.robot = robot;
        }

        public double getDesiredSpeed() {
            return (robot.aDistance(destination) > LXXUtils.getStopDistance(robot.getSpeed()) + Rules.MAX_VELOCITY)
                    ? 8
                    : 0;
        }
    }

}
