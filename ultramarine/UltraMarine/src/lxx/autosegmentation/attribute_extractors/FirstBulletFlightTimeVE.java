package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.UltraMarine;
import robocode.Rules;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class FirstBulletFlightTimeVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        LXXBullet b = bulletManager.getFirstBullet();
        if (b == null) {
            return 21;
        }
        return (int) (b.getCurrentPos().aDistance(t) / (Rules.getBulletSpeed(b.getBullet().getPower()) * 2));
    }
}
