package lxx.ts_log.attributes.attribute_extractors;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;

import java.util.List;

import static java.lang.Math.min;

/**
 * User: jdev
 * Date: 11.02.12
 */
public class FireTimeDiff implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        final int timeTillFire = office.getRobot().getTurnsToGunCool();
        final int timeSinceFire = myBullets.size() > 0 ? (int) (me.getTime() - myBullets.get(myBullets.size() - 1).getWave().getLaunchTime()) : Integer.MAX_VALUE;
        return min(timeTillFire, timeSinceFire);
    }

}
