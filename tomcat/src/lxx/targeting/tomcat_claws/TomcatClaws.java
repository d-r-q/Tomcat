/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.Tomcat;
import lxx.model.TurnSnapshot;
import lxx.office.TurnSnapshotsLog;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.targeting.Target;
import lxx.targeting.tomcat_claws.clustering.Cluster2D;
import lxx.targeting.tomcat_claws.clustering.ClusterBuilder;
import lxx.targeting.tomcat_claws.data_analise.DataView;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class TomcatClaws implements Gun {

    private static final Set<Cluster2D> NO_PREDICTED_POSES = Collections.unmodifiableSet(new HashSet<Cluster2D>());

    private static final int AIMING_TIME = 2;

    private final ClusterBuilder clusterBuilder = new ClusterBuilder();

    private final Tomcat robot;
    private final TurnSnapshotsLog log;
    private final DataView dataView;

    private APoint robotPosAtFireTime;
    private APoint targetingPos;
    private Set<Cluster2D> clusters;

    public TomcatClaws(Tomcat robot, TurnSnapshotsLog log, DataView dataView) {
        this.robot = robot;
        this.log = log;
        this.dataView = dataView;
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        APoint initialPos = t.getPosition();
        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0) {
            targetingPos = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, initialPos));
        }

        if (targetingPos == null) {

            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getVelocityModule() * AIMING_TIME);

            targetingPos = getTargetingPos(t, dataView.getDataSet(log.getLastSnapshot(t)), bulletSpeed);
            if (targetingPos == null) {
                return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(NO_PREDICTED_POSES, initialPos));
            }
        }

        final double angleToPredictedPos = Utils.normalAbsoluteAngle(robotPosAtFireTime.angleTo(targetingPos));

        return new GunDecision(getGunTurnAngle(angleToPredictedPos), new TCPredictionData(clusters, initialPos));
    }


    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    private APoint getTargetingPos(Target t, Set<TurnSnapshot> starts, double bulletSpeed) {
        final List<APoint> futurePoses = getFuturePoses(t, starts, bulletSpeed);
        clusters = clusterBuilder.createCluster(futurePoses);
        final Cluster2D biggestCluster = getBiggestCluster(clusters);
        if (biggestCluster == null) {
            return null;
        }
        return biggestCluster.getCenterPoint();
    }

    private List<APoint> getFuturePoses(Target t, Set<TurnSnapshot> starts, double bulletSpeed) {
        final List<APoint> futurePoses = new ArrayList<APoint>();
        for (TurnSnapshot start : starts) {
            final APoint futurePos = getFuturePos(t, start, bulletSpeed);
            if (futurePos != null) {
                futurePoses.add(futurePos);
            }
        }
        return futurePoses;
    }

    private APoint getFuturePos(Target t, TurnSnapshot start, double bulletSpeed) {
        APoint futurePos = new LXXPoint(t.getPosition());

        int timeDelta = -AIMING_TIME;
        TurnSnapshot currentSnapshot = start.getNext();
        while (!isBulletHitEnemy(futurePos, timeDelta, bulletSpeed)) {
            if (currentSnapshot == null) {
                return null;
            }
            final DeltaVector dv = LXXUtils.getEnemyDeltaVector(start, currentSnapshot);
            final double alpha = t.getAbsoluteHeadingRadians() + dv.getAlphaRadians();
            futurePos = new LXXPoint(t.getPosition().project(alpha, dv.getLength()));
            if (!robot.getState().getBattleField().contains(futurePos)) {
                return null;
            }
            currentSnapshot = currentSnapshot.getNext();
            timeDelta++;
        }

        return futurePos;
    }

    private boolean isBulletHitEnemy(APoint predictedPos, long timeDelta, double bulletSpeed) {
        final double angleToPredictedPos = robotPosAtFireTime.angleTo(predictedPos);
        final int bulletTravelledDistance = (int) (timeDelta * bulletSpeed);
        final LXXPoint bulletPos = (LXXPoint) robotPosAtFireTime.project(angleToPredictedPos, bulletTravelledDistance);
        final Rectangle2D enemyRectAtPredictedPos = LXXUtils.getBoundingRectangleAt(predictedPos);
        return enemyRectAtPredictedPos.contains(bulletPos) || bulletTravelledDistance > robotPosAtFireTime.aDistance(predictedPos) + LXXConstants.ROBOT_SIDE_HALF_SIZE;
    }

    private static Cluster2D getBiggestCluster(final Set<Cluster2D> clusters) {
        int biggestClusterSize = Integer.MIN_VALUE;
        Cluster2D biggestCluster = null;
        for (Cluster2D c : clusters) {
            if (c.getEntries().size() > biggestClusterSize) {
                biggestClusterSize = c.getEntries().size();
                biggestCluster = c;
            }
        }
        return biggestCluster;
    }

}
