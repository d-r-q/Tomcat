/**
 * $Id$
 *
 * Copyright (c) 2009 Zodiac Interactive. All Rights Reserved.
 */
package lxx.targeting.mg4;

public class DeltaVector {

    public final int alpha;
    public final double dist;

    public DeltaVector(int alpha, double dist) {
        this.alpha = alpha;
        this.dist = dist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeltaVector that = (DeltaVector)o;

        if (alpha != that.alpha) return false;
        if (dist != that.dist) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = alpha;
        result = (int) (31 * result + dist);
        return result;
    }

    public String toString() {
        return "(" + alpha + " : " + dist + ")";
    }
}
