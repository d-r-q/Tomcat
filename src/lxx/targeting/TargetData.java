/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting;

import java.util.ArrayList;
import java.util.List;

public class TargetData {

    private final List<Double> visitedGuessFactors = new ArrayList<Double>();

    public void addVisit(double guessFactor) {
        visitedGuessFactors.add(guessFactor);
    }

    public List<Double> getVisitedGuessFactors() {
        return visitedGuessFactors;
    }
}
