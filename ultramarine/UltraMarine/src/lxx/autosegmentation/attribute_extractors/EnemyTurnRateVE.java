package lxx.autosegmentation.attribute_extractors;

import lxx.UltraMarine;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 07.03.2010
 */
public class EnemyTurnRateVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) toDegrees(t.getHeadingDelta());
    }
}
