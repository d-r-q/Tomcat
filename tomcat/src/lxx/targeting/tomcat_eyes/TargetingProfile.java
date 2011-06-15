/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.utils.LXXConstants;
import lxx.utils.Median;

import static java.lang.Math.toDegrees;

public class TargetingProfile {

    public int totalNormalBearingOffsets = 0;
    public int positiveNormalBearingOffsetsCount = 0;
    public int negativeNormalBearingOffsetsCount = 0;
    public int hitCount = 0;
    public Median bearingOffsetsMedian = new Median(2000);

    public void addBearingOffset(double bearingOffsetRadians, boolean isHit) {
        totalNormalBearingOffsets++;
        if (bearingOffsetRadians < -LXXConstants.RADIANS_2) {
            negativeNormalBearingOffsetsCount++;
        } else {
            positiveNormalBearingOffsetsCount++;
        }

        if (isHit) {
            hitCount++;
        }

        bearingOffsetsMedian.addValue((int) toDegrees(bearingOffsetRadians));
    }

}
