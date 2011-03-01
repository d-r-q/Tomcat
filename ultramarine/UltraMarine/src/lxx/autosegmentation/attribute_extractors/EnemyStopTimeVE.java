package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class EnemyStopTimeVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        if ((um.getTime() - t.getLastTrevelTime()) > 255) {
            return 255;
        }
        return (int) (um.getTime() - t.getLastTrevelTime());
    }
}
