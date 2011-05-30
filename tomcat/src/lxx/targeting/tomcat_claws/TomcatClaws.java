/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.Tomcat;
import lxx.office.Office;
import lxx.office.TargetManager;
import lxx.office.TurnSnapshotsLog;
import lxx.simulator.RobocodeDuelSimulator;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.classification.MovementClassifier;
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
    private RobocodeDuelSimulator duelSimulator;
    private APoint robotPosAtFireTime;

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
            return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, robotPosAtFireTime, turnSnapshotsLog));
        }

        if (predictedPoses == null || predictedPoses.size() == 0) {
            predictedPoses = new LinkedList<LXXPoint>();
            robot.setDebugProperty("Use targeting config", targetingConfig.getName());
            robot.setDebugProperty("Enemy gun type", tomcatEyes.getEnemyGunType(t).toString());
            duelSimulator = new RobocodeDuelSimulator(t, robot, t.getTime(), robot.getRoundNum(), targetingConfig.getAttributes(), bulletManager.getBullets());
            robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getVelocityModule() * AIMING_TIME);

            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            buildPattern(bulletSpeed);

            if (predictedPoses == null || predictedPoses.size() == 0) {
                return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, robotPosAtFireTime, turnSnapshotsLog));
            }
        }

        final double angleToPredictedPos = getAngleToPredictedPos(predictedPoses.getLast(), this.robotPosAtFireTime);

        return new GunDecision(getGunTurnAngle(angleToPredictedPos), new TCPredictionData(predictedPoses, robotPosAtFireTime, turnSnapshotsLog));
    }

    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    public void buildPattern(double bulletSpeed) {
        final MovementClassifier movementClassifier = tomcatEyes.getConfiguration(targetManager.getDuelOpponent()).getMovementClassifier();

        long timeDelta = -AIMING_TIME;
        while (!isBulletHitEnemy(duelSimulator.getEnemyProxy(), timeDelta, bulletSpeed)) {
            final MovementDecision movementDecision = movementClassifier.classify(duelSimulator.getSimulatorSnapshot());
            duelSimulator.setEnemyMovementDecision(movementDecision);
            duelSimulator.setMyMovementDecision(new MovementDecision(1, 0, robot.getVelocity() >= 0 ? MovementDecision.MovementDirection.FORWARD : MovementDecision.MovementDirection.BACKWARD));
            duelSimulator.doTurn();
            if (timeDelta >= 0) {
                final LXXRobotState enemyState = duelSimulator.getEnemyProxy().getState();
                final LXXPoint predictedPos = new LXXPoint(enemyState);
                predictedPoses.add(predictedPos);
            }
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
