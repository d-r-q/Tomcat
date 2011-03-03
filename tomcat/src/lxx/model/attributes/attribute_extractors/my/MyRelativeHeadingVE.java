/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.my;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;
import robocode.util.Utils;

import static java.lang.Math.toDegrees;

public class MyRelativeHeadingVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) toDegrees(Utils.normalRelativeAngle(me.getState().getAbsoluteHeadingRadians() - enemy.angleTo(me)));
    }
}
