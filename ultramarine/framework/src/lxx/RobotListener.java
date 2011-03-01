package lxx;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;

/**
 * User: jdev
 * Date: 02.11.2009
 */
public interface RobotListener {

    void onBulletHitBullet(BulletHitBulletEvent e);
    void onHitByBullet(HitByBulletEvent e);
    void onBulletHit(BulletHitEvent event);

    void onBulletMissed(BulletMissedEvent event);

    void tick();
}
