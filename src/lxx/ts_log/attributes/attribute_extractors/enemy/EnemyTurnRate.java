/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshotImpl;
import lxx.MySnapshotImpl;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.bullets.LXXBullet;
import lxx.LXXRobot;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 07.03.2010
 */
public class EnemyTurnRate implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return toDegrees(enemy.getTurnRateRadians());
    }

}
