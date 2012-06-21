/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.Tomcat;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.office.PropertiesManager;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.targeting.Target;
import lxx.targeting.tomcat_claws.data_analise.DataView;
import lxx.targeting.tomcat_claws.data_analise.DataViewManager;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.utils.*;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.*;

import static java.lang.Math.*;

public class TomcatClaws implements Gun {

    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_0_5;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private static AvgValue avgMinDiff = new AvgValue(5000);

    private static final int AIMING_TIME = 2;

    private final Tomcat robot;
    private final TurnSnapshotsLog log;
    private final DataViewManager dataViewManager;

    private APoint robotPosAtFireTime;
    private List<APoint> futurePoses;
    private Map<Double, Double> bearingOffsetDangers;
    private double bestBearingOffset;
    private Map<DataView, List<IntervalDoubleDanger>> dataViewsPredictions;

    public TomcatClaws(Tomcat robot, TurnSnapshotsLog log, DataViewManager dataViewManager) {
        this.robot = robot;
        this.log = log;
        this.dataViewManager = dataViewManager;
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        final APoint initialPos = t.getPosition();
        robotPosAtFireTime = robot.project(robot.getCurrentSnapshot().getAbsoluteHeadingRadians(), robot.getSpeed() * AIMING_TIME);

        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0) {
            futurePoses = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(bearingOffsetDangers, futurePoses, robotPosAtFireTime, initialPos, new HashMap<DataView, List<IntervalDoubleDanger>>()));
        }

        if (futurePoses == null) {
            bestBearingOffset = getBearingOffset(t, Rules.getBulletSpeed(firePower), log.getLastSnapshot(t));
        }

        return new GunDecision(getGunTurnAngle(Utils.normalAbsoluteAngle(robotPosAtFireTime.angleTo(t) + bestBearingOffset)),
                new TCPredictionData(bearingOffsetDangers, futurePoses, robotPosAtFireTime, initialPos, dataViewsPredictions));
    }


    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    private double getBearingOffset(Target t, double bulletSpeed, final TurnSnapshot snapshot) {
        dataViewsPredictions = new HashMap<DataView, List<IntervalDoubleDanger>>();
        futurePoses = new ArrayList<APoint>();
        final List<IntervalDoubleDanger> botIntervalsRadians = new ArrayList<IntervalDoubleDanger>();
        final Map<APoint, IntervalDoubleDanger> ivalCache = new HashMap<APoint, IntervalDoubleDanger>();
        final HashMap<TurnSnapshot, APoint> futurePosesCache = new HashMap<TurnSnapshot, APoint>();
        final double angleToTarget = robotPosAtFireTime.angleTo(t);

        final AvgValue[] avgValues = new AvgValue[dataViewManager.getDuelDataViews().length];
        int avgValueIdx = 0;
        for (DataView dv : dataViewManager.getDuelDataViews()) {
            final List<APoint> viewFuturePoses = getFuturePoses(t, dv.getDataSet(snapshot), bulletSpeed, futurePosesCache);

            avgValues[avgValueIdx] = new AvgValue(viewFuturePoses.size());

            final List<IntervalDoubleDanger> viewIvals = new ArrayList<IntervalDoubleDanger>();
            for (APoint pnt : viewFuturePoses) {
                IntervalDoubleDanger cachedIval = ivalCache.get(pnt);
                if (cachedIval == null) {
                    final double angleToPnt = robotPosAtFireTime.angleTo(pnt);
                    final double bearingOffset = Utils.normalRelativeAngle(angleToPnt - angleToTarget);
                    final double botWidth = LXXUtils.getRobotWidthInRadians(angleToPnt, robotPosAtFireTime.aDistance(pnt)) * 0.4;
                    final double bo1 = bearingOffset - botWidth;
                    final double bo2 = bearingOffset + botWidth;
                    cachedIval = new IntervalDoubleDanger(min(bo1, bo2), max(bo1, bo2), 1);
                    ivalCache.put(pnt, cachedIval);
                }
                viewIvals.add(cachedIval);
                avgValues[avgValueIdx].addValue(cachedIval.center());
            }

            avgValueIdx++;

            dataViewsPredictions.put(dv, viewIvals);
        }

        final AvgValue[] avgDiffSquares = new AvgValue[dataViewManager.getDuelDataViews().length];
        int avgDiffSquaresIdx = 0;
        double minDiff = Integer.MAX_VALUE;
        DataView bestView = null;
        for (DataView dv : dataViewManager.getDuelDataViews()) {
            avgDiffSquares[avgDiffSquaresIdx] = new AvgValue(dataViewsPredictions.get(dv).size());
            for (IntervalDouble ival : dataViewsPredictions.get(dv)) {
                avgDiffSquares[avgDiffSquaresIdx].addValue(Math.pow(ival.center() - avgValues[avgDiffSquaresIdx].getCurrentValue(), 2));
            }

            final double avgDiff = Math.sqrt(avgDiffSquares[avgDiffSquaresIdx].getCurrentValue());
            if (avgDiff < minDiff) {
                minDiff = avgDiff;
                bestView = dv;
            }

            avgDiffSquaresIdx++;
        }
        avgMinDiff.addValue(minDiff);
        PropertiesManager.setDebugProperty("Avg min diff", avgMinDiff.toString());

        for (IntervalDoubleDanger ival : dataViewsPredictions.get(bestView)) {
            botIntervalsRadians.add(ival);
        }

        Collections.sort(botIntervalsRadians);

        bearingOffsetDangers = new LinkedHashMap<Double, Double>();
        BearingOffsetDanger maxDangerBo = new BearingOffsetDanger(0, 0);
        for (double wavePointBearingOffset = -MAX_BEARING_OFFSET; wavePointBearingOffset <= MAX_BEARING_OFFSET + LXXConstants.RADIANS_0_1; wavePointBearingOffset += BEARING_OFFSET_STEP) {
            double bearingOffsetDanger = 0;
            for (IntervalDoubleDanger ival : botIntervalsRadians) {
                if (ival.a > wavePointBearingOffset) {
                    break;
                } else if (ival.b < wavePointBearingOffset) {
                } else if (abs(wavePointBearingOffset - ival.center()) < ival.getLength() / 2) {
                    bearingOffsetDanger += ival.danger;
                } else {
                    bearingOffsetDanger += (ival.getLength() / 2) / abs(wavePointBearingOffset - ival.center()) * ival.danger;
                }
            }

            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
            if (bearingOffsetDanger > maxDangerBo.danger) {
                maxDangerBo = new BearingOffsetDanger(wavePointBearingOffset, bearingOffsetDanger);
            }
        }

        return maxDangerBo.bearingOffset;
    }

    private List<APoint> getFuturePoses(Target t, Collection<TurnSnapshot> starts, double bulletSpeed, Map<TurnSnapshot, APoint> futurePosesCache) {
        final List<APoint> futurePoses = new ArrayList<APoint>();
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
        final LXXPoint targetPos = t.getPosition();
        APoint futurePos = new LXXPoint(targetPos);

        TurnSnapshot currentSnapshot = start.next;
        currentSnapshot = skip(currentSnapshot, AIMING_TIME);
        final BattleField battleField = robot.getBattleField();
        final double absoluteHeadingRadians = t.getAbsoluteHeadingRadians();
        BulletState bs;
        final double speedSum = bulletSpeed + Rules.MAX_VELOCITY;
        long timeDelta;
        double bulletTravelledDistance = bulletSpeed;
        while ((bs = isBulletHitEnemy(futurePos, bulletTravelledDistance)) == BulletState.COMING) {
            if (currentSnapshot == null) {
                return null;
            }
            final DeltaVector dv = LXXUtils.getEnemyDeltaVector(start, currentSnapshot);
            final double alpha = absoluteHeadingRadians + dv.getAlphaRadians();
            futurePos = targetPos.project(alpha, dv.getLength());
            if (!battleField.contains(futurePos)) {
                return null;
            }
            timeDelta = currentSnapshot.getTime() - start.getTime() - AIMING_TIME;
            bulletTravelledDistance = timeDelta * bulletSpeed;
            int minBulletFlightTime = max((int) ((robotPosAtFireTime.aDistance(futurePos) - bulletTravelledDistance) / speedSum) - 1, 1);
            currentSnapshot = skip(currentSnapshot, minBulletFlightTime);
        }

        if (bs == BulletState.PASSED) {
            System.out.println("[WARN] Future pos calculation error");
        }

        return futurePos;
    }

    private TurnSnapshot skip(TurnSnapshot start, int count) {

        for (int i = 0; i < count; i++) {
            if (start == null) {
                return null;
            }
            start = start.next;
        }

        return start;
    }

    private BulletState isBulletHitEnemy(APoint predictedPos, double bulletTravelledDistance) {
        final double angleToPredictedPos = robotPosAtFireTime.angleTo(predictedPos);
        final LXXPoint bulletPos = (LXXPoint) robotPosAtFireTime.project(angleToPredictedPos, bulletTravelledDistance);
        final Rectangle2D enemyRectAtPredictedPos = LXXUtils.getBoundingRectangleAt(predictedPos);
        if (enemyRectAtPredictedPos.contains(bulletPos)) {
            return BulletState.HITTING;
        } else if (bulletTravelledDistance > robotPosAtFireTime.aDistance(predictedPos) + LXXConstants.ROBOT_SIDE_HALF_SIZE) {
            return BulletState.PASSED;
        }
        return BulletState.COMING;
    }
    private enum BulletState {
        COMING,
        HITTING,
        PASSED
    }

}
