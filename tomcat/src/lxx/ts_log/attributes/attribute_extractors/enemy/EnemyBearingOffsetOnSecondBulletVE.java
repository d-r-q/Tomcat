/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.LXXRobotState;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.signum;
import static java.lang.Math.toDegrees;

public class EnemyBearingOffsetOnSecondBulletVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        if (myBullets.size() < 2) {
            return 0;
        }

        final LXXBullet firstBullet = myBullets.get(1);
        final LXXRobotState targetState = firstBullet.getTargetStateAtFireTime();
        double lateralDirection = signum(LXXUtils.lateralVelocity2(firstBullet.getFirePosition(), targetState, targetState.getSpeed(), targetState.getAbsoluteHeadingRadians()));
        return toDegrees(firstBullet.getBearingOffsetRadians(enemy)) * lateralDirection;
    }
}
