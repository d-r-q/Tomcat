package lxx.autosegmentation.attribute_extractors;

import lxx.autosegmentation.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 14.03.2010
 */
public class FirePowerVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) round(um.firePower());
    }
}
