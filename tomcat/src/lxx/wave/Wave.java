/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.wave;

import lxx.utils.APoint;
import lxx.utils.LXXPoint;
import lxx.utils.LXXRobotState;
import robocode.util.Utils;

import java.awt.*;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class Wave {

    private final LXXRobotState sourceState;
    private final LXXRobotState targetState;

    private final long launchTime;
    private final double speed;

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
        Rectangle targetRect = new Rectangle((int) (targetState.getRobot().getX() - width / 2), (int) (targetState.getRobot().getY() - height / 2),
                (int) width, (int) height);
        LXXPoint bulletPos = (LXXPoint) sourceState.project(sourceState.angleTo(targetState.getRobot()), getTraveledDistance());
        return targetRect.contains(bulletPos);
    }

    public APoint getSourcePos() {
        return sourceState;
    }

    public APoint getTargetPos() {
        return targetState;
    }

    public LXXRobotState getSourceStateAtFireTime() {
        return sourceState;
    }

    public LXXRobotState getTargetStateAtFireTime() {
        return targetState;
    }

    public long getLaunchTime() {
        return launchTime;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
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

    @Override
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
