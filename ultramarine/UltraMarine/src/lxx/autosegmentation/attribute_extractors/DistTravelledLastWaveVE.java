package lxx.autosegmentation.attribute_extractors;

import lxx.autosegmentation.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

/**
 * User: jdev
 * Date: 11.03.2010
 */
public class DistTravelledLastWaveVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) (t.getDistTravelledLastWave() / 20);
    }
}
