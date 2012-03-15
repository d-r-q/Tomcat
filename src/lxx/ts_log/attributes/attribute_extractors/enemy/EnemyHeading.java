/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshotImpl;
import lxx.LXXRobot;
import lxx.MySnapshotImpl;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyHeading implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return toDegrees(enemy.getState().getAbsoluteHeadingRadians());
    }

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return toDegrees(enemy.getAbsoluteHeadingRadians());
    }
}
