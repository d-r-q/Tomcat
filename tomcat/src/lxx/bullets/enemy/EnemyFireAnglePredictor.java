/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
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
import robocode.Rules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;
import static java.lang.Math.signum;

public class EnemyFireAnglePredictor implements BulletManagerListener {

    private static final int FIRE_DETECTION_LATENCY = 2;

    private static final Map<String, PSTree<Double>> logs = new HashMap<String, PSTree<Double>>();

    private final Map<LXXBullet, PSTreeEntry<Double>> entriesByBullets = new HashMap<LXXBullet, PSTreeEntry<Double>>();

    private final TurnSnapshotsLog turnSnapshotsLog;
    private final Tomcat robot;
    private final TomcatEyes tomcatEyes;

    public EnemyFireAnglePredictor(TurnSnapshotsLog turnSnapshotsLog, Tomcat robot, TomcatEyes tomcatEyes) {
        this.turnSnapshotsLog = turnSnapshotsLog;
        this.robot = robot;
        this.tomcatEyes = tomcatEyes;
    }

    public AimingPredictionData getPredictionData(Target t) {
        final PSTree<Double> log = getLog(t.getName());

        final TurnSnapshot predicate = turnSnapshotsLog.getLastSnapshot(t, FIRE_DETECTION_LATENCY);
        final List<Double> bearingOffsets = getBearingOffsets(log, predicate, t.getFirePower(), t);

        return new EnemyBulletsPredictionData(bearingOffsets);

    }

    private List<Double> getBearingOffsets(PSTree<Double> log, TurnSnapshot predicate, double firePower, Target t) {
        List<PSTreeEntry<Double>> entries = null;
        for (int delta = 0; delta <= 2; delta++) {
            entries = log.getSimilarEntries(getLimits(predicate, delta));
            if (entries.size() > 0) {
                break;
            }
        }
        final double lateralVelocity = LXXUtils.lateralVelocity(LXXUtils.getEnemyPos(predicate), LXXUtils.getMyPos(predicate),
                predicate.getMySpeed(), predicate.getMyAbsoluteHeadingRadians());
        final double lateralDirection = signum(lateralVelocity);
        final double bulletSpeed = Rules.getBulletSpeed(firePower);
        final double maxEscapeAngleAcc = LXXUtils.getMaxEscapeAngle(t, robot.getState(), bulletSpeed);
        final double maxEscapeAngleQuick = LXXUtils.getMaxEscapeAngle(bulletSpeed);
        final List<Double> bearingOffsets = new LinkedList<Double>();
        if (entries.size() > 0) {
            for (PSTreeEntry<Double> entry : entries) {
                bearingOffsets.add(entry.result * lateralDirection * maxEscapeAngleQuick);
            }
        } else {
            final GunType enemyGunType = tomcatEyes.getEnemyGunType(t);
            if (enemyGunType != GunType.HEAD_ON) {
                bearingOffsets.add(maxEscapeAngleAcc * lateralDirection);
            }
            if (enemyGunType == GunType.UNKNOWN || enemyGunType == GunType.HEAD_ON) {
                bearingOffsets.add(0D);
            }
        }

        return bearingOffsets;
    }

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts, int delta) {
        return LXXUtils.toMap(AttributesManager.myLateralSpeed,
                new Interval((int) round(LXXUtils.limit(AttributesManager.myLateralSpeed, ts.getAttrValue(AttributesManager.myLateralSpeed) - delta)),
                        (int) round(LXXUtils.limit(AttributesManager.myLateralSpeed, ts.getAttrValue(AttributesManager.myLateralSpeed) + delta))));
    }

    private static PSTree<Double> getLog(String enemyName) {
        PSTree<Double> log = logs.get(enemyName);
        if (log == null) {
            log = createLog();
            logs.put(enemyName, log);
        }
        return log;
    }

    private static PSTree<Double> createLog() {
        final Attribute[] splitAttributes = {
                AttributesManager.myLateralSpeed,
        };
        return new PSTree<Double>(splitAttributes, 2, 0.001);
    }

    public void bulletFired(LXXBullet bullet) {
        final PSTreeEntry<Double> entry = new PSTreeEntry<Double>(turnSnapshotsLog.getLastSnapshot((Target) bullet.getOwner(), FIRE_DETECTION_LATENCY));
        entriesByBullets.put(bullet, entry);
    }

    public void bulletHit(LXXBullet bullet) {
        setBulletGF(bullet);
        entriesByBullets.remove(bullet);
    }

    public void bulletIntercepted(LXXBullet bullet) {
        setBulletGF(bullet);
        entriesByBullets.remove(bullet);
    }

    public void setBulletGF(LXXBullet bullet) {
        final double guessFactor = bullet.getRealBearingOffsetRadians() * bullet.getTargetLateralDirection() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed());
        final PSTree<Double> log = getLog(bullet.getOwner().getName());

        final PSTreeEntry<Double> entry = entriesByBullets.get(bullet);
        entry.result = guessFactor;
        log.addEntry(entry);
    }


    public void bulletMiss(LXXBullet bullet) {
        entriesByBullets.remove(bullet);
    }

    public void bulletPassing(LXXBullet bullet) {
    }
}