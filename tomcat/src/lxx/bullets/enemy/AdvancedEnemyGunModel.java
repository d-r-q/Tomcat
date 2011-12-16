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
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;
import lxx.utils.time_profiling.TimeProfileProperties;
import lxx.utils.tr_tree.LoadedTRTreeEntry;
import lxx.utils.tr_tree.TRTreeEntry;
import lxx.utils.tr_tree.TrinaryRTree;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.*;
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
        final PSTreeEntry<UndirectedGuessFactor> entry = entriesByBullets.remove(bullet);
        final double direction = bullet.getTargetLateralDirection();
        double undirectedGuessFactor = bullet.getWave().getHitBearingOffsetInterval().center() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
        entry.result = new UndirectedGuessFactor(undirectedGuessFactor, direction);
        final LoadedKdTreeEntry<UndirectedGuessFactor> kDentry = new LoadedKdTreeEntry<UndirectedGuessFactor>(((EnemyBulletPredictionData) bullet.getAimPredictionData()).getTs(), new UndirectedGuessFactor(undirectedGuessFactor, direction));
        getLogSet(bullet.getOwner().getName()).learn(entry, kDentry);
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

        private KdTreeAdapter<LoadedKdTreeEntry<UndirectedGuessFactor>> kdLog;
        private PSTree<UndirectedGuessFactor> log;
        private TrinaryRTree<TRTreeEntry> trtLog;
        private Map<Attribute, Double> halfSideLength = LXXUtils.toMap(
                AttributesManager.myLateralSpeed, 2D,
                AttributesManager.myAcceleration, 0D,
                AttributesManager.distBetween, 75D,
                AttributesManager.myDistToForwardWall, 50D,
                AttributesManager.myDistLast10Ticks, 20D);

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
            this.log = new PSTree<UndirectedGuessFactor>(this.attrs, 2, 0.0001);
            this.trtLog = new TrinaryRTree<TRTreeEntry>(attrs);
            this.kdLog = new KdTreeAdapter<LoadedKdTreeEntry<UndirectedGuessFactor>>(this.attrs, 5000);
        }

        private List<PastBearingOffset> getBearingOffsets(TurnSnapshot predicate, double firePower, Collection<BulletShadow> bulletShadows) {
            Interval[] range = getRange(predicate);

            TimeProfileProperties.PS_RANGE_SEARCH_TIME.start();
            final PSTreeEntry<UndirectedGuessFactor>[] entries = log.getSimilarEntries(range);
            office.getTimeProfiler().stopAndSaveProperty(TimeProfileProperties.PS_RANGE_SEARCH_TIME);

            IntervalDouble[] range2 = getRange2(predicate);

            TimeProfileProperties.TR_RANGE_SEARCH_TIME.start();
            final TRTreeEntry[] entries2 = trtLog.rangeSearch(range2);
            Arrays.sort(entries2, new Comparator<TRTreeEntry>() {
                public int compare(TRTreeEntry o1, TRTreeEntry o2) {
                    return o1.location.roundTime - o2.location.roundTime;
                }
            });
            office.getTimeProfiler().stopAndSaveProperty(TimeProfileProperties.TR_RANGE_SEARCH_TIME);

            TimeProfileProperties.MOV_KNN_TIME.start();
            List<KdTree.Entry<LoadedKdTreeEntry<UndirectedGuessFactor>>> nearestNeighbours = kdLog.getNearestNeighbours(predicate, max(1, entries.length));
            Collections.sort(nearestNeighbours, new Comparator<KdTree.Entry<LoadedKdTreeEntry<UndirectedGuessFactor>>>() {
                public int compare(KdTree.Entry<LoadedKdTreeEntry<UndirectedGuessFactor>> o1, KdTree.Entry<LoadedKdTreeEntry<UndirectedGuessFactor>> o2) {
                    return o2.value.turnSnapshot.roundTime - o1.value.turnSnapshot.roundTime;
                }
            });
            office.getTimeProfiler().stopAndSaveProperty(TimeProfileProperties.MOV_KNN_TIME);

            if (entries.length != entries2.length) {
                trtLog.rangeSearch(range2);
                System.out.println("AAAAAAAAAAAAA");
            }

            final double lateralDirection = LXXUtils.lateralDirection(predicate.getEnemyImage(), predicate.getMeImage());
            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            final double maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);

            final List<PastBearingOffset> bearingOffsets = new LinkedList<PastBearingOffset>();
            int notShadowedBulletsCount = 0;
            for (TRTreeEntry e : entries2) {
                LoadedTRTreeEntry<UndirectedGuessFactor> entry = (LoadedTRTreeEntry<UndirectedGuessFactor>) e;
                if (notShadowedBulletsCount == 5) {
                    break;
                }
                if (entry.data.lateralDirection != 0 && lateralDirection != 0) {

                    final double bearingOffset = entry.data.guessFactor * entry.data.lateralDirection * lateralDirection * maxEscapeAngleQuick;
                    if (isShadowed(bearingOffset, bulletShadows)) {
                        continue;
                    } else {
                        notShadowedBulletsCount++;
                    }
                    bearingOffsets.add(new PastBearingOffset(entry.location, bearingOffset, 1));
                } else {
                    boolean hasNotShadowed = false;
                    final double bearingOffset1 = entry.data.guessFactor * 1 * maxEscapeAngleQuick;
                    if (!isShadowed(bearingOffset1, bulletShadows)) {
                        hasNotShadowed = true;
                        bearingOffsets.add(new PastBearingOffset(entry.location, bearingOffset1, 1));
                    }

                    final double bearingOffset2 = entry.data.guessFactor * -1 * maxEscapeAngleQuick;
                    if (!isShadowed(bearingOffset2, bulletShadows)) {
                        hasNotShadowed = true;
                        bearingOffsets.add(new PastBearingOffset(entry.location, bearingOffset2, 1));
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

        private Interval[] getRange(TurnSnapshot center) {
            final Interval[] res = new Interval[AttributesManager.attributesCount()];
            for (Attribute attr : attrs) {
                double delta = halfSideLength.get(attr);
                res[attr.id] = new Interval((int) round(LXXUtils.limit(attr, center.getAttrValue(attr) - delta)),
                        (int) round(LXXUtils.limit(attr, center.getAttrValue(attr) + delta)));
            }

            return res;
        }

        private IntervalDouble[] getRange2(TurnSnapshot center) {
            final IntervalDouble[] res = new IntervalDouble[AttributesManager.attributesCount()];
            for (Attribute attr : attrs) {
                double delta = halfSideLength.get(attr);
                res[attr.id] = new IntervalDouble((int) round(LXXUtils.limit(attr, center.getAttrValue(attr) - delta)),
                        (int) round(LXXUtils.limit(attr, center.getAttrValue(attr) + delta)));
            }

            return res;
        }

        public void addEntry(PSTreeEntry<UndirectedGuessFactor> entry, LoadedKdTreeEntry<UndirectedGuessFactor> kdEntry) {
            log.addEntry(entry);
            trtLog.insert(new LoadedTRTreeEntry<UndirectedGuessFactor>(entry.predicate, entry.result));
            kdLog.addEntry(kdEntry);
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

        public void learn(PSTreeEntry<UndirectedGuessFactor> entry, LoadedKdTreeEntry<UndirectedGuessFactor> kdEntry) {
            for (Log log : visitLogsSet) {
                log.addEntry(entry, kdEntry);
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
                final PSTreeEntry<UndirectedGuessFactor> entry = new PSTreeEntry<UndirectedGuessFactor>(predicate);
                entry.result = new UndirectedGuessFactor(undirectedGuessFactor, direction);
                final LoadedKdTreeEntry<UndirectedGuessFactor> kdEntry = new LoadedKdTreeEntry<UndirectedGuessFactor>(predicate, new UndirectedGuessFactor(undirectedGuessFactor, direction));
                for (Log log : hitLogsSet) {
                    log.addEntry(entry, kdEntry);
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
