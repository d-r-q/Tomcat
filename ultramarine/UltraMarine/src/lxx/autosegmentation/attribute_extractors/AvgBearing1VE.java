package lxx.autosegmentation.attribute_extractors;

import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.LXXBullet;
import lxx.UltraMarine;

import java.util.List;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class AvgBearing1VE implements AttributeValueExtractor {

    public int getAttributeValue(Target t, UltraMarine um, BulletManager bulletManager) {
        final List<LXXBullet> bullets = bulletManager.getBullets();
        if (bullets.size() == 0) {
            return 46;
        }
        int avgBulletBearing1 = 0;
        for (LXXBullet bullet : bullets) {
            avgBulletBearing1 += robocode.util.Utils.normalRelativeAngleDegrees(bullet.getBullet().getHeading() - toDegrees(um.angleTo(t)));
        }
        avgBulletBearing1 /= bullets.size() > 0 ? bullets.size() : 1;

        return avgBulletBearing1 / 4;
    }

}
