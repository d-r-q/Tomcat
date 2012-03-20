/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshotImpl;
import lxx.MySnapshotImpl;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

/**
 * User: jdev
 * Date: 02.03.2010
 */
public class EnemyAcceleration implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return enemy.getAcceleration();
    }

}
