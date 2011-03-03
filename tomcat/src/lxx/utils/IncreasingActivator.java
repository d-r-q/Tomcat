/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

public class IncreasingActivator {

    private final double noActivityThreshold;
    private final double maxActivityThreshold;
    private final double blockingThershold;
    private final double activeIntervalLength;

    public IncreasingActivator(double noActivityThreshold, double maxActivityThreshold, double blockingThershold) {
        this.noActivityThreshold = noActivityThreshold;
        this.maxActivityThreshold = maxActivityThreshold;
        this.blockingThershold = blockingThershold;

        activeIntervalLength = maxActivityThreshold - noActivityThreshold;
    }

    public double activate(double x) {
        if (x <= noActivityThreshold) {
            return 0;
        } else if (x >= blockingThershold) {
            return 10;
        } else if (x >= maxActivityThreshold) {
            return 1;
        } else {
            return (x - noActivityThreshold) / (activeIntervalLength);
        }
    }

}
