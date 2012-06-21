/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXUtils;
import robocode.util.Utils;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyDistanceToReverseWall implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return LXXUtils.limit(0, enemy.getPosition().distanceToWall(enemy.getBattleField(), Utils.normalAbsoluteAngle(enemy.getAbsoluteHeadingRadians() + Math.PI)), Integer.MAX_VALUE);
    }

}
