/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;
import robocode.util.Utils;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

public class EnemyBearingToMeVE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) round(toDegrees(Utils.normalRelativeAngle(enemy.angleTo(me) - enemy.getState().getAbsoluteHeadingRadians())));
    }

}
