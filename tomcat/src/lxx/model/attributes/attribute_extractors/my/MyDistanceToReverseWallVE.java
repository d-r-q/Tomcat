/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.my;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;

import java.util.List;

/**
 * User: jdev
 * Date: 08.08.2010
 */
public class MyDistanceToReverseWallVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return me.getPosition().distanceToWall(me.getState().getBattleField(), me.getState().getAbsoluteHeadingRadians());
    }

}
