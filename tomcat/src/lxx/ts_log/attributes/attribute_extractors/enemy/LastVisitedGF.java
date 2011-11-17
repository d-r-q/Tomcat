/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

public class LastVisitedGF implements AttributeValueExtractor {

    private int offset;

    public LastVisitedGF(int offset) {
        this.offset = offset;
    }

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        if (enemy.getVisitedGuessFactors().size() < offset) {
            return 0;
        }

        return enemy.getVisitedGuessFactors().get(enemy.getVisitedGuessFactors().size() - offset);
    }
}
