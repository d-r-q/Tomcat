/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.my;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;

public class MyTravelTimeVE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return LXXUtils.limit(0, (int) (me.getTime() - me.getLastStopTime()), 255);
    }

}
