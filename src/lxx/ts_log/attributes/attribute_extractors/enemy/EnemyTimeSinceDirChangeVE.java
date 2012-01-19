package lxx.ts_log.attributes.attribute_extractors.enemy;

import lxx.LXXRobot;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;

import java.util.List;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class EnemyTimeSinceDirChangeVE implements AttributeValueExtractor {
    public double getAttributeValue(LXXRobot enemy, LXXRobot me, List<LXXBullet> myBullets, Office office) {
        return enemy.getTime() - enemy.getLastDirChangeTime();
    }
}
