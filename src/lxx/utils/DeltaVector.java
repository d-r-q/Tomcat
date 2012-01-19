/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.io.Serializable;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

public class DeltaVector implements Serializable {

    private final double alphaRadians;
    private final double length;

    public DeltaVector(double alphaRadians, double length) {
        this.alphaRadians = alphaRadians;
        this.length = length;
    }

    public double getAlphaRadians() {
        return alphaRadians;
    }

    public double getLength() {
        return length;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeltaVector that = (DeltaVector) o;

        if (alphaRadians != that.alphaRadians) return false;
        if (round(length) != round(that.length)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        long temp;
        temp = alphaRadians != +0.0d ? Double.doubleToLongBits(alphaRadians) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = length != +0.0d ? Double.doubleToLongBits(length) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String toString() {
        return "(" + toDegrees(alphaRadians) + " : " + length + ")";
    }
}
