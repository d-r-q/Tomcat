package lxx.model.attributes.attribute_extractors;

import lxx.utils.LXXRobot;

import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 09.03.11
 */
public class DistanceBetween_100VE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me) {
        return (int) round(enemy.aDistance(me) / 100);
    }

}
