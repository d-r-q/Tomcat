/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.office.PropertiesManager;
import lxx.targeting.GunType;
import lxx.utils.LXXConstants;

import java.util.HashMap;
import java.util.Map;


/**
 * User: jdev
 * Date: 01.03.2011
 */
public class TomcatEyes implements BulletManagerListener {

    private static final Map<String, TargetingProfile> targetingProfiles = new HashMap<String, TargetingProfile>();

    public void bulletHit(LXXBullet bullet) {
        processBullet(bullet);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        processBullet(bullet);
    }

    private void processBullet(LXXBullet bullet) {
        final double bearingOffset = bullet.getRealBearingOffsetRadians();
        // todo: swap targetState & sourceState
        getTargetingProfile(bullet.getSourceState().getName()).addBearingOffset(bullet.getTargetState(), bullet.getWave().getSourceState(),
                bearingOffset * bullet.getTargetLateralDirection(), bullet.getSpeed());
        PropertiesManager.setDebugProperty("Enemy gun type", getEnemyGunType(bullet.getSourceState().getName()).toString());
    }

    private TargetingProfile getTargetingProfile(String name) {
        TargetingProfile tp = targetingProfiles.get(name);
        if (tp == null) {
            tp = new TargetingProfile();
            targetingProfiles.put(name, tp);
        }

        return tp;
    }

    public GunType getEnemyGunType(String name) {
        final TargetingProfile tp = getTargetingProfile(name);
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

    public void bulletPassing(LXXBullet bullet) {
    }

    public void bulletMiss(LXXBullet bullet) {
    }

    public void bulletFired(LXXBullet bullet) {
    }

}
