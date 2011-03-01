/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

public enum WallType {

    TOP(LXXConstants.RADIANS_0, LXXConstants.RADIANS_90, LXXConstants.RADIANS_270),
    RIGHT(LXXConstants.RADIANS_90, LXXConstants.RADIANS_180, LXXConstants.RADIANS_0),
    BOTTOM(LXXConstants.RADIANS_180, LXXConstants.RADIANS_270, LXXConstants.RADIANS_90),
    LEFT(LXXConstants.RADIANS_270, LXXConstants.RADIANS_0, LXXConstants.RADIANS_180);

    public final double fromCenterAngle;

    public final double clockwiseAngle;
    public final double counterClockwiseAngle;

    private WallType(double fromCenterAngle, double clockwiseAngle, double counterClockwiseAngle) {
        this.fromCenterAngle = fromCenterAngle;


        this.clockwiseAngle = clockwiseAngle;
        this.counterClockwiseAngle = counterClockwiseAngle;
    }

}
