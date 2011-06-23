/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.AimingPredictionData;
import lxx.utils.Interval;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;
import robocode.Rules;

import java.util.*;

import static java.lang.Math.round;
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
    private final Tomcat robot;
    private final TomcatEyes tomcatEyes;

    public EnemyFireAnglePredictor(TurnSnapshotsLog turnSnapshotsLog, Tomcat robot, TomcatEyes tomcatEyes) {
        this.turnSnapshotsLog = turnSnapshotsLog;
        this.robot = robot;
        this.tomcatEyes = tomcatEyes;
    }

    public AimingPredictionData getPredictionData(Target t) {
        final PSTree<Double> log = getLog(t.getName());

        final TurnSnapshot predicate = turnSnapshotsLog.getLastSnapshot(t, FIRE_DETECTION_LATENCY);
        final List<Double> bearingOffsets = getBearingOffsets(log, predicate, t.getFirePower(), t);

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

        return new EnemyBulletsPredictionData(bearingOffsetDangers, bearingOffsets);

    }

    private List<Double> getBearingOffsets(PSTree<Double> log, TurnSnapshot predicate, double firePower, Target t) {
        final List<PSTreeEntry<Double>> entries = log.getSimilarEntries(getLimits(predicate));
        final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                predicate.getMySpeed(), predicate.getMyAbsoluteHeadingRadians());
        final double lateralDirection = signum(lateralVelocity);
        final List<Double> bearingOffsets = new LinkedList<Double>();
        if (entries.size() > 0) {
            for (PSTreeEntry<Double> entry : entries) {
                bearingOffsets.add(entry.result * lateralDirection);
            }
        } else {
            final GunType enemyGunType = tomcatEyes.getEnemyGunType(t);
            if (enemyGunType != GunType.HEAD_ON) {
                final double maxEscapeAngle = LXXUtils.getMaxEscapeAngle(t, robot.getState(), Rules.getBulletSpeed(firePower));
                bearingOffsets.add(maxEscapeAngle * lateralDirection);
            }
            if (enemyGunType == GunType.UNKNOWN || enemyGunType == GunType.HEAD_ON) {
                bearingOffsets.add(0D);
            }
        }

        return bearingOffsets;
    }

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts) {
        return LXXUtils.toMap(AttributesManager.myLateralSpeed,
                new Interval((int) round(LXXUtils.limit(AttributesManager.myLateralSpeed, ts.getAttrValue(AttributesManager.myLateralSpeed) - 1)),
                        (int) round(LXXUtils.limit(AttributesManager.myLateralSpeed, ts.getAttrValue(AttributesManager.myLateralSpeed) + 1))));
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
                AttributesManager.myLateralSpeed,
        };
        return new PSTree<Double>(splitAttributes, 2, 0.001);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<Double> entry = new PSTreeEntry<Double>(turnSnapshotsLog.getLastSnapshot((Target) bullet.getOwner(), FIRE_DETECTION_LATENCY));
        entriesByBullets.put(bullet, entry);
    }

    public void bulletHit(LXXBullet bullet) {
        setBulletGF(bullet);
        entriesByBullets.remove(bullet);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        setBulletGF(bullet);
        entriesByBullets.remove(bullet);
    }

    public void setBulletGF(LXXBullet bullet) {
        final double guessFactor = bullet.getRealBearingOffsetRadians() * bullet.getTargetLateralDirection();
        final PSTree<Double> log = getLog(bullet.getOwner().getName());

        final PSTreeEntry<Double> entry = entriesByBullets.get(bullet);
        entry.result = guessFactor;
        log.addEntry(entry);
    }


    public void bulletMiss(LXXBullet bullet) {
        entriesByBullets.remove(bullet);
    }

    public void bulletPassing(LXXBullet bullet) {
    }
}