/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.bullets;

import lxx.RobotListener;
import lxx.events.FireEvent;
import lxx.events.LXXKeyEvent;
import lxx.events.LXXPaintEvent;
import lxx.events.TickEvent;
import lxx.utils.LXXGraphics;
import robocode.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class BulletManager implements RobotListener {

    private static boolean paintEnabled = false;

    private final List<LXXBullet> bullets = new ArrayList<LXXBullet>();
    private final List<BulletManagerListener> listeners = new LinkedList<BulletManagerListener>();

    private void addBullet(LXXBullet bullet) {
        bullets.add(bullet);
    }

    private void onBulletHitBullet(BulletHitBulletEvent e) {
        LXXBullet b = getBullet(e.getBullet());
        removeBullet(b);
    }

    private void removeBullet(LXXBullet b) {
        bullets.remove(b);
    }

    private void onBulletHit(BulletHitEvent event) {
        LXXBullet b = getBullet(event.getBullet());
        if (b.getTarget().getName().equals(event.getBullet().getVictim())) {
            for (BulletManagerListener listener : listeners) {
                listener.bulletHit(b);
            }
        } else {
            if (b.getTravelledDistance() >= b.getDistanceToTarget()) {
                for (BulletManagerListener listener : listeners) {
                    listener.bulletMiss(b);
                }
            }
        }
        removeBullet(b);
    }

    private void onBulletMissed(BulletMissedEvent event) {
        final LXXBullet b = getBullet(event.getBullet());
        if (b != null && b.getTarget() != null && b.getTarget().isAlive()) {
            for (BulletManagerListener lst : listeners) {
                lst.bulletMiss(b);
            }
        }
        removeBullet(b);
    }

    private LXXBullet getBullet(Bullet bullet) {
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
        for (LXXBullet b : bullets) {
            if (b.getFirePosition().aDistance(b.getTarget()) > b.getTravelledDistance()) {
                return b;
            }
        }
        return null;
    }

    public void addListener(BulletManagerListener listener) {
        listeners.add(listener);
    }

    private void onTick() {
        List<LXXBullet> toDelete = new ArrayList<LXXBullet>();
        for (LXXBullet b1 : bullets) {
            if (!b1.getBullet().isActive()) {
                toDelete.add(b1);
            }
        }

        bullets.removeAll(toDelete);
    }

    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            onTick();
        } else if (event instanceof BulletMissedEvent) {
            onBulletMissed((BulletMissedEvent) event);
        } else if (event instanceof BulletHitEvent) {
            onBulletHit((BulletHitEvent) event);
        } else if (event instanceof BulletHitBulletEvent) {
            onBulletHitBullet((BulletHitBulletEvent) event);
        } else if (event instanceof FireEvent) {
            addBullet(((FireEvent) event).getBullet());
        } else if (event instanceof LXXPaintEvent && bullets.size() > 0 && paintEnabled) {
            final LXXBullet firstBullet = getFirstBullet();
            if (firstBullet == null) {
                return;
            }
            LXXGraphics g = ((LXXPaintEvent) event).getGraphics();
            firstBullet.getAimPredictionData().paint(g, firstBullet);
        } else if (event instanceof LXXKeyEvent) {
            if (Character.toUpperCase(((LXXKeyEvent) event).getKeyChar()) == 'G') {
                paintEnabled = !paintEnabled;
            }
        }
    }

    public List<LXXBullet> getBullets() {
        return bullets;
    }
}
