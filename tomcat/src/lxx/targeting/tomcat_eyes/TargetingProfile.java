/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_eyes;

import lxx.utils.Interval;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

public class TargetingProfile {

    private final Interval bearingOffsetsInterval = new Interval(0, 0);
    private final List<Double> bearingOffsets = new ArrayList<Double>();

    public void addBearingOffset(double bearingOffsetRadians) {
        bearingOffsets.add(bearingOffsetRadians);
        bearingOffsetsInterval.extend((int) round(toDegrees(bearingOffsetRadians)));
    }

    public Interval getBearingOffsetsInterval() {
        return bearingOffsetsInterval;
    }

    public List<Double> getBearingOffsets() {
        return bearingOffsets;
    }
}
