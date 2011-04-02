/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;

import java.util.List;

/**
 * User: jdev
 * Date: 02.03.2010
 */
public class EnemyAccelerationVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return (int) enemy.getAcceleration();
    }
}
