/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.my;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;

/**
 * User: jdev
 * Date: 08.08.2010
 */
public class MyDistanceToForwardWallVE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) me.getPosition().distanceToWall(me.getState().getBattleField(), me.getState().getAbsoluteHeadingRadians());
    }

}
