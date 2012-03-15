package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.EnemySnapshotImpl;
import lxx.LXXRobot;
import lxx.MySnapshotImpl;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class EnemyTimeSinceDirChange implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return enemy.getTime() - ((EnemySnapshot)enemy).getLastDirChangeTime();
    }

    public double getAttributeValue(EnemySnapshotImpl enemy, MySnapshotImpl me) {
        return enemy.getSnapshotTime() - enemy.getLastDirChangeTime();
    }
}
