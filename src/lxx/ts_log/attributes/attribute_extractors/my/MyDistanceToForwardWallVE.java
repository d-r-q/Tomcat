/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.bullets.LXXBullet;
import lxx.LXXRobot;
import lxx.utils.LXXUtils;

import java.util.List;

/**
 * User: jdev
 * Date: 08.08.2010
 */
public class MyDistanceToForwardWallVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return LXXUtils.limit(0, me.getPosition().distanceToWall(me.getState().getBattleField(), me.getState().getAbsoluteHeadingRadians()), Integer.MAX_VALUE);
    }

}
