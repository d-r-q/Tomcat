/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.target;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXUtils;
import robocode.util.Utils;

import java.util.List;

public class EnemyDistanceToReverceWallVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return LXXUtils.limit(0, enemy.getPosition().distanceToWall(enemy.getState().getBattleField(), Utils.normalAbsoluteAngle(enemy.getState().getAbsoluteHeadingRadians() + Math.PI)), Integer.MAX_VALUE);
    }
}
