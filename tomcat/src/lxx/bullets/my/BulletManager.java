/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.my;

import lxx.RobotListener;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.events.FireEvent;
import lxx.events.LXXKeyEvent;
import lxx.events.LXXPaintEvent;
import lxx.events.TickEvent;
import lxx.paint.LXXGraphics;
import lxx.utils.LXXPoint;
import lxx.utils.wave.Wave;
import robocode.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

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
        for (BulletManagerListener listener : listeners) {
            listener.bulletFired(bullet);
        }
    }

    private void onBulletHitBullet(BulletHitBulletEvent e) {
        final LXXBullet b = getBullet(e.getBullet());
        if (b == null) {
            return;
        }
        removeBullet(b);
    }

    private void removeBullet(LXXBullet b) {
        bullets.remove(b);
    }

    private void onBulletHit(BulletHitEvent event) {
        final LXXBullet b = getBullet(event.getBullet());
        if (b == null) {
            return;
        }
        if (b.getTarget().getName().equals(event.getName())) {
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

    public LXXBullet getBullet(Bullet b) {
        for (LXXBullet bullet : bullets) {
            final Wave w = bullet.getWave();
            if (abs(w.getSpeed() - Rules.getBulletSpeed(b.getPower())) < 0.1 &&
                    abs(w.getTraveledDistance() - w.getSourcePosAtFireTime().aDistance(new LXXPoint(b.getX(), b.getY()))) < w.getSpeed() + 1) {
                return bullet;
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

    public LXXBullet getLastBullet() {
        if (bullets.size() == 0) {
            return null;
        }
        return bullets.get(bullets.size() - 1);
    }
}
