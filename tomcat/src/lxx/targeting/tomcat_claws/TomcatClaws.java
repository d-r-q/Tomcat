/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.Tomcat;
import lxx.model.TurnSnapshot;
import lxx.office.Office;
import lxx.office.TargetManager;
import lxx.office.TurnSnapshotsLog;
import lxx.simulator.RobocodeDuelSimulator;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.classification.ClassificationIterator;
import lxx.targeting.tomcat_eyes.TargetingConfiguration;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 21.02.11
 */
public class TomcatClaws implements Gun {

    private static final List<LXXPoint> NO_PREDICTED_POSES = Collections.unmodifiableList(new ArrayList<LXXPoint>());
    private static final int AIMING_TIME = 2;

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final TomcatEyes tomcatEyes;
    private final BulletManager bulletManager;
    private final TurnSnapshotsLog turnSnapshotsLog;

    private LinkedList<LXXPoint> predictedPoses = null;
    private LinkedList<MovementDecision> predictedDecs = null;
    private LinkedList<LXXRobotState> predictedStates = null;
    private LinkedList<TurnSnapshot> predictedTSs = null;
    private RobocodeDuelSimulator duelSimulator;
    private APoint robotPosAtFireTime;
    private LXXRobotState enemyProxyState;
    private Target.TargetState duelOpponentState;

    public TomcatClaws(Office office, TomcatEyes tomcatEyes) {
        this.targetManager = office.getTargetManager();
        this.tomcatEyes = tomcatEyes;
        bulletManager = office.getBulletManager();
        turnSnapshotsLog = office.getTurnSnapshotsLog();

        robot = office.getRobot();
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        final TargetingConfiguration targetingConfig = tomcatEyes.getConfiguration(t);
        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0 || targetingConfig == null) {
            predictedPoses = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, predictedDecs, predictedStates, robotPosAtFireTime, turnSnapshotsLog, targetManager.getDuelOpponent().getState(), predictedTSs, null));
        }

        if (predictedPoses == null || predictedPoses.size() == 0) {
            predictedPoses = new LinkedList<LXXPoint>();
            predictedDecs = new LinkedList<MovementDecision>();
            predictedStates = new LinkedList<LXXRobotState>();
            predictedTSs = new LinkedList<TurnSnapshot>();
            robot.setDebugProperty("Use targeting config", targetingConfig.getName());
            robot.setDebugProperty("Enemy gun type", tomcatEyes.getEnemyGunType(t).toString());
            duelSimulator = new RobocodeDuelSimulator(t, robot, t.getTime(), robot.getRoundNum(), targetingConfig.getAttributes(), bulletManager.getBullets());
            enemyProxyState = duelSimulator.getEnemyProxy().getState();
            duelOpponentState = targetManager.getDuelOpponent().getState();
            if (enemyProxyState.getVelocity() != targetManager.getDuelOpponent().getVelocity()) {
                System.out.println("BBBBBBBBBBBBBBBBBBB");
            }
            robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getVelocityModule() * AIMING_TIME);

            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            buildPattern(bulletSpeed);
            for (int i = 0; i < predictedPoses.size() - 3; i++) {
                double dist1 = predictedPoses.get(i).aDistance(predictedPoses.get(i + 1));
                double dist2 = predictedPoses.get(i + 1).aDistance(predictedPoses.get(i + 2));
                if (abs(dist1 - dist2) > 2.2) {
                    predictedPoses = new LinkedList<LXXPoint>();
                    predictedDecs = new LinkedList<MovementDecision>();
                    predictedStates = new LinkedList<LXXRobotState>();
                    duelSimulator = new RobocodeDuelSimulator(t, robot, t.getTime(), robot.getRoundNum(), targetingConfig.getAttributes(), bulletManager.getBullets());
                    buildPattern(bulletSpeed);
                }
            }
            if (predictedPoses.get(0).aDistance(predictedPoses.get(1)) < targetManager.getDuelOpponent().getVelocityModule() - 0.5) {
                predictedPoses = new LinkedList<LXXPoint>();
                predictedDecs = new LinkedList<MovementDecision>();
                predictedStates = new LinkedList<LXXRobotState>();
                duelSimulator = new RobocodeDuelSimulator(t, robot, t.getTime(), robot.getRoundNum(), targetingConfig.getAttributes(), bulletManager.getBullets());
                buildPattern(bulletSpeed);
            }

            if (predictedPoses == null || predictedPoses.size() == 0) {
                return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, predictedDecs, predictedStates, robotPosAtFireTime, turnSnapshotsLog, targetManager.getDuelOpponent().getState(), predictedTSs, enemyProxyState));
            }
        }

        if (enemyProxyState.getVelocity() != targetManager.getDuelOpponent().getVelocity()) {
                System.out.println("BBBBBBBBBBBBBBBBBBB");
            }

        final double angleToPredictedPos = getAngleToPredictedPos(predictedPoses.getLast(), this.robotPosAtFireTime);

        return new GunDecision(getGunTurnAngle(angleToPredictedPos), new TCPredictionData(predictedPoses, predictedDecs, predictedStates, robotPosAtFireTime, turnSnapshotsLog, duelOpponentState, predictedTSs, enemyProxyState));
    }

    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    public void buildPattern(double bulletSpeed) {
        final ClassificationIterator classificationIterator = tomcatEyes.getConfiguration(targetManager.getDuelOpponent()).getMovementClassifier().classificationIterator();

        long timeDelta = -AIMING_TIME;
        while (!isBulletHitEnemy(duelSimulator.getEnemyProxy(), timeDelta, bulletSpeed)) {
            final MovementDecision movementDecision = classificationIterator.next(duelSimulator.getSimulatorSnapshot());
            duelSimulator.setEnemyMovementDecision(movementDecision);
            duelSimulator.setMyMovementDecision(new MovementDecision(1, 0, robot.getVelocity() >= 0 ? MovementDecision.MovementDirection.FORWARD : MovementDecision.MovementDirection.BACKWARD));
            duelSimulator.doTurn();
            final LXXRobotState enemyState = duelSimulator.getEnemyProxy().getState();
            final LXXPoint predictedPos = new LXXPoint(enemyState);
            predictedPoses.add(predictedPos);
            predictedDecs.add(movementDecision);
            predictedStates.addLast(duelSimulator.getEnemyProxy().getState());
            predictedTSs.addLast(duelSimulator.getSimulatorSnapshot());
            timeDelta++;
        }
    }

    private Double getAngleToPredictedPos(APoint predictedPos, APoint robotFuturePos) {
        return Utils.normalAbsoluteAngle(robotFuturePos.angleTo(predictedPos));
    }

    private boolean isBulletHitEnemy(APoint predictedPos, long timeDelta, double bulletSpeed) {
        final double angleToPredictedPos = robotPosAtFireTime.angleTo(predictedPos);
        final int bulletTravelledDistance = (int) (timeDelta * bulletSpeed);
        final LXXPoint bulletPos = (LXXPoint) robotPosAtFireTime.project(angleToPredictedPos, bulletTravelledDistance);
        final Rectangle2D enemyRectAtPredictedPos = LXXUtils.getBoundingRectangleAt(predictedPos);
        return enemyRectAtPredictedPos.contains(bulletPos) || bulletTravelledDistance > robotPosAtFireTime.aDistance(predictedPos) + LXXConstants.ROBOT_SIDE_HALF_SIZE;
    }

}
