package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.UltraMarine;
import robocode.Rules;

/**
 * User: jdev
 * Date: 24.02.2010
 */
public class LateralAccelerationVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        return (int) (t.getVelocityDelta() * Math.sin(t.getHeading() - um.angleTo(t)));
    }

    /**
 * User: jdev
     * Date: 10.03.2010
     */
    public static class FirstBulletFlightTimeVE implements AttributeValueExtractor {
        public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
            LXXBullet b = bulletManager.getFirstBullet();
            if (b == null) {
                return 21;
            }
            final int res = (int) (b.getCurrentPos().aDistance(t) / (Rules.getBulletSpeed(b.getBullet().getPower()) * 2));
            if (res > 21) {
                return 21;
            }
            return res;
        }
    }

    /**
 * User: jdev
     * Date: 10.03.2010
     */
    public static class LastBulletFlightTimeVE implements AttributeValueExtractor {
        public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
            LXXBullet b = bulletManager.getLastBullet();
            if (b == null) {
                return 21;
            }
            final int res = (int) (b.getCurrentPos().aDistance(t) / (Rules.getBulletSpeed(b.getBullet().getPower()) * 2));
            if (res > 21) {
                return 21;
            }
            return res;
        }
    }

    /**
 * User: jdev
     * Date: 10.03.2010
     */
    public static class MyGunHeatVE implements AttributeValueExtractor {
        public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
            return (int) um.getGunHeat() * 10;
        }
    }
}
