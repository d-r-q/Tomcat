/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;
import robocode.util.Utils;

import java.util.List;

public class EnemyDistanceToReverceWallVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return (int) enemy.getPosition().distanceToWall(enemy.getState().getBattleField(), Utils.normalAbsoluteAngle(enemy.getState().getAbsoluteHeadingRadians() + Math.PI));
    }
}
