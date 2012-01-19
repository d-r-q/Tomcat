/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.bullets.LXXBullet;
import lxx.LXXRobot;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyBearingToHOWallVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return toDegrees(enemy.getState().getBattleField().getBearingOffsetToWall(enemy.getPosition(), enemy.getState().getAbsoluteHeadingRadians()));
    }
}
