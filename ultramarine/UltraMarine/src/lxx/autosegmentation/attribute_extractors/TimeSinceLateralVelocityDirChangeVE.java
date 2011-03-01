package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

/**
 * User: jdev
 * Date: 24.02.2010
 */
public class TimeSinceLateralVelocityDirChangeVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        if ((um.getTime() - t.getLastLateralDirChange()) > 255) {
            return 255;
        }
        return (int) (um.getTime() - t.getLastLateralDirChange());
    }
}
