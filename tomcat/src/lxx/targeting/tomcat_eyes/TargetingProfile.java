/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.utils.LXXConstants;

import static java.lang.Math.abs;

public class TargetingProfile {

    public int totalHits = 0;
    public int zeroGFHitCount = 0;
    public int positiveGFHitCount = 0;
    public int negativeGFHitCount = 0;

    public void addBearingOffset(double bearingOffsetRadians) {
        totalHits++;
        if (abs(bearingOffsetRadians) < LXXConstants.RADIANS_10) {
            zeroGFHitCount++;
        } else if (bearingOffsetRadians > 0) {
            positiveGFHitCount++;
        } else {
            negativeGFHitCount++;
        }
    }

}
