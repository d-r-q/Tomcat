/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;

import java.util.List;

/**
 * User: jdev
 * Date: 09.03.11
 */
public class DistanceBetween_100VE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return enemy.aDistance(me) / 100;
    }

}
