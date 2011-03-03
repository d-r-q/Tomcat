/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets;

import lxx.fire_log.EntryMatch;
import lxx.fire_log.FireLog;
import lxx.fire_log.FireLogEntry;
import lxx.model.BattleSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.office.BattleSnapshotManager;
import lxx.targeting.Target;
import lxx.utils.*;
import lxx.wave.Wave;
import robocode.Rules;
import robocode.util.Utils;

import java.util.*;

import static java.lang.Math.*;

public class EnemyFireAnglePredictor {

    private static final double A = 0.02;
    private static final int B = 20;

    private static final int FIRE_DETECTION_LATENCY = 1;
    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_1;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private static final Map<String, FireLog<Double>> logs = new HashMap<String, FireLog<Double>>();

    private final Map<Wave, FireLogEntry<Double>> entriesByWaves = new HashMap<Wave, FireLogEntry<Double>>();

    private final BattleSnapshotManager battleSnapshotManager;

    public EnemyFireAnglePredictor(BattleSnapshotManager battleSnapshotManager) {
        this.battleSnapshotManager = battleSnapshotManager;
    }

    public void enemyFire(Wave wave) {
        FireLogEntry<Double> e = new FireLogEntry<Double>(new LXXPoint(wave.getSourceStateAtFireTime()), wave.getTargetStateAtFireTime(),
                battleSnapshotManager.getLastSnapshots((Target) wave.getSourceStateAtFireTime().getRobot(), FIRE_DETECTION_LATENCY).get(0));
        entriesByWaves.put(wave, e);
    }

    // todo(zhidkov): add flat movement
    @SuppressWarnings({"UnusedDeclaration"})
    public void updateWaveState(Wave w) {
        updateWaveState(w, w.getSourcePosAtFireTime().angleTo(w.getTargetStateAtFireTime().getRobot()));
    }

    // todo(zhidkov): rename
    public void updateWaveState(Wave w, double bulletHeading) {
        final BattleSnapshot bs = battleSnapshotManager.getSnapshotByRoundTime(w.getSourceStateAtFireTime().getRobot().getName(), w.getLaunchTime());
        final double lateralVelocity = LXXUtils.lateralVelocity(w.getSourceStateAtFireTime(), w.getTargetStateAtFireTime(),
                bs.getAttrValue(AttributesManager.myVelocityModule), toRadians(bs.getAttrValue(AttributesManager.myAbsoluteHeading)));
        final double lateralDirection = signum(lateralVelocity);
        final double maxEscapeAngle = getMaxEscapeAngle(w.getSpeed());

        final Double guessFactor = maxEscapeAngle == 0 ? 0 : Utils.normalRelativeAngle(bulletHeading - w.getSourcePosAtFireTime().angleTo(w.getTargetPosAtFireTime())) * lateralDirection / maxEscapeAngle;
        addEntry(w, guessFactor);
    }

    private double getMaxEscapeAngle(double bulletSpeed) {
        return QuickMath.asin(Rules.MAX_VELOCITY / bulletSpeed);
    }

    private void addEntry(Wave w, Double guessFactor) {
        final FireLog<Double> log = getLog(w.getSourceStateAtFireTime().getRobot().getName());

        final FireLogEntry<Double> entry = entriesByWaves.get(w);
        entry.result = guessFactor;
        log.addEntry(entry);
    }

    public AimingPredictionData getPredictionData(Target t) {
        final FireLog<Double> log = getLog(t.getName());

        final List<Double> bearingOffsets = new ArrayList<Double>();
        final BattleSnapshot predicate1 = battleSnapshotManager.getLastSnapshot(t, FIRE_DETECTION_LATENCY);
        getBearingOffsets(log, bearingOffsets, predicate1, t.getFirePower());

        final int bearingOffsetsCount = bearingOffsets.size();
        final Map<Double, Double> bearingOffsetDangers = new TreeMap<Double, Double>();
        for (double wavePointBearingOffset = -MAX_BEARING_OFFSET; wavePointBearingOffset <= MAX_BEARING_OFFSET + LXXConstants.RADIANS_0_1; wavePointBearingOffset += BEARING_OFFSET_STEP) {
            double bearingOffsetDanger = 0;
            if (bearingOffsetsCount > 0) {
                for (Double bulletBearingOffset : bearingOffsets) {
                    // this is empirical selected formula, which
                    // produce smooth peaks for bearing offsets with predicted bullets
                    final double difference = bulletBearingOffset - wavePointBearingOffset;
                    final double differenceSquare = difference * difference;
                    final double bearingOffsetsDifference = differenceSquare + A;
                    bearingOffsetDanger += 1D / (bearingOffsetsDifference * B);
                }
            }
            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
        }

        return new EnemyAimingPredictionData(bearingOffsetDangers);

    }

    private void getBearingOffsets(FireLog<Double> log, List<Double> bearingOffsets, BattleSnapshot predicate, double firePower) {
        final List<EntryMatch<Double>> matches = log.getSimilarEntries(predicate, 1000);
        final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                predicate.getAttrValue(AttributesManager.myVelocityModule), toRadians(predicate.getAttrValue(AttributesManager.myAbsoluteHeading)));
        final double lateralDirection = signum(lateralVelocity);
        final double maxEscapeAngle = getMaxEscapeAngle(Rules.getBulletSpeed(firePower));
        if (matches.size() > 0) {
            for (EntryMatch<Double> match : matches) {
                if (bearingOffsets.size() > ceil(log.getEntryCount() * 0.2)) {
                    break;
                }

                bearingOffsets.add(match.result * lateralDirection * maxEscapeAngle);
            }
        } else {
            bearingOffsets.add(0D);
            bearingOffsets.add(maxEscapeAngle * lateralDirection);
        }
    }

    private static FireLog<Double> getLog(String enemyName) {
        FireLog<Double> log = logs.get(enemyName);
        if (log == null) {
            log = createLog();
            logs.put(enemyName, log);
        }
        return log;
    }

    private static FireLog<Double> createLog() {
        final Attribute[] splitAttributes = {
                AttributesManager.myVelocityModule, AttributesManager.myRelativeHeading,
                AttributesManager.distBetween, AttributesManager.myDistToForwardWall};
        final double[] attrWeights = new double[AttributesManager.attributesCount()];
        double weight = 1;
        for (Attribute a : splitAttributes) {
            attrWeights[a.getId()] = weight;
            weight = (weight * 2) + 0.5;
        }
        return new FireLog<Double>(splitAttributes, splitAttributes, attrWeights, 2, 0.02);
    }

}