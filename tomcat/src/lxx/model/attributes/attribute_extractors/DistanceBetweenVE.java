/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors;

import lxx.utils.LXXRobot;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class DistanceBetweenVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) me.aDistance(enemy);
    }
}
