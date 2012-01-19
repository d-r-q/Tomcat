/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;

import java.util.List;

public class RoundTimeVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return enemy.getTime();
    }

}
