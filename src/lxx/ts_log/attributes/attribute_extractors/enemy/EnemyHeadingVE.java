/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyHeadingVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return toDegrees(enemy.getState().getAbsoluteHeadingRadians());
    }
}
