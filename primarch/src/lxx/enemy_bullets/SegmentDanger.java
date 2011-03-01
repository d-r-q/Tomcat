/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets;

/**
 * User: jdev
 * Date: 07.09.2010
 */
public class SegmentDanger<T> {

    public final T bearingOffset;
    public final double match;

    public SegmentDanger(T bearingOffset, double match) {
        this.bearingOffset = bearingOffset;
        this.match = match;
    }
}