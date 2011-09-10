/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.wave;

import lxx.LXXRobotState;
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

    private final long launchTime;
    private final double speed;

    private IntervalDouble hitInterval;

    public Wave(LXXRobotState source, LXXRobotState target, double speed, long launchTime) {
        this.sourceState = source;
        this.targetState = target;

        this.launchTime = launchTime;
        this.speed = speed;
    }

    public double getTraveledDistance() {
        return (sourceState.getRobot().getTime() - launchTime + 1) * speed;
    }

    public boolean check() {
        final double width = targetState.getRobot().getWidth();
        final double height = targetState.getRobot().getHeight();
        final Rectangle targetRect = new Rectangle((int) (targetState.getRobot().getX() - width / 2), (int) (targetState.getRobot().getY() - height / 2),
                (int) width, (int) height);
        final LXXPoint bulletPos = (LXXPoint) sourceState.project(sourceState.angleTo(targetState.getRobot()), getTraveledDistance());
        final boolean contains = targetRect.contains(bulletPos);
        if (contains) {
            final double bo = LXXUtils.bearingOffset(sourceState, targetState, targetState.getRobot());
            final double targetWidth = LXXUtils.getRobotWidthInRadians(sourceState, targetState.getRobot());
            final IntervalDouble currentInterval = new IntervalDouble(bo - targetWidth / 2, bo + targetWidth / 2);
            if (hitInterval == null) {
                hitInterval = currentInterval;
            } else {
                hitInterval.a = min(hitInterval.a, currentInterval.a);
                hitInterval.b = max(hitInterval.b, currentInterval.b);
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

    public IntervalDouble getHitInterval() {
        return hitInterval;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wave wave = (Wave) o;

        if (launchTime != wave.launchTime) return false;
        if (!Utils.isNear(wave.speed, speed)) return false;
        if (sourceState != null ? !sourceState.equals(wave.sourceState) : wave.sourceState != null)
            return false;
        if (targetState != null ? !targetState.equals(wave.targetState) : wave.targetState != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        long temp;
        result = sourceState != null ? sourceState.hashCode() : 0;
        result = 31 * result + (targetState != null ? targetState.hashCode() : 0);
        result = 31 * result + (int) (launchTime ^ (launchTime >>> 32));
        temp = (long) (speed * 100);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
