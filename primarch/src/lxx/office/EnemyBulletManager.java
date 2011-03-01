/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Primarch;
import lxx.RobotListener;
import lxx.enemy_bullets.EnemyFireAnglePredictor;
import lxx.events.LXXKeyEvent;
import lxx.events.LXXPaintEvent;
import lxx.events.TickEvent;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.bullets.BulletManagerListener;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import robocode.*;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 09.01.2010
 */
public class EnemyBulletManager implements WaveCallback, TargetManagerListener, RobotListener {

    private static boolean paintEnabled = false;

    private final Map<String, Set<Wave>> waves = new HashMap<String, Set<Wave>>();
    private final Map<Wave, Bullet> hittedBullets = new HashMap<Wave, Bullet>();
    private final Map<Wave, Bullet> interceptedBullets = new HashMap<Wave, Bullet>();
    private final Map<Wave, LXXBullet> predictedBullets = new HashMap<Wave, LXXBullet>();
    private final List<BulletManagerListener> listeners = new ArrayList<BulletManagerListener>();
    private final EnemyFireAnglePredictor enemyFireAnglePredictor;

    private final WaveManager waveManager;
    private final LXXRobot robot;

    private HitByBulletEvent lastHitEvent;
    private int bulletsInAir;

    public EnemyBulletManager(Office office, Primarch robot) {
        enemyFireAnglePredictor = new EnemyFireAnglePredictor(office.getBattleSnapshotManager());
        this.waveManager = office.getWaveManager();
        this.robot = robot;
    }

    public void targetUpdated(Target target) {
        if (target.isFireLastTick()) {
            Set<Wave> ws = waves.get(target.getName());
            if (ws == null) {
                ws = new HashSet<Wave>();
                waves.put(target.getName(), ws);
            }
            final Wave wave = waveManager.launchWave(target.getPrevState(), robot, Rules.getBulletSpeed(target.getExpectedEnergy() - target.getEnergy()), this);
            ws.add(wave);

            enemyFireAnglePredictor.enemyFire(wave);

            final double angleToTarget = 0;
            final APoint bulletPos = wave.getSourceStateAtFireTime().project(angleToTarget, wave.getTraveledDistance());
            final LXXBullet b = new LXXBullet(
                    new Bullet(angleToTarget, bulletPos.getX(), bulletPos.getY(), LXXUtils.getBulletPower(wave.getSpeed()), wave.getSourceStateAtFireTime().getRobot().getName(), wave.getTargetStateAtFireTime().getRobot().getName(), true, -1),
                    wave, enemyFireAnglePredictor.getPredictionData(target));
            predictedBullets.put(wave, b);
        }
    }

    public void wavePassing(Wave w) {
        if (!hittedBullets.containsKey(w) && lastHitEvent != null && robot.getTime() == lastHitEvent.getTime() && w.getSourceStateAtFireTime().getRobot().getName().equals(lastHitEvent.getName())) {
            hittedBullets.put(w, lastHitEvent.getBullet());
            for (BulletManagerListener listener : listeners) {
                listener.bulletHit(new LXXBullet(lastHitEvent.getBullet(), w, null));
            }
            enemyFireAnglePredictor.updateWaveState(w, lastHitEvent.getHeadingRadians());
        } else if (!hittedBullets.containsKey(w)) {
            // todo(zhidkov): implement flatteners
            //enemyFireAnglePredictor.updateWaveState(w);
        }
    }

    public void waveBroken(Wave w) {
        if (hittedBullets.containsKey(w)) {
            hittedBullets.remove(w);
        } else if (!interceptedBullets.containsKey(w)) {
            for (BulletManagerListener listener : listeners) {
                listener.bulletMiss(new LXXBullet(null, w, null));
            }
        }
        waves.get(w.getSourceStateAtFireTime().getRobot().getName()).remove(w);
    }

    public void paint(LXXGraphics g) {
        if (paintEnabled) {
            for (LXXBullet bullet : getBullets()) {
                final AimingPredictionData aimPredictionData = bullet.getAimPredictionData();
                if (aimPredictionData != null) {
                    aimPredictionData.paint(g, bullet);
                }
            }
        }
    }

    public Wave getClosestWave() {
        Wave res = null;
        double minTime = Double.MAX_VALUE;
        for (Set<Wave> ws : waves.values()) {
            for (Wave w : ws) {
                double dist = w.getSourceStateAtFireTime().aDistance(robot) - w.getTraveledDistance();
                if (w.getTraveledDistance() < w.getSourceStateAtFireTime().aDistance(robot) &&
                        dist / w.getSpeed() < minTime &&
                        !hittedBullets.containsKey(w) &&
                        !interceptedBullets.containsKey(w)) {
                    minTime = dist / w.getSpeed();
                    res = w;
                }
            }
        }

        return res;
    }

    private LXXBullet getBullet(Wave wave) {
        final LXXBullet lxxBullet = predictedBullets.get(wave);
        if (lxxBullet == null) {
            return null;
        }
        final double angleToTarget = lxxBullet.getHeadingRadians();
        final APoint bulletPos = wave.getSourceStateAtFireTime().project(angleToTarget, wave.getTraveledDistance());
        final Bullet bullet = new Bullet(angleToTarget, bulletPos.getX(), bulletPos.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceStateAtFireTime().getRobot().getName(), wave.getTargetStateAtFireTime().getRobot().getName(), true, -1);
        return new LXXBullet(bullet, wave, lxxBullet.getAimPredictionData());
    }

    public Collection<Wave> getWaves() {
        final List<Wave> res = new ArrayList<Wave>();
        for (Set<Wave> waves : this.waves.values()) {
            res.addAll(waves);
        }
        return res;
    }

    public void addListener(BulletManagerListener listener) {
        listeners.add(listener);
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        final Wave w = getWave(e.getHitBullet());
        if (w == null) {
            return;
        }
        enemyFireAnglePredictor.updateWaveState(w, e.getHitBullet().getHeadingRadians());
        interceptedBullets.put(w, e.getBullet());
    }

    public Wave getWave(Bullet b) {
        final Set<Wave> targetWaves = waves.get(b.getName());
        if (targetWaves == null) {
            return null;
        }
        for (Wave w : targetWaves) {
            if (abs(w.getSpeed() - Rules.getBulletSpeed(b.getPower())) < 0.0001 &&
                    abs(w.getTraveledDistance() - w.getSourcePos().aDistance(new LXXPoint(b.getX(), b.getY()))) < w.getSpeed() * 2) {
                return w;
            }
        }
        return null;
    }

    public void onHitByBullet(HitByBulletEvent e) {
        lastHitEvent = e;
    }

    public LXXBullet getClosestBullet() {
        final Wave closestWave = getClosestWave();
        if (closestWave == null) {
            return null;
        }

        return getBullet(closestWave);
    }

    public List<LXXBullet> getBullets() {
        final List<LXXBullet> bullets = new ArrayList<LXXBullet>();

        final Collection<Wave> waves = getWaves();
        if (waves == null) {
            return bullets;
        }
        for (Wave w : waves) {
            if (w.getTraveledDistance() < w.getSourceStateAtFireTime().aDistance(robot) &&
                    !hittedBullets.containsKey(w) && !interceptedBullets.containsKey(w)) {
                final LXXBullet lxxBullet = getBullet(w);
                if (lxxBullet != null) {
                    bullets.add(lxxBullet);
                }
            }
        }

        Collections.sort(bullets, new Comparator<LXXBullet>() {

            public int compare(LXXBullet o1, LXXBullet o2) {
                return (int) signum(o2.getTravelledDistance() - o1.getTravelledDistance());
            }
        });

        return bullets;
    }

    public void onEvent(Event event) {
        if (event instanceof HitByBulletEvent) {
            onHitByBullet((HitByBulletEvent) event);
        } else if (event instanceof BulletHitBulletEvent) {
            onBulletHitBullet((BulletHitBulletEvent) event);
        } else if (event instanceof LXXPaintEvent && paintEnabled) {
            paint(((LXXPaintEvent) event).getGraphics());
        } else if (event instanceof TickEvent) {
            bulletsInAir = getBullets().size();
        } else if (event instanceof LXXKeyEvent) {
            if (Character.toUpperCase(((LXXKeyEvent) event).getKeyChar()) == 'M') {
                paintEnabled = !paintEnabled;
            }
        }
    }

    public int getBulletsInAirCount() {
        return bulletsInAir;
    }

    public boolean isNoBulletsInAir() {
        return getBulletsInAirCount() == 0;
    }

    public boolean hasBulletsOnAir() {
        return getBulletsInAirCount() > 0;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public LXXBullet getImaginaryBullet(Target t) {
        final Wave wave = new Wave(t.getState(), robot.getState(), Rules.getBulletSpeed(t.getFirePower()), robot.getTime() + 1);
        final Bullet bullet = new Bullet(t.angleTo(robot), t.getX(), t.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceStateAtFireTime().getRobot().getName(), wave.getTargetStateAtFireTime().getRobot().getName(), true, -1);
        return new LXXBullet(bullet, wave, enemyFireAnglePredictor.getPredictionData(t));
    }
}
