/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.my;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class MyLateralVelocityModule_2VE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return (int) abs(round(LXXUtils.lateralVelocity(enemy, me.getState()) / 2));
    }

}
