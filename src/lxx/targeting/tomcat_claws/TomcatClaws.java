/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.BearingOffsetDanger;
import lxx.strategies.Gun;
import lxx.strategies.GunDecision;
import lxx.targeting.Target;
import lxx.targeting.tomcat_claws.data_analise.DataView;
import lxx.targeting.tomcat_claws.data_analise.DataViewManager;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.utils.*;
import lxx.utils.wave.Wave;
import lxx.utils.wave.WaveCallback;
import lxx.utils.wave.WaveManager;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;
import java.util.*;

import static java.lang.Math.*;

public class TomcatClaws implements Gun, WaveCallback, BulletManagerListener {

    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_0_5;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private static final int AIMING_TIME = 2;

    private final Tomcat robot;
    private final TurnSnapshotsLog log;
    private final DataViewManager dataViewManager;
    private final WaveManager waveManager;

    private APoint robotPosAtFireTime;
    private List<APoint> futurePoses;
    private Map<Double, Double> bearingOffsetDangers;
    private double bestBearingOffset;
    private Map<DataView, List<IntervalDouble>> dataViewsPredictions;

    public TomcatClaws(Tomcat robot, TurnSnapshotsLog log, DataViewManager dataViewManager, WaveManager waveManager) {
        this.robot = robot;
        this.log = log;
        this.dataViewManager = dataViewManager;
        this.waveManager = waveManager;
    }

    public GunDecision getGunDecision(Target t, double firePower) {
        final double angleToTarget = robot.angleTo(t);
        final APoint initialPos = t.getPosition();
        robotPosAtFireTime = robot.project(robot.getCurrentSnapshot().getAbsoluteHeadingRadians(), robot.getSpeed() * AIMING_TIME);

        if (robot.getTurnsToGunCool() > AIMING_TIME || t.getEnergy() == 0) {
            futurePoses = null;
            return new GunDecision(getGunTurnAngle(angleToTarget), new TCPredictionData(bearingOffsetDangers, futurePoses, robotPosAtFireTime, initialPos, new HashMap<DataView, List<IntervalDouble>>()));
        }

        if (futurePoses == null) {
            bestBearingOffset = getBearingOffset(t, Rules.getBulletSpeed(firePower));
        }

        return new GunDecision(getGunTurnAngle(Utils.normalAbsoluteAngle(robotPosAtFireTime.angleTo(t) + bestBearingOffset)),
                new TCPredictionData(bearingOffsetDangers, futurePoses, robotPosAtFireTime, initialPos, dataViewsPredictions));
    }


    private double getGunTurnAngle(double angleToPredictedPos) {
        return Utils.normalRelativeAngle(angleToPredictedPos - robot.getGunHeadingRadians());
    }

    private double getBearingOffset(Target t, double bulletSpeed) {
        dataViewsPredictions = new HashMap<DataView, List<IntervalDouble>>();
        futurePoses = new ArrayList<APoint>();
        final List<IntervalDoubleDanger> botIntervalsRadians = new ArrayList<IntervalDoubleDanger>();
        final Map<APoint, IntervalDoubleDanger> ivalCache = new HashMap<APoint, IntervalDoubleDanger>();
        final HashMap<TurnSnapshot, APoint> futurePosesCache = new HashMap<TurnSnapshot, APoint>();
        final double angleToTarget = robotPosAtFireTime.angleTo(t);

        double minHitRate = Integer.MAX_VALUE;
        double maxHitRate = Integer.MIN_VALUE;

        for (DataView dv : dataViewManager.getDuelDataViews()) {
            minHitRate = min(minHitRate, dv.getHitRate().getCurrentValue());
            maxHitRate = max(maxHitRate, dv.getHitRate().getCurrentValue());
        }
        
        for (DataView dv : dataViewManager.getDuelDataViews()) {            
            final double dataViewDanger = pow((dv.getHitRate().getCurrentValue() - minHitRate) / (maxHitRate - minHitRate), 3);
            final List<APoint> viewFuturePoses = getFuturePoses(t, dv.getDataSet(log.getLastSnapshot(t)), bulletSpeed, futurePosesCache);
            final List<IntervalDouble> viewIvals = new ArrayList<IntervalDouble>();
            for (APoint pnt : viewFuturePoses) {
                IntervalDoubleDanger ival = ivalCache.get(pnt);
                if (ival == null) {
                    final double angleToPnt = robotPosAtFireTime.angleTo(pnt);
                    final double bearingOffset = Utils.normalRelativeAngle(angleToPnt - angleToTarget);
                    final double botWidth = LXXUtils.getRobotWidthInRadians(angleToPnt, robotPosAtFireTime.aDistance(pnt)) * 0.75;
                    final double bo1 = bearingOffset - botWidth;
                    final double bo2 = bearingOffset + botWidth;
                    ival = new IntervalDoubleDanger(min(bo1, bo2), max(bo1, bo2), dataViewDanger);
                    ivalCache.put(pnt, ival);
                }
                if (dataViewDanger > 0) {
                    botIntervalsRadians.add(ival);
                }
                viewIvals.add(ival);
            }
            dataViewsPredictions.put(dv, viewIvals);
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
                } else if (abs(wavePointBearingOffset - ival.center()) < ival.getLength() / 4) {
                    bearingOffsetDanger += ival.danger;
                } else {
                    bearingOffsetDanger += (ival.getLength() / 4) / abs(wavePointBearingOffset - ival.center()) * ival.danger;
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

    public void bulletFired(LXXBullet bullet) {
        waveManager.addCallback(this, bullet.getWave());
    }

    public void waveBroken(Wave w) {
        final TCPredictionData predictionData = (TCPredictionData) w.getCarriedBullet().getAimPredictionData();

        final Map<DataView, List<IntervalDouble>> dataViewsPredictions = predictionData.getDataViewsPredictions();
        final double bulletFlightTime = w.getSourceState().aDistance(w.getTargetState()) / w.getSpeed();
        for (DataView dv : dataViewsPredictions.keySet()) {
            final List<IntervalDouble> intervalDoubles = dataViewsPredictions.get(dv);
            dv.addHitRate(getHitRate(w.getHitBearingOffsetInterval(), intervalDoubles) * bulletFlightTime);
        }
    }

    private double getHitRate(IntervalDouble hitInterval, List<IntervalDouble> predictedIntervals) {
        if (predictedIntervals.size() == 0) {
            return 0;
        }

        double hitCount = 0;
        double maxHitCount = 0;
        for (IntervalDouble ival : predictedIntervals) {
            final double intersection = hitInterval.intersection(ival);
            final double minIvalLength = min(ival.getLength(), hitInterval.getLength());
            maxHitCount++;
            if (intersection > 0) {
                hitCount += intersection / minIvalLength;
            }
        }

        if (maxHitCount == 0) {
            return 0;
        }

        return hitCount / maxHitCount;
    }

    public void bulletHit(LXXBullet bullet) {
    }

    public void bulletMiss(LXXBullet bullet) {
    }

    public void bulletIntercepted(LXXBullet bullet) {
    }

    public void bulletPassing(LXXBullet bullet) {
    }

    public void wavePassing(Wave w) {
    }

    private enum BulletState {
        COMING,
        HITTING,
        PASSED
    }

}
