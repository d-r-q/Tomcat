/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.EnemySnapshotImpl;
import lxx.LXXRobot;
import lxx.MySnapshotImpl;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.bullets.LXXBullet;

import java.util.List;

/**
 * User: jdev
 * Date: 08.08.2010
 */
public class MyAcceleration implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return me.getAcceleration();
    }

}
