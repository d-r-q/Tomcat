/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.Tomcat;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.bullets.BulletManager;
import lxx.utils.LXXUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TomcatEyes implements TargetManagerListener {

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

    private static final Map<double[], TargetingConfiguration> targetingConfigurations = new HashMap<double[], TargetingConfiguration>();

    static {
        targetingConfigurations.put(new double[]{7.892, 0.073, 7.892, 0.073, 86.284, 88.172, 548.25, 55.816, 558.79, 384.78}, getTargetingConfig("Walls", wallsAttributes));

        TargetingConfiguration crazyTC = getTargetingConfig("Crazy", crazyAttributes);
        targetingConfigurations.put(new double[]{7.297, 0.083, 7.916, 0.540, 14.810, 147.405, 655.90, 41.761, 467.83, 273.12}, crazyTC);
        targetingConfigurations.put(new double[]{6.703, 0.115, 7.845, 0.947, 70.497, -64.406, 535.56, 44.501, 447.17, 207.50}, crazyTC);

        TargetingConfiguration starTC = getTargetingConfig("Star", drussAttributes);
        targetingConfigurations.put(new double[]{7.338, 0.026, 7.847, 0.204, 86.675, 84.947, 599.04, 74.480, 546.72, 316.82}, starTC);
        targetingConfigurations.put(new double[]{-6.864, 0.070, 7.758, 0.309, 70.128, -93.761, 480.73, 73.364, 512.71, 234.60}, starTC);

        final TargetingConfiguration drussTC = getTargetingConfig("Druss", drussAttributes);
        targetingConfigurations.put(new double[]{5.273, 5.331, 5.902, 5.489, 86.115, 76.972, 595.34, 67.093, 580.6, 257.13}, drussTC);
        targetingConfigurations.put(new double[]{-4.074, -0.230, 6.394, 1.155, 75.664, -61.314, 382.14, 73.098, 439.89, 112.20}, drussTC);
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

    private static final Map<Target, MovementMetaProfile> movementMetaProfiles = new HashMap<Target, MovementMetaProfile>();

    private final Tomcat robot;
    private final BulletManager bulletManager;

    public TomcatEyes(Tomcat robot, BulletManager bulletManager) {
        this.robot = robot;
        this.bulletManager = bulletManager;
    }

    private static TargetingConfiguration getTargetingConfig(String name, Attribute[] attributes) {
        int[] indexes = new int[attributes.length];
        double[] weights = new double[AttributesManager.attributesCount()];
        double weight = 1;
        int idx = 0;
        for (Attribute a : attributes) {
            indexes[idx++] = a.getId();
            weights[a.getId()] = weight / a.getActualRange();
            weight = weight * 4 + 1;
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

    private MovementMetaProfile getMovementMetaProfile(Target t) {
        MovementMetaProfile mmp = movementMetaProfiles.get(t);
        if (mmp == null) {
            mmp = new MovementMetaProfile(t, robot, bulletManager);
            movementMetaProfiles.put(t, mmp);
        }

        return mmp;
    }

    public void targetUpdated(Target target) {
        final MovementMetaProfile movementMetaProfile = getMovementMetaProfile(target);
        movementMetaProfile.update();
        // todo(zhidkov): remove it
        robot.setDebugProperty("mmp", movementMetaProfile.toShortString());
    }
}
