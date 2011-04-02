package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXRobot;

import java.util.List;

/**
 * User: jdev
 * Date: 26.02.11
 */
public class EnemyStopTimeVE implements AttributeValueExtractor {
    public int getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        return (int) (enemy.getTime() - enemy.getLastTravelTime());
    }
}
