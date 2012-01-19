/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

public class FirstBulletFlightTimeToEnemyVE implements AttributeValueExtractor {


    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        if (myBullets.size() == 0) {
            return 0;
        }

        LXXBullet firstBullet;
        int idx = 0;
        double bulletFlightTime;
        do {
            if (idx == myBullets.size()) {
                return 0;
            }
            firstBullet = myBullets.get(idx++);
            bulletFlightTime = (firstBullet.getFirePosition().aDistance(enemy) - firstBullet.getTravelledDistance()) /
                    firstBullet.getSpeed();
        } while (bulletFlightTime < 1);

        return bulletFlightTime;
    }
}
