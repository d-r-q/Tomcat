package lxx.ts_log.attributes.attribute_extractors.target;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 16.05.11
 */
public class EnemyBearingToFirstBullet implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        if (myBullets.size() == 0) {
            return 0;
        }

        LXXBullet firstBullet;
        int idx = 0;
        double bulletFlightTime;
        do {
            if (idx == myBullets.size()) {
                return 0;
            }
            firstBullet = myBullets.get(idx++);
            bulletFlightTime = (firstBullet.getFirePosition().aDistance(enemy) - firstBullet.getFirePosition().aDistance(firstBullet.getCurrentPosition())) /
                    firstBullet.getSpeed();
        } while (bulletFlightTime < 1);

        return toDegrees(Utils.normalRelativeAngle(enemy.angleTo(firstBullet.getFirePosition()) - enemy.getState().getAbsoluteHeadingRadians()));
    }
}
