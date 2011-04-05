/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.RobotListener;
import lxx.Tomcat;
import lxx.enemy_bullets.EnemyFireAnglePredictor;
import lxx.enemy_bullets.GFAimingPredictionData;
import lxx.events.LXXKeyEvent;
import lxx.events.LXXPaintEvent;
import lxx.events.TickEvent;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.bullets.BulletManagerListener;
import lxx.targeting.bullets.LXXBullet;
import lxx.targeting.bullets.LXXBulletState;
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

    private static final int SAFE_BULLET_SPEED = 10;

    private final Map<Wave, LXXBullet> predictedBullets = new HashMap<Wave, LXXBullet>();
    private final List<BulletManagerListener> listeners = new LinkedList<BulletManagerListener>();
    private final EnemyFireAnglePredictor enemyFireAnglePredictor;

    private final WaveManager waveManager;
    private final LXXRobot robot;

    private int bulletsOnAir;

    public EnemyBulletManager(Office office, Tomcat robot) {
        enemyFireAnglePredictor = new EnemyFireAnglePredictor(office.getTurnSnapshotsLog());
        addListener(enemyFireAnglePredictor);
        this.waveManager = office.getWaveManager();
        this.robot = robot;
    }

    public void targetUpdated(Target target) {
        if (target.isFireLastTick()) {
            final double bulletPower = target.getExpectedEnergy() - target.getEnergy();
            final double bulletSpeed = Rules.getBulletSpeed(bulletPower);

            final LXXRobotState targetPrevState = target.getPrevState();
            final LXXRobotState robotPrevState = robot.getPrevState();

            final double angleToMe = targetPrevState.angleTo(robotPrevState);

            final Bullet fakeBullet = new Bullet(angleToMe, targetPrevState.getX(), targetPrevState.getY(),
                    bulletPower, target.getName(), robot.getName(), true, -1);

            final Wave wave = waveManager.launchWave(targetPrevState, robotPrevState,
                    bulletSpeed, this);

            final LXXBullet lxxBullet = new LXXBullet(fakeBullet, wave, enemyFireAnglePredictor.getPredictionData(target));

            predictedBullets.put(wave, lxxBullet);

            for (BulletManagerListener listener : listeners) {
                listener.bulletFired(lxxBullet);
            }
        }
    }

    public void wavePassing(Wave w) {
        final LXXBullet lxxBullet = getLXXBullet(w);
        for (BulletManagerListener listener : listeners) {
            listener.bulletPassing(lxxBullet);
        }
    }

    public void waveBroken(Wave w) {
        final LXXBullet lxxBullet = getLXXBullet(w);
        if (lxxBullet.getState() == LXXBulletState.ON_AIR) {
            lxxBullet.setState(LXXBulletState.MISSED);
            for (BulletManagerListener listener : listeners) {
                listener.bulletMiss(lxxBullet);
            }
        }

        predictedBullets.remove(w);
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        final Wave w = getWave(e.getHitBullet());
        if (w == null) {
            System.out.println("[WARN] intercept not detected bullet");
            return;
        }

        final LXXBullet lxxBullet = getLXXBullet(w, e.getHitBullet());
        lxxBullet.setState(LXXBulletState.INTERCEPTED);

        for (BulletManagerListener listener : listeners) {
            listener.bulletIntercepted(lxxBullet);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        final Wave w = getWave(e.getBullet());
        if (w == null) {
            System.out.println("[WARN] hit by not detected bullet");
            return;
        }

        final LXXBullet lxxBullet = getLXXBullet(w, e.getBullet());
        lxxBullet.setState(LXXBulletState.HITTED);
        for (BulletManagerListener listener : listeners) {
            listener.bulletHit(lxxBullet);
        }
    }

    public Wave getWave(Bullet b) {
        for (Wave w : predictedBullets.keySet()) {
            if (abs(w.getSpeed() - Rules.getBulletSpeed(b.getPower())) < 0.0001 &&
                    abs(w.getTraveledDistance() - w.getSourcePosAtFireTime().aDistance(new LXXPoint(b.getX(), b.getY()))) < w.getSpeed() + 1) {
                return w;
            }
        }
        return null;
    }

    private LXXBullet getLXXBullet(Wave wave) {
        final Bullet bullet = getFakeBullet(wave);
        return getLXXBullet(wave, bullet);
    }

    private Bullet getFakeBullet(Wave wave) {
        final double bulletHeading = wave.getSourcePosAtFireTime().angleTo(wave.getTargetPosAtFireTime());
        final APoint bulletPos = wave.getSourceStateAtFireTime().project(bulletHeading, wave.getTraveledDistance());
        return new Bullet(bulletHeading, bulletPos.getX(), bulletPos.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceStateAtFireTime().getRobot().getName(), wave.getTargetStateAtLaunchTime().getRobot().getName(), true, -1);
    }

    private LXXBullet getLXXBullet(Wave wave, Bullet bullet) {
        final LXXBullet lxxBullet = predictedBullets.get(wave);
        if (lxxBullet == null) {
            return null;
        }

        lxxBullet.setBullet(bullet);
        return lxxBullet;
    }

    public List<LXXBullet> getBulletsOnAir(int flightTimeLimit) {
        final List<LXXBullet> bullets = new ArrayList<LXXBullet>();

        for (LXXBullet lxxBullet : predictedBullets.values()) {
            double flightTime = (lxxBullet.getFirePosition().aDistance(lxxBullet.getTarget()) - lxxBullet.getTravelledDistance()) / lxxBullet.getSpeed();
            if (flightTime > flightTimeLimit && lxxBullet.getState() == LXXBulletState.ON_AIR) {
                bullets.add(lxxBullet);
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
            bulletsOnAir = getBulletsOnAir(2).size();
        } else if (event instanceof LXXKeyEvent) {
            if (Character.toUpperCase(((LXXKeyEvent) event).getKeyChar()) == 'M') {
                paintEnabled = !paintEnabled;
            }
        }
    }

    public int getBulletsOnAirCount() {
        return bulletsOnAir;
    }

    public boolean isNoBulletsInAir() {
        return getBulletsOnAirCount() == 0;
    }

    public boolean hasBulletsOnAir() {
        return getBulletsOnAirCount() > 0;
    }

    public LXXBullet createSafeBullet(Target target) {
        final Wave wave = new Wave(target.getState(), robot.getState(), SAFE_BULLET_SPEED, robot.getTime() + 1);
        final Bullet bullet = new Bullet(target.angleTo(robot), target.getX(), target.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceStateAtFireTime().getRobot().getName(), wave.getTargetStateAtLaunchTime().getRobot().getName(), true, -1);

        final HashMap<Double, Double> matches = new HashMap<Double, Double>();
        for (double bearingOffset = -LXXConstants.RADIANS_45; bearingOffset <= LXXConstants.RADIANS_45 + 0.01; bearingOffset += LXXConstants.RADIANS_1) {
            matches.put(bearingOffset, 0.01D);
        }

        return new LXXBullet(bullet, wave, new GFAimingPredictionData(matches));
    }

    public void paint(LXXGraphics g) {
        if (paintEnabled) {
            for (LXXBullet bullet : getBulletsOnAir(2)) {
                final AimingPredictionData aimPredictionData = bullet.getAimPredictionData();
                if (aimPredictionData != null) {
                    aimPredictionData.paint(g, bullet);
                }
            }
        }
    }

    public void addListener(BulletManagerListener listener) {
        listeners.add(listener);
    }

}
