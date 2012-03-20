/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshotImpl;
import lxx.MySnapshotImpl;
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
public class EnemyDistanceToForwardWall implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return LXXUtils.limit(0, enemy.getPosition().distanceToWall(enemy.getBattleField(), enemy.getAbsoluteHeadingRadians()), Integer.MAX_VALUE);
    }

}
