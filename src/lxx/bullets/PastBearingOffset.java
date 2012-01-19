/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets;

import lxx.ts_log.TurnSnapshot;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 05.08.11
 */
public class PastBearingOffset implements Comparable<PastBearingOffset> {

    public final TurnSnapshot source;
    public final double bearingOffset;
    public final double danger;

    public PastBearingOffset(TurnSnapshot source, double bearingOffset, double danger) {
        this.source = source;
        this.bearingOffset = bearingOffset;
        this.danger = danger;
    }

    public int compareTo(PastBearingOffset o) {
        return (int) signum(bearingOffset - o.bearingOffset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PastBearingOffset that = (PastBearingOffset) o;

        if (Double.compare(that.bearingOffset, bearingOffset) != 0) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = source != null ? source.hashCode() : 0;
        temp = bearingOffset != +0.0d ? Double.doubleToLongBits(bearingOffset) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
