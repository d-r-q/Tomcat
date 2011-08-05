/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.Tomcat;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.targeting.Target;
import lxx.targeting.tomcat_claws.data_analise.DataView;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.*;

import static java.lang.Math.*;

public class TomcatClaws implements Gun {

    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_1;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private static final int AIMING_TIME = 2;
    private static final int NO_BEARING_OFFSET = 0;

    private final Tomcat robot;
    private final TurnSnapshotsLog log;
    private final DataView dataView;

    private APoint robotPosAtFireTime;
    private List<APoint> futurePoses;
    private Map<Double, Double> bearingOffsetDangers;
    private double bestBearingOffset;

    public TomcatClaws(Tomcat robot, TurnSnapshotsLog log, DataView dataView) {
        this.robot = robot;
        this.log = log;
        this.dataView = dataView;
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        final APoint initialPos = t.getPosition();
        robotPosAtFireTime = robot.project(robot.getAbsoluteHeadingRadians(), robot.getSpeed() * AIMING_TIME);

        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0) {
            bearingOffsetDangers = new HashMap<Double, Double>();
            futurePoses = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(bearingOffsetDangers, futurePoses, robotPosAtFireTime, initialPos));
        }

        if (bearingOffsetDangers.size() == 0) {
            bestBearingOffset = getBearingOffset(t, dataView.getDataSet(log.getLastSnapshot(t)), Rules.getBulletSpeed(firePower));
        }

        return new GunDecision(getGunTurnAngle(Utils.normalAbsoluteAngle(robotPosAtFireTime.angleTo(t) + bestBearingOffset)),
                new TCPredictionData(bearingOffsetDangers, futurePoses, robotPosAtFireTime, initialPos));
    }


    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    private double getBearingOffset(Target t, Collection<TurnSnapshot> starts, double bulletSpeed) {
        futurePoses = getFuturePoses(t, starts, bulletSpeed);
        final List<IntervalDouble> botIntervalsRadians = new ArrayList<IntervalDouble>();
        for (APoint pnt : futurePoses) {
            final double bearingOffset = LXXUtils.bearingOffset(robotPosAtFireTime, t, pnt);
            final double botWidth = LXXUtils.getRobotWidthInRadians(robotPosAtFireTime, pnt);
            final double bo1 = bearingOffset - botWidth / 2;
            final double bo2 = bearingOffset + botWidth / 2;
            botIntervalsRadians.add(new IntervalDouble(min(bo1, bo2), max(bo1, bo2)));
        }

        bearingOffsetDangers = new TreeMap<Double, Double>();
        double maxDanger = 0;
        for (double wavePointBearingOffset = -MAX_BEARING_OFFSET; wavePointBearingOffset <= MAX_BEARING_OFFSET + LXXConstants.RADIANS_0_1; wavePointBearingOffset += BEARING_OFFSET_STEP) {
            double bearingOffsetDanger = 0;
            for (IntervalDouble ival : botIntervalsRadians) {
                if (ival.contains(wavePointBearingOffset)) {
                    //final double dist = abs(wavePointBearingOffset - ival.center());
                    bearingOffsetDanger++;
                }
            }

            maxDanger = max(maxDanger, bearingOffsetDanger);

            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
        }

        if (maxDanger == 0) {
            return NO_BEARING_OFFSET;
        }

        final List<Double> candidates = new ArrayList<Double>();
        for (double wavePointBearingOffset = -MAX_BEARING_OFFSET; wavePointBearingOffset <= MAX_BEARING_OFFSET + LXXConstants.RADIANS_0_1; wavePointBearingOffset += BEARING_OFFSET_STEP) {
            if (bearingOffsetDangers.get(wavePointBearingOffset) == maxDanger) {
                candidates.add(wavePointBearingOffset);
            }
        }

        return candidates.get((int) (candidates.size() * random()));
    }

    private List<APoint> getFuturePoses(Target t, Collection<TurnSnapshot> starts, double bulletSpeed) {
        final List<APoint> futurePoses = new ArrayList<APoint>();
        final Map<TurnSnapshot, APoint> futurePosesCache = new HashMap<TurnSnapshot, APoint>();
        for (TurnSnapshot start : starts) {
            APoint futurePos = futurePosesCache.get(start);
            if (futurePos == null) {
                futurePos = getFuturePos(t, start, bulletSpeed);
                futurePosesCache.put(start, futurePos);
            }

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

}
