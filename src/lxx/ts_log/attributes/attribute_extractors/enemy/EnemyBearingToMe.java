/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import robocode.util.Utils;

import static java.lang.Math.toDegrees;

public class EnemyBearingToMe implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return toDegrees(Utils.normalRelativeAngle(enemy.angleTo(me) - enemy.getAbsoluteHeadingRadians()));
    }

}
