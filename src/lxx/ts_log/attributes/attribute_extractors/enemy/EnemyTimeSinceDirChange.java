package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class EnemyTimeSinceDirChange implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return enemy.getSnapshotTime() - enemy.getLastDirChangeTime();
    }
}
