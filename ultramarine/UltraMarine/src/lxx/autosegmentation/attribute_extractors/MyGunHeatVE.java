package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class MyGunHeatVE implements AttributeValueExtractor {

    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) um.getGunHeat() * 10;
    }

}
