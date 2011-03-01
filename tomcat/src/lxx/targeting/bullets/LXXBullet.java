/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.bullets;

import lxx.utils.APoint;
import lxx.utils.AimingPredictionData;
import lxx.utils.LXXRobot;
import lxx.utils.LXXUtils;
import lxx.wave.Wave;
import robocode.Bullet;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public class LXXBullet {

    private final Bullet bullet;
    private final AimingPredictionData aimPredictionData;
    private final Wave wave;

    public LXXBullet(Bullet bullet, Wave w, AimingPredictionData aimPredictionData) {
        this.bullet = bullet;
        this.aimPredictionData = aimPredictionData;
        this.wave = w;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public LXXRobot getTarget() {
        return wave.getTargetStateAtFireTime().getRobot();
    }

    public APoint getFirePosition() {
        return wave.getSourceStateAtFireTime();
    }

    public double getTravelledDistance() {
        return wave.getTraveledDistance();
    }

    public double getDistanceToTarget() {
        return wave.getSourceStateAtFireTime().aDistance(wave.getTargetStateAtFireTime().getRobot());
    }

    public APoint getTargetPos() {
        return wave.getTargetStateAtFireTime();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LXXBullet lxxBullet = (LXXBullet) o;

        if (wave.getLaunchTime() != lxxBullet.wave.getLaunchTime()) return false;
        if (!wave.getSourceStateAtFireTime().getRobot().getName().equals(lxxBullet.wave.getSourceStateAtFireTime().getRobot().getName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = wave.getSourceStateAtFireTime().hashCode();
        result = 31 * result + (int) (wave.getLaunchTime() ^ (wave.getLaunchTime() >>> 32));
        return result;
    }

    public int getTargetLateralDirection() {
        return (int) signum(LXXUtils.lateralVelocity2(wave.getSourceStateAtFireTime(), wave.getTargetStateAtFireTime(),
                wave.getTargetStateAtFireTime().getVelocityModule(), wave.getTargetStateAtFireTime().getAbsoluteHeadingRadians()));
    }

    public double angleToTargetPos() {
        return wave.getSourceStateAtFireTime().angleTo(wave.getTargetStateAtFireTime());
    }

    public Wave getWave() {
        return wave;
    }

    public long getFireTime() {
        return wave.getLaunchTime();
    }
}
