/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model;

import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;
import lxx.utils.DeltaVector;

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
    private final String targetName;

    private TurnSnapshot prev;
    private TurnSnapshot next;

    public TurnSnapshot(double[] attributeValues, long time, int round, String targetName) {
        this.attributeValues = attributeValues;
        this.time = time;
        this.round = round;
        this.targetName = targetName;
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

    public TurnSnapshot getPrev() {
        return prev;
    }

    public double[] toArray() {
        return attributeValues;
    }

    public void setPrev(TurnSnapshot prev) {
        if (time - 1 != prev.time) {
            throw new RuntimeException("Snapshot skipped");
        }
        this.prev = prev;
    }

    public TurnSnapshot getNext() {
        return next;
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TurnSnapshot that = (TurnSnapshot) o;

        if (round != that.round) return false;
        if (time != that.time) return false;
        if (!targetName.equals(that.targetName)) return false;

        return true;
    }

    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + round;
        result = 31 * result + targetName.hashCode();
        return result;
    }

    public double getMyVelocityModule() {
        return attributeValues[AttributesManager.myVelocityModule.getId()];
    }

    public double getMyAbsoluteHeadingRadians() {
        return toRadians(attributeValues[AttributesManager.myAbsoluteHeadingDegrees.getId()]);
    }

    public double getEnemyVelocity() {
        return attributeValues[AttributesManager.enemyVelocity.getId()];
    }

    public double getEnemyAbsoluteHeading() {
        return toRadians(attributeValues[AttributesManager.enemyAbsoluteHeading.getId()]);
    }
}
