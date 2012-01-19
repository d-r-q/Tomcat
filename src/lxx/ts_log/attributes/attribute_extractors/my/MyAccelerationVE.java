/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.LXXRobot;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.bullets.LXXBullet;

import java.util.List;

/**
 * User: jdev
 * Date: 08.08.2010
 */
public class MyAccelerationVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return me.getAcceleration();
    }
}
