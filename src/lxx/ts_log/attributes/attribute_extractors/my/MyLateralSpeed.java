/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.my;

import lxx.EnemySnapshotImpl;
import lxx.LXXRobot;
import lxx.MySnapshotImpl;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXUtils;

import java.util.List;

import static java.lang.Math.abs;

public class MyLateralSpeed implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return abs(LXXUtils.lateralVelocity(enemy, me.getState()));
    }

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return abs(LXXUtils.lateralVelocity(enemy, me));
    }

}
