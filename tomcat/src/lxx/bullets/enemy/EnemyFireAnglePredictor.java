/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.ps_tree.EntryMatch;
import lxx.targeting.Target;
import lxx.utils.AimingPredictionData;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.signum;

public class EnemyFireAnglePredictor implements BulletManagerListener {

    private static final double A = 0.02;
    private static final int B = 20;

    private static final int FIRE_DETECTION_LATENCY = 1;
    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_1;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private static final Map<String, PSTree<Double>> logs = new HashMap<String, PSTree<Double>>();

    private final Map<LXXBullet, PSTreeEntry<Double>> entriesByBullets = new HashMap<LXXBullet, PSTreeEntry<Double>>();

    private final TurnSnapshotsLog turnSnapshotsLog;

    public EnemyFireAnglePredictor(TurnSnapshotsLog turnSnapshotsLog) {
        this.turnSnapshotsLog = turnSnapshotsLog;
    }

    public AimingPredictionData getPredictionData(Target t) {
        final PSTree<Double> log = getLog(t.getName());

        final TurnSnapshot predicate = turnSnapshotsLog.getLastSnapshot(t, FIRE_DETECTION_LATENCY);
        final List<Double> bearingOffsets = getBearingOffsets(log, predicate, t.getFirePower());

        final int bearingOffsetsCount = bearingOffsets.size();
        final Map<Double, Double> bearingOffsetDangers = new TreeMap<Double, Double>();
        for (double wavePointBearingOffset = -MAX_BEARING_OFFSET; wavePointBearingOffset <= MAX_BEARING_OFFSET + LXXConstants.RADIANS_0_1; wavePointBearingOffset += BEARING_OFFSET_STEP) {
            double bearingOffsetDanger = 0;
            if (bearingOffsetsCount > 0) {
                for (Double bulletBearingOffset : bearingOffsets) {
                    // this is empirical selected formula, which
                    // produce smooth peaks for bearing offsets
                    final double difference = bulletBearingOffset - wavePointBearingOffset;
                    final double differenceSquare = difference * difference;
                    final double bearingOffsetsDifference = differenceSquare + A;
                    bearingOffsetDanger += 1D / (bearingOffsetsDifference * B);
                }
            }
            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
        }

        return new GFAimingPredictionData(bearingOffsetDangers);

    }

    private List<Double> getBearingOffsets(PSTree<Double> log, TurnSnapshot predicate, double firePower) {
        final List<EntryMatch<Double>> matches = log.getSimilarEntries(predicate, 1);
        final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                predicate.getMyVelocityModule(), predicate.getMyAbsoluteHeadingRadians());
        final double lateralDirection = signum(lateralVelocity);
        final List<Double> bearingOffsets = new LinkedList<Double>();
        if (matches.size() > 0) {
            for (EntryMatch<Double> match : matches) {
                bearingOffsets.add(match.result * lateralDirection);
            }
        } else {
            final double maxEscapeAngle = LXXUtils.getMaxEscapeAngle(Rules.getBulletSpeed(firePower));
            bearingOffsets.add(maxEscapeAngle * lateralDirection);
            bearingOffsets.add(0D);
        }

        return bearingOffsets;
    }

    private static PSTree<Double> getLog(String enemyName) {
        PSTree<Double> log = logs.get(enemyName);
        if (log == null) {
            log = createLog();
            logs.put(enemyName, log);
        }
        return log;
    }

    private static PSTree<Double> createLog() {
        final Attribute[] splitAttributes = {
                AttributesManager.myLateralVelocity_2,
        };
        return new PSTree<Double>(splitAttributes, 2, 0.02);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<Double> entry = new PSTreeEntry<Double>(turnSnapshotsLog.getLastSnapshots((Target) bullet.getOwner(), FIRE_DETECTION_LATENCY).get(0));
        entriesByBullets.put(bullet, entry);
    }

    public void bulletHit(LXXBullet bullet) {
        setBulletBearingOffset(bullet);
        entriesByBullets.remove(bullet);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        setBulletBearingOffset(bullet);
        entriesByBullets.remove(bullet);
    }

    public void setBulletBearingOffset(LXXBullet bullet) {
        final double bearingOffset = bullet.getRealBearingOffsetRadians() * bullet.getTargetLateralDirection();
        final PSTree<Double> log = getLog(bullet.getOwner().getName());

        final PSTreeEntry<Double> entry = entriesByBullets.get(bullet);
        entry.result = bearingOffset;
        log.addEntry(entry);
    }


    public void bulletMiss(LXXBullet bullet) {
        entriesByBullets.remove(bullet);
    }

    public void bulletPassing(LXXBullet bullet) {
    }
}