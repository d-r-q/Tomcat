package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.EnemySnapshot;
import lxx.MySnapshot;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.LXXUtils;

/**
 * User: Aleksey Zhidkov
 * Date: 21.06.12
 */
public class EnemyLateralDirection implements AttributeValueExtractor {

    public double getAttributeValue(EnemySnapshot enemy, MySnapshot me) {
        return LXXUtils.lateralDirection(me, enemy);
    }

}
