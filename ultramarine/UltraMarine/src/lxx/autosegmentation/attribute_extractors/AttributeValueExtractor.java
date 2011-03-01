package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.UltraMarine;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public interface AttributeValueExtractor {

    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager);

}
