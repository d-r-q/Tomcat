/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.my;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;

import static java.lang.Math.round;

public class MyLateralVelocityVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) round(LXXUtils.lateralVelocity(enemy, me.getState()) * 3);

    }
}
