package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;
import robocode.Rules;

/**
 * User: jdev
 * Date: 24.02.2010
 */
public class BulletFlightTimeVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) (um.aDistance(t) / Rules.getBulletSpeed(um.firePower()));
    }
}
