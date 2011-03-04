package kc.serpent.utils;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class AABuffer {
    public static final int DIMENSIONS = 7;
    public static final double MINIMUM_AA_WEIGHT = 0.01;

    public double[][][] indexStats = new double[DIMENSIONS][][];
    public double[][] slices = new double[DIMENSIONS][];

    double[] features = new double[DIMENSIONS];
    int[] indexes = new int[DIMENSIONS];

    int[] aaDirections;
    double[][] aaWeights;
    double maxWeight;

    public HashMap waveStatLists = new HashMap();

    public AABuffer(int[] sliceIndexes, double[][][] metaSlices, double[][][][] metaIndexStats) {
        for (int i = 0; i < sliceIndexes.length; i++) {
            if (sliceIndexes[i] != 0) {
                slices[i] = metaSlices[i][sliceIndexes[i]];
                indexStats[i] = metaIndexStats[i][sliceIndexes[i]];//getIndexStats(slices[i], bounds[i);
            } else {
                slices[i] = new double[]{};
            }
        }
    }

    public void clearWaveStatsLists() {
        waveStatLists.clear();
    }

    public void setIndexes(Wave w) {
        features[0] = w.normalizedDistance;
        features[1] = Math.abs(w.latVelocity);
        features[2] = w.accel;
        features[3] = w.vChangeTimer;
        features[4] = w.lastDTraveled;
        features[5] = w.wallAhead;
        features[6] = w.wallReverse;

        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = KUtils.index(features[i], slices[i]);
        }
    }

    public void setStatList(Wave w) {
        setIndexes(w);

        aaDirections = new int[DIMENSIONS];
        aaWeights = new double[DIMENSIONS][2];
        maxWeight = 1;

        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] != 0) {
                double[] currentStats = indexStats[i][indexes[i]];
                double diff = currentStats[LEFT_BOUND] - (features[i] - currentStats[LEFT_WINDOW]);//left bound - left edge of window
                if (diff > 0) {
                    diff /= 2 * currentStats[LEFT_WINDOW];
                    aaDirections[i] = -1;
                    maxWeight *= (aaWeights[i][0] = 1 - diff);
                    aaWeights[i][1] = diff;
                }
            }
            if (indexes[i] != slices[i].length) {
                double[] currentStats = indexStats[i][indexes[i]];
                double diff = features[i] + currentStats[RIGHT_WINDOW] - currentStats[RIGHT_BOUND];//right edge of window - right bound
                if (diff > 0) {
                    diff /= 2 * currentStats[RIGHT_WINDOW];
                    aaDirections[i] = 1;
                    maxWeight *= (aaWeights[i][0] = 1 - diff);
                    aaWeights[i][1] = diff;
                }
            }
        }

        ArrayList statList = new ArrayList();
        setStatList(statList, new int[DIMENSIONS], maxWeight, 0);
        waveStatLists.put(w, statList);
    }

    public void setStatList(ArrayList statList, int[] currentIndexes, double weight, int dimension) {
        if (weight / maxWeight < MINIMUM_AA_WEIGHT) {
            return;
        }

        if (dimension == DIMENSIONS) {
            /*System.out.print("WEIGHT: " + weight + " ");
               for(int i = 0; i < DIMENSIONS; i++) {
                   System.out.print(currentIndexes[i] + " ");
               }
               System.out.println();

               if(weight == maxWeight) {
                   System.out.println();
               }*/

            setStatList(statList, currentIndexes, weight);
            statList.add(new Double(weight));
            return;
        }

        if (aaDirections[dimension] != 0) {
            currentIndexes[dimension] = indexes[dimension] + aaDirections[dimension];
            setStatList(statList, currentIndexes, weight * aaWeights[dimension][1] / aaWeights[dimension][0], dimension + 1);
        }
        currentIndexes[dimension] = indexes[dimension];
        setStatList(statList, currentIndexes, weight, dimension + 1);
    }

    public abstract void setStatList(ArrayList statList, int[] currentIndexes, double weight);

    public void logHit(double GF, Wave w, double waveWeight) {
        if (!waveStatLists.containsKey(w)) {
            setStatList(w);
        }
        ArrayList statList = (ArrayList) waveStatLists.get(w);

        logHit(GF, statList, waveWeight);
    }

    public abstract void logHit(double GF, ArrayList statList, double waveWeight);

    static final int LEFT_WINDOW = 0;
    static final int RIGHT_WINDOW = 1;
    static final int LEFT_BOUND = 2;
    static final int RIGHT_BOUND = 3;

    public static double[][][][] getIndexStats(double[][][] slices, double[][] bounds) {
        double[][][][] currentStats = new double[slices.length][][][];
        for (int i = 0; i < slices.length; i++) {
            currentStats[i] = new double[slices[i].length][][];
            for (int ii = 1; ii < slices[i].length; ii++) {
                if (slices[i][ii] == null) {
                    continue;
                }
                currentStats[i][ii] = getIndexStats(slices[i][ii], bounds[i]);
            }
        }
        return currentStats;
    }

    public static double[][] getIndexStats(double[] slices, double[] bounds) {
        double[][] currentStats = new double[slices.length + 1][4];

        for (int i = 0; i <= slices.length; i++) {
            double halfWindow;
            if (i == 0) {
                halfWindow = (slices[i] - bounds[0]) / 2;
                currentStats[i][LEFT_WINDOW] = 0;
                currentStats[i][RIGHT_WINDOW] = halfWindow;
                currentStats[i][LEFT_BOUND] = bounds[0];
                currentStats[i][RIGHT_BOUND] = slices[i];
            } else if (i == slices.length) {
                halfWindow = (bounds[1] - slices[i - 1]) / 2;
                currentStats[i][LEFT_WINDOW] = currentStats[i - 1][RIGHT_WINDOW] = Math.min(currentStats[i - 1][RIGHT_WINDOW], halfWindow);
                currentStats[i][RIGHT_WINDOW] = 0;
                currentStats[i][LEFT_BOUND] = slices[i - 1];
                currentStats[i][RIGHT_BOUND] = bounds[1];
            } else {
                halfWindow = (slices[i] - slices[i - 1]) / 2;
                currentStats[i][LEFT_WINDOW] = currentStats[i - 1][RIGHT_WINDOW] = Math.min(currentStats[i - 1][RIGHT_WINDOW], halfWindow);
                currentStats[i][RIGHT_WINDOW] = halfWindow;
                currentStats[i][LEFT_BOUND] = slices[i - 1];
                currentStats[i][RIGHT_BOUND] = slices[i];
            }
        }

        return currentStats;
    }
}
