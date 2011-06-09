/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.debug.HitStats;
import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.*;

public class EnemyMaxDangerBearingOffsetVE implements AttributeValueExtractor {
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
        final double lateralDirection = signum(LXXUtils.lateralVelocity2(firstBullet.getCurrentPosition(), enemy, enemy.getState().getVelocityModule(), enemy.getState().getAbsoluteHeadingRadians()));

        double[] hitStats = HitStats.hitStats[HitStats.getLatVelIdx(firstBullet)];

        double maxHits = 0;
        int maxHitsIdx = 0;
        for (int i = 0; i < hitStats.length - 1; i++) {
            if (hitStats[i] > maxHits) {
                maxHits = hitStats[i];
                maxHitsIdx = i;
            }
        }

        final double maxDangerBearingOffset = toRadians(maxHitsIdx - 45) * lateralDirection;
        final double enemyBearingOffset = LXXUtils.bearingOffset(firstBullet.getFirePosition(), firstBullet.getTargetStateAtFireTime(), enemy) * lateralDirection;
        if (maxDangerBearingOffset > enemyBearingOffset) {
            if (lateralDirection > 0) {
                return toDegrees(maxDangerBearingOffset - enemyBearingOffset);
            } else {
                return toDegrees(enemyBearingOffset - maxDangerBearingOffset);
            }
        } else {
            if (lateralDirection < 0) {
                return toDegrees(enemyBearingOffset - maxDangerBearingOffset);
            } else {
                return toDegrees(maxDangerBearingOffset - enemyBearingOffset);
            }
        }
    }
}
