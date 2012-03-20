package lxx.ts_log.attributes.attribute_extractors;

import lxx.EnemySnapshotImpl;
import lxx.LXXRobot;
import lxx.MySnapshotImpl;
import lxx.bullets.BulletSnapshot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;

import java.util.List;

import static java.lang.Math.min;

/**
 * User: jdev
 * Date: 11.02.12
 */
public class FireTimeDiff implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        final List<BulletSnapshot> myBullets = me.getBulletsInAir();
        final int timeTillFire = me.getTurnsToGunCool();
        final int timeSinceFire = myBullets.size() > 0 ? (int) (me.getSnapshotTime() - myBullets.get(myBullets.size() - 1).getLaunchTime()) : Integer.MAX_VALUE;
        return min(timeTillFire, timeSinceFire);
    }

}
