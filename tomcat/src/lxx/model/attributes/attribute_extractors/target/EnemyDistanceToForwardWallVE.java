/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyDistanceToForwardWallVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) enemy.getPosition().distanceToWall(enemy.getState().getBattleField(), enemy.getState().getAbsoluteHeadingRadians());
    }
}
