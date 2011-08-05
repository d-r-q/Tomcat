/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.LXXRobotState;
import lxx.RobotListener;
import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.events.LXXKeyEvent;
import lxx.events.LXXPaintEvent;
import lxx.office.Office;
import lxx.office.PropertiesManager;
import lxx.paint.LXXGraphics;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.utils.APoint;
import lxx.utils.AimingPredictionData;
import lxx.utils.LXXPoint;
import lxx.utils.LXXUtils;
import lxx.utils.wave.Wave;
import lxx.utils.wave.WaveCallback;
import lxx.utils.wave.WaveManager;
import robocode.*;

import java.util.*;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 09.01.2010
 */
public class EnemyBulletManager implements WaveCallback, TargetManagerListener, RobotListener {

    private static final EnemyBulletsPredictionData EMPTY_PREDICTION_DATA = new EnemyBulletsPredictionData(new ArrayList<Double>());
    private static boolean paintEnabled = false;
    private static int ghostBulletsCount = 0;

    private final Map<Wave, LXXBullet> predictedBullets = new HashMap<Wave, LXXBullet>();
    private final List<BulletManagerListener> listeners = new LinkedList<BulletManagerListener>();
    private final EnemyFireAnglePredictor enemyFireAnglePredictor;

    private final WaveManager waveManager;
    private final Tomcat robot;

    private AimingPredictionData futureBulletAimingPredictionData;

    public EnemyBulletManager(Office office, Tomcat robot) {
        enemyFireAnglePredictor = new EnemyFireAnglePredictor(office.getTurnSnapshotsLog(), robot, office.getTomcatEyes());
        addListener(enemyFireAnglePredictor);
        this.waveManager = office.getWaveManager();
        this.robot = robot;
    }

    public void targetUpdated(Target target) {
        if (target.isFireLastTick() || (!target.isAlive() && target.getGunHeat() == 0)) {
            final double bulletPower = max(0.1, max(0, target.getExpectedEnergy()) - target.getEnergy());
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
            ghostBulletsCount++;
            PropertiesManager.setDebugProperty("Ghost bullets count", String.valueOf(ghostBulletsCount));
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
            ghostBulletsCount++;
            PropertiesManager.setDebugProperty("Ghost bullets count", String.valueOf(ghostBulletsCount));
            return;
        }

        final LXXBullet lxxBullet = getLXXBullet(w, e.getBullet());
        lxxBullet.setState(LXXBulletState.HITTED);
        for (BulletManagerListener listener : listeners) {
            listener.bulletHit(lxxBullet);
        }
    }

    private Wave getWave(Bullet b) {
        for (Wave w : predictedBullets.keySet()) {
            if (abs(w.getSpeed() - Rules.getBulletSpeed(b.getPower())) < 0.1 &&
                    abs(w.getTraveledDistance() - (w.getSourcePosAtFireTime().aDistance(new LXXPoint(b.getX(), b.getY())) + b.getVelocity())) < w.getSpeed() + 1) {
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
        } else if (event instanceof LXXKeyEvent) {
            if (Character.toUpperCase(((LXXKeyEvent) event).getKeyChar()) == 'M') {
                paintEnabled = !paintEnabled;
            }
        }
    }

    public LXXBullet createFutureBullet(Target target) {
        double timeToFire = round(target.getGunHeat() / robot.getGunCoolingRate());
        if (timeToFire == 1 || timeToFire == 2) {
            futureBulletAimingPredictionData = enemyFireAnglePredictor.getPredictionData(target);
        } else if (timeToFire > 2) {
            futureBulletAimingPredictionData = EMPTY_PREDICTION_DATA;
        }
        final Wave wave = new Wave(target.getState(), robot.getState(), Rules.getBulletSpeed(target.getFirePower()), (long) (robot.getTime() + timeToFire));
        final Bullet bullet = new Bullet(target.angleTo(robot), target.getX(), target.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceStateAtFireTime().getRobot().getName(), wave.getTargetStateAtLaunchTime().getRobot().getName(), true, -1);

        return new LXXBullet(bullet, wave, futureBulletAimingPredictionData);
    }

    public void paint(LXXGraphics g) {
        if (paintEnabled) {
            for (LXXBullet bullet : getBulletsOnAir(0)) {
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
