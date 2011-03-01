package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class TimeSinceMyLastFireVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        final int res = (int) (um.getTime() - ((UltraMarine) um).getLastFireTime()) / 2;
        if (res > 20) {
            return 21;
        }
        return res;
    }
}
