/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyBearingToHOWallVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) round(toDegrees(enemy.getState().getBattleField().getBearingOffsetToWall(enemy, enemy.getState().getAbsoluteHeadingRadians())));
    }
}
