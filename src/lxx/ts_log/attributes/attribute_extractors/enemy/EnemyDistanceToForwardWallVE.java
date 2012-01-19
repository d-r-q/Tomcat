/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.LXXRobot;
import lxx.utils.LXXUtils;

import java.util.List;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyDistanceToForwardWallVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return LXXUtils.limit(0, enemy.getPosition().distanceToWall(enemy.getState().getBattleField(), enemy.getState().getAbsoluteHeadingRadians()), Integer.MAX_VALUE);
    }
}
