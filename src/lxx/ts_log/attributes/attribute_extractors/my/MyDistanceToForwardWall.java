/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXUtils;

/**
 * User: jdev
 * Date: 08.08.2010
 */
public class MyDistanceToForwardWall implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return LXXUtils.limit(0, me.getPosition().distanceToWall(me.getBattleField(), me.getAbsoluteHeadingRadians()), Integer.MAX_VALUE);
    }

}
