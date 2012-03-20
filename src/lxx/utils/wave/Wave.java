/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.wave;

import lxx.LXXRobot2;
import lxx.LXXRobotSnapshot2;
import lxx.bullets.LXXBullet;
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

    private final LXXRobotSnapshot2 sourceState;
    private final LXXRobotSnapshot2 targetState;

    private final long launchTime;
    private final double speed;

    public final double noBearingOffset;

    private boolean isPassed = false;
    private IntervalDouble hitBearingOffsetInterval;
    private LXXBullet carriedBullet;
    private LXXRobot2 target;

    public Wave(LXXRobotSnapshot2 sourceState, LXXRobotSnapshot2 targetState, LXXRobot2 target, double speed, long launchTime) {
        this.target = target;

        this.launchTime = launchTime;
        this.speed = speed;
        this.targetState = targetState;
        this.sourceState = sourceState;
        this.noBearingOffset = sourceState.angleTo(targetState);
    }

    public double getTraveledDistance() {
        return (target.getTime() - launchTime + 1) * speed;
    }

    public boolean check() {
        final double width = target.getWidth();
        final double height = target.getHeight();
        final Rectangle targetRect = new Rectangle((int) (target.getX() - width / 2), (int) (target.getY() - height / 2),
                (int) width, (int) height);
        final double angleToTarget = sourceState.angleTo(target);
        final LXXPoint bulletPos = (LXXPoint) sourceState.project(angleToTarget, getTraveledDistance());
        final boolean contains = targetRect.contains(bulletPos);
        if (contains) {
            isPassed = true;
            final double bo = Utils.normalRelativeAngle(angleToTarget - noBearingOffset);
            final double targetWidth = LXXUtils.getRobotWidthInRadians(angleToTarget, sourceState.aDistance(targetState));
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

    public LXXRobotSnapshot2 getSourceState() {
        return sourceState;
    }

    public LXXRobotSnapshot2 getTargetState() {
        return targetState;
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

    public LXXRobot2 getTarget() {
        return target;
    }
}
