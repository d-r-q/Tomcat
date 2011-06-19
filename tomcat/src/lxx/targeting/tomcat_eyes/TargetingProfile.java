/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.LXXRobotState;
import lxx.utils.LXXUtils;
import lxx.utils.Median;

import static java.lang.Math.abs;

public class TargetingProfile {

    public Median distWithHoBoMedian = new Median(1000);
    public Median distWithLinearBOMedian = new Median(1000);

    public void addBearingOffset(LXXRobotState enemy, LXXRobotState me, double bearingOffsetRadians, double bulletSpeed) {
        distWithHoBoMedian.addValue(abs(bearingOffsetRadians));
        final double linearBo = abs(LXXUtils.lateralVelocity(enemy, me) / bulletSpeed);
        distWithLinearBOMedian.addValue(abs(bearingOffsetRadians - linearBo));
    }

}
