/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.target;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

/**
 * User: jdev
 * Date: 02.03.2010
 */
public class EnemyAccelerationVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return enemy.getAcceleration();
    }
}
