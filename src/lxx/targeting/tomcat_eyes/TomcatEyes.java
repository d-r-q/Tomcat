/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.LXXRobot;
import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.office.PropertiesManager;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.utils.LXXConstants;

import java.util.HashMap;
import java.util.Map;


/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TomcatEyes implements TargetManagerListener, BulletManagerListener {

    private static final Map<LXXRobot, MovementMetaProfile> movementMetaProfiles = new HashMap<LXXRobot, MovementMetaProfile>();
    private static final Map<LXXRobot, TargetingProfile> targetingProfiles = new HashMap<LXXRobot, TargetingProfile>();

    private final Tomcat robot;

    public TomcatEyes(Tomcat robot) {
        this.robot = robot;
    }

    public void targetUpdated(Target target) {
        final MovementMetaProfile movementMetaProfile = getMovementMetaProfile(target);
        movementMetaProfile.update(target, robot);
        PropertiesManager.setDebugProperty("Enemy's preferred distance", String.valueOf(movementMetaProfile.getPreferredDistance()));
        PropertiesManager.setDebugProperty("Can enemy ram", String.valueOf(movementMetaProfile.canRam()));
    }

    private MovementMetaProfile getMovementMetaProfile(LXXRobot t) {
        MovementMetaProfile mmp = movementMetaProfiles.get(t);
        if (mmp == null) {
            mmp = new MovementMetaProfile();
            movementMetaProfiles.put(t, mmp);
        }

        return mmp;
    }

    public void bulletHit(LXXBullet bullet) {
        processBullet(bullet);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        processBullet(bullet);
    }

    private void processBullet(LXXBullet bullet) {
        final double bearingOffset = bullet.getRealBearingOffsetRadians();
        getTargetingProfile(bullet.getOwner()).addBearingOffset(bullet.getTargetStateAtFireTime(), bullet.getWave().getSourceStateAtFireTime(),
                bearingOffset * bullet.getTargetLateralDirection(), bullet.getSpeed());
        PropertiesManager.setDebugProperty("Enemy gun type", getEnemyGunType(bullet.getOwner()).toString());
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
        if (tp.bearingOffsets == 0) {
            return GunType.UNKNOWN;
        } else if (tp.distWithHoBoMedian.getMedian() < LXXConstants.RADIANS_10 && tp.bearingOffsetsInteval.a > -LXXConstants.RADIANS_15) {
            return GunType.HEAD_ON;
        } else if (tp.distWithLinearBOMedian.getMedian() < LXXConstants.RADIANS_15 && tp.bearingOffsetsInteval.a > -LXXConstants.RADIANS_15) {
            return GunType.LINEAR;
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
