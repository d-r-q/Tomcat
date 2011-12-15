/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import ags.utils.KdTree;
import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.office.Office;
import lxx.targeting.GunType;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.*;
import lxx.utils.ps_tree.PSTreeEntry;
import lxx.utils.time_profiling.TimeProfileProperties;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.pow;
import static java.lang.StrictMath.signum;

public class AdvancedEnemyGunModel {

    public static final int FIRE_DETECTION_LATENCY = 2;
    private static final int BEST_LOGS_COUNT = 3;

    private static final Map<String, LogSet> logSets = new HashMap<String, LogSet>();

    private final Map<LXXBullet, PSTreeEntry<UndirectedGuessFactor>> entriesByBullets = new HashMap<LXXBullet, PSTreeEntry<UndirectedGuessFactor>>();

    private final TurnSnapshotsLog turnSnapshotsLog;
    private final Office office;

    public AdvancedEnemyGunModel(TurnSnapshotsLog turnSnapshotsLog, Office office) {
        this.turnSnapshotsLog = turnSnapshotsLog;
        this.office = office;
    }

    public EnemyBulletPredictionData getPredictionData(LXXRobot t, final TurnSnapshot turnSnapshot, Collection<BulletShadow> bulletShadows) {
        return getLogSet(t.getName()).getPredictionData(turnSnapshot, t, bulletShadows);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<UndirectedGuessFactor> entry = new PSTreeEntry<UndirectedGuessFactor>(turnSnapshotsLog.getLastSnapshot(bullet.getOwner(), FIRE_DETECTION_LATENCY));
        entriesByBullets.put(bullet, entry);
    }

    public void processHit(LXXBullet bullet) {
        final LogSet logSet = getLogSet(bullet.getOwner().getName());

        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        updateEnemyHitRate(logSet, aimPredictionData, true);

        logSet.learn(bullet, entriesByBullets.get(bullet).predicate, true);
        if (office.getStatisticsManager().getEnemyHitRate().getHitCount() == 4) {
            logSet.shortLogs.addAll(logSet.visitLogsSet);
            logSet.midLogs.addAll(logSet.visitLogsSet);
            logSet.longLogs.addAll(logSet.visitLogsSet);
            logSet.enemyHitRateLogs.addAll(logSet.visitLogsSet);
        }
    }

    private void updateEnemyHitRate(LogSet logSet, EnemyBulletPredictionData aimPredictionData, boolean isHit) {
        for (Log log : logSet.hitLogsSet) {
            if (aimPredictionData.getBearingOffsets(log) != null) {
                if (isHit) {
                    log.enemyHitRate.hit();
                } else {
                    log.enemyHitRate.miss();
                }
            }
        }
        for (Log log : logSet.visitLogsSet) {
            if (aimPredictionData.getBearingOffsets(log) != null) {
                if (isHit) {
                    log.enemyHitRate.hit();
                } else {
                    log.enemyHitRate.miss();
                }
            }
        }
    }

    public void processIntercept(LXXBullet bullet) {
        getLogSet(bullet.getOwner().getName()).learn(bullet, entriesByBullets.get(bullet).predicate, true);
    }

    public void processMiss(LXXBullet bullet) {
        LogSet logSet = getLogSet(bullet.getOwner().getName());
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        updateEnemyHitRate(logSet, aimPredictionData, false);
        logSet.learn(bullet, entriesByBullets.get(bullet).predicate, false);
    }

    private LogSet getLogSet(String enemyName) {
        LogSet logSet = logSets.get(enemyName);
        if (logSet == null) {
            logSet = createLogSet();
            logSets.put(enemyName, logSet);
        }
        return logSet;
    }

    public void processVisit(LXXBullet bullet) {
        final double direction = bullet.getTargetLateralDirection();
        double undirectedGuessFactor = bullet.getWave().getHitBearingOffsetInterval().center() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
        final LoadedKdTreeEntry<UndirectedGuessFactor> entry = new LoadedKdTreeEntry<UndirectedGuessFactor>(((EnemyBulletPredictionData) bullet.getAimPredictionData()).getTs(), new UndirectedGuessFactor(undirectedGuessFactor, direction));
        getLogSet(bullet.getOwner().getName()).learn(entry);
    }

    public void updateBulletPredictionData(LXXBullet bullet) {
        final LXXRobot owner = bullet.getOwner();
        final long roundTime = LXXUtils.getRoundTime(owner.getTime(), owner.getRound());
        updateOldData(bullet);
        calculateNewData(bullet, roundTime);
    }

    private void updateOldData(LXXBullet bullet) {
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        final boolean isShadowsChanged = bullet.getBulletShadows().size() != aimPredictionData.getBulletShadows().size();
        for (Log log : aimPredictionData.getLogs()) {
            if (!isNeedInUpdate(log, bullet, aimPredictionData, isShadowsChanged)) {
                continue;
            }
            aimPredictionData.addLogPrediction(log,
                    log.getBearingOffsets(aimPredictionData.getTs(), bullet.getBullet().getPower(), bullet.getBulletShadows()));
        }
        if (isShadowsChanged) {
            aimPredictionData.setBulletShadows(bullet.getBulletShadows());
        }
    }

    private void calculateNewData(LXXBullet bullet, long roundTime) {
        final EnemyBulletPredictionData aimPredictionData = (EnemyBulletPredictionData) bullet.getAimPredictionData();
        final LogSet logSet = getLogSet(bullet.getOwner().getName());
        final List<PastBearingOffset> bearingOffsets = new ArrayList<PastBearingOffset>();
        for (Log log : logSet.getBestLogs()) {
            List<PastBearingOffset> logBearingOffsets = aimPredictionData.getBearingOffsets(log);
            if (logBearingOffsets == null) {
                logBearingOffsets = log.getBearingOffsets(aimPredictionData.getTs(), bullet.getBullet().getPower(), bullet.getBulletShadows());
                aimPredictionData.addLogPrediction(log, logBearingOffsets);
            }
            bearingOffsets.addAll(logBearingOffsets);
        }

        if (bearingOffsets.size() != 0) {
            aimPredictionData.setPredictedBearingOffsets(bearingOffsets);
            aimPredictionData.setPredictionRoundTime(roundTime);
        }
    }

    private boolean isNeedInUpdate(Log log, LXXBullet bullet, EnemyBulletPredictionData aimPredictionData, boolean isShadowsChanged) {
        return (isShadowsChanged ||
                (log.type == LogType.HIT_LOG && log.lastUpdateRoundTime > aimPredictionData.getPredictionRoundTime()) ||
                hasShadowedBOs(aimPredictionData.getBearingOffsets(log), bullet.getBulletShadows()));
    }

    private boolean hasShadowedBOs(List<PastBearingOffset> bos, Collection<BulletShadow> shadows) {
        for (BulletShadow shadow : shadows) {
            for (PastBearingOffset bo : bos) {
                if (shadow.contains(bo.bearingOffset)) {
                    return true;
                }
            }
        }
        return false;
    }

    class Log {

        private KdTreeAdapter<LoadedKdTreeEntry<UndirectedGuessFactor>> log;

        private final AvgValue shortAvgHitRate = new AvgValue(3);
        private final AvgValue midAvgHitRate = new AvgValue(11);
        private final AvgValue longAvgHitRate = new AvgValue(100);

        private final AvgValue shortAvgMissRate = new AvgValue(3);
        private final AvgValue midAvgMissRate = new AvgValue(11);
        private final AvgValue longAvgMissRate = new AvgValue(100);

        private final HitRate enemyHitRate = new HitRate();

        private final Attribute[] attrs;
        private final LogType type;

        public int usage = 0;
        private long lastUpdateRoundTime;

        private Log(Attribute[] attrs, LogType type) {
            this.attrs = attrs;
            this.type = type;
            this.log = new KdTreeAdapter<LoadedKdTreeEntry<UndirectedGuessFactor>>(this.attrs, 5000);
        }

        private List<PastBearingOffset> getBearingOffsets(TurnSnapshot predicate, double firePower, Collection<BulletShadow> bulletShadows) {
            TimeProfileProperties.MOV_KNN_TIME.start();
            final List<KdTree.Entry<LoadedKdTreeEntry<UndirectedGuessFactor>>> entries = log.getNearestNeighbours(predicate);
            office.getTimeProfiler().stopAndSaveProperty(TimeProfileProperties.MOV_KNN_TIME);

            final double lateralDirection = LXXUtils.lateralDirection(predicate.getEnemyImage(), predicate.getMeImage());
            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            final double maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);

            final List<PastBearingOffset> bearingOffsets = new LinkedList<PastBearingOffset>();
            int notShadowedBulletsCount = 0;
            for (KdTree.Entry<LoadedKdTreeEntry<UndirectedGuessFactor>> entry : entries) {
                if (notShadowedBulletsCount == 5) {
                    break;
                }
                if (entry.value.result.lateralDirection != 0 && lateralDirection != 0) {

                    final double bearingOffset = entry.value.result.guessFactor * entry.value.result.lateralDirection * lateralDirection * maxEscapeAngleQuick;
                    if (isShadowed(bearingOffset, bulletShadows)) {
                        continue;
                    } else {
                        notShadowedBulletsCount++;
                    }
                    bearingOffsets.add(new PastBearingOffset(entry.value.turnSnapshot, bearingOffset, 1));
                } else {
                    boolean hasNotShadowed = false;
                    final double bearingOffset1 = entry.value.result.guessFactor * 1 * maxEscapeAngleQuick;
                    if (!isShadowed(bearingOffset1, bulletShadows)) {
                        hasNotShadowed = true;
                        bearingOffsets.add(new PastBearingOffset(entry.value.turnSnapshot, bearingOffset1, 1));
                    }

                    final double bearingOffset2 = entry.value.result.guessFactor * -1 * maxEscapeAngleQuick;
                    if (!isShadowed(bearingOffset2, bulletShadows)) {
                        hasNotShadowed = true;
                        bearingOffsets.add(new PastBearingOffset(entry.value.turnSnapshot, bearingOffset2, 1));
                    }

                    if (hasNotShadowed) {
                        notShadowedBulletsCount++;
                    }
                }
            }

            return bearingOffsets;
        }

        private boolean isShadowed(double bearingOffset, Collection<BulletShadow> shadows) {
            for (BulletShadow shadow : shadows) {
                if (shadow.contains(bearingOffset)) {
                    return true;
                }
            }

            return false;
        }

        public void addEntry(LoadedKdTreeEntry<UndirectedGuessFactor> entry) {
            log.addEntry(entry);
            lastUpdateRoundTime = LXXUtils.getRoundTime(office.getTime(), office.getRobot().getRoundNum());
        }

    }

    private class LogSet {

        private final List<Log> hitLogsSet = new ArrayList<Log>();
        private final List<Log> visitLogsSet = new ArrayList<Log>();

        private final List<Log> shortLogs = new ArrayList<Log>();
        private final List<Log> midLogs = new ArrayList<Log>();
        private final List<Log> longLogs = new ArrayList<Log>();
        private final List<Log> enemyHitRateLogs = new ArrayList<Log>();
        private final List[] bestLogs = {shortLogs, midLogs, longLogs, enemyHitRateLogs};

        public void learn(LoadedKdTreeEntry<UndirectedGuessFactor> entry) {
            for (Log log : visitLogsSet) {
                log.addEntry(entry);
            }
        }

        public EnemyBulletPredictionData getPredictionData(TurnSnapshot ts, LXXRobot t, Collection<BulletShadow> bulletShadows) {
            final List<PastBearingOffset> bearingOffsets = new ArrayList<PastBearingOffset>();

            final Map<Log, List<PastBearingOffset>> bestLogsBearingOffsets = new HashMap<Log, List<PastBearingOffset>>();
            final long roundTime = LXXUtils.getRoundTime(t.getTime(), t.getRound());
            for (Log log : getBestLogs()) {
                final List<PastBearingOffset> logBOs = log.getBearingOffsets(ts, t.getFirePower(), bulletShadows);
                bestLogsBearingOffsets.put(log, logBOs);
                bearingOffsets.addAll(logBOs);
                log.usage++;
            }

            if (bearingOffsets.size() == 0) {
                final GunType enemyGunType = office.getTomcatEyes().getEnemyGunType(t);
                fillWithSimpleBOs(ts, t, bearingOffsets, enemyGunType);
            }

            return new EnemyBulletPredictionData(bearingOffsets, roundTime, bestLogsBearingOffsets, ts, bulletShadows);
        }

        private void updateBestLogs() {
            Collections.sort(shortLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum((o2.shortAvgHitRate.getCurrentValue() - o2.shortAvgMissRate.getCurrentValue()) -
                            (o1.shortAvgHitRate.getCurrentValue() - o1.shortAvgMissRate.getCurrentValue()));
                }
            });
            Collections.sort(midLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum((o2.midAvgHitRate.getCurrentValue() - o2.midAvgMissRate.getCurrentValue()) -
                            (o1.midAvgHitRate.getCurrentValue() - o1.midAvgMissRate.getCurrentValue()));
                }
            });
            Collections.sort(longLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum((o2.longAvgHitRate.getCurrentValue() - o2.longAvgMissRate.getCurrentValue()) -
                            (o1.longAvgHitRate.getCurrentValue() - o1.longAvgMissRate.getCurrentValue()));
                }
            });
            Collections.sort(enemyHitRateLogs, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    if (o1.enemyHitRate.getFireCount() == 0) {
                        return 1;
                    } else if (o2.enemyHitRate.getFireCount() == 0) {
                        return -1;
                    }
                    return (int) signum(o1.enemyHitRate.getHitRate() - o2.enemyHitRate.getHitRate());
                }
            });
        }

        private Set<Log> getBestLogs() {
            final Set<Log> bestLogs = new HashSet<Log>();
            for (List<Log> logs : this.bestLogs) {
                for (int i = 0; i < BEST_LOGS_COUNT; i++) {
                    bestLogs.add(logs.get(i));
                }
            }
            return bestLogs;
        }

        private void fillWithSimpleBOs(TurnSnapshot ts, LXXRobot t, List<PastBearingOffset> bearingOffsets, GunType enemyGunType) {
            final double lateralDirection = LXXUtils.lateralDirection(ts.getEnemyImage(), ts.getMeImage());
            final double bulletSpeed = Rules.getBulletSpeed(t.getFirePower());
            final double maxEscapeAngleAcc = LXXUtils.getMaxEscapeAngle(t, office.getRobot().getState(), bulletSpeed);
            if (enemyGunType != GunType.HEAD_ON) {
                if (lateralDirection != 0) {
                    bearingOffsets.add(new PastBearingOffset(ts, maxEscapeAngleAcc * lateralDirection, 1));
                } else {
                    bearingOffsets.add(new PastBearingOffset(ts, maxEscapeAngleAcc * 1, 1));
                    bearingOffsets.add(new PastBearingOffset(ts, maxEscapeAngleAcc * -1, 1));
                }
            }
            if (enemyGunType == GunType.UNKNOWN || enemyGunType == GunType.HEAD_ON) {
                bearingOffsets.add(new PastBearingOffset(ts, 0D, 1));
            }
        }

        public void learn(LXXBullet bullet, TurnSnapshot predicate, boolean isHit) {
            recalculateLogSetEfficiency(bullet, visitLogsSet, isHit);
            recalculateLogSetEfficiency(bullet, hitLogsSet, isHit);
            updateBestLogs();
            if (isHit) {
                final double direction = bullet.getTargetLateralDirection();
                final double undirectedGuessFactor = bullet.getRealBearingOffsetRadians() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
                final LoadedKdTreeEntry<UndirectedGuessFactor> entry = new LoadedKdTreeEntry<UndirectedGuessFactor>(predicate, new UndirectedGuessFactor(undirectedGuessFactor, direction));
                for (Log log : hitLogsSet) {
                    log.addEntry(entry);
                }
            }
        }

        private void recalculateLogSetEfficiency(LXXBullet bullet, List<Log> logSet, boolean isHit) {
            for (Log log : logSet) {
                final EnemyBulletPredictionData ebpd = (EnemyBulletPredictionData) bullet.getAimPredictionData();
                List<PastBearingOffset> bearingOffsets = ebpd.getBearingOffsets(log);
                if (bearingOffsets == null) {
                    bearingOffsets = log.getBearingOffsets(ebpd.getTs(), bullet.getBullet().getPower(), bullet.getBulletShadows());
                }
                double logEfficiency = calculateEfficiency(bullet, bearingOffsets, isHit);
                if (isHit) {
                    log.shortAvgHitRate.addValue(logEfficiency);
                    log.midAvgHitRate.addValue(logEfficiency);
                    log.longAvgHitRate.addValue(logEfficiency);
                } else {
                    log.shortAvgMissRate.addValue(logEfficiency);
                    log.midAvgMissRate.addValue(logEfficiency);
                    log.longAvgMissRate.addValue(logEfficiency);
                }
            }
        }

        private double calculateEfficiency(LXXBullet bullet, List<PastBearingOffset> bearingOffsets, boolean isHit) {
            final IntervalDouble effectiveInterval;
            if (isHit) {
                final double robotHalfSizeRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), bullet.getTarget()) / 2;
                final double currentBO = bullet.getRealBearingOffsetRadians();
                effectiveInterval = new IntervalDouble(currentBO - robotHalfSizeRadians, currentBO + robotHalfSizeRadians);
            } else {
                final IntervalDouble hitInterval = bullet.getWave().getHitBearingOffsetInterval();
                effectiveInterval = new IntervalDouble(hitInterval.center() - hitInterval.getLength() * 0.4,
                        hitInterval.center() + hitInterval.getLength() * 0.4);
            }

            double totalDanger = 0;
            double realDanger = 0;
            for (PastBearingOffset pastBo : bearingOffsets) {
                totalDanger += pastBo.danger;
                if (effectiveInterval.contains(pastBo.bearingOffset)) {
                    realDanger += pastBo.danger;
                }
            }

            if (totalDanger == 0) {
                return 0;
            }

            return realDanger / totalDanger;
        }

    }

    private LogSet createLogSet() {
        final LogSet res = new LogSet();

        res.visitLogsSet.addAll(createVisitLogs());
        res.hitLogsSet.addAll(createHitLogs());

        res.shortLogs.addAll(res.hitLogsSet);
        res.midLogs.addAll(res.hitLogsSet);
        res.longLogs.addAll(res.hitLogsSet);
        res.enemyHitRateLogs.addAll(res.hitLogsSet);

        res.updateBestLogs();

        return res;
    }

    private List<Log> createVisitLogs() {
        final Attribute[] possibleAttributes = {
                AttributesManager.myLateralSpeed,
                AttributesManager.myAcceleration,
                AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall,
        };

        return createLogs(possibleAttributes, new Attribute[0], 1, Integer.MAX_VALUE, LogType.VISIT_LOG);
    }

    private List<Log> createHitLogs() {
        final Attribute[] possibleAttributes = {
                AttributesManager.myAcceleration,
                AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall,
                AttributesManager.myDistLast10Ticks,
        };
        return createLogs(possibleAttributes, new Attribute[]{AttributesManager.myLateralSpeed}, 1, Integer.MAX_VALUE, LogType.HIT_LOG);
    }

    private List<Log> createLogs(Attribute[] possibleAttributes, Attribute[] requiredAttributes,
                                 int minElements, int maxElements, LogType logType) {
        final List<Log> logs = new ArrayList<Log>();
        for (int i = 0; i < pow(2, possibleAttributes.length); i++) {
            final List<Attribute> attrs = new LinkedList<Attribute>();
            attrs.addAll(Arrays.asList(requiredAttributes));
            for (int bit = 0; bit < possibleAttributes.length; bit++) {
                if ((i & (1 << bit)) != 0) {
                    attrs.add(possibleAttributes[bit]);
                }
            }

            if (attrs.size() < minElements || attrs.size() > maxElements) {
                continue;
            }
            logs.add(new Log(attrs.toArray(new Attribute[attrs.size()]), logType));
        }
        return logs;
    }

    private enum LogType {
        HIT_LOG,
        VISIT_LOG
    }

}
