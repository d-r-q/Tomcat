/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class EnemySpeed implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return enemy.getSpeed();
    }

}
