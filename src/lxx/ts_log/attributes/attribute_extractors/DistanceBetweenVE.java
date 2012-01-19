/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;

import java.util.List;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class DistanceBetweenVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return me.aDistance(enemy);
    }

}
