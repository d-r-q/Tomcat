/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.bullets.BulletSnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

public class FirstBulletFlightTimeToEnemy implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        final List<BulletSnapshot> myBullets = me.getBulletsInAir();
        if (myBullets.size() == 0) {
            return 0;
        }

        BulletSnapshot firstBullet;
        int idx = 0;
        double bulletFlightTime;
        do {
            if (idx == myBullets.size()) {
                return 0;
            }
            firstBullet = myBullets.get(idx++);
            bulletFlightTime = (firstBullet.getOwnerState().aDistance(enemy) - firstBullet.getTravelledDistance()) /
                    firstBullet.getSpeed();
        } while (bulletFlightTime < 1);

        return bulletFlightTime;
    }

}
