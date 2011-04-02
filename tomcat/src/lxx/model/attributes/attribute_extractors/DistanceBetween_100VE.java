package lxx.model.attributes.attribute_extractors;

import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;

import java.util.List;

import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 09.03.11
 */
public class DistanceBetween_100VE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return (int) round(enemy.aDistance(me) / 100);
    }

}
