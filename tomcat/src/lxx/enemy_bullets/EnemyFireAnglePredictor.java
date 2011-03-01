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
import lxx.utils.AimingPredictionData;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.LXXUtils;
import lxx.wave.Wave;
import robocode.util.Utils;

import java.util.*;

import static java.lang.Math.signum;
import static java.lang.Math.toRadians;

public class EnemyFireAnglePredictor {

    private static final double A = 0.0125;
    private static final int B = 20;

    private static final int FIRE_DETECTION_LATENCY = 2;
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
                battleSnapshotManager.getLastSnapshots((Target) wave.getSourceStateAtFireTime().getRobot(), 2).get(0));
        entriesByWaves.put(wave, e);
    }

    // todo(zhidkov): add flat movement
    @SuppressWarnings({"UnusedDeclaration"})
    public void updateWaveState(Wave w) {
        updateWaveState(w, w.getSourcePos().angleTo(w.getTargetStateAtFireTime().getRobot()));
    }

    // todo(zhidkov): rename
    public void updateWaveState(Wave w, double bulletHeading) {
        final BattleSnapshot bs = battleSnapshotManager.getSnapshotByRoundTime(w.getSourceStateAtFireTime().getRobot().getName(), w.getLaunchTime());
        final double lateralDirection = signum(LXXUtils.lateralVelocity2(w.getSourceStateAtFireTime(), w.getTargetStateAtFireTime(),
                bs.getAttrValue(AttributesManager.myVelocityModule), toRadians(bs.getAttrValue(AttributesManager.myAbsoluteHeading))));
        final Double bearingOffsetRadians = Utils.normalRelativeAngle(bulletHeading - w.getSourcePos().angleTo(w.getTargetPos())) * lateralDirection;
        addEntry(w, bearingOffsetRadians);
    }

    private void addEntry(Wave w, Double bearingOffsetRadians) {
        final FireLog<Double> log = getLog(w.getSourceStateAtFireTime().getRobot().getName());

        final FireLogEntry<Double> entry = entriesByWaves.get(w);
        entry.result = bearingOffsetRadians;
        log.addEntry(entry);
    }

    public AimingPredictionData getPredictionData(Target t) {
        final FireLog<Double> log = getLog(t.getName());

        final List<Double> bearingOffsets = new ArrayList<Double>();
        final BattleSnapshot predicate = battleSnapshotManager.getLastSnapshot(t, FIRE_DETECTION_LATENCY);
        if (predicate != null) {
            final List<EntryMatch<Double>> matches = log.getSimilarEntries(predicate, 1000);
            if (matches.size() > 0) {
                final double lateralDirection = signum(LXXUtils.lateralVelocity2(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                        predicate.getAttrValue(AttributesManager.myVelocityModule), toRadians(predicate.getAttrValue(AttributesManager.myAbsoluteHeading))));
                final double matchLimit = matches.get(0).match * 1.7;

                for (EntryMatch<Double> match : matches) {
                    if (match.match > matchLimit) {
                        break;
                    }

                    bearingOffsets.add(match.result * lateralDirection);
                }
            } else {
                bearingOffsets.add(0D);
            }
        }

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
                    bearingOffsetDanger += 1 / (bearingOffsetsDifference * B);
                }
            }
            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
        }

        return new EnemyAimingPredictionData(bearingOffsetDangers);

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
                AttributesManager.myVelocityModule, AttributesManager.distBetween,
                AttributesManager.myDistToForwardWall};
        final double[] attrWeights = new double[AttributesManager.attributesCount()];
        double weight = 1;
        for (Attribute a : splitAttributes) {
            attrWeights[a.getId()] = weight;
            weight = (weight * 2) + 0.5;
        }
        return new FireLog<Double>(splitAttributes, splitAttributes, attrWeights, 2, 0.02);
    }

}