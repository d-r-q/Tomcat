/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import static java.lang.Math.max;
import static java.lang.StrictMath.min;

/**
 * User: jdev
 * Date: 10.12.11
 */
public class ValueInfo {

    private final AvgValue avgValue;
    private double maxValue = Long.MIN_VALUE;
    private double minValue = Long.MAX_VALUE;

    public ValueInfo(int deph) {
        avgValue = new AvgValue(deph);
    }

    public void addValue(double value) {
        maxValue = max(maxValue, value);
        minValue = min(minValue, value);
        avgValue.addValue(value);
    }

    @Override
    public String toString() {
        if (maxValue == Long.MIN_VALUE) {
            return "[ No Data ]";
        } else if (maxValue == minValue) {
            return String.format("[ %,14.0f ]", minValue);
        } else {
            return String.format("[ %,9.0f | %,9.0f | %,14.0f]", minValue, avgValue.getCurrentValue(), maxValue);
        }
    }
}
