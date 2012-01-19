/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

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
import lxx.utils.*;
import lxx.utils.time_profiling.TimeProfileProperties;
import lxx.utils.time_profiling.TimeProfiler;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.signum;
import static java.lang.StrictMath.min;

public class WaveSurfingMovement implements Movement, Painter {

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final PointsGenerator pointsGenerator;
    private final TimeProfiler timeProfiler;
    private Target duelOpponent;
    private MovementDirectionPrediction prevPrediction;
    private RobotImage pifImage;
    private List<WSPoint> secondCWPoints;
    private List<WSPoint> secondCCWPoints;

    public WaveSurfingMovement(Office office) {
        this.robot = office.getRobot();
        this.targetManager = office.getTargetManager();
        this.enemyBulletManager = office.getEnemyBulletManager();
        timeProfiler = office.getTimeProfiler();

        pointsGenerator = new PointsGenerator(new DistanceController(office.getTargetManager()), robot.getState().getBattleField());
    }

    public MovementDecision getMovementDecision() {
        duelOpponent = targetManager.getDuelOpponent();
        final List<LXXBullet> lxxBullets = getBullets();
        if (needToReselectOrbitDirection(lxxBullets)) {
            TimeProfileProperties.SELECT_ORBIT_DIRECTION_TIME.start();
            selectOrbitDirection(lxxBullets);
            timeProfiler.stopAndSaveProperty(TimeProfileProperties.SELECT_ORBIT_DIRECTION_TIME);
        }

        final Target.TargetState opponent = duelOpponent == null ? null : duelOpponent.getState();
        final LXXPoint surfPoint = pointsGenerator.getSurfPoint(opponent, lxxBullets.get(0));

        return pointsGenerator.getMovementDecision(surfPoint, prevPrediction.minDangerPoint, robot.getState(), opponent);
    }

    private boolean needToReselectOrbitDirection(List<LXXBullet> bullets) {
        return prevPrediction == null ||
                isBulletsUpdated(bullets) ||
                (duelOpponent != null && signum(duelOpponent.getVelocity()) != prevPrediction.enemyVelocitySign) ||
                robot.aDistance(prevPrediction.minDangerPoint) <= LXXUtils.getStopDistance(robot.getSpeed()) + Rules.MAX_VELOCITY;
    }

    private boolean isBulletsUpdated(List<LXXBullet> newBullets) {
        return (newBullets.get(0).getAimPredictionData()).getPredictionRoundTime() !=
                prevPrediction.firstBulletPredictionTime;
    }

    private void selectOrbitDirection(List<LXXBullet> lxxBullets) {
        MovementDirectionPrediction nextPrediction = new MovementDirectionPrediction();
        nextPrediction.bullets = lxxBullets;
        nextPrediction.firstBulletPredictionTime = lxxBullets.get(0).getAimPredictionData().getPredictionRoundTime();
        nextPrediction.enemyVelocitySign = duelOpponent != null ? signum(duelOpponent.getVelocity()) : 0;
        nextPrediction.cwPoints = predictMovementInDirection(lxxBullets, OrbitDirection.CLOCKWISE, new RobotImage(robot.getState()), duelOpponent == null ? null : new RobotImage(duelOpponent.getState()));
        nextPrediction.ccwPoints = predictMovementInDirection(lxxBullets, OrbitDirection.COUNTER_CLOCKWISE, new RobotImage(robot.getState()), duelOpponent == null ? null : new RobotImage(duelOpponent.getState()));
        final List<WSPoint> futurePoses = new ArrayList<WSPoint>();
        futurePoses.addAll(nextPrediction.cwPoints);
        futurePoses.addAll(nextPrediction.ccwPoints);
        Collections.sort(futurePoses);
        if (lxxBullets.size() >= 2) {
            for (int i = 0; i < min(futurePoses.size(), 6); i++) {
                final WSPoint futurePos = futurePoses.get(i);
                if (i > 0 && futurePos.danger.getDanger() > nextPrediction.minDangerPoint.danger.getDanger()) {
                    break;
                }
                final PointDanger minDangerOnSecondWave = getMinPointDanger(new RobotImage(robot.getState()), duelOpponent == null ? null : new RobotImage(duelOpponent.getState()), lxxBullets, futurePos);
                futurePos.danger.setMinDangerOnSecondWave(minDangerOnSecondWave);
                if (nextPrediction.minDangerPoint == null || futurePos.danger.getDanger() < nextPrediction.minDangerPoint.danger.getDanger()) {
                    nextPrediction.minDangerPoint = futurePos;
                    nextPrediction.pifImage = pifImage;
                    nextPrediction.secondCWPoints = secondCWPoints;
                    nextPrediction.secondCCWPoints = secondCCWPoints;
                }
            }
        } else {
            nextPrediction.minDangerPoint = futurePoses.get(0);
        }
        prevPrediction = nextPrediction;
    }

    private PointDanger getMinPointDanger(RobotImage robotImage, RobotImage opponentImg, List<LXXBullet> lxxBullets, APoint dst) {
        final RobotImage meImg = new RobotImage(robotImage);
        final RobotImage oppImg = opponentImg != null ? new RobotImage(opponentImg) : opponentImg;
        int time = pointsGenerator.playForwardWaveSuring(dst, lxxBullets.get(0), meImg, oppImg);

        List<LXXBullet> secondBullets = lxxBullets.subList(1, lxxBullets.size());
        final List<WSPoint> secondWavePoints = new ArrayList<WSPoint>();
        final APoint secondSurfPoint = oppImg != null ? oppImg : secondBullets.get(0).getFirePosition();
        final APoint secondDstPointCW = secondSurfPoint.project(Utils.normalAbsoluteAngle(secondSurfPoint.angleTo(robotImage) + LXXConstants.RADIANS_135 * OrbitDirection.CLOCKWISE.sign), secondSurfPoint.aDistance(meImg) * 10);
        secondCWPoints = pointsGenerator.generatePoints(secondDstPointCW, secondBullets.get(0), new RobotImage(meImg), oppImg != null ? new RobotImage(oppImg) : oppImg, time);
        final APoint secondDstPointCCW = secondSurfPoint.project(Utils.normalAbsoluteAngle(secondSurfPoint.angleTo(robotImage) + LXXConstants.RADIANS_135 * OrbitDirection.COUNTER_CLOCKWISE.sign), secondSurfPoint.aDistance(meImg) * 10);
        secondCCWPoints = pointsGenerator.generatePoints(secondDstPointCCW, secondBullets.get(0), new RobotImage(meImg), oppImg != null ? new RobotImage(oppImg) : oppImg, time);
        secondWavePoints.addAll(secondCWPoints);
        secondWavePoints.addAll(secondCCWPoints);
        WSPoint minDangerPoint = new WSPoint(robotImage, new PointDanger(null, 10000, 0));
        for (WSPoint pnt : secondWavePoints) {
            pnt.danger.calculateDanger();
            if (minDangerPoint.danger.getDanger() > pnt.danger.getDanger()) {
                minDangerPoint = pnt;
                pifImage = meImg;
            }
        }
        return minDangerPoint.danger;
    }

    private List<WSPoint> predictMovementInDirection(List<LXXBullet> lxxBullets, OrbitDirection orbitDirection, RobotImage robotImage, RobotImage opponentImg) {
        final APoint surfPoint = opponentImg != null ? opponentImg : lxxBullets.get(0).getFirePosition();
        final APoint dstPoint = surfPoint.project(Utils.normalAbsoluteAngle(surfPoint.angleTo(robotImage) + LXXConstants.RADIANS_135 * orbitDirection.sign), surfPoint.aDistance(robotImage) * 10);
        final List<WSPoint> wsPoints = pointsGenerator.generatePoints(dstPoint, lxxBullets.get(0), new RobotImage(robotImage), opponentImg != null ? new RobotImage(opponentImg) : opponentImg, 0);
        for (WSPoint pnt : wsPoints) {
            pnt.orbitDirection = orbitDirection;
            if (prevPrediction != null && pnt.orbitDirection == prevPrediction.minDangerPoint.orbitDirection) {
                pnt.danger.setDangerMultiplier(0.95);
            }
            pnt.danger.calculateDanger();
        }
        return wsPoints;
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

        drawPath(g, prevPrediction.cwPoints, new Color(0, 255, 0, 200));
        drawPath(g, prevPrediction.ccwPoints, new Color(255, 0, 0, 200));

        g.setColor(new Color(0, 255, 0, 200));
        g.drawCircle(prevPrediction.minDangerPoint, 16);
        g.drawCross(prevPrediction.minDangerPoint, 16);

        if (prevPrediction.pifImage != null) {
            g.setColor(new Color(0, 0, 255, 200));
            g.drawCircle(prevPrediction.pifImage, 16);
            g.drawCross(prevPrediction.pifImage, 16);

            drawPath(g, prevPrediction.secondCWPoints, new Color(0, 255, 255, 200));
            drawPath(g, prevPrediction.secondCCWPoints, new Color(255, 255, 0, 200));
        }
    }

    private void drawPath(LXXGraphics g, List<WSPoint> points, Color color) {
        g.setColor(color);
        for (WSPoint pnt : points) {
            g.drawCircle(pnt, 4);
        }
    }

    public class MovementDirectionPrediction {

        public List<LXXBullet> bullets;
        public List<WSPoint> cwPoints;
        public List<WSPoint> ccwPoints;
        public double enemyVelocitySign;
        public long firstBulletPredictionTime;
        public WSPoint minDangerPoint;
        public RobotImage pifImage;
        public List<WSPoint> secondCWPoints;
        public List<WSPoint> secondCCWPoints;
    }

}
