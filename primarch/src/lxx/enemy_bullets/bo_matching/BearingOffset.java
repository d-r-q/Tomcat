/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets.bo_matching;

import lxx.model.BattleSnapshot;
import lxx.office.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 19.02.11
 */
public class BearingOffset {

    private final List<BattleSnapshot> predicates = new ArrayList<BattleSnapshot>();

    private final double bearingOffset;
    private final Timer timer;

    private DensityFormula densityFormula;

    public BearingOffset(double bearingOffset, Timer timer) {
        this.bearingOffset = bearingOffset;
        this.timer = timer;
    }

    public double match(BattleSnapshot battleSnapshot) {
        final List<BattleSnapshot> selectedSnapshots = densityFormula.selectSnapshots(predicates, timer.getBattleTime());
        if (selectedSnapshots.size() == 0) {
            return 0;
        }

        final double[] weights = densityFormula.getWeights();
        final int[] indexes = densityFormula.getIndexes();
        final double dimensionVolume = densityFormula.calculateDimensionVolume();

        double totalDist = 0;
        for (BattleSnapshot bs : selectedSnapshots) {
            totalDist += bs.quickDistance(indexes, battleSnapshot, weights) / dimensionVolume;
        }

        return 1 - totalDist / selectedSnapshots.size();
    }

    public boolean tryFormula(DensityFormula formula) {
        if (formula.calculateDensity(predicates, timer.getBattleTime()) >
                densityFormula.calculateDensity(predicates, timer.getBattleTime())) {
            densityFormula = formula;
            return true;
        }

        return false;
    }

}
