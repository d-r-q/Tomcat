package lxx.autosegmentation.attribute_extractors;

import lxx.autosegmentation.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.UltraMarine;
import lxx.utils.Utils;
import robocode.Rules;import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 14.03.2010
 */
public class FistBulletBearingVE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        LXXBullet b = bulletManager.getFirstBullet();
            if (b == null) {
                return 19;
            }
            return (int) toDegrees(robocode.util.Utils.normalRelativeAngle(Utils.angle(b.getCurrentPos(), t) - b.getBullet().getHeading())) / 10;
    }
}
