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
 * Date: 23.02.2010
 */
public class EnemyBearingToHOWall implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return toDegrees(enemy.getState().getBattleField().getBearingOffsetToWall(enemy.getPosition(), enemy.getState().getAbsoluteHeadingRadians()));
    }

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return toDegrees(enemy.getBattleField().getBearingOffsetToWall(enemy.getPosition(), enemy.getAbsoluteHeadingRadians()));
    }
}
