package lxx.autosegmentation.attribute_extractors;

import lxx.autosegmentation.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

/**
 * User: jdev
 * Date: 28.02.2010
 */
public class EnemyYVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) (t.getY() / 10);
    }
}
