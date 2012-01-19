/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.toDegrees;

public class EnemyBearingToMeVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return toDegrees(Utils.normalRelativeAngle(enemy.angleTo(me) - enemy.getState().getAbsoluteHeadingRadians()));
    }

}
