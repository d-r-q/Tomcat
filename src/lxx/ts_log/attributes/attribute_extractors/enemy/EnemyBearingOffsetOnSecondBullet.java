/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.bullets.BulletSnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.APoint;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.toDegrees;

public class EnemyBearingOffsetOnSecondBullet implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        final List<BulletSnapshot> myBullets = me.getBulletsInAir();
        if (myBullets.size() < 2) {
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
        if (idx == myBullets.size()) {
            return 0;
        }
        final BulletSnapshot secondBullet = myBullets.get(idx);

        final APoint interceptPos = enemy.project(enemy.getAbsoluteHeadingRadians(), enemy.getSpeed() * bulletFlightTime);
        double lateralDirection = LXXUtils.lateralDirection(secondBullet.getOwnerState(), secondBullet.getTargetState());
        return toDegrees(secondBullet.getBearingOffsetRadians(interceptPos)) * lateralDirection;
    }

}
