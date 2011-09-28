/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.LXXRobot;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.office.Office;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.AvgValue;
import lxx.utils.Interval;
import lxx.utils.IntervalDouble;
import lxx.utils.LXXUtils;
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.StrictMath.min;
import static java.lang.StrictMath.signum;

public class AdvancedEnemyGunModel implements BulletManagerListener {

    private static final int FIRE_DETECTION_LATENCY = 2;

    private static final Map<String, LogSet> logSets = new HashMap<String, LogSet>();

    private final Map<LXXBullet, PSTreeEntry<UndirectedGuessFactor>> entriesByBullets = new HashMap<LXXBullet, PSTreeEntry<UndirectedGuessFactor>>();
    private final Set<LXXBullet> processedBullets = new HashSet<LXXBullet>();

    private final TurnSnapshotsLog turnSnapshotsLog;
    private final Office office;

    public AdvancedEnemyGunModel(TurnSnapshotsLog turnSnapshotsLog, Office office) {
        this.turnSnapshotsLog = turnSnapshotsLog;
        this.office = office;
    }

    public EnemyBulletPredictionData getPredictionData(Target t) {
        return getLogSet(t.getName()).getPredictionData(turnSnapshotsLog.getLastSnapshot(t, FIRE_DETECTION_LATENCY), t);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<UndirectedGuessFactor> entry = new PSTreeEntry<UndirectedGuessFactor>(turnSnapshotsLog.getLastSnapshot((Target) bullet.getOwner(), FIRE_DETECTION_LATENCY));
        entriesByBullets.put(bullet, entry);
    }

    public void bulletPassing(LXXBullet bullet) {
        final PSTreeEntry<UndirectedGuessFactor> entry = entriesByBullets.get(bullet);
        if (processedBullets.contains(bullet)) {
            return;
        }

        final double direction = bullet.getTargetLateralDirection();
        double undirectedGuessFactor = bullet.getBearingOffsetRadians(bullet.getTarget().getPosition()) / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
        entry.result = new UndirectedGuessFactor(undirectedGuessFactor, direction);
        getLogSet(bullet.getOwner().getName()).learn(entry);

        processedBullets.add(bullet);
    }

    public void bulletHit(LXXBullet bullet) {
        getLogSet(bullet.getOwner().getName()).learn(bullet, entriesByBullets.get(bullet).predicate, true);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        getLogSet(bullet.getOwner().getName()).learn(bullet, entriesByBullets.get(bullet).predicate, true);
    }

    public void bulletMiss(LXXBullet bullet) {
        getLogSet(bullet.getOwner().getName()).learn(bullet, entriesByBullets.get(bullet).predicate, false);
    }

    private LogSet getLogSet(String enemyName) {
        LogSet logSet = logSets.get(enemyName);
        if (logSet == null) {
            logSet = createLogSet();
            logSets.put(enemyName, logSet);
        }
        return logSet;
    }

    private class Log {

        private PSTree<UndirectedGuessFactor> log;
        private Map<Attribute, Double> halfSideLength = LXXUtils.toMap(
                AttributesManager.myLateralSpeed, 2D,
                AttributesManager.myAcceleration, 0D,
                AttributesManager.distBetween, 75D,
                AttributesManager.myDistToForwardWall, 50D);
        private double efficiency = 1;

        private final AvgValue shortAvgHitRate = new AvgValue(3);
        private final AvgValue midAvgHitRate = new AvgValue(11);
        private final AvgValue longAvgHitRate = new AvgValue(100);

        private final AvgValue shortAvgMissRate = new AvgValue(3);
        private final AvgValue midAvgMissRate = new AvgValue(11);
        private final AvgValue longAvgMissRate = new AvgValue(100);

        private Attribute[] attrs;
        public int usage = 0;

        private Log(Attribute[] attrs) {
            this.attrs = attrs;
            this.log = new PSTree<UndirectedGuessFactor>(this.attrs, 2, 0.0001);
        }

        public EnemyBulletPredictionData getPredictionData(TurnSnapshot ts, LXXRobot t) {
            final List<PastBearingOffset> bearingOffsets = getBearingOffsets(ts, t.getFirePower());

            return new EnemyBulletPredictionData(bearingOffsets, (int) ts.getAttrValue(AttributesManager.enemyOutgoingWavesCollected), 0);
        }

        private List<PastBearingOffset> getBearingOffsets(TurnSnapshot predicate, double firePower) {
            final List<PSTreeEntry<UndirectedGuessFactor>> entries = log.getSimilarEntries(getLimits(predicate));
            Collections.sort(entries, new Comparator<PSTreeEntry>() {
                public int compare(PSTreeEntry o1, PSTreeEntry o2) {
                    return o2.predicate.roundTime - o1.predicate.roundTime;
                }
            });

            final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                    predicate.getMySpeed(), predicate.getMyAbsoluteHeadingRadians());
            final double lateralDirection = lateralVelocity != 0 ? signum(lateralVelocity) : 1;
            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            final double maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);

            final List<PastBearingOffset> bearingOffsets = new LinkedList<PastBearingOffset>();
            if (entries.size() > 0) {
                for (PSTreeEntry<UndirectedGuessFactor> entry : entries.subList(0, min(3, entries.size()))) {
                    try {
                        if (entry.result.lateralDirection != 0 && lateralDirection != 0) {
                            bearingOffsets.add(new PastBearingOffset(entry.predicate,
                                    entry.result.guessFactor * entry.result.lateralDirection * lateralDirection * maxEscapeAngleQuick,
                                    1));
                        } else {
                            bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * 1 * maxEscapeAngleQuick, 1));
                            bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * -1 * maxEscapeAngleQuick, 1));
                        }
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                }
            }

            return bearingOffsets;
        }

        private Map<Attribute, Interval> getLimits(TurnSnapshot center) {
            final Map<Attribute, Interval> res = new HashMap<Attribute, Interval>();
            for (Attribute attr : attrs) {
                double delta = halfSideLength.get(attr);
                res.put(attr,
                        new Interval((int) round(LXXUtils.limit(attr, center.getAttrValue(attr) - delta)),
                                (int) round(LXXUtils.limit(attr, center.getAttrValue(attr) + delta))));
            }

            return res;
        }

    }

    private class LogSet {

        private int FIRST_SHORT_IDX = 0;
        private int SECOND_SHORT_IDX = 1;
        private int FIRST_MID_IDX = 2;
        private int SECOND_MID_IDX = 3;
        private int FIRST_LONG_IDX = 4;
        private int SECOND_LONG_IDX = 5;

        private List<Log> hitLogsSet = new ArrayList<Log>();
        private List<Log> visitLogsSet = new ArrayList<Log>();

        public void learn(PSTreeEntry<UndirectedGuessFactor> entry) {
            for (Log log : visitLogsSet) {
                log.log.addEntry(entry);
            }
        }

        public EnemyBulletPredictionData getPredictionData(TurnSnapshot ts, Target t) {
            final List<PastBearingOffset> bearingOffsets = new ArrayList<PastBearingOffset>();

            final Log[] bestLogs = new Log[6];
            final List<Log> allLogs = new ArrayList<Log>(hitLogsSet);
            allLogs.addAll(visitLogsSet);
            for (Log hitLog : allLogs) {
                updateBestLog(bestLogs, hitLog, FIRST_SHORT_IDX, 0);
                updateBestLog(bestLogs, hitLog, SECOND_SHORT_IDX, 0);
                updateBestLog(bestLogs, hitLog, FIRST_MID_IDX, 1);
                updateBestLog(bestLogs, hitLog, SECOND_MID_IDX, 1);
                updateBestLog(bestLogs, hitLog, FIRST_LONG_IDX, 2);
                updateBestLog(bestLogs, hitLog, SECOND_LONG_IDX, 2);
            }

            for (Log log : bestLogs) {
                bearingOffsets.addAll(log.getBearingOffsets(ts, t.getFirePower()));
                log.usage++;
            }

            if (bearingOffsets.size() == 0) {
                final GunType enemyGunType = office.getTomcatEyes().getEnemyGunType(t);
                fillWithSimpleBOs(ts, t, bearingOffsets, enemyGunType);
            }

            return new EnemyBulletPredictionData(bearingOffsets, (int) ts.getAttrValue(AttributesManager.enemyOutgoingWavesCollected), t.getTime());
        }

        private void updateBestLog(Log[] bestLogs, Log log, int idx, int type) {
            // todo(zhidkov): refactor this!
            boolean isFirst = (idx % 2) == 1 && log == bestLogs[idx - 1];
            if (isFirst) {
                return;
            }
            switch (type) {
                case 0:
                    if (bestLogs[idx] == null ||
                            (bestLogs[idx].shortAvgHitRate.getCurrentValue() - bestLogs[idx].shortAvgMissRate.getCurrentValue() <
                                    log.shortAvgHitRate.getCurrentValue() - log.shortAvgMissRate.getCurrentValue())) {
                        bestLogs[idx] = log;
                    }
                    break;
                case 1:
                    if (bestLogs[idx] == null ||
                            (bestLogs[idx].midAvgHitRate.getCurrentValue() - bestLogs[idx].midAvgMissRate.getCurrentValue() <
                                    log.midAvgHitRate.getCurrentValue() - log.midAvgMissRate.getCurrentValue())) {
                        bestLogs[idx] = log;
                    }
                    break;
                case 2:
                    if (bestLogs[idx] == null ||
                            (bestLogs[idx].longAvgHitRate.getCurrentValue() - bestLogs[idx].longAvgMissRate.getCurrentValue() <
                                    log.longAvgHitRate.getCurrentValue() - log.longAvgMissRate.getCurrentValue())) {
                        bestLogs[idx] = log;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + type);
            }
        }

        private void fillWithSimpleBOs(TurnSnapshot ts, Target t, List<PastBearingOffset> bearingOffsets, GunType enemyGunType) {
            final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(ts), LXXUtils.getMyPos(ts),
                    ts.getMySpeed(), ts.getMyAbsoluteHeadingRadians());
            final double lateralDirection = Math.signum(lateralVelocity);
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
            recalculateLogSetEfficiency(bullet, predicate, visitLogsSet, isHit);
            recalculateLogSetEfficiency(bullet, predicate, hitLogsSet, isHit);

            if (isHit) {
                final double direction = bullet.getTargetLateralDirection();
                final double undirectedGuessFactor = bullet.getRealBearingOffsetRadians() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
                final PSTreeEntry<UndirectedGuessFactor> entry = new PSTreeEntry<UndirectedGuessFactor>(predicate);
                entry.result = new UndirectedGuessFactor(undirectedGuessFactor, direction);
                for (Log log : hitLogsSet) {
                    log.log.addEntry(entry);
                }
            }
        }

        private void recalculateLogSetEfficiency(LXXBullet bullet, TurnSnapshot predicate, List<Log> logSet, boolean isHit) {
            Log bestVisitLog = null;
            for (Log log : logSet) {
                final EnemyBulletPredictionData ebpd = log.getPredictionData(predicate, bullet.getOwner());
                double logEfficiency = calculateEfficiency(bullet, ebpd, isHit);
                if (isHit) {
                    log.shortAvgHitRate.addValue(logEfficiency);
                    log.midAvgHitRate.addValue(logEfficiency);
                    log.longAvgHitRate.addValue(logEfficiency);
                } else {
                    log.shortAvgMissRate.addValue(logEfficiency);
                    log.midAvgMissRate.addValue(logEfficiency);
                    log.longAvgMissRate.addValue(logEfficiency);
                }
                log.efficiency = log.efficiency * 0.9 + logEfficiency;
                if (bestVisitLog == null || log.efficiency > bestVisitLog.efficiency) {
                    bestVisitLog = log;
                }
            }

            if (bestVisitLog != null && bestVisitLog.efficiency > 0) {
                final double efficiency = bestVisitLog.efficiency;
                for (Log log : logSet) {
                    log.efficiency = log.efficiency / efficiency;
                }
            }

            /*Collections.sort(logSet, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum(o2.efficiency - o1.efficiency);
                }
            });*/
        }

        private double calculateEfficiency(LXXBullet bullet, EnemyBulletPredictionData ebpd, boolean isHit) {
            final IntervalDouble effectiveInterval;
            if (isHit) {
                final double robotHalfSizeRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), bullet.getTarget()) / 2;
                final double currentBO = bullet.getRealBearingOffsetRadians();
                effectiveInterval = new IntervalDouble(currentBO - robotHalfSizeRadians, currentBO + robotHalfSizeRadians);
            } else {
                final IntervalDouble hitInterval = bullet.getWave().getHitInterval();
                effectiveInterval = new IntervalDouble(hitInterval.center() - hitInterval.getLength() * 0.4,
                        hitInterval.center() + hitInterval.getLength() * 0.4);
            }

            double totalDanger = 0;
            double realDanger = 0;
            int currentRound = bullet.getOwner().getRound();
            for (PastBearingOffset pastBo : ebpd.getPredictedBearingOffsets()) {
                // todo: it's dirty hack! rewrite it!
                if (pastBo.source.getRound() == currentRound && pastBo.source.getTime() >= bullet.getWave().getLaunchTime() - 3) {
                    continue;
                }

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

        return res;
    }

    private List<Log> createVisitLogs() {
        final Attribute[] possibleAttributes = {
                AttributesManager.myLateralSpeed,
                AttributesManager.myAcceleration,
                AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall,
        };

        return createLogs(possibleAttributes, new Attribute[0], 1, Integer.MAX_VALUE);
    }

    private List<Log> createHitLogs() {
        final Attribute[] possibleAttributes = {
                AttributesManager.myAcceleration,
                AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall,
        };
        return createLogs(possibleAttributes, new Attribute[]{AttributesManager.myLateralSpeed}, 1, Integer.MAX_VALUE);
    }

    private List<Log> createLogs(Attribute[] possibleAttributes, Attribute[] requiredAttributes,
                                 int minElements, int maxElements) {
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
            logs.add(new Log(attrs.toArray(new Attribute[attrs.size()])));
        }
        return logs;
    }

}
