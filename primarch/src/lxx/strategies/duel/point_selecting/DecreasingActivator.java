/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel.point_selecting;

public class DecreasingActivator {

    private final double blockingThreshold;
    private final double maxActivityThreshold;
    private final double noActivityThreshold;
    private final double activeIntervalLength;

    public DecreasingActivator(double noActivityThreshold, double maxActivityThreshold, double blockingThreshold) {
        this.noActivityThreshold = noActivityThreshold;
        this.maxActivityThreshold = maxActivityThreshold;
        this.blockingThreshold = blockingThreshold;

        activeIntervalLength = noActivityThreshold - maxActivityThreshold;
    }

    public double activate(double x) {
        if (x <= blockingThreshold) {
            return 10;
        } else if (x <= maxActivityThreshold) {
            return 1;
        } else if (x >= noActivityThreshold) {
            return 0;
        } else {
            return 1 - (x - maxActivityThreshold) / (activeIntervalLength);
        }
    }

}
