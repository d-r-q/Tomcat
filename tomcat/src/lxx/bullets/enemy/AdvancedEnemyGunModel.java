/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.LXXRobot;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.targeting.Target;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
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

    private final Map<LXXBullet, PSTreeEntry<Double>> entriesByBullets = new HashMap<LXXBullet, PSTreeEntry<Double>>();

    private final Set<LXXBullet> processedBullets = new HashSet();

    private final TurnSnapshotsLog turnSnapshotsLog;

    public AdvancedEnemyGunModel(TurnSnapshotsLog turnSnapshotsLog) {
        this.turnSnapshotsLog = turnSnapshotsLog;
    }

    public EnemyBulletPredictionData getPredictionData(Target t) {
        return getLogSet(t.getName()).getPredictionData(turnSnapshotsLog.getLastSnapshot(t, FIRE_DETECTION_LATENCY), t);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<Double> entry = new PSTreeEntry<Double>(turnSnapshotsLog.getLastSnapshot((Target) bullet.getOwner(), FIRE_DETECTION_LATENCY));
        entriesByBullets.put(bullet, entry);
    }

    public void bulletPassing(LXXBullet bullet) {
        final PSTreeEntry<Double> entry = entriesByBullets.get(bullet);
        if (processedBullets.contains(bullet)) {
            return;
        }
        final double direction = bullet.getTargetLateralDirection();

        if (direction != 0) {
            entry.result = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), bullet.getTarget()) * direction / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
            getLogSet(bullet.getOwner().getName()).learn(entry);
        } else {
            entry.result = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), bullet.getTarget()) * 1 / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
            getLogSet(bullet.getOwner().getName()).learn(entry);
            entry.result = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), bullet.getTarget()) * -1 / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
            getLogSet(bullet.getOwner().getName()).learn(entry);
        }
        processedBullets.add(bullet);
    }

    public void bulletHit(LXXBullet bullet) {
        getLogSet(bullet.getOwner().getName()).learn(bullet, entriesByBullets.get(bullet).predicate);
    }

    private static LogSet getLogSet(String enemyName) {
        LogSet logSet = logSets.get(enemyName);
        if (logSet == null) {
            logSet = LogSet.createLogSet();
            logSets.put(enemyName, logSet);
        }
        return logSet;
    }

    public void bulletIntercepted(LXXBullet bullet) {
    }

    public void bulletMiss(LXXBullet bullet) {
    }

    private static class Log {

        private PSTree<Double> log;
        private double sideLengthPercents;
        private double decayRate;
        private double efficiency = 1;
        private Attribute[] attrs;

        private Log(Attribute[] attrs, double sideLengthPercents, double decayRate) {
            this.attrs = attrs;
            this.log = new PSTree<Double>(this.attrs, 2, 0.0001);
            this.sideLengthPercents = sideLengthPercents;
            this.decayRate = decayRate;
        }

        public EnemyBulletPredictionData getPredictionData(TurnSnapshot ts, LXXRobot t) {
            final List<PastBearingOffset> bearingOffsets = getBearingOffsets(ts, t.getFirePower());

            return new EnemyBulletPredictionData(bearingOffsets, (int) ts.getAttrValue(AttributesManager.enemyOutgoingWavesCollected));
        }

        private List<PastBearingOffset> getBearingOffsets(TurnSnapshot predicate, double firePower) {
            List<PSTreeEntry<Double>> entries = log.getSimilarEntries(getLimits(predicate));

            final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                    predicate.getMySpeed(), predicate.getMyAbsoluteHeadingRadians());
            final double lateralDirection = lateralVelocity != 0 ? signum(lateralVelocity) : 1;
            final double bulletSpeed = Rules.getBulletSpeed(firePower);
            final double maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);

            final List<PastBearingOffset> bearingOffsets = new LinkedList<PastBearingOffset>();
            if (entries.size() > 0) {
                for (PSTreeEntry<Double> entry : entries.subList(0, min(69, entries.size()))) {
                    try {
                        final double danger = entry.predicate.getAttrValue(AttributesManager.enemyOutgoingWavesCollected) /
                                predicate.getAttrValue(AttributesManager.enemyOutgoingWavesCollected);
                        bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result * lateralDirection * maxEscapeAngleQuick,
                                1 * efficiency));
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                }
            }

            if (bearingOffsets.size() == 0) {
                bearingOffsets.add(new PastBearingOffset(predicate, 0, 0));
            }

            return bearingOffsets;
        }

        private Map<Attribute, Interval> getLimits(TurnSnapshot center) {
            final Map<Attribute, Interval> res = new HashMap<Attribute, Interval>();
            for (Attribute attr : attrs) {
                double delta = attr.getActualRange() * sideLengthPercents;
                res.put(attr,
                        new Interval((int) round(LXXUtils.limit(attr, center.getAttrValue(attr) - delta)),
                                (int) round(LXXUtils.limit(attr, center.getAttrValue(attr) + delta))));
            }

            return res;
        }

    }

    private static class LogSet {

        private List<Log> logSet = new ArrayList<Log>();
        private Log bestLog;

        public void learn(PSTreeEntry<Double> entry) {
            for (Log log : logSet) {
                log.log.addEntry(entry);
            }
        }

        public EnemyBulletPredictionData getPredictionData(TurnSnapshot ts, Target t) {
            final List<PastBearingOffset> bearingOffsets = new ArrayList<PastBearingOffset>();
            for (int i = 0; i < 1; i++) {
                bearingOffsets.addAll(logSet.get(i).getBearingOffsets(ts, t.getFirePower()));
            }
            return new EnemyBulletPredictionData(bearingOffsets, (int) ts.getAttrValue(AttributesManager.enemyOutgoingWavesCollected));
        }

        private static LogSet createLogSet() {
            final LogSet res = new LogSet();
            final Attribute[] possibleAttributes = {
                    AttributesManager.myAcceleration,
                    AttributesManager.distBetween,
                    AttributesManager.myDistToForwardWall,
            };

            final double[] sideLengths = {0.1};
            final double[] decayRates = {2};

            for (int i = 2; i < pow(possibleAttributes.length, 2); i++) {
                final List<Attribute> attrs = new LinkedList<Attribute>();
                attrs.add(AttributesManager.myLateralSpeed);
                for (int bit = 0; bit < possibleAttributes.length; bit++) {
                    if ((i & (1 << bit)) != 0) {
                        attrs.add(possibleAttributes[bit]);
                    }
                }

                if (attrs.size() < 2) {
                    continue;
                }

                for (double sideLength : sideLengths) {
                    for (double decayRate : decayRates) {
                        res.logSet.add(new Log(attrs.toArray(new Attribute[attrs.size()]), sideLength, decayRate));
                    }
                }
            }

            res.bestLog = res.logSet.get(res.logSet.size() - 1);

            return res;
        }

        public void learn(LXXBullet bullet, TurnSnapshot predicate) {
            for (Log log : logSet) {
                final EnemyBulletPredictionData ebpd = log.getPredictionData(predicate, bullet.getOwner());
                double logEfficiency = calculateEfficiency(bullet, ebpd);
                log.efficiency = log.efficiency * 0.75 + logEfficiency * 0.25;
                if (log.efficiency > bestLog.efficiency) {
                    bestLog = log;
                }
            }

            for (Log log : logSet) {
                if (bestLog.efficiency > 0) {
                    log.efficiency = log.efficiency / bestLog.efficiency;
                }
            }

            Collections.sort(logSet, new Comparator<Log>() {
                public int compare(Log o1, Log o2) {
                    return (int) signum(o2.efficiency - o1.efficiency);
                }
            });
        }

        private double calculateEfficiency(LXXBullet bullet, EnemyBulletPredictionData ebpd) {
            final double robotHalfSizeRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), bullet.getTarget()) / 2;
            final double currentBO = LXXUtils.bearingOffset(bullet.getFirePosition(), bullet.getTargetStateAtFireTime(), bullet.getTarget());
            final IntervalDouble robotIntervalRadians = new IntervalDouble(currentBO - robotHalfSizeRadians, currentBO + robotHalfSizeRadians);

            double totalDanger = 0;
            double realDanger = 0;
            int currentRound = bullet.getOwner().getRound();
            for (PastBearingOffset pastBo : ebpd.getPredictedBearingOffsets()) {
                if (pastBo.source.getRound() == currentRound && pastBo.source.getTime() >= bullet.getWave().getLaunchTime()) {
                    continue;
                }

                totalDanger += pastBo.danger;
                if (robotIntervalRadians.contains(pastBo.bearingOffset)) {
                    realDanger += pastBo.danger;
                }
            }

            if (totalDanger == 0) {
                return 0;
            }

            return realDanger / totalDanger;
        }
    }

}
