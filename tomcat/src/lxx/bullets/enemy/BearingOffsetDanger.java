/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

/**
 * User: jdev
 * Date: 07.09.2010
 */
public class BearingOffsetDanger {

    public final double bearingOffset;
    public final double danger;

    public BearingOffsetDanger(double bearingOffset, double match) {
        this.bearingOffset = bearingOffset;
        this.danger = match;
    }
}