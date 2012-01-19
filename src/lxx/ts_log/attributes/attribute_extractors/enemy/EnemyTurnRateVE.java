/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

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
public class EnemyTurnRateVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return toDegrees(enemy.getState().getTurnRateRadians());
    }

}
