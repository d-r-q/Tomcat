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
        targetingConfigurations.put(new double[]{7.892, 0.073, 7.892, 0.073, 86.284, 88.172, 548.25, 55.816, 558.79, 384.78}, getTargetingConfig("Walls", wallsAttributes, 0.001));

        final TargetingConfiguration crazyTC = getTargetingConfig("Crazy", crazyAttributes, 0.001);
        targetingConfigurations.put(new double[]{2.392, 0.543, 7.233, 4.382, 43.933, 0.034, 468.59, 42.337, 473.16, 248.61}, crazyTC);

        final ComplexMovementClassifier drussCMC = new ComplexMovementClassifier(drussAccelAttrs, drussTurnAttrs, drussAccelRanges);
        final TargetingConfiguration drussTC =
                new TargetingConfiguration("druss", drussCMC, drussCMC.getAttributes());
        targetingConfigurations.put(new double[]{-0.339, 0.027, 5.462, 1.478, 78.810, 0.032, 527.38, 75.587, 542.83, 255.65}, drussTC);

        final TargetingConfiguration doctorBobTC =
                new TargetingConfiguration("doctorBob", new AdjustingClassifier(), AdjustingClassifier.getAttributes());
        targetingConfigurations.put(new double[]{-0.018, -0.057, 6.213, 3.896, 68.950, 0.090, 261.97, 70.343, 254.22, 209.67}, doctorBobTC);

        /*final ProbCMC ocnirpCMC = new ProbCMC(ocnirpAccelAttrs, ocnirpTurnAttrs, ocnirpAccelRanges);
        final TargetingConfiguration ocnipTC =
                new TargetingConfiguration("ocnirp", ocnirpCMC, ocnirpCMC.getAttributes());
        targetingConfigurations.put(new double[]{0.241, -0.049, 5.421, 1.130, 86.131, -8.813, 423.946, 77.684, 430.038, 286.050}, ocnipTC);
        targetingConfigurations.put(new double[]{-0.205, 0.063, 5.552, 1.049, 86.999, -2.427, 431.295, 77.148, 438.764, 250.235}, ocnipTC);*/
    }

    private static final double[] weights = {
            100D / 17,
            100D / 21,
            100D / 9,
            100D / 11,
            100D / 91,
            100D / 180,
            100D / 850,
            100D / 91,
            100D / 1700,
            100D / 425};

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
        robot.setDebugProperty("emmp", Arrays.toString(movementMetaProfile.toArray()));

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
