package kc.serpent.utils;

import java.util.ArrayList;

public abstract class VCSAABuffer extends AABuffer {
    public static int AIM_FACTORS;
    public static int MIDDLE_FACTOR;
    public static double SMOOTHING_WINDOW;
    public static double SMOOTHING;

    public boolean dynamicWeighting;
    public double rolling;
    public double desiredWeight;
    public double bufferWeight;

    double scoreTotal;
    double weightTotal;

    public float[][][][][][][][] hitStats;

    public VCSAABuffer(int[] sliceIndexes, double[][][] metaSlices, double[][][][] metaIndexStats) {
        super(sliceIndexes, metaSlices, metaIndexStats);
    }

    public void setHitStats() {
        hitStats = new float[slices[0].length + 1]
                [slices[1].length + 1]
                [slices[2].length + 1]
                [slices[3].length + 1]
                [slices[4].length + 1]
                [slices[5].length + 1]
                [slices[6].length + 1]
                [AIM_FACTORS + 1];
    }

    public void setStatList(ArrayList statList, int[] currentIndexes, double weight) {
        float[] currentBins = hitStats[currentIndexes[0]]
                [currentIndexes[1]]
                [currentIndexes[2]]
                [currentIndexes[3]]
                [currentIndexes[4]]
                [currentIndexes[5]]
                [currentIndexes[6]];
        statList.add(currentBins);
    }

    public double[] getBins(double[] bins, Wave w) {
        if (!waveStatLists.containsKey(w)) {
            setStatList(w);
        }
        ArrayList statList = (ArrayList) waveStatLists.get(w);

        for (int i = 0; i < statList.size(); i += 2) {
            float[] currentBins = (float[]) (statList.get(i));
            double weight = ((Double) statList.get(i + 1)).doubleValue();
            for (int ii = 0; ii < AIM_FACTORS; ii++) {
                bins[ii] += getAimFactorScore(currentBins, ii, weight);
            }
        }

        return bins;
    }

    public void logHit(double GF, ArrayList statList, double waveWeight) {
        int aimFactor = KUtils.toFactor(GF, MIDDLE_FACTOR, AIM_FACTORS);

        for (int i = 0; i < statList.size(); i += 2) {
            float[] currentBins = (float[]) (statList.get(i));
            double weight = ((Double) statList.get(i + 1)).doubleValue();
            logHit(currentBins, aimFactor, waveWeight * weight);
        }
    }

    public void logHit(float[] bins, int aimFactor, double weight) {
        if (dynamicWeighting && bins[AIM_FACTORS] > desiredWeight) {
            scoreTotal += weight * bins[aimFactor] / bins[AIM_FACTORS];
            weightTotal += weight;
            bufferWeight = KUtils.sixteenth(scoreTotal / weightTotal);
        }

        weight *= 1 - rolling;
        double rollingDecrease = 1 - weight;

        if (rolling != 0) {
            bins[aimFactor] *= rollingDecrease;
        }

        double totalAddition = weight;
        bins[aimFactor] += weight;

        double smoothedValue = weight;
        for (int i = aimFactor - 1; i >= 0; i--) {
            if (rolling != 0) {
                bins[i] *= rollingDecrease;
            } else {
                if (i < aimFactor - SMOOTHING_WINDOW) {
                    break;
                }
            }

            if (i >= aimFactor - SMOOTHING_WINDOW) {
                smoothedValue *= SMOOTHING;
                bins[i] += smoothedValue;
                totalAddition += smoothedValue;
            }
        }
        smoothedValue = weight;
        for (int i = aimFactor + 1; i < AIM_FACTORS; i++) {
            if (rolling != 0) {
                bins[i] *= rollingDecrease;
            } else {
                if (i > aimFactor + SMOOTHING_WINDOW) {
                    break;
                }
            }

            if (i <= aimFactor + SMOOTHING_WINDOW) {
                smoothedValue *= SMOOTHING;
                bins[i] += smoothedValue;
                totalAddition += smoothedValue;
            }
        }

        if (rolling != 0) {
            bins[AIM_FACTORS] *= rollingDecrease;
        }
        bins[AIM_FACTORS] += totalAddition;
    }

    public double getAimFactorScore(float[] bins, int aimFactor, double weight) {
        return bufferWeight * bins[aimFactor] * weight / Math.max(bins[AIM_FACTORS], desiredWeight);
    }
}
				