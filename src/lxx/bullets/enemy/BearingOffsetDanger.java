/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 07.09.2010
 */
public class BearingOffsetDanger implements Comparable<BearingOffsetDanger> {

    public final double bearingOffset;
    public final double danger;

    public BearingOffsetDanger(double bearingOffset, double match) {
        this.bearingOffset = bearingOffset;
        this.danger = match;
    }

    public int compareTo(BearingOffsetDanger o) {
        return (int) signum(bearingOffset - o.bearingOffset);
    }

}