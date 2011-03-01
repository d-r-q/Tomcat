package lxx.targeting.bullets;

import lxx.targeting.TargetManager;
import lxx.targeting.Target;
import lxx.RobotListener;
import lxx.utils.LXXPoint;
import lxx.utils.Utils;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import static java.lang.StrictMath.sin;
import static java.lang.Math.cos;

import robocode.*;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class BulletManager implements RobotListener {

    private final List<LXXBullet> bullets = new ArrayList<LXXBullet>();
    private final List<BulletManagerListener> listeners = new ArrayList<BulletManagerListener>();

    private final TargetManager targetManager;
    public int bulletCount = 0;
    public int eventCount = 0;
    private int naCount;

    public BulletManager(TargetManager targetManager) {
        this.targetManager = targetManager;
    }

    public void addBullet(LXXBullet bullet) {
        bullets.add(bullet);
        bulletCount++;
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        if (naCount > 0) {
            //System.out.println("onBulletHitBullet");
        }
        eventCount++;
        LXXBullet b = getBullet(e.getBullet());
        for (BulletManagerListener lst : listeners) {
            lst.bulletNotHit(b);
        }
        removeBullet(b);
    }

    private void removeBullet(LXXBullet b) {
        if (b.getBullet().isActive()) {
            throw new RuntimeException("Something wrong!");
        }
        if (!bullets.remove(b)) {
            throw new RuntimeException("Something wrong!");
        }
        /*for (LXXBullet b1 : bullets) {
            if (!b1.getBullet().isActive()) {
                // todo: fix me
                bullets.remove(b1);
            }
        }*/
    }

    public void onHitByBullet(HitByBulletEvent e) {
    }

    public void onBulletHit(BulletHitEvent event) {
        if (naCount > 0) {
            //System.out.println("onBulletHit");
        }
        eventCount++;
        LXXBullet b = getBullet(event.getBullet());
        if (b.getTarget().getName().equals(event.getBullet().getVictim())) {
            for (BulletManagerListener listener : listeners) {
                listener.bulletHit(b);
            }
        } else {
            if (b.getTravelledDistance() < b.getDistanceToTarget()) {
                for (BulletManagerListener listener : listeners) {
                    listener.bulletNotHit(b);
                }
            } else {
                for (BulletManagerListener listener : listeners) {
                    listener.bulletMiss(b);
                }
            }
        }
        removeBullet(b);
    }

    public void onBulletMissed(BulletMissedEvent event) {
        if (naCount > 0) {
            //System.out.println("onBulletMissed");
        }
        eventCount++;
        LXXBullet b = getBullet(event.getBullet());
        if (b.getTarget().isAlive()) {
            for (BulletManagerListener lst : listeners) {
                lst.bulletMiss(b);
            }
        } else {
            for (BulletManagerListener lst : listeners) {
                lst.bulletNotHit(b);
            }
        }
        removeBullet(b);
    }

    public void tick() {
        List<LXXBullet> toDelete = new ArrayList<LXXBullet>();
        for (LXXBullet b1 : bullets) {
            if (!b1.getBullet().isActive()) {
                toDelete.add(b1);
            }
        }

        bullets.removeAll(toDelete);
    }

    public LXXBullet getBullet(Bullet bullet) {
        for (LXXBullet b : bullets) {
            if (bullet.equals(b.getBullet())) {
                return b;
            }
        }

        return null;
    }

    public LXXBullet getFirstBullet() {
        if (bullets.size() == 0) {
            return null;
        }
        return bullets.get(0);
    }

    public LXXBullet getLastBullet() {
        if (bullets.size() == 0) {
            return null;
        }
        return bullets.get(bullets.size() - 1);
    }

    public void addListener(BulletManagerListener listener) {
        listeners.add(listener);
    }

    public List<LXXBullet> getBullets() {
        return bullets;
    }


    public void paint(Graphics2D g) {
        LXXBullet nextBullet = null;
        for (LXXBullet b : bullets) {
            final double travelledDistance = b.getTravelledDistance();
            if (travelledDistance > b.getFirePosition().aDistance(b.getTarget())) {
                continue;
            }
            if (nextBullet == null) {
                nextBullet = b;
            }
            final LXXPoint src = b.getFirePosition();

            g.setColor(Color.BLUE);
            g.drawOval((int) (b.getTargetPos().getX() - 10), (int) (b.getTargetPos().getY() - 10), 20, 20);

            g.setColor(Color.CYAN);
            LXXPoint dst = new LXXPoint(b.getBullet().getX(), b.getBullet().getY());
            g.drawLine((int) src.getX(), (int) src.getY(), (int) dst.getX(), (int) dst.getY());

            g.setColor(Color.DARK_GRAY);
            g.drawOval((int) (src.getX() - travelledDistance), (int) (src.getY() - travelledDistance),
                    (int) travelledDistance * 2, (int) travelledDistance * 2);
        }
        if (nextBullet != null && nextBullet.getPredictionData() != null) {
            nextBullet.getPredictionData().paint(g);
        }
    }
}
