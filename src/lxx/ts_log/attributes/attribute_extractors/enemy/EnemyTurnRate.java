/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 07.03.2010
 */
public class EnemyTurnRate implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return toDegrees(enemy.getTurnRateRadians());
    }

}
