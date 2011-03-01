package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.UltraMarine;
import lxx.utils.Utils;

import java.util.List;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class AvgBearing2VE implements AttributeValueExtractor {
    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        final List<LXXBullet> bullets = bulletManager.getBullets();
        if (bullets.size() == 0) {
            return 46;
        }
        int avgBulletBearing2 = 0;
        for (LXXBullet bullet : bullets) {
            avgBulletBearing2 += robocode.util.Utils.normalRelativeAngleDegrees(bullet.getBullet().getHeading() - toDegrees(Utils.angle(bullet.getCurrentPos(), t)));
        }
        avgBulletBearing2 /= bullets.size() > 0 ? bullets.size() : 1;

        return avgBulletBearing2 / 5;
    }
}
