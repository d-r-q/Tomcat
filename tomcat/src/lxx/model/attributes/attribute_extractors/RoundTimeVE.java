/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors;

import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;

import java.util.List;

public class RoundTimeVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return enemy.getTime();
    }

}
