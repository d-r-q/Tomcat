/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.EnemySnapshotImpl;
import lxx.LXXRobot;
import lxx.MySnapshot;
import lxx.MySnapshotImpl;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

public class MyDistanceLast10Ticks implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return ((MySnapshot)me).getLast10TicksDist();
    }

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return me.getLast10TicksDist();
    }

}
