/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.bullets.LXXBullet;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.LXXRobot;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.abs;

public class MyLateralVelocityModule_2VE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return abs(LXXUtils.lateralVelocity(enemy, me.getState()) / 2);
    }

}
