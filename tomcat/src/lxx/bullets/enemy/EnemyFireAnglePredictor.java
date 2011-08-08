/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.AimingPredictionData;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;
import lxx.utils.ps_tree.PSTree;
import lxx.utils.ps_tree.PSTreeEntry;
import robocode.Bullet;
import robocode.Rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;
import static java.lang.Math.signum;
import static java.lang.StrictMath.min;

public class EnemyFireAnglePredictor implements BulletManagerListener {

    private static final int FIRE_DETECTION_LATENCY = 2;

    private static final Map<String, PSTree<UndirectedGuessFactor>> hitLogs = new HashMap<String, PSTree<UndirectedGuessFactor>>();
    private static final Map<String, PSTree<UndirectedGuessFactor>> visitLogs = new HashMap<String, PSTree<UndirectedGuessFactor>>();

    private final Map<LXXBullet, PSTreeEntry<UndirectedGuessFactor>> entriesByBullets = new HashMap<LXXBullet, PSTreeEntry<UndirectedGuessFactor>>();

    private final TurnSnapshotsLog turnSnapshotsLog;
    private final Tomcat robot;
    private final TomcatEyes tomcatEyes;

    public EnemyFireAnglePredictor(TurnSnapshotsLog turnSnapshotsLog, Tomcat robot, TomcatEyes tomcatEyes) {
        this.turnSnapshotsLog = turnSnapshotsLog;
        this.robot = robot;
        this.tomcatEyes = tomcatEyes;
    }

    public AimingPredictionData getPredictionData(Target t) {
        final PSTree<UndirectedGuessFactor> hitLog = getHitLog(t.getName());
        final PSTree<UndirectedGuessFactor> visitLog = getVisitLog(t.getName());

        final TurnSnapshot predicate = turnSnapshotsLog.getLastSnapshot(t, FIRE_DETECTION_LATENCY);
        final List<PastBearingOffset> bearingOffsets = getBearingOffsets(hitLog, visitLog, predicate, t.getFirePower(), t);
        final GunType enemyGunType = tomcatEyes.getEnemyGunType(t);

        return new EnemyBulletPredictionData(bearingOffsets, enemyGunType == GunType.ADVANCED ? (int) predicate.getAttrValue(AttributesManager.enemyOutgoingWavesCollected) : -1);
    }

    private List<PastBearingOffset> getBearingOffsets(PSTree<UndirectedGuessFactor> hitLog, PSTree<UndirectedGuessFactor> visitLog, TurnSnapshot predicate, double firePower, Target t) {
        List<PSTreeEntry<UndirectedGuessFactor>> entries = null;
        for (int delta = 0; delta <= 2; delta++) {
            entries = hitLog.getSimilarEntries(getLimits(predicate, delta));
            if (entries.size() > 3) {
                break;
            }
        }
        final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                predicate.getMySpeed(), predicate.getMyAbsoluteHeadingRadians());
        final double lateralDirection = signum(lateralVelocity);
        final double bulletSpeed = Rules.getBulletSpeed(firePower);
        final double maxEscapeAngleAcc = LXXUtils.getMaxEscapeAngle(t, robot.getState(), bulletSpeed);
        final double maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);
        final GunType enemyGunType = tomcatEyes.getEnemyGunType(t);

        final List<PastBearingOffset> bearingOffsets = new LinkedList<PastBearingOffset>();
        if (entries != null && entries.size() > 0) {
            for (PSTreeEntry<UndirectedGuessFactor> entry : entries.subList(0, min(3, entries.size()))) {
                if (entry.result.lateralDirection != 0 && lateralDirection != 0) {
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * lateralDirection * entry.result.lateralDirection * maxEscapeAngleQuick, 1));
                } else {
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * 1 * maxEscapeAngleQuick, 1));
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * -1 * maxEscapeAngleQuick, 1));
                }
            }
        }
        if (bearingOffsets.size() == 0) {
            if (enemyGunType != GunType.HEAD_ON) {
                if (lateralDirection != 0) {
                    bearingOffsets.add(new PastBearingOffset(predicate, maxEscapeAngleAcc * lateralDirection, 1));
                } else {
                    bearingOffsets.add(new PastBearingOffset(predicate, maxEscapeAngleAcc * 1, 1));
                    bearingOffsets.add(new PastBearingOffset(predicate, maxEscapeAngleAcc * -1, 1));
                }
            }
            if (enemyGunType == GunType.UNKNOWN || enemyGunType == GunType.HEAD_ON) {
                bearingOffsets.add(new PastBearingOffset(predicate, 0D, 1));
            }
        }

        if (enemyGunType == GunType.ADVANCED) {
            entries = visitLog.getSimilarEntries(getLimits(predicate, 1));
            for (PSTreeEntry<UndirectedGuessFactor> entry : entries) {
                if (bearingOffsets.size() > 6) {
                    break;
                }
                if (entry.result.lateralDirection != 0 && lateralDirection != 0) {
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * lateralDirection * entry.result.lateralDirection * maxEscapeAngleQuick, 1));
                } else {
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * 1 * maxEscapeAngleQuick, 1));
                    bearingOffsets.add(new PastBearingOffset(entry.predicate, entry.result.guessFactor * -1 * maxEscapeAngleQuick, 1));
                }
            }
        }

        return bearingOffsets;
    }

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts, int delta) {
        return LXXUtils.toMap(AttributesManager.myLateralSpeed,
                new Interval((int) round(LXXUtils.limit(AttributesManager.myLateralSpeed, ts.getAttrValue(AttributesManager.myLateralSpeed) - delta)),
                        (int) round(LXXUtils.limit(AttributesManager.myLateralSpeed, ts.getAttrValue(AttributesManager.myLateralSpeed) + delta))));
    }

    private static PSTree<UndirectedGuessFactor> getHitLog(String enemyName) {
        PSTree<UndirectedGuessFactor> log = hitLogs.get(enemyName);
        if (log == null) {
            log = createLog();
            hitLogs.put(enemyName, log);
        }
        return log;
    }

    private static PSTree<UndirectedGuessFactor> getVisitLog(String enemyName) {
        PSTree<UndirectedGuessFactor> log = visitLogs.get(enemyName);
        if (log == null) {
            log = createLog();
            visitLogs.put(enemyName, log);
        }
        return log;
    }

    private static PSTree<UndirectedGuessFactor> createLog() {
        final Attribute[] splitAttributes = {
                AttributesManager.myLateralSpeed,
        };
        return new PSTree<UndirectedGuessFactor>(splitAttributes, 2, 0.001);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<UndirectedGuessFactor> entry = new PSTreeEntry<UndirectedGuessFactor>(turnSnapshotsLog.getLastSnapshot((Target) bullet.getOwner(), FIRE_DETECTION_LATENCY));
        entriesByBullets.put(bullet, entry);
    }

    public void bulletHit(LXXBullet bullet) {
        setBulletGF(bullet, getHitLog(bullet.getOwner().getName()));
        entriesByBullets.remove(bullet);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        setBulletGF(bullet, getHitLog(bullet.getOwner().getName()));
        entriesByBullets.remove(bullet);
    }

    public void setBulletGF(LXXBullet bullet, final PSTree<UndirectedGuessFactor> log) {
        final PSTreeEntry<UndirectedGuessFactor> entry = entriesByBullets.get(bullet);
        final double lateralDirection = bullet.getTargetLateralDirection();
        entry.result = new UndirectedGuessFactor(bullet.getRealBearingOffsetRadians() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed()), lateralDirection);
        log.addEntry(entry);
    }


    public void bulletMiss(LXXBullet bullet) {
        final Bullet b = bullet.getBullet();
        bullet.setBullet(new Bullet(bullet.getFirePosition().angleTo(bullet.getTarget()), b.getX(), b.getY(), b.getPower(), b.getName(), b.getVictim(), b.isActive(), -1));
        setBulletGF(bullet, getVisitLog(bullet.getOwner().getName()));
        entriesByBullets.remove(bullet);
    }

    public void bulletPassing(LXXBullet bullet) {
    }

    private class UndirectedGuessFactor implements Serializable {

        public final double guessFactor;
        public final double lateralDirection;

        private UndirectedGuessFactor(double guessFactor, double lateralDirection) {
            this.guessFactor = guessFactor;
            this.lateralDirection = lateralDirection;
        }
    }

}