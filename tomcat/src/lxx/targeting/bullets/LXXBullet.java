/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.bullets;

import lxx.utils.*;
import lxx.wave.Wave;
import robocode.Bullet;
import robocode.util.Utils;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class LXXBullet {

    private final AimingPredictionData aimPredictionData;
    private final Wave wave;

    private Bullet bullet;
    private LXXBulletState state;

    public LXXBullet(Bullet bullet, Wave w, AimingPredictionData aimPredictionData) {
        this.bullet = bullet;
        this.aimPredictionData = aimPredictionData;
        this.wave = w;

        state = LXXBulletState.ON_AIR;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public LXXRobot getTarget() {
        return wave.getTargetStateAtLaunchTime().getRobot();
    }

    public APoint getFirePosition() {
        return wave.getSourceStateAtFireTime();
    }

    public double getTravelledDistance() {
        return wave.getTraveledDistance();
    }

    public AimingPredictionData getAimPredictionData() {
        return aimPredictionData;
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
        return wave.getSourceStateAtFireTime().angleTo(wave.getTargetPosAtFireTime());
    }

    public void setBullet(Bullet bullet) {
        this.bullet = bullet;
    }

    public void setState(LXXBulletState state) {
        this.state = state;
    }

    public double getRealBearingOffsetRadians() {
        return Utils.normalRelativeAngle(bullet.getHeadingRadians() - noBearingOffset());
    }

    public double getBearingOffsetRadians(LXXPoint pnt) {
        return Utils.normalRelativeAngle(getFirePosition().angleTo(pnt) - noBearingOffset());
    }

    public double getTargetLateralDirection() {
        final double lateralVelocity = LXXUtils.lateralVelocity(getFirePosition(), getTargetStateAtFireTime());
        return signum(lateralVelocity);
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

    public LXXPoint getCurrentPosition() {
        return new LXXPoint(bullet.getX(), bullet.getY());
    }
}
