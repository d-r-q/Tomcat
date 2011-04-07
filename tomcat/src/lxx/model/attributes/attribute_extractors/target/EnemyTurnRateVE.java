/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 07.03.2010
 */
public class EnemyTurnRateVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return toDegrees(enemy.getState().getTurnRateRadians());
    }

}
