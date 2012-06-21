/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.LXXRobotSnapshot;
import lxx.MySnapshot;
import lxx.bullets.BulletSnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.APoint;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 30.04.11
 */
public class EnemyBearingOffsetOnFirstBullet implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        List<BulletSnapshot> myBullets = me.getBulletsInAir();
        if (myBullets.size() == 0) {
            return 0;
        }

        BulletSnapshot firstBullet;
        int idx = 0;
        double bulletFlightTime;
        do {
            if (idx == myBullets.size()) {
                return 0;
            }
            firstBullet = myBullets.get(idx++);
            bulletFlightTime = firstBullet.getFlightTime(enemy);
        } while (bulletFlightTime < 1);

        final LXXRobotSnapshot targetState = firstBullet.getTargetState();
        double lateralDirection = LXXUtils.lateralDirection(firstBullet.getOwnerState(), targetState);
        return toDegrees(firstBullet.getBearingOffsetRadians(targetState)) * lateralDirection;
    }

}
