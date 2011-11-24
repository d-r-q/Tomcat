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
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.signum;

public class WaveSurfingMovement implements Movement, Painter {

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final PointsGenerator pointsGenerator;
    private double distanceToTravel;
    private Target duelOpponent;
    private MovementDirectionPrediction prevPrediction;
    private MovementDirectionPrediction clockwisePrediction;
    private MovementDirectionPrediction counterClockwisePrediction;

    public WaveSurfingMovement(Office office, TomcatEyes tomcatEyes) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();

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

        return pointsGenerator.getMovementDecision(surfPoint, prevPrediction.minDangerPoint, robot.getState(), opponent);
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
        prevPrediction = movementDirectionPrediction;
    }

    private MovementDirectionPrediction predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection, RobotImage robotImage, RobotImage opponentImg) {
        final MovementDirectionPrediction prediction = new MovementDirectionPrediction();
        prediction.enemyPos = duelOpponent != null ? duelOpponent.getPosition() : null;
        prediction.bullets = lxxBullets;
        final LXXBullet firstBullet = lxxBullets.get(0);
        prediction.firstBulletPredictionTime = firstBullet.getAimPredictionData().getPredictionRoundTime();
        prediction.orbitDirection = orbitDirection;
        double distance = 0;
        APoint prevPoint = robot.getPosition();
        final APoint firePosition = firstBullet.getFirePosition();
        final APoint dstPoint = firePosition.project(Utils.normalAbsoluteAngle(firePosition.angleTo(robotImage) + LXXConstants.RADIANS_135 * orbitDirection.sign), firePosition.aDistance(robotImage));
        robot.getLXXGraphics().drawCircle(dstPoint, 40);
        prediction.points = pointsGenerator.generatePoints(dstPoint, lxxBullets, new RobotImage(robotImage), opponentImg != null ? new RobotImage(opponentImg) : opponentImg, 0);
        prediction.enemyAccelSign = duelOpponent != null ? signum(duelOpponent.getAcceleration()) : 0;
        prediction.distanceBetween = duelOpponent != null ? duelOpponent.aDistance(robot) : 0;
        for (WSPoint pnt : prediction.points) {
            distance += prevPoint.aDistance(pnt);

            if (pnt.danger.danger < prediction.minDanger.danger || prediction.minDangerPoint == null) {
                prediction.minDanger = pnt.danger;
                prediction.distToMinDangerPoint = distance;
                prediction.minDangerPoint = pnt;
            }
            prevPoint = pnt;
        }

        if (lxxBullets.size() >= 2) {
            final RobotImage meImg = new RobotImage(robotImage);
            final RobotImage oppImg = opponentImg != null ? new RobotImage(opponentImg) : opponentImg;
            int time = pointsGenerator.playForwardWaveSuring(prediction.minDangerPoint, firstBullet, meImg, oppImg);

            prediction.pifImg = new RobotImage(meImg);

            List<LXXBullet> secondBullets = lxxBullets.subList(1, lxxBullets.size());
            final List<WSPoint> secondWavePoints = new ArrayList<WSPoint>();
            final APoint secondFirePosition = secondBullets.get(0).getFirePosition();
            final APoint secondDstPointCW = secondFirePosition.project(Utils.normalAbsoluteAngle(secondFirePosition.angleTo(robotImage) + LXXConstants.RADIANS_135 * OrbitDirection.CLOCKWISE.sign), secondFirePosition.aDistance(meImg));
            final List<WSPoint> cwPoints = pointsGenerator.generatePoints(secondDstPointCW, secondBullets, new RobotImage(meImg), oppImg != null ? new RobotImage(oppImg) : oppImg, time);
            final APoint secondDstPointCCW = secondFirePosition.project(Utils.normalAbsoluteAngle(secondFirePosition.angleTo(robotImage) + LXXConstants.RADIANS_135 * OrbitDirection.COUNTER_CLOCKWISE.sign), secondFirePosition.aDistance(meImg));
            final List<WSPoint> ccwPoints = pointsGenerator.generatePoints(secondDstPointCCW, secondBullets, new RobotImage(meImg), oppImg != null ? new RobotImage(oppImg) : oppImg, time);
            prediction.secondCWPoints = cwPoints;
            prediction.secondCCWPoints = ccwPoints;
            secondWavePoints.addAll(cwPoints);
            secondWavePoints.addAll(ccwPoints);
            WSPoint minDangerPoint = null;
            for (WSPoint pnt : secondWavePoints) {
                if (minDangerPoint == null || minDangerPoint.danger.danger > pnt.danger.danger) {
                    minDangerPoint = pnt;
                }
            }
            if (minDangerPoint != null) {
                prediction.secondMinDangerPoint = minDangerPoint;
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

        drawPath(g, clockwisePrediction.points, new Color(0, 255, 0, 200), (WSPoint) clockwisePrediction.minDangerPoint);
        drawPath(g, counterClockwisePrediction.points, new Color(255, 0, 0, 200), (WSPoint) counterClockwisePrediction.minDangerPoint);

        if (prevPrediction.secondCWPoints != null && prevPrediction.secondCCWPoints != null) {
            drawPath(g, prevPrediction.secondCWPoints, new Color(0, 255, 255, 200), prevPrediction.secondMinDangerPoint);
            drawPath(g, prevPrediction.secondCCWPoints, new Color(255, 255, 0, 200), prevPrediction.secondMinDangerPoint);
            robot.getLXXGraphics().setColor(new Color(255, 0, 0, 100));
            robot.getLXXGraphics().drawCircle(prevPrediction.pifImg, 8);
        }
    }

    private void drawPath(LXXGraphics g, List<WSPoint> points, Color color, WSPoint bestPoint) {
        g.setColor(color);
        for (WSPoint pnt : points) {
            if (bestPoint.equals(pnt)) {
                g.fillCircle(pnt, 4);
            } else {
                g.drawCircle(pnt, 2);
            }
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
        public List<WSPoint> secondCWPoints;
        public List<WSPoint> secondCCWPoints;
        public WSPoint secondMinDangerPoint;
    }

}
