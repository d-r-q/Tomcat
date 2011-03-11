/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.Tomcat;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.BulletManagerListener;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TomcatEyes implements TargetManagerListener, BulletManagerListener {

    private static final Attribute[] wallsAttributes = new Attribute[]{
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyDistanceToCenter,
            AttributesManager.enemyStopTime,
            AttributesManager.enemyBearingToForwardWall,
            AttributesManager.enemyVelocityModule,
    };

    private static final Attribute[] crazyAttributes = new Attribute[]{
            AttributesManager.enemyBearingToForwardWall,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyDistanceToCenter,
            AttributesManager.enemyTurnRate,
            AttributesManager.enemyVelocity,
            AttributesManager.enemyTurnTime,
    };

    private static final Attribute[] drussAttributes = new Attribute[]{
            AttributesManager.enemyStopTime,
            AttributesManager.enemyTurnTime,
            AttributesManager.enemyTravelTime,
            AttributesManager.enemyBearingToForwardWall,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyBearingToMe,
    };

    private static final Attribute[] doctorBobAttributes = new Attribute[]{
            AttributesManager.enemyBearingToForwardWall,
            AttributesManager.enemyDistanceToForwardWall,
            AttributesManager.enemyStopTime,
            AttributesManager.enemyTravelTime,
            AttributesManager.distBetween,
            AttributesManager.enemyBearingToMe,
            AttributesManager.enemyVelocityModule,
    };

    private static final Map<double[], TargetingConfiguration> targetingConfigurations = new HashMap<double[], TargetingConfiguration>();

    static {
        targetingConfigurations.put(new double[]{7.892, 0.073, 7.892, 0.073, 86.284, 88.172, 548.25, 55.816, 558.79, 384.78}, getTargetingConfig("Walls", wallsAttributes, 10));

        TargetingConfiguration crazyTC = getTargetingConfig("Crazy", crazyAttributes, 4);
        targetingConfigurations.put(new double[]{2.392, 0.543, 7.233, 4.382, 43.933, 0.034, 468.59, 42.337, 473.16, 248.61}, crazyTC);

        final TargetingConfiguration drussTC = getTargetingConfig("Druss", drussAttributes, 4);
        targetingConfigurations.put(new double[]{-0.339, 0.027, 5.462, 1.478, 78.810, 0.032, 527.38, 75.587, 542.83, 255.65}, drussTC);

        TargetingConfiguration doctorBobTC = getTargetingConfig("DoctorBob", doctorBobAttributes, 4);
        targetingConfigurations.put(new double[]{-0.018, -0.057, 6.213, 3.896, 68.950, 0.090, 261.97, 70.343, 254.22, 209.67}, doctorBobTC);
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

    public TomcatEyes(Tomcat robot, BulletManager bulletManager) {
        this.robot = robot;
        this.bulletManager = bulletManager;
    }

    private static TargetingConfiguration getTargetingConfig(String name, Attribute[] attributes, int weightMultiplier) {
        int[] indexes = new int[attributes.length];
        double[] weights = new double[AttributesManager.attributesCount()];
        double weight = 1;
        int idx = 0;
        for (Attribute a : attributes) {
            indexes[idx++] = a.getId();
            weights[a.getId()] = weight / a.getActualRange();
            weight = weight * weightMultiplier + 1;
        }
        return new TargetingConfiguration(name, attributes, weights, indexes);
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
        // todo(zhidkov): remove it
        robot.setDebugProperty("mmp", movementMetaProfile.toShortString());
        robot.setDebugProperty("Enemy's preferred distance", String.valueOf(movementMetaProfile.getPreferredDistance()));
        robot.setDebugProperty("Enemy rammer", String.valueOf(movementMetaProfile.isRammer()));
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
