/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting;

import lxx.utils.AvgValue;

import java.util.ArrayList;
import java.util.List;

public class TargetData {

    private final List<Double> visitedGuessFactors = new ArrayList<Double>();
    private AvgValue avgFireDelay = new AvgValue(2000);

    public void addVisit(double guessFactor) {
        visitedGuessFactors.add(guessFactor);
    }

    public List<Double> getVisitedGuessFactors() {
        return visitedGuessFactors;
    }

    public void addFireDelay(long fireDelay) {
        avgFireDelay.addValue(fireDelay);
    }

    public double getAvgFireDelay() {
        return avgFireDelay.getCurrentValue();
    }

}
