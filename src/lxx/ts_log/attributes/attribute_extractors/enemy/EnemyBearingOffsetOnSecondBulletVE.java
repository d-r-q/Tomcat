/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.APoint;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.toDegrees;

public class EnemyBearingOffsetOnSecondBulletVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        if (myBullets.size() < 2) {
            return 0;
        }

        LXXBullet firstBullet;
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
        final LXXBullet secondBullet = myBullets.get(idx);

        final APoint interceptPos = enemy.project(enemy.getState().getAbsoluteHeadingRadians(), enemy.getState().getSpeed() * bulletFlightTime);
        double lateralDirection = LXXUtils.lateralDirection(secondBullet.getFirePosition(), secondBullet.getTargetStateAtFireTime());
        return toDegrees(secondBullet.getBearingOffsetRadians(interceptPos)) * lateralDirection;
    }
}
