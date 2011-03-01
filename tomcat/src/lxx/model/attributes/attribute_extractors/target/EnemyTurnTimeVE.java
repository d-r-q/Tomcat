/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;

public class EnemyTurnTimeVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) (enemy.getTime() - enemy.getLastNotTurnTime());
    }
}
