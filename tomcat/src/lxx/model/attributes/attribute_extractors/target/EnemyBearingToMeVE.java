/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.toDegrees;

public class EnemyBearingToMeVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return toDegrees(Utils.normalRelativeAngle(enemy.angleTo(me) - enemy.getState().getAbsoluteHeadingRadians()));
    }

}
