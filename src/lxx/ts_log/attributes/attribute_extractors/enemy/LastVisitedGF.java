/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

public class LastVisitedGF implements AttributeValueExtractor {

    private int offset;

    public LastVisitedGF(int offset) {
        this.offset = offset;
    }

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        if (enemy.getVisitedGuessFactors().size() < offset) {
            return 0;
        }

        return enemy.getVisitedGuessFactors().get(enemy.getVisitedGuessFactors().size() - offset);
    }
}
