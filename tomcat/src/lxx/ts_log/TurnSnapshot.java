/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log;

import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;

import java.io.Serializable;
import java.util.Arrays;

import static java.lang.Math.toRadians;
import static java.lang.StrictMath.round;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class TurnSnapshot implements Serializable {

    private static final int FIFTEEN_BITS = 0x7FFF;
    private final double[] attributeValues;
    private final long time;
    private final int round;

    // access optimisation
    public TurnSnapshot next;
    public final int roundTime;

    public TurnSnapshot(double[] attributeValues, long time, int round) {
        this.attributeValues = attributeValues;
        this.time = time;
        this.round = round;
        if (round > FIFTEEN_BITS || time > FIFTEEN_BITS) {
            throw new IllegalArgumentException("Too large round-time: " + round + " - " + time);
        }
        this.roundTime = (int) (((round & FIFTEEN_BITS) << 15) | (time & FIFTEEN_BITS));
    }

    public int getRoundedAttrValue(Attribute a) {
        return (int) round(attributeValues[a.getId()]);
    }

    public double getAttrValue(Attribute a) {
        return attributeValues[a.getId()];
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
        return attributeValues[AttributesManager.mySpeed.getId()];
    }

    public double getMyAbsoluteHeadingRadians() {
        return toRadians(attributeValues[AttributesManager.myAbsoluteHeadingDegrees.getId()]);
    }

    public double getEnemyAbsoluteHeading() {
        return toRadians(attributeValues[AttributesManager.enemyAbsoluteHeading.getId()]);
    }
}
