/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.LXXRobotState;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.APoint;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 30.04.11
 */
public class EnemyBearingOffsetOnFirstBulletVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        if (myBullets.size() == 0) {
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

        final LXXRobotState targetState = firstBullet.getTargetStateAtFireTime();
        final APoint interceptPos = enemy.project(enemy.getState().getAbsoluteHeadingRadians(), enemy.getState().getSpeed() * bulletFlightTime);
        double lateralDirection = LXXUtils.lateralDirection(firstBullet.getFirePosition(), targetState);
        return toDegrees(firstBullet.getBearingOffsetRadians(interceptPos)) * lateralDirection;
    }
}
