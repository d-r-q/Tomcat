/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets;

import lxx.LXXRobot;
import lxx.LXXRobotState;
import lxx.bullets.enemy.BulletShadow;
import lxx.utils.*;
import lxx.utils.wave.Wave;
import robocode.Bullet;
import robocode.util.Utils;

import java.util.*;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class LXXBullet {

    private final Wave wave;

    private Bullet bullet;
    private LXXBulletState state;
    private AimingPredictionData aimPredictionData;
    private final Map<LXXBullet, BulletShadow> bulletShadows = new HashMap<LXXBullet, BulletShadow>();
    private final List<IntervalDouble> mergedShadows = new ArrayList<IntervalDouble>();
    private LXXPoint firePosition;

    public LXXBullet(Bullet bullet, Wave w, AimingPredictionData aimPredictionData) {
        this.bullet = bullet;
        this.aimPredictionData = aimPredictionData;
        this.wave = w;

        firePosition = new LXXPoint(wave.getSourcePosAtFireTime());
        state = LXXBulletState.ON_AIR;
    }

    public LXXBullet(Bullet bullet, Wave w) {
        this(bullet, w, null);
    }

    public Bullet getBullet() {
        return bullet;
    }

    public LXXRobot getTarget() {
        return wave.getTargetStateAtLaunchTime().getRobot();
    }

    public LXXPoint getFirePosition() {
        return firePosition;
    }

    public double getTravelledDistance() {
        return wave.getTraveledDistance();
    }

    public AimingPredictionData getAimPredictionData() {
        return aimPredictionData;
    }

    public void setAimPredictionData(AimingPredictionData aimPredictionData) {
        this.aimPredictionData = aimPredictionData;
    }

    public LXXRobot getOwner() {
        return wave.getSourceStateAtFireTime().getRobot();
    }

    public double getHeadingRadians() {
        return bullet.getHeadingRadians();
    }

    public double getSpeed() {
        return bullet.getVelocity();
    }

    public LXXBulletState getState() {
        return state;
    }

    public LXXRobotState getTargetStateAtFireTime() {
        return wave.getTargetStateAtLaunchTime();
    }

    public double getDistanceToTarget() {
        return wave.getSourceStateAtFireTime().aDistance(wave.getTargetStateAtLaunchTime().getRobot());
    }

    public double noBearingOffset() {
        return wave.noBearingOffset;
    }

    public void setBullet(Bullet bullet) {
        this.bullet = bullet;
    }

    public void setState(LXXBulletState state) {
        this.state = state;
    }

    public double getRealBearingOffsetRadians() {
        return Utils.normalRelativeAngle(bullet.getHeadingRadians() - wave.noBearingOffset);
    }

    public double getBearingOffsetRadians(APoint pnt) {
        return Utils.normalRelativeAngle(getFirePosition().angleTo(pnt) - wave.noBearingOffset);
    }

    public double getTargetLateralDirection() {
        return LXXUtils.lateralDirection(getFirePosition(), getTargetStateAtFireTime());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LXXBullet lxxBullet = (LXXBullet) o;

        if (wave.getLaunchTime() != lxxBullet.wave.getLaunchTime()) return false;
        if (!wave.getSourceStateAtFireTime().getRobot().getName().equals(lxxBullet.wave.getSourceStateAtFireTime().getRobot().getName()))
            return false;

        return true;
    }

    public int hashCode() {
        int result = wave.getSourceStateAtFireTime().hashCode();
        result = 31 * result + (int) (wave.getLaunchTime() ^ (wave.getLaunchTime() >>> 32));
        return result;
    }

    public Wave getWave() {
        return wave;
    }

    public double getFlightTime(APoint pnt) {
        return (getFirePosition().aDistance(pnt) - getTravelledDistance()) / getSpeed();
    }

    public void addBulletShadow(LXXBullet bullet, BulletShadow shadow) {
        bulletShadows.put(bullet, shadow);
        for (IntervalDouble bs : mergedShadows) {
            if (bs.intersects(shadow)) {
                bs.merge(shadow);
                return;
            }
        }
        mergedShadows.add(new IntervalDouble(shadow));
    }

    public BulletShadow getBulletShadow(LXXBullet bullet) {
        return bulletShadows.get(bullet);
    }

    public Collection<BulletShadow> getBulletShadows() {
        return bulletShadows.values();
    }

    public void removeBulletShadow(LXXBullet bullet) {
        bulletShadows.remove(bullet);
        mergedShadows.clear();
        for (BulletShadow shadow : bulletShadows.values()) {
            boolean isMerged = false;
            for (IntervalDouble bs : mergedShadows) {
                if (bs.intersects(shadow)) {
                    bs.merge(shadow);
                    isMerged = true;
                    break;
                }
            }
            if (!isMerged) {
                mergedShadows.add(new IntervalDouble(shadow));
            }
        }
    }

    public List<IntervalDouble> getMergedShadows() {
        return mergedShadows;
    }
}
