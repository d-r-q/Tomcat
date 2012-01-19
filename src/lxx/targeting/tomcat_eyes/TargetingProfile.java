/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.LXXRobotState;
import lxx.utils.Interval;
import lxx.utils.IntervalDouble;
import lxx.utils.LXXUtils;
import lxx.utils.Median;

import static java.lang.Math.abs;

public class TargetingProfile {

    public Median distWithHoBoMedian = new Median(1000);
    public Median distWithLinearBOMedian = new Median(1000);
    public IntervalDouble bearingOffsetsInteval = new IntervalDouble(0, 0);
    public int bearingOffsets = 0;

    public void addBearingOffset(LXXRobotState enemy, LXXRobotState me, double bearingOffsetRadians, double bulletSpeed) {
        bearingOffsets++;
        distWithHoBoMedian.addValue(abs(bearingOffsetRadians));
        final double linearBo = abs(LXXUtils.lateralVelocity(enemy, me) / bulletSpeed);
        distWithLinearBOMedian.addValue(abs(bearingOffsetRadians - linearBo));
        bearingOffsetsInteval.extend(bearingOffsetRadians);
    }

}
