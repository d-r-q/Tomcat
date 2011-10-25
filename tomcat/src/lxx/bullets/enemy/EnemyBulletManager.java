/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.LXXRobot;
import lxx.LXXRobotState;
import lxx.RobotListener;
import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.bullets.PastBearingOffset;
import lxx.bullets.my.BulletManager;
import lxx.events.LXXKeyEvent;
import lxx.events.LXXPaintEvent;
import lxx.events.TickEvent;
import lxx.office.Office;
import lxx.office.PropertiesManager;
import lxx.paint.LXXGraphics;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.utils.*;
import lxx.utils.wave.Wave;
import lxx.utils.wave.WaveCallback;
import lxx.utils.wave.WaveManager;
import robocode.*;
import robocode.Event;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 09.01.2010
 */
public class EnemyBulletManager implements WaveCallback, TargetManagerListener, RobotListener, BulletManagerListener {

    private static final EnemyBulletPredictionData EMPTY_PREDICTION_DATA = new EnemyBulletPredictionData(new ArrayList<PastBearingOffset>(), 0L);

    private static boolean paintEnabled = false;
    private static int ghostBulletsCount = 0;

    private final Map<Wave, LXXBullet> predictedBullets = new HashMap<Wave, LXXBullet>();
    private final List<BulletManagerListener> listeners = new LinkedList<BulletManagerListener>();
    private final AdvancedEnemyGunModel enemyFireAnglePredictor;

    private final WaveManager waveManager;
    private final Tomcat robot;
    private final TurnSnapshotsLog turnSnapshotsLog;
    private final BulletManager bulletManager;

    private AimingPredictionData futureBulletAimingPredictionData;
    private double nextFireTime;

    public EnemyBulletManager(Office office, Tomcat robot) {
        enemyFireAnglePredictor = new AdvancedEnemyGunModel(office.getTurnSnapshotsLog(), office);
        addListener(enemyFireAnglePredictor);
        this.waveManager = office.getWaveManager();
        this.robot = robot;
        this.bulletManager = office.getBulletManager();
        bulletManager.addListener(this);
        turnSnapshotsLog = office.getTurnSnapshotsLog();
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

            final LXXBullet lxxBullet = new LXXBullet(fakeBullet, wave);
            final Map<LXXBullet, BulletShadow> bulletShadows = getBulletShadows(lxxBullet);
            addBulletShadows(lxxBullet, bulletShadows);
            lxxBullet.setAimPredictionData(enemyFireAnglePredictor.getPredictionData(target, turnSnapshotsLog.getLastSnapshot(target, AdvancedEnemyGunModel.FIRE_DETECTION_LATENCY),
                    bulletShadows.values()));

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
        if (lxxBullet != null && lxxBullet.getState() == LXXBulletState.ON_AIR) {
            lxxBullet.setState(LXXBulletState.MISSED);
            for (BulletManagerListener listener : listeners) {
                listener.bulletMiss(lxxBullet);
            }
        }

        predictedBullets.remove(w);
        updateBulletsOnAir();
    }

    private void updateBulletsOnAir() {
        for (LXXBullet bullet : predictedBullets.values()) {
            final LXXRobot owner = bullet.getOwner();
            bullet.setAimPredictionData(enemyFireAnglePredictor.getPredictionData(owner,
                    ((AEGMPredictionData) bullet.getAimPredictionData()).getTs(),
                    bullet.getBulletShadows()));
        }
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

        predictedBullets.remove(w);
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

        predictedBullets.remove(w);
        updateBulletsOnAir();
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
        } else if (event instanceof TickEvent) {
            checkBulletShadows();
        }
    }

    private void checkBulletShadows() {
        final List<LXXBullet> myBullets = bulletManager.getBullets();
        final List<LXXBullet> enemyBullets = getBulletsOnAir(0);
        for (LXXBullet enemyBullet : enemyBullets) {
            final double enemyBulletCurDist = enemyBullet.getTravelledDistance();
            final APoint ebFirePos = enemyBullet.getFirePosition();
            final double enemyBulletNextDist = enemyBulletCurDist + enemyBullet.getSpeed();
            for (LXXBullet myBullet : myBullets) {
                final double myBulletCurDist = myBullet.getTravelledDistance() + myBullet.getSpeed();
                final APoint mbFirePos = myBullet.getFirePosition();

                final APoint myBulletCurPos = mbFirePos.project(myBullet.getHeadingRadians(), myBulletCurDist);
                final APoint myBulletNextPos = mbFirePos.project(myBullet.getHeadingRadians(), myBulletCurDist + myBullet.getSpeed());
                robot.getLXXGraphics().fillCircle(myBulletCurPos, 3);

                final BulletShadow bulletShadow = enemyBullet.getBulletShadow(myBullet);
                if (bulletShadow != null && ebFirePos.aDistance(mbFirePos) > myBulletCurDist) {
                    if (ebFirePos.aDistance(myBulletCurPos) > enemyBulletCurDist &&
                            ebFirePos.aDistance(myBulletNextPos) < enemyBulletNextDist) {
                        bulletShadow.isPassed = true;
                    } else if (ebFirePos.aDistance(myBulletNextPos) > enemyBulletCurDist &&
                            ebFirePos.aDistance(myBulletCurPos) < enemyBulletNextDist) {
                        bulletShadow.isPassed = true;
                    } else if (ebFirePos.aDistance(myBulletCurPos) > enemyBulletNextDist &&
                            ebFirePos.aDistance(myBulletNextPos) < enemyBulletNextDist) {
                        bulletShadow.isPassed = true;
                    } else if (ebFirePos.aDistance(myBulletCurPos) > enemyBulletCurDist &&
                            ebFirePos.aDistance(myBulletNextPos) < enemyBulletCurDist) {
                        bulletShadow.isPassed = true;
                    }
                }
            }
        }
    }

    public LXXBullet createFutureBullet(Target target) {
        if (target.getGunHeat() > 0) {
            nextFireTime = robot.getTime() + ceil(target.getGunHeat() / robot.getGunCoolingRate());
        }
        final double timeToFire = round(target.getGunHeat() / robot.getGunCoolingRate());
        final Wave wave = new Wave(target.getState(), robot.getState(), Rules.getBulletSpeed(target.getFirePower()), (long) (robot.getTime() + timeToFire));
        final Bullet bullet = new Bullet(target.angleTo(robot), target.getX(), target.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceStateAtFireTime().getRobot().getName(), wave.getTargetStateAtLaunchTime().getRobot().getName(), true, -1);

        final LXXBullet lxxBullet = new LXXBullet(bullet, wave);
        final Map<LXXBullet, BulletShadow> bulletShadows = getBulletShadows(lxxBullet);
        addBulletShadows(lxxBullet, bulletShadows);
        if (timeToFire <= LXXUtils.getStopTime(robot.getSpeed()) && robot.getTime() < nextFireTime) {
            futureBulletAimingPredictionData = enemyFireAnglePredictor.getPredictionData(target, turnSnapshotsLog.getLastSnapshot(target, AdvancedEnemyGunModel.FIRE_DETECTION_LATENCY), bulletShadows.values());
        } else if (timeToFire > 2) {
            futureBulletAimingPredictionData = EMPTY_PREDICTION_DATA;
        }
        lxxBullet.setAimPredictionData(futureBulletAimingPredictionData);
        return lxxBullet;
    }

    public void paint(LXXGraphics g) {
        if (paintEnabled) {
            for (LXXBullet bullet : getBulletsOnAir(0)) {

                g.setColor(new Color(0, 255, 0, 150));
                for (IntervalDouble bulletShadow : bullet.getBulletShadows()) {
                    final double alpha1 = bullet.noBearingOffset() + bulletShadow.a;
                    final double alpha2 = bullet.noBearingOffset() + bulletShadow.b;
                    final double curDist = bullet.getTravelledDistance();
                    final double step = max(bulletShadow.getLength() / 20, 0.001);
                    for (double alpha = alpha1; alpha <= alpha2; alpha += step) {
                        g.drawLine(bullet.getFirePosition(), alpha, curDist - 7, 40);
                    }
                }

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

    private Map<LXXBullet, BulletShadow> getBulletShadows(LXXBullet enemyBullet) {
        int timeDelta = 0;
        final List<LXXBullet> myBullets = bulletManager.getBullets();
        final Map<LXXBullet, BulletShadow> bulletShadows = new HashMap<LXXBullet, BulletShadow>();
        final APoint ebFirePos = enemyBullet.getFirePosition();
        do {

            final double enemyBulletCurDist = enemyBullet.getTravelledDistance() + enemyBullet.getSpeed() * timeDelta;
            final double enemyBulletNextDist = enemyBulletCurDist + enemyBullet.getSpeed();

            final List<LXXBullet> toRemove = new ArrayList<LXXBullet>();

            for (LXXBullet myBullet : myBullets) {
                final double myBulletCurDist = myBullet.getTravelledDistance() + myBullet.getSpeed() * timeDelta;
                final APoint mbFirePos = myBullet.getFirePosition();

                final APoint myBulletCurPos = mbFirePos.project(myBullet.getHeadingRadians(), myBulletCurDist);
                if (!robot.getState().getBattleField().contains(myBulletCurPos)) {
                    toRemove.add(myBullet);
                    continue;
                }
                final APoint myBulletNextPos = mbFirePos.project(myBullet.getHeadingRadians(), myBulletCurDist + myBullet.getSpeed());

                APoint pnt1 = null;
                APoint pnt2 = null;
                if (ebFirePos.aDistance(myBulletCurPos) < enemyBulletNextDist &&
                        ebFirePos.aDistance(myBulletNextPos) > enemyBulletCurDist) {
                    pnt1 = myBulletCurPos;
                    pnt2 = myBulletNextPos;
                    toRemove.add(myBullet);
                } else if (ebFirePos.aDistance(myBulletNextPos) < enemyBulletCurDist &&
                        ebFirePos.aDistance(myBulletCurPos) > enemyBulletNextDist) {
                    pnt1 = LXXUtils.intersection(myBulletCurPos, myBulletNextPos, ebFirePos, enemyBulletCurDist)[0];
                    pnt2 = LXXUtils.intersection(myBulletCurPos, myBulletNextPos, ebFirePos, enemyBulletNextDist)[0];
                    toRemove.add(myBullet);
                } else if (ebFirePos.aDistance(myBulletCurPos) > enemyBulletNextDist &&
                        ebFirePos.aDistance(myBulletNextPos) < enemyBulletNextDist) {
                    pnt1 = myBulletNextPos;
                    pnt2 = LXXUtils.intersection(myBulletCurPos, myBulletNextPos, ebFirePos, enemyBulletNextDist)[0];
                    toRemove.add(myBullet);
                } else if (ebFirePos.aDistance(myBulletCurPos) > enemyBulletCurDist &&
                        ebFirePos.aDistance(myBulletNextPos) < enemyBulletCurDist) {
                    pnt1 = LXXUtils.intersection(myBulletCurPos, myBulletNextPos, ebFirePos, enemyBulletCurDist)[0];
                    pnt2 = myBulletCurPos;
                    toRemove.add(myBullet);
                }

                if (pnt1 != null) {
                    final double bo1 = LXXUtils.bearingOffset(ebFirePos, enemyBullet.getTargetStateAtFireTime(), pnt1);
                    final double bo2 = LXXUtils.bearingOffset(ebFirePos, enemyBullet.getTargetStateAtFireTime(), pnt2);
                    bulletShadows.put(myBullet, new BulletShadow(min(bo1, bo2), max(bo1, bo2)));
                }
            }
            myBullets.removeAll(toRemove);
            toRemove.clear();
            timeDelta++;
        } while (myBullets.size() > 0);

        return bulletShadows;
    }

    public void bulletFired(LXXBullet bullet) {
        recalculateBulletShadows();
        updateBulletsOnAir();
    }

    private void recalculateBulletShadows() {
        for (LXXBullet enemyBullet : getBulletsOnAir(0)) {
            final Map<LXXBullet, BulletShadow> bulletShadows = getBulletShadows(enemyBullet);
            getBulletShadows(enemyBullet);
            addBulletShadows(enemyBullet, bulletShadows);
        }
    }

    private void addBulletShadows(LXXBullet enemyBullet, Map<LXXBullet, BulletShadow> bulletShadows) {
        for (Map.Entry<LXXBullet, BulletShadow> bulletShadow : bulletShadows.entrySet()) {
            enemyBullet.addBulletShadow(bulletShadow.getKey(), bulletShadow.getValue());
        }
    }

    public void bulletIntercepted(LXXBullet bullet) {
        for (LXXBullet enemyBullet : getBulletsOnAir(0)) {
            if (enemyBullet.getBulletShadow(bullet) != null &&
                    !enemyBullet.getBulletShadow(bullet).isPassed) {
                enemyBullet.removeBulletShadow(bullet);
            }
        }
        updateBulletsOnAir();
    }

    public void bulletHit(LXXBullet bullet) {
    }

    public void bulletMiss(LXXBullet bullet) {
    }

    public void bulletPassing(LXXBullet bullet) {
    }

}
