package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXRobot;

/**
 * User: jdev
 * Date: 26.02.11
 */
public class EnemyStopTimeVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) (enemy.getTime() - enemy.getLastTravelTime());
    }
}
