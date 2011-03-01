/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark2;

import java.util.Arrays;

import static java.lang.Math.min;

public class AvgValue {

    private final double[] values;
    private final int deph;
    private int valuesCount;
    private double currentSum;

    public AvgValue(int deph) {
        this.deph = deph;
        values = new double[deph];
    }

    public AvgValue(AvgValue ethalon) {
        values = Arrays.copyOf(ethalon.values, ethalon.values.length);
        deph = ethalon.deph;
        valuesCount = ethalon.valuesCount;
        currentSum = ethalon.currentSum;
    }

    public void addValue(double newValue) {
        currentSum = currentSum - values[valuesCount % values.length] + newValue;
        values[valuesCount % values.length] = newValue;
        valuesCount++;
    }

    public double getCurrentValue() {
        return currentSum / min(valuesCount, deph);
    }

}
