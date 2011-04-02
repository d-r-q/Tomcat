package lxx.model.attributes.attribute_extractors.target;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 02.04.11
 */
public class FirstBulletBearingOffsetVE implements AttributeValueExtractor {

    public int getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets) {
        if (myBullets.size() == 0) {
            return 0;
        }

        final LXXBullet firstBullet = myBullets.get(0);
        final double bulletFlightTime = (firstBullet.getFirePosition().aDistance(enemy) - firstBullet.getFirePosition().aDistance(firstBullet.getCurrentPosition())) /
                firstBullet.getSpeed();
        final double latDir = signum(LXXUtils.lateralVelocity2(firstBullet.getFirePosition(), enemy, enemy.getState().getVelocityModule(), enemy.getState().getAbsoluteHeadingRadians()));
        final LXXPoint maxEnemyPos = enemy.getPosition().project(firstBullet.getFirePosition().angleTo(enemy) + LXXConstants.RADIANS_90 * latDir, Rules.MAX_VELOCITY * bulletFlightTime);
        final double maxEscapeAngle = abs(Utils.normalRelativeAngle(firstBullet.getCurrentPosition().angleTo(maxEnemyPos) - firstBullet.getCurrentPosition().angleTo(enemy)));
        final double bearingOffset = Utils.normalRelativeAngle(firstBullet.getHeadingRadians() - firstBullet.getCurrentPosition().angleTo(enemy)) / maxEscapeAngle;


        return (int) LXXUtils.limit(-4, bearingOffset * 2, 4);
    }

}
