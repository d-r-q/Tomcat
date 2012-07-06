/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.LXXRobotSnapshot;
import lxx.RobotListener;
import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.LXXBulletState;
import lxx.bullets.my.BulletManager;
import lxx.events.FireEvent;
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
import lxx.utils.time_profiling.TimeProfile;
import lxx.utils.wave.Wave;
import lxx.utils.wave.WaveCallback;
import lxx.utils.wave.WaveManager;
import robocode.*;
import robocode.Event;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 09.01.2010
 */
public class EnemyBulletManager implements WaveCallback, TargetManagerListener, RobotListener {

    private static final EnemyBulletPredictionData EMPTY_PREDICTION_DATA = new EnemyBulletPredictionData(new ArrayList<BearingOffsetDanger>(), 0L, null, null, null);

    private static boolean paintEnabled = false;
    private static int ghostBulletsCount = 0;

    private final Map<Wave, LXXBullet> predictedBullets = new HashMap<Wave, LXXBullet>();
    private final List<BulletManagerListener> listeners = new LinkedList<BulletManagerListener>();
    private final AdvancedEnemyGunModel enemyGunModel;

    private final WaveManager waveManager;
    private final Tomcat robot;
    private final TurnSnapshotsLog turnSnapshotsLog;
    private final BulletManager bulletManager;

    private double nextFireTime;

    public EnemyBulletManager(Office office, Tomcat robot) {
        enemyGunModel = new AdvancedEnemyGunModel(office);
        this.waveManager = office.getWaveManager();
        this.robot = robot;
        this.bulletManager = office.getBulletManager();
        turnSnapshotsLog = office.getTurnSnapshotsLog();
    }

    public void targetUpdated(Target target) {
        if (target.isFireLastTick() || (!target.isAlive() && target.getGunHeat() == 0)) {
            final double bulletPower = max(0.1, max(0, target.getExpectedEnergy()) - target.getEnergy());
            final double bulletSpeed = Rules.getBulletSpeed(bulletPower);

            final LXXRobotSnapshot targetPrevState = target.getPrevSnapshot();
            final LXXRobotSnapshot robotPrevState = robot.getPrevSnapshot();

            final double angleToMe = targetPrevState.angleTo(robotPrevState);

            final Bullet fakeBullet = new Bullet(angleToMe, targetPrevState.getX(), targetPrevState.getY(),
                    bulletPower, target.getName(), robot.getName(), true, -1);

            final Wave wave = waveManager.launchWave(targetPrevState, robotPrevState, robot, bulletSpeed, this);

            final LXXBullet lxxBullet = new LXXBullet(fakeBullet, wave);
            final Map<LXXBullet, BulletShadow> bulletShadows = getBulletShadows(lxxBullet, bulletManager.getBullets());
            addBulletShadows(lxxBullet, bulletShadows);
            lxxBullet.setAimPredictionData(enemyGunModel.getPredictionData(target, turnSnapshotsLog.getLastSnapshot(target, AdvancedEnemyGunModel.FIRE_DETECTION_LATENCY),
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
        TimeProfile.EBM_WAVE_TIME.start();
        final LXXBullet lxxBullet = getLXXBullet(w);
        if (lxxBullet != null && lxxBullet.getState() == LXXBulletState.ON_AIR) {
            lxxBullet.setState(LXXBulletState.MISSED);
            for (BulletManagerListener listener : listeners) {
                listener.bulletMiss(lxxBullet);
            }
        }
        predictedBullets.remove(w);
        enemyGunModel.processMiss(lxxBullet);
        updateBulletsOnAir();
        enemyGunModel.processVisit(lxxBullet);
        TimeProfile.EBM_WAVE_TIME.stop();
    }

    private void updateBulletsOnAir() {
        for (LXXBullet bullet : predictedBullets.values()) {
            if (bullet.getState() == LXXBulletState.ON_AIR) {
                enemyGunModel.updateBulletPredictionData(bullet);
            }
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

        for (LXXBullet enemyBullet : getAllBulletsOnAir()) {
            final LXXBullet bullet = bulletManager.getLXXBullet(e.getBullet());
            if (enemyBullet.getBulletShadow(bullet) != null &&
                    !enemyBullet.getBulletShadow(bullet).isPassed) {
                enemyBullet.removeBulletShadow(bullet);
            }
        }

        enemyGunModel.processIntercept(lxxBullet);
        updateBulletsOnAir();

        // todo(zhidkov): fix me
        /*final LXXPoint calculatedPos = lxxBullet.getFirePosition().project(lxxBullet.getHeadingRadians(), lxxBullet.getTravelledDistance() - lxxBullet.getSpeed());
        if (abs(e.getHitBullet().getX() - calculatedPos.x) > 0.5 || abs(e.getHitBullet().getY() - calculatedPos.y) > 0.5) {
            assert abs(e.getHitBullet().getX() - calculatedPos.x) < 0.5;
            assert abs(e.getHitBullet().getY() - calculatedPos.y) < 0.5;
        }*/
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

        enemyGunModel.processHit(lxxBullet);
        updateBulletsOnAir();

        // todo(zhidkov): fix me
        /*final LXXPoint calculatedPos = lxxBullet.getFirePosition().project(lxxBullet.getHeadingRadians(), lxxBullet.getTravelledDistance());
        if (abs(e.getBullet().getX() - calculatedPos.x) > 0.5 || abs(e.getBullet().getY() - calculatedPos.y) > 0.5) {
            assert abs(e.getBullet().getX() - calculatedPos.x) < 0.5;
            assert abs(e.getBullet().getY() - calculatedPos.y) < 0.5;
        }*/
    }

    private Wave getWave(Bullet b) {
        for (Wave w : predictedBullets.keySet()) {
            if (abs(w.getSpeed() - Rules.getBulletSpeed(b.getPower())) < 0.1 &&
                    abs(w.getTraveledDistance() - (w.getSourceState().aDistance(new LXXPoint(b.getX(), b.getY())) + b.getVelocity())) < w.getSpeed() + 1) {
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
        final double bulletHeading = wave.getSourceState().angleTo(wave.getTargetState());
        final APoint bulletPos = wave.getSourceState().project(bulletHeading, wave.getTraveledDistance());
        return new Bullet(bulletHeading, bulletPos.getX(), bulletPos.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceState().getName(), wave.getTargetState().getName(), true, -1);
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
            final double flightTime = (lxxBullet.getFirePosition().aDistance(lxxBullet.getTarget()) - lxxBullet.getTravelledDistance()) / lxxBullet.getSpeed();
            if (flightTime > flightTimeLimit && lxxBullet.getState() == LXXBulletState.ON_AIR) {
                bullets.add(lxxBullet);
            }
        }

        Collections.sort(bullets, new Comparator<LXXBullet>() {

            public int compare(LXXBullet o1, LXXBullet o2) {
                return (int) signum(o1.getFlightTime(robot) - o2.getFlightTime(robot));
            }
        });

        return bullets;
    }

    public List<LXXBullet> getAllBulletsOnAir() {
        final List<LXXBullet> bullets = new ArrayList<LXXBullet>();

        for (LXXBullet lxxBullet : predictedBullets.values()) {
            if (lxxBullet.getState() == LXXBulletState.ON_AIR) {
                bullets.add(lxxBullet);
            }
        }

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
        } else if (event instanceof FireEvent) {
            bulletFired(((FireEvent) event).getBullet());
        }
    }

    private void checkBulletShadows() {
        final List<LXXBullet> myBullets = bulletManager.getBullets();
        final List<LXXBullet> enemyBullets = getAllBulletsOnAir();
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
        final double ticksUntilFire = round(target.getGunHeat() / robot.getGunCoolingRate());
        final long fireTime = (long) (robot.getTime() + ticksUntilFire);
        final Wave wave = new Wave(target.getCurrentSnapshot(), robot.getCurrentSnapshot(), robot, Rules.getBulletSpeed(target.getFirePower()), fireTime);
        final Bullet bullet = new Bullet(target.angleTo(robot), target.getX(), target.getY(), LXXUtils.getBulletPower(wave.getSpeed()),
                wave.getSourceState().getName(), wave.getTargetState().getName(), true, -1);

        final LXXBullet lxxBullet = new LXXBullet(bullet, wave);
        final AimingPredictionData futureBulletAimingPredictionData;
        if (ticksUntilFire <= LXXUtils.getStopTime(robot.getSpeed()) && robot.getTime() <= nextFireTime) {
            final Map<LXXBullet, BulletShadow> bulletShadows = getBulletShadows(lxxBullet, bulletManager.getBullets());
            addBulletShadows(lxxBullet, bulletShadows);
            final int timeDelta = ticksUntilFire > 0
                    ? 0
                    : 1;
            futureBulletAimingPredictionData = enemyGunModel.getPredictionData(target, turnSnapshotsLog.getLastSnapshot(target, timeDelta), bulletShadows.values());
        } else {
            futureBulletAimingPredictionData = EMPTY_PREDICTION_DATA;
        }
        lxxBullet.setAimPredictionData(futureBulletAimingPredictionData);
        return lxxBullet;
    }

    private void addBulletShadows(LXXBullet lxxBullet, Map<LXXBullet, BulletShadow> bulletShadows) {
        for (Map.Entry<LXXBullet, BulletShadow> e : bulletShadows.entrySet()) {
            lxxBullet.addBulletShadow(e.getKey(), e.getValue());
        }
    }

    public void paint(LXXGraphics g) {
        if (paintEnabled) {
            for (LXXBullet bullet : getAllBulletsOnAir()) {

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

    private Map<LXXBullet, BulletShadow> getBulletShadows(LXXBullet enemyBullet, final List<LXXBullet> myBullets) {
        int timeDelta = 0;
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
                if (!robot.getBattleField().contains(myBulletCurPos)) {
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
                    final double bo1 = enemyBullet.getBearingOffsetRadians(pnt1);
                    final double bo2 = enemyBullet.getBearingOffsetRadians(pnt2);
                    bulletShadows.put(myBullet, new BulletShadow(min(bo1, bo2), max(bo1, bo2)));
                }
            }
            myBullets.removeAll(toRemove);
            toRemove.clear();
            timeDelta++;
        } while (myBullets.size() > 0);

        return bulletShadows;
    }

    private void bulletFired(LXXBullet bullet) {
        for (LXXBullet enemyBullet : getAllBulletsOnAir()) {
            final Map<LXXBullet, BulletShadow> bulletShadows = getBulletShadows(enemyBullet, LXXUtils.asModifiableList(bullet));
            final BulletShadow shadow = bulletShadows.get(bullet);
            if (shadow == null) {
                continue;
            }
            enemyBullet.addBulletShadow(bullet, shadow);
        }
        updateBulletsOnAir();
    }

}
