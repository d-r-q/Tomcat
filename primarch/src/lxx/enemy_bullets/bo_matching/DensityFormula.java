/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets.bo_matching;

import lxx.model.BattleSnapshot;
import lxx.model.attributes.Attribute;
import lxx.utils.LXXUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jdev
 * Date: 19.02.11
 */
public class DensityFormula {

    private final Attribute[] attributes;
    private final long battleTimeLimit;
    private final int[] indexes;
    private final double[] weights;

    public DensityFormula(Attribute[] attributes, long battleTimeLimit, double[] weights) {
        this.attributes = attributes;
        this.battleTimeLimit = battleTimeLimit;
        this.weights = weights;

        this.indexes = new int[attributes.length];
        int idx = 0;
        for (Attribute a : attributes) {
            indexes[idx++] = a.getId();
        }
    }


    public double calculateDensity(List<BattleSnapshot> battleSnapshots, long battleCurrentTime) {
        final List<BattleSnapshot> selectedSnapshots = selectSnapshots(battleSnapshots, battleCurrentTime);
        if (selectedSnapshots.size() < 2) {
            return 0;
        }
        return selectedSnapshots.size() / (calculateVolume(selectedSnapshots) / calculateDimensionVolume());
    }

    public double[] getWeights() {
        final double[] weights = new double[attributes.length];
        for (Attribute a : attributes) {
            weights[a.getId()] = this.weights[a.getId()] / a.getActualRange();
        }
        return weights;
    }

    public int[] getIndexes() {
        return indexes;
    }

    private double calculateVolume(List<BattleSnapshot> battleSnapshots) {
        final double[] weights = getWeights();

        BattleSnapshot mdBs1 = battleSnapshots.get(0);
        BattleSnapshot mdBs2 = battleSnapshots.get(1);
        double maxDist = mdBs1.quickDistance(indexes, mdBs2, weights);
        for (int i = 2; i < battleSnapshots.size(); i++) {
            final BattleSnapshot bs = battleSnapshots.get(i);
            final double dist1 = mdBs1.quickDistance(indexes, bs, weights);
            if (dist1 > maxDist) {
                maxDist = dist1;
                mdBs2 = bs;
                i--;
                continue;
            }

            final double dist2 = mdBs2.quickDistance(indexes, bs, weights);
            if (dist2 > maxDist) {
                maxDist = dist2;
                mdBs2 = bs;
                i--;
            }
        }

        return maxDist;
    }

    public List<BattleSnapshot> selectSnapshots(List<BattleSnapshot> battleSnapshots, long battleCurrentTime) {
        final long timeLimit = battleCurrentTime - battleTimeLimit;
        final List<BattleSnapshot> selectedSnapshots = new ArrayList<BattleSnapshot>();

        for (int i = battleSnapshots.size() - 1; i >= 0; i--) {
            if (battleSnapshots.get(i).getBattleTime() < timeLimit) {
                break;
            }
            selectedSnapshots.add(battleSnapshots.get(i));
        }

        return selectedSnapshots;
    }

    public double calculateDimensionVolume() {
        double[] minPoint = new double[attributes.length];
        double[] maxPoint = new double[attributes.length];

        int idx = 0;
        for (Attribute a : attributes) {
            minPoint[idx] = a.getActualMin();
            maxPoint[idx] = a.getActualMax();
            idx++;
        }

        return LXXUtils.manhattanDistance(minPoint, maxPoint, getWeights());
    }

}
