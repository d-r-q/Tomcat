/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log;

import lxx.LXXRobotState;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.LXXUtils;

import java.io.Serializable;
import java.util.Arrays;

import static java.lang.Math.toRadians;
import static java.lang.StrictMath.round;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class TurnSnapshot implements Serializable {

    private final double[] attributeValues;
    private final long time;
    private final int round;
    private final LXXRobotState meImage;
    private final LXXRobotState enemyImage;

    // access optimisation
    public TurnSnapshot next;
    public final int roundTime;

    public TurnSnapshot(double[] attributeValues, long time, int round, LXXRobotState meImage, LXXRobotState enemyImage) {
        this.attributeValues = attributeValues;
        this.time = time;
        this.round = round;
        this.meImage = meImage;
        this.enemyImage = enemyImage;
        this.roundTime = LXXUtils.getRoundTime(time, round);
    }

    public int getRoundedAttrValue(Attribute a) {
        return (int) round(attributeValues[a.id]);
    }

    public double getAttrValue(Attribute a) {
        return attributeValues[a.id];
    }

    public long getTime() {
        return time;
    }

    public int getRound() {
        return round;
    }

    public double[] toArray() {
        return attributeValues;
    }

    public void setNext(TurnSnapshot next) {
        if (time + 1 != next.time) {
            throw new RuntimeException("Snapshot skipped");
        }
        this.next = next;
    }

    public String toString() {
        return Arrays.toString(attributeValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TurnSnapshot that = (TurnSnapshot) o;

        return roundTime == that.roundTime;

    }

    @Override
    public int hashCode() {
        return roundTime;
    }

    public double getMySpeed() {
        return attributeValues[AttributesManager.mySpeed.id];
    }

    public double getMyAbsoluteHeadingRadians() {
        return toRadians(attributeValues[AttributesManager.myAbsoluteHeadingDegrees.id]);
    }

    public double getEnemyAbsoluteHeading() {
        return toRadians(attributeValues[AttributesManager.enemyAbsoluteHeading.id]);
    }

    public LXXRobotState getMeImage() {
        return meImage;
    }

    public LXXRobotState getEnemyImage() {
        return enemyImage;
    }
}
