package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

/**
 * User: jdev
 * Date: 05.08.11
 */
public class EnemyOutgoingWavesCollectedVE implements AttributeValueExtractor {

    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return office.getStatisticsManager().getEnemyHitRate().getFireCount();
    }

}
