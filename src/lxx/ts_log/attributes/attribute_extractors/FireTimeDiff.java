package lxx.ts_log.attributes.attribute_extractors;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.bullets.BulletSnapshot;

import java.util.List;

import static java.lang.Math.min;

/**
 * User: jdev
 * Date: 11.02.12
 */
public class FireTimeDiff implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        final List<BulletSnapshot> myBullets = me.getBulletsInAir();
        final int timeTillFire = me.getTurnsToGunCool();
        final int timeSinceFire = myBullets.size() > 0 ? (int) (me.getSnapshotTime() - myBullets.get(myBullets.size() - 1).getLaunchTime()) : Integer.MAX_VALUE;
        return min(timeTillFire, timeSinceFire);
    }

}
