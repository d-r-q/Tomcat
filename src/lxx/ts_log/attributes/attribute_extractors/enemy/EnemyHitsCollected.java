package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.office.StatisticsManager;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

/**
 * User: Aleksey Zhidkov
 * Date: 19.06.12
 */
public class EnemyHitsCollected implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return enemy.getHitsCollected();
    }

}
