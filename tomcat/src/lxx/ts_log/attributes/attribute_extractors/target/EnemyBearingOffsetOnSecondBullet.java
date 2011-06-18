/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.target;

import lxx.LXXRobot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.bullets.LXXBullet;
import lxx.LXXRobotState;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.signum;
import static java.lang.Math.toDegrees;

public class EnemyBearingOffsetOnSecondBullet implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
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
            bulletFlightTime = (firstBullet.getFirePosition().aDistance(enemy) - firstBullet.getFirePosition().aDistance(firstBullet.getCurrentPosition())) /
                    firstBullet.getSpeed();
        } while (bulletFlightTime < 1);
        if (idx == myBullets.size()) {
            return 0;
        }
        firstBullet = myBullets.get(idx);

        final LXXRobotState targetState = firstBullet.getTargetStateAtFireTime();
        double lateralDirection = signum(LXXUtils.lateralVelocity2(firstBullet.getFirePosition(), targetState, targetState.getVelocityModule(), targetState.getAbsoluteHeadingRadians()));
        return toDegrees(LXXUtils.bearingOffset(firstBullet.getFirePosition(), targetState, enemy)) * lateralDirection;
    }
}
