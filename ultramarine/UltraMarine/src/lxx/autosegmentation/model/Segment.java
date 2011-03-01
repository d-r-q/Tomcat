package lxx.autosegmentation.model;

import lxx.utils.LXXConstants;

import java.util.*;
import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * User: jdev
 * Date: 18.02.2010
 */
public class Segment implements Comparable<Integer> {

    private int minValue = Integer.MAX_VALUE;
    private int maxValue = Integer.MIN_VALUE;

    private int actualMinValue = Integer.MAX_VALUE;
    private int actualMaxValue = Integer.MIN_VALUE;

    private AttributeValue[] entries;
    private int attributeCount = 0;
    private int visitCount;

    public Segment(int minValue, int maxValue) {
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("AYYYYYYYYYYYYYYYYYYYYYYY!");
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
        actualMinValue = (maxValue - minValue) / 2 + minValue;
        actualMaxValue = (maxValue - minValue) / 2 + minValue;

        entries = new AttributeValue[this.maxValue - this.minValue + 1];
    }

    public Segment(int actualMinValue, int actualMaxValue, int minValue, int maxValue) {
        if (minValue > maxValue || actualMinValue > actualMaxValue) {
            throw new IllegalArgumentException("AYYYYYYYYYYYYYYYYYYYYYYY!");
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.actualMinValue = actualMinValue;
        this.actualMaxValue = actualMaxValue;

        entries = new AttributeValue[this.maxValue - this.minValue + 1];
    }

    public void addEntry(int attributeValue, int guessFactor) {
        if (attributeValue < actualMinValue) {
            actualMinValue = attributeValue;

            /*if (actualMinValue != Integer.MAX_VALUE && actualMaxValue != Integer.MIN_VALUE) {
                AttributeValue[] newEntries = new AttributeValue[actualMaxValue - actualMinValue + 1];
                System.arraycopy(entries, 0, newEntries, 1, entries.length);
                entries = newEntries;
            }*/
        }
        if (attributeValue > actualMaxValue) {
            actualMaxValue = attributeValue;

            /*if (actualMinValue != Integer.MAX_VALUE && actualMaxValue != Integer.MIN_VALUE) {
                AttributeValue[] newEntries = new AttributeValue[maxValue - actualMinValue + 1];
                System.arraycopy(entries, 0, newEntries, 0, entries.length);
                entries = newEntries;
            }*/
        }

        int avIdx = 0;
        avIdx = getAvIdx(attributeValue);
        if (entries[avIdx] == null) {
            entries[avIdx] = new AttributeValue(attributeValue);
            attributeCount++;
            checkAttributesValue();
        }

        entries[avIdx].addGuessFactor(guessFactor);
        visitCount++;
    }

    private int getAvIdx(int attributeValue) {
        return attributeValue - minValue;
    }

    public int getMinValue() {
        return actualMinValue;
    }

    public int getMaxValue() {
        return actualMaxValue;
    }

    public int compareTo(Integer o) {
        return actualMinValue - o;
    }

    public AttributeValue getAttributeValue(int attributeValue) {
        return entries[getAvIdx(attributeValue)];
    }

    public AttributeValue[] getAttributeValues() {
        return entries;
    }

    public double getMathExpection() {
        double res = 0;

        int usedCount = 0;
        for (AttributeValue av : entries) {
            if (av != null && av.getVisitCount() > 10) {
                res += av.getMathExpection();
                usedCount++;
            }
        }
        if (usedCount == 0) {
            return 0;
        }

        return res / usedCount;
    }

    public double getSrKVOtkl() {
        if (attributeCount == 0) {
            return LXXConstants.MAX_GUESS_FACTOR * 4;
        }
        double res = 0;
        int usedCount = 0;
        for (AttributeValue av : entries) {
            if (av != null && av.getVisitCount() > 10) {
                res += av.getSrKVOtkl();
                usedCount++;
            }
        }

        if (usedCount == 0) {
            return LXXConstants.MAX_GUESS_FACTOR;
        }

        return res / usedCount;
    }

    public double getMESum() {
        double res = 0;

        int avCount = 0;
        for (AttributeValue av : entries) {
            if (av != null) {
                avCount++;
                res += abs(av.getMathExpection());
            }
        }

        if (avCount == 0) {
            return 0;
        }

        return res / avCount;
    }

    public int getMaxVisitCount() {
        int maxVisitCount = Integer.MIN_VALUE;

        for (AttributeValue av : entries) {
            if (av != null) {
                if (av.getVisitCount() > maxVisitCount) {
                    maxVisitCount = av.getVisitCount();
                }
            }
        }

        return maxVisitCount;
    }

    public List<Extremum> getExtremums() {
        List<Extremum> res = new ArrayList<Extremum>();
        final int param = (int)round((actualMaxValue - actualMinValue) / 1D);
        double[] ySum;
        double scale = param / (double)(actualMaxValue - actualMinValue);
        int delta = (int)(actualMinValue * scale);
        ySum = new double[param + 1];
        for (int i = actualMinValue; i < actualMaxValue; i++) {
            AttributeValue av = null;
            try {
                av = getAttributeValue(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (av == null) {
                continue;
            }
            ySum[(int)((i * scale) - delta)] += av.getMathExpection();
        }

        for (int i = 1; i < ySum.length - 1; i++) {
            if (ySum[i] > ySum[i - 1] && ySum[i] > ySum[i + 1]) {
                res.add(new Extremum((i + delta) / scale, ySum[i]));
            }
        }

        return res;
    }

    public List<Segment> split(double x) {
        if (x == actualMinValue || x == actualMaxValue) {
            return null;
        }
        List<Segment> subSegments = new ArrayList<Segment>();
        subSegments.add(new Segment(actualMinValue, (int)x, minValue, maxValue));
        subSegments.add(new Segment((int)x + 1, actualMaxValue, minValue, maxValue));
        for (AttributeValue av : entries) {
            if (av == null) {
                continue;
            }
            if (av.getAttributeValue() >= actualMinValue && av.getAttributeValue() <= (int)x) {
                final Segment s = subSegments.get(0);
                final int idx = s.getAvIdx(av.attributeValue);
                if (s.entries[idx] != null) {
                    System.out.println("Something wrong!");
                }
                s.entries[s.getAvIdx(av.attributeValue)] = av;
                s.attributeCount++;
                s.checkAttributesValue();
                s.visitCount += av.getVisitCount();
            } else if (av.getAttributeValue() > (int)x && av.getAttributeValue() <= actualMaxValue) {
                final Segment s = subSegments.get(1);
                final int idx = s.getAvIdx(av.attributeValue);
                if (s.entries[idx] != null) {
                    System.out.println("Something wrong!");
                }
                s.entries[idx] = av;
                s.attributeCount++;
                s.checkAttributesValue();
                s.visitCount += av.getVisitCount();
            } else {
                throw new RuntimeException("Something wrong");
            }
        }
        if (subSegments.get(0).getAttributeValuesCount() + subSegments.get(1).getAttributeValuesCount() !=
                getAttributeValuesCount()) {
            System.out.println("AAAAAAAAa");
        }

        return subSegments;
    }

    public List<Segment> split() {
        if (visitCount == 0) {
            return split((min(actualMaxValue, maxValue) - max(actualMinValue, minValue)) / 2 + max(actualMinValue, minValue));
        }
        List<Extremum> extremums = getExtremums();
        Extremum minExtremum = new Extremum(0, Integer.MAX_VALUE);
        Extremum maxExtremum = new Extremum(0, Integer.MIN_VALUE);

        double minX = Integer.MAX_VALUE;
        for (Extremum e : extremums) {
            if (e.y < minExtremum.y && e.x > max(actualMinValue, minValue) + 2) {
                minExtremum = e;
                if (e.x < minX) {
                    minX = e.x;
                }
            }

            if (e.y > maxExtremum.y && e.x < min(actualMaxValue, maxValue) - 2) {
                maxExtremum = e;
                if (e.x < minX) {
                    minX = e.x;
                }
            }
        }

        final double x;
        if (extremums.size() > 2) {
            x = Math.abs((minExtremum.x - maxExtremum.x) / 2) + minX;
        } else if (extremums.size() == 1 && minX != Integer.MAX_VALUE &&
                minX < min(actualMaxValue, maxValue) - 2 && minX > max(actualMinValue, minValue) + 2) {
            x = minX;
        } else {
            x = (min(actualMaxValue, maxValue) - max(actualMinValue, minValue)) / 2 + max(actualMinValue, minValue);
        }
        return split(x);
    }

    public int getVisitCount() {
        return visitCount;
    }

    public double getPlotnost() {
        int visitedAvCount = 0;
        for (int i = actualMinValue; i <= actualMaxValue; i++) {
            if (getAttributeValue(i) != null) {
                visitedAvCount++;
            }
        }

        return visitedAvCount / (double)(actualMaxValue - actualMinValue + 1);
    }

    public void setEntries(AttributeValue[] entries) {
        this.entries = new AttributeValue[entries.length];
        attributeCount = 0;
        visitCount = 0;
        for (int i = 0; i < entries.length; i++) {
            AttributeValue av = entries[i];
            this.entries[i] = entries[i];
            if (av != null) {
                if (av.getAttributeValue() < actualMinValue) {
                    actualMinValue = av.getAttributeValue();
                } else if (av.getAttributeValue() > actualMaxValue) {
                    actualMaxValue = av.getAttributeValue();
                }
                attributeCount++;
                checkAttributesValue();
                visitCount += av.getVisitCount();
            }
        }
    }

    public int getLength() {
        return actualMaxValue - actualMinValue + 1;
    }

    public Interval getGFInteval(double percentage) {
        int[] gfCounts = new int[LXXConstants.MAX_GUESS_FACTOR * 2 + 1];

        int totalGFCounts = 0;
        for (AttributeValue av : entries) {
            if (av != null) {
                int[] avGFCounts = av.getGuessFactors();
                for (int i = 0; i < avGFCounts.length; i++) {
                    gfCounts[i] += avGFCounts[i];
                    totalGFCounts += avGFCounts[i];
                }
            }
        }

        int intervalGFCounts = totalGFCounts;
        int a = 0;
        int b = gfCounts.length - 1;
        while (intervalGFCounts >= totalGFCounts * percentage && a < b - 1) {
            if (gfCounts[a] < gfCounts[b]) {
                intervalGFCounts -= gfCounts[a];
                a++;

                if (gfCounts[a] == gfCounts[b]) {
                    intervalGFCounts -= gfCounts[b];
                    b--;
                }
            } else {
                intervalGFCounts -= gfCounts[b];
                b--;

                if (gfCounts[a] == gfCounts[b]) {
                    intervalGFCounts -= gfCounts[a];
                    a++;
                }
            }
        }

        return new Interval(a - LXXConstants.MAX_GUESS_FACTOR, b - LXXConstants.MAX_GUESS_FACTOR);
    }

    public int getAttributeValuesCount() {
        return attributeCount;
    }

    public int getActualMinValue() {
        return actualMinValue;
    }

    public int getActualMaxValue() {
        return actualMaxValue;
    }

    public void checkAttributesValue() {
        int actualAVCount = 0;
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] != null) {
                if (entries[i].getAttributeValue() != i + minValue) {
                    throw new RuntimeException("Something wrong!");
                }
                actualAVCount++;
            }
        }

        if (actualAVCount != attributeCount) {
            throw new RuntimeException("Attribute counter broken!");
        }
    }

}
