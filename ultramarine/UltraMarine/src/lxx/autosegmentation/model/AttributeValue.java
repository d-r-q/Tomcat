package lxx.autosegmentation.model;

import lxx.utils.LXXConstants;

import static java.lang.Math.sqrt;

/**
 * User: jdev
 * Date: 18.02.2010
 */
public class AttributeValue implements Comparable<AttributeValue> {

    public final int attributeValue;
    //public final Map<Integer, Integer> gfVisitCounts = new HashMap<Integer, Integer>();
    private final int[] gfVisitCounts = new int[LXXConstants.MAX_GUESS_FACTOR * 2 + 1];

    private int gfCount;
    private double mathExpection;
    private double srKVotkl;

    public AttributeValue(int attributeValue) {
        this.attributeValue = attributeValue;
    }

    public void addGuessFactor(int guessFactor) {
        gfVisitCounts[guessFactor + LXXConstants.MAX_GUESS_FACTOR]++;

        gfCount++;

        this.mathExpection = 0;
        for (int i = 0; i < gfVisitCounts.length; i++) {
            mathExpection += (i - LXXConstants.MAX_GUESS_FACTOR) * ((double)gfVisitCounts[i] / gfCount);
        }

        double res = 0;

        int gfVisits = 0;
        for (int i = 0; i < gfVisitCounts.length; i++) {
            double dif = mathExpection - (i - LXXConstants.MAX_GUESS_FACTOR);
            res += dif * dif * gfVisitCounts[i];
            gfVisits += gfVisitCounts[i];
        }
        if (gfVisits == 0) {
            srKVotkl = LXXConstants.MAX_GUESS_FACTOR * 4;
        }

        srKVotkl =  sqrt(res / gfVisits);
    }

    public int compareTo(AttributeValue another) {
        return attributeValue - another.attributeValue;
    }

    public double getMathExpection() {
        return this.mathExpection;
    }

    public Integer getMediana() {
        /*List<Integer> guessFactors = new ArrayList<Integer>();

        for (Integer guessFactor : gfVisitCounts.keySet()) {
            for (int i = 0; i < gfVisitCounts.get(guessFactor); i++) {
                guessFactors.add(guessFactor);
            }
        }

        if (guessFactors.size() == 1) {
            return guessFactors.get(0);
        }
        int half = guessFactors.size() / 2 - 1;

        return (guessFactors.get(half) + guessFactors.get(half + 1)) / 2;*/
        return null;
    }

    public Double getAvgGuessFactor() {
        double totalGFValue = 0;
        double gfCount = 0;

        for (int i = 0; i < gfVisitCounts.length; i++) {
            totalGFValue += (i - LXXConstants.MAX_GUESS_FACTOR) * gfVisitCounts[i];
            gfCount += gfVisitCounts[i];
        }

        return totalGFValue / gfCount;
    }

    public Double getSrKVOtkl() {
        return srKVotkl;
    }

    public int getVisitCount() {
        return (int) gfCount;
    }

    public int getAttributeValue() {
        return attributeValue;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttributeValue that = (AttributeValue) o;

        return attributeValue == that.attributeValue;

    }

    public int hashCode() {
        return attributeValue;
    }

    public int[] getGuessFactors() {
        return gfVisitCounts;
    }
}
