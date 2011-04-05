/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXPoint;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 02.04.11
 */
public class FirstBulletBearingOffsetVE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
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
        final LXXPoint maxEnemyPos = enemy.getPosition().project(enemy.getState().getAbsoluteHeadingRadians(), Rules.MAX_VELOCITY * bulletFlightTime);
        final double maxEscapeAngle = abs(Utils.normalRelativeAngle(firstBullet.getCurrentPosition().angleTo(maxEnemyPos) - firstBullet.getCurrentPosition().angleTo(enemy)));
        final double bearingOffset = Utils.normalRelativeAngle(firstBullet.getHeadingRadians() - firstBullet.getCurrentPosition().angleTo(enemy)) / maxEscapeAngle;
        final double lateralDirection = signum(LXXUtils.lateralVelocity2(firstBullet.getCurrentPosition(), enemy, enemy.getState().getVelocityModule(), enemy.getState().getAbsoluteHeadingRadians()));

        return (int) LXXUtils.limit(-2, round(bearingOffset) * lateralDirection, 2);
    }

}
