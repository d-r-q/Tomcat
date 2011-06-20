/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

public class LXXCircle {

    public final APoint center;
    public final double radius;

    public LXXCircle(APoint center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public double toRadians(double distance) {
        return (distance * LXXConstants.RADIANS_360) / 2 * Math.PI * radius;
    }
}
