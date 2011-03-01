package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class BearingToClosestWallVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) toDegrees(t.bearingToClosestWall());
    }
}
