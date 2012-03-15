/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.wave;

import lxx.LXXRobotSnapshot2;
import lxx.LXXRobotState;
import lxx.bullets.LXXBullet;
import lxx.utils.APoint;
import lxx.utils.IntervalDouble;
import lxx.utils.LXXPoint;
import lxx.utils.LXXUtils;
import robocode.util.Utils;

import java.awt.*;

import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class Wave {

    private final LXXRobotState sourceState;
    private final LXXRobotState targetState;

    private final LXXRobotSnapshot2 sourceState2;
    private final LXXRobotSnapshot2 targetState2;

    private final long launchTime;
    private final double speed;

    public final double noBearingOffset;

    private boolean isPassed = false;
    private IntervalDouble hitBearingOffsetInterval;
    private LXXBullet carriedBullet;

    public Wave(LXXRobotState source, LXXRobotState target, double speed, long launchTime, LXXRobotSnapshot2 sourceState2, LXXRobotSnapshot2 targetState2) {
        this.sourceState = source;
        this.targetState = target;

        this.launchTime = launchTime;
        this.speed = speed;
        this.targetState2 = targetState2;
        this.sourceState2 = sourceState2;
        this.noBearingOffset = sourceState.angleTo(targetState);
    }

    public double getTraveledDistance() {
        return (sourceState.getRobot().getTime() - launchTime + 1) * speed;
    }

    public boolean check() {
        final double width = targetState.getRobot().getWidth();
        final double height = targetState.getRobot().getHeight();
        final Rectangle targetRect = new Rectangle((int) (targetState.getRobot().getX() - width / 2), (int) (targetState.getRobot().getY() - height / 2),
                (int) width, (int) height);
        final double angleToTarget = sourceState.angleTo(targetState.getRobot());
        final LXXPoint bulletPos = (LXXPoint) sourceState.project(angleToTarget, getTraveledDistance());
        final boolean contains = targetRect.contains(bulletPos);
        if (contains) {
            isPassed = true;
            final double bo = Utils.normalRelativeAngle(angleToTarget - noBearingOffset);
            final double targetWidth = LXXUtils.getRobotWidthInRadians(angleToTarget, sourceState.aDistance(targetState.getRobot()));
            final IntervalDouble currentInterval = new IntervalDouble(bo - targetWidth / 2, bo + targetWidth / 2);
            if (hitBearingOffsetInterval == null) {
                hitBearingOffsetInterval = currentInterval;
            } else {
                hitBearingOffsetInterval.a = min(hitBearingOffsetInterval.a, currentInterval.a);
                hitBearingOffsetInterval.b = max(hitBearingOffsetInterval.b, currentInterval.b);
            }
        }
        return contains;
    }

    public APoint getSourcePosAtFireTime() {
        return sourceState;
    }

    public APoint getTargetPosAtFireTime() {
        return targetState;
    }

    public LXXRobotState getSourceStateAtFireTime() {
        return sourceState;
    }

    public LXXRobotState getTargetStateAtLaunchTime() {
        return targetState;
    }

    public long getLaunchTime() {
        return launchTime;
    }

    public double getSpeed() {
        return speed;
    }

    public IntervalDouble getHitBearingOffsetInterval() {
        return hitBearingOffsetInterval;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public LXXBullet getCarriedBullet() {
        return carriedBullet;
    }

    public void setCarriedBullet(LXXBullet carriedBullet) {
        this.carriedBullet = carriedBullet;
    }

    public LXXRobotSnapshot2 getSourceState2() {
        return sourceState2;
    }

    public LXXRobotSnapshot2 getTargetState2() {
        return targetState2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wave wave = (Wave) o;

        return launchTime == wave.launchTime && sourceState.equals(wave.sourceState);

    }

    @Override
    public int hashCode() {
        int result = sourceState.hashCode();
        result = 31 * result + (int) (launchTime ^ (launchTime >>> 32));
        return result;
    }
}
