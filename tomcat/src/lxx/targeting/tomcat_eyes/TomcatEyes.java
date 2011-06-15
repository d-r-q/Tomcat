/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.Tomcat;
import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.office.TurnSnapshotsLog;
import lxx.strategies.MovementDecision;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.BulletManagerListener;
import lxx.targeting.bullets.LXXBullet;
import lxx.targeting.classification.AdjustingClassifier;
import lxx.targeting.classification.ComplexMovementClassifier;
import lxx.targeting.classification.ProbCMC;
import lxx.targeting.classification.SegmentationTreeMovementClassifier;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TomcatEyes implements TargetManagerListener, BulletManagerListener {

    private static final Attribute[] wallsAttributes = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyBearingToForwardWall,
            AttributesManager.enemyStopTime,
            AttributesManager.enemyDistanceToCenter,
            AttributesManager.enemyBearingToMe,
    };

    private static final Attribute[] crazyAttributes = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyTurnTime,
            AttributesManager.enemyTurnRate,
            AttributesManager.enemyDistanceToCenter,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToForwardWall,
    };

    private static final Attribute[] drussAttributes = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyAcceleration,
            AttributesManager.firstBulletFlightTime,
            AttributesManager.enemyTravelTime,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyDistanceToCenter,
    };

    private static final Attribute[] doctorBobAttributes = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyBearingToMe,
            AttributesManager.distBetween,
            AttributesManager.enemyTravelTime,
            AttributesManager.enemyStopTime,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToForwardWall,
    };

    private static final Attribute[] drussAccelAttrs = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyAcceleration,
            AttributesManager.firstBulletFlightTime,
            AttributesManager.firstBulletBearingOffset,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingOffsetOnFirstBullet,
            AttributesManager.enemyBearingOffsetOnSecondBullet,
            AttributesManager.enemyTravelTime,
    };

    private static final Map<Attribute, Integer> drussAccelRanges = new HashMap<Attribute, Integer>();

    static {
        drussAccelRanges.put(AttributesManager.enemyVelocity, 0);
        drussAccelRanges.put(AttributesManager.enemyAcceleration, 0);
        drussAccelRanges.put(AttributesManager.firstBulletFlightTime, 2);
        drussAccelRanges.put(AttributesManager.enemyBearingOffsetOnFirstBullet, 10);
        drussAccelRanges.put(AttributesManager.enemyBearingOffsetOnSecondBullet, 15);
        drussAccelRanges.put(AttributesManager.enemyDistanceToForwardWall, 25);
        drussAccelRanges.put(AttributesManager.enemyTravelTime, 3);
        drussAccelRanges.put(AttributesManager.firstBulletBearingOffset, 2);
    }

    private static final Attribute[] drussTurnAttrs = new Attribute[]{
            AttributesManager.enemyVelocityModule,
            AttributesManager.enemyBearingToFirstBullet,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyTurnRate,
    };

    private static final Attribute[] ocnirpAccelAttrs = new Attribute[]{
            AttributesManager.enemyVelocity,
            AttributesManager.enemyAcceleration,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyTravelTime,
    };

    private static final Map<Attribute, Integer> ocnirpAccelRanges = new HashMap<Attribute, Integer>();

    static {
        ocnirpAccelRanges.put(AttributesManager.enemyVelocity, 0);
        ocnirpAccelRanges.put(AttributesManager.enemyAcceleration, 0);
        ocnirpAccelRanges.put(AttributesManager.enemyTravelTime, 1);
        ocnirpAccelRanges.put(AttributesManager.enemyDistanceToForwardWall, 30);
    }

    private static final Attribute[] ocnirpTurnAttrs = new Attribute[]{
            AttributesManager.enemyVelocityModule,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyTurnRate,
    };

    private static final Map<double[], TargetingConfiguration> targetingConfigurations = new HashMap<double[], TargetingConfiguration>();

    static {
        targetingConfigurations.put(new double[]{6.271, 0.989, 1.341, 1.142, 53.924, 90.174, 449.76, 29.939, 465.97, 359.19, 88.09, 6.7, 0.1, 276.46}, getTargetingConfig("Walls", wallsAttributes, 0.001));

        final TargetingConfiguration crazyTC = getTargetingConfig("Crazy", crazyAttributes, 0.001);
        targetingConfigurations.put(new double[]{3.107, 0.657, 0.452, 4.394, 46.750, 82.177, 378.81, 38.499, 376.19, 250.80, 78.71, 7.2, 0.2, 311.06}, crazyTC);

        final ComplexMovementClassifier drussCMC = new ComplexMovementClassifier(drussAccelAttrs, drussTurnAttrs, drussAccelRanges);
        final TargetingConfiguration drussTC =
                new TargetingConfiguration("druss", drussCMC, drussCMC.getAttributes());
        targetingConfigurations.put(new double[]{-0.120, -0.089, 0.328, 1.369, 78.147, 92.983, 481.46, 14.596, 496.46, 301.84, 95.14, 4.1, 1.0, 253.06}, drussTC);
        targetingConfigurations.put(new double[]{-0.025, 0.009, 0.806, 2.030, 70.753, 88.742, 406.98, 10.067, 404.23, 175.36, 88.10, 5.3, 0.7, 307.35}, drussTC); // Midboss
        targetingConfigurations.put(new double[]{-0.114, -0.014, 0.397, 3.033, 68.239, 92.469, 451.41, 14.137, 462.64, 279.18, 92.64, 4.6, 1.0, 258.44}, drussTC); // Seraphim 1

        final TargetingConfiguration doctorBobTC =
                new TargetingConfiguration("doctorBob", new AdjustingClassifier(), AdjustingClassifier.getAttributes());
        targetingConfigurations.put(new double[]{-0.050, -0.107, 0.190, 3.985, 69.742, 80.683, 254.02, 13.773, 241.82, 188.39, 80.95, 6.2, 0.5, 348.14}, doctorBobTC);

        final ProbCMC ocnirpCMC = new ProbCMC(ocnirpAccelAttrs, ocnirpTurnAttrs, ocnirpAccelRanges);
        final TargetingConfiguration ocnipTC =
                new TargetingConfiguration("ocnirp", ocnirpCMC, ocnirpCMC.getAttributes());
        targetingConfigurations.put(new double[]{-0.191, -0.005, 0.706, 1.049, 87.200, 90.772, 442.35, 10.573, 445.06, 289.73, 90.61, 4.9, 0.8, 246.28}, ocnipTC);
        targetingConfigurations.put(new double[]{-0.283, 0.016, 0.199, 2.364, 73.233, 94.551, 502.42, 16.876, 512.72, 265.04, 91.16, 6.1, 0.7, 272.35}, ocnipTC); // RaikoMX
    }

    private static final double[] weights = {
            1 * 1000, // avgVelocity
            1 * 1000, // avgTurnRate
            1 * 1000, // avgStopTime
            1 * 1000, // avgTurnRateModule
            1 * 1000, // avgAttackAngle
            3 * 1000, // avgBearing
            1 * 1000, // avgDistanceBetween
            1 * 1000, // avgTravelTime
            1 * 1000, // avgDistanceToFirstBulletPos
            1 * 1000, // avgDistanceToCenter
            3 * 1000, // avgFirstBulletBearing
            1 * 1000, // avgVelocityModuleOnFirstBullet
            3 * 1000, // avgAccelOnFirstBullet
            1 * 1000, // avgDistToForwardWall
    };
    static {
        double[][] ranges = new double[2][weights.length];
        Arrays.fill(ranges[0], Integer.MAX_VALUE);
        Arrays.fill(ranges[1], Integer.MIN_VALUE);
        for (double[] pnt : targetingConfigurations.keySet()) {
            for (int i = 0; i < pnt.length; i++) {
                ranges[0][i] = min(ranges[0][i], pnt[i]);
                ranges[1][i] = max(ranges[1][i], pnt[i]);
            }
        }

        for (int i = 0; i < weights.length; i++) {
            weights[i] /= ranges[1][i] - ranges[0][i];
        }
    }

    private static final Map<LXXRobot, MovementMetaProfile> movementMetaProfiles = new HashMap<LXXRobot, MovementMetaProfile>();
    private static final Map<LXXRobot, TargetingProfile> targetingProfiles = new HashMap<LXXRobot, TargetingProfile>();

    private final Tomcat robot;
    private final BulletManager bulletManager;
    private final TurnSnapshotsLog turnSnapshotsLog;

    public TomcatEyes(Tomcat robot, BulletManager bulletManager, TurnSnapshotsLog turnSnapshotsLog) {
        this.robot = robot;
        this.bulletManager = bulletManager;
        this.turnSnapshotsLog = turnSnapshotsLog;
    }

    private static TargetingConfiguration getTargetingConfig(String name, Attribute[] attributes, double maxIntervalLength) {
        return new TargetingConfiguration(name, new SegmentationTreeMovementClassifier(attributes, maxIntervalLength), attributes);
    }

    public TargetingConfiguration getConfiguration(Target t) {
        double minDist = Integer.MAX_VALUE;
        TargetingConfiguration minDistTC = null;
        for (double[] mmp : targetingConfigurations.keySet()) {
            double dist = LXXUtils.weightedManhattanDistance(mmp, getMovementMetaProfile(t).toArray(), weights);
            if (dist < minDist) {
                minDist = dist;
                minDistTC = targetingConfigurations.get(mmp);
            }
        }

        return minDistTC;
    }

    public void targetUpdated(Target target) {
        final MovementMetaProfile movementMetaProfile = getMovementMetaProfile(target);
        movementMetaProfile.update(target, robot, bulletManager);
        robot.setDebugProperty("Enemy's preferred distance", String.valueOf(movementMetaProfile.getPreferredDistance()));
        robot.setDebugProperty("Enemy rammer", String.valueOf(movementMetaProfile.isRammer()));
        robot.setDebugProperty("emmp", movementMetaProfile.toShortString());

        final TurnSnapshot turnSnapshot = turnSnapshotsLog.getLastSnapshot(target, 1);
        if (turnSnapshot == null) {
            return;
        }
        final MovementDecision movementDecision = MovementDecision.getMovementDecision(turnSnapshotsLog.getLastSnapshot(target));

        for (TargetingConfiguration tc : targetingConfigurations.values()) {
            tc.getMovementClassifier().learn(turnSnapshot, movementDecision);
        }
    }

    private MovementMetaProfile getMovementMetaProfile(LXXRobot t) {
        MovementMetaProfile mmp = movementMetaProfiles.get(t);
        if (mmp == null) {
            mmp = new MovementMetaProfile();
            movementMetaProfiles.put(t, mmp);
        }

        return mmp;
    }

    public boolean isRammer(Target target) {
        final MovementMetaProfile movementMetaProfile = getMovementMetaProfile(target);
        return movementMetaProfile.isRammer();
    }

    public void bulletHit(LXXBullet bullet) {
        processBullet(bullet);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        processBullet(bullet);
    }

    private void processBullet(LXXBullet bullet) {
        final double bearingOffset = bullet.getRealBearingOffsetRadians();
        getTargetingProfile(bullet.getOwner()).addBearingOffset(bearingOffset * bullet.getTargetLateralDirection(), false);
    }

    private TargetingProfile getTargetingProfile(LXXRobot t) {
        TargetingProfile tp = targetingProfiles.get(t);
        if (tp == null) {
            tp = new TargetingProfile();
            targetingProfiles.put(t, tp);
        }

        return tp;
    }

    public GunType getEnemyGunType(LXXRobot enemy) {
        final TargetingProfile tp = getTargetingProfile(enemy);
        if (tp.positiveNormalBearingOffsetsCount >= tp.totalNormalBearingOffsets * 0.85 ||
                tp.hitCount <= robot.getRoundNum() + 1) {
            return GunType.SIMPLE;
        } else {
            return GunType.ADVANCED;
        }
    }

    public int getEnemyPreferredDistance(LXXRobot enemy) {
        return getMovementMetaProfile(enemy).getPreferredDistance();
    }

    public void bulletPassing(LXXBullet bullet) {
    }

    public void bulletMiss(LXXBullet bullet) {
    }

    public void bulletFired(LXXBullet bullet) {
    }

}
