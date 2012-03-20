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
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.toDegrees;

public class EnemyBearingToMe implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return toDegrees(Utils.normalRelativeAngle(enemy.angleTo(me) - enemy.getAbsoluteHeadingRadians()));
    }

}
