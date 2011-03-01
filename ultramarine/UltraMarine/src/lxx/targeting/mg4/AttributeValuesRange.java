/**
 * $Id$
 *
 * Copyright (c) 2009 Zodiac Interactive. All Rights Reserved.
 */
package lxx.targeting.mg4;

import lxx.autosegmentation.model.Attribute;

import static java.lang.Math.min;

public class AttributeValuesRange {

    //private final SortedMap<Integer, AttributeVisitCount> attributeValues = new TreeMap<Integer, AttributeVisitCount>();
    private final Double[] visitCounts;

    private final Attribute attribute;
    private int visits = 0;
    private int attrsCount = 0;

    public double maxVisitCount = 0;
    private static final double dec = 0.1;

    public AttributeValuesRange(Attribute attribute) {
        this.attribute = attribute;
        visitCounts = new Double[attribute.getMaxValue() - attribute.getMinValue() + 1];
    }

    public void addVisit(int attributeValue) {
        if (getAttributeVisitCount(attributeValue) == null) {
            visitCounts[attributeValue - attribute.getMinValue()] = 0D;
            attrsCount++;
        }

        visitCounts[attributeValue - attribute.getMinValue()]++;
        if (attributeValue - attribute.getMinValue() - 1 >= 0 &&
                visitCounts[attributeValue - attribute.getMinValue() - 1] != null) {
            visitCounts[attributeValue - attribute.getMinValue() - 1] += 0.5;
        }
        if (attributeValue - attribute.getMinValue() + 1 < visitCounts.length &&
                visitCounts[attributeValue - attribute.getMinValue() + 1] != null) {
            visitCounts[attributeValue - attribute.getMinValue() + 1] += 0.5;
        }
        if (visitCounts[attributeValue - attribute.getMinValue()] > maxVisitCount) {
            maxVisitCount = visitCounts[attributeValue - attribute.getMinValue()];
        }
        double newMax = 0;
        /*for (int i = 0; i < visitCounts.length; i++) {
            if (visitCounts[attributeValue - attribute.getMinValue()] != null && visitCounts[attributeValue - attribute.getMinValue()] > 0.1) {
                visitCounts[attributeValue - attribute.getMinValue()] -= min(dec, visitCounts[attributeValue - attribute.getMinValue()]);
            }
            if (visitCounts[attributeValue - attribute.getMinValue()] > newMax) {
                newMax = visitCounts[attributeValue - attribute.getMinValue()];
            }
        }*/
        //maxVisitCount = newMax;
        visits++;
    }

    private Double getAttributeVisitCount(int attributeValue) {
        try {
        return visitCounts[attributeValue - attribute.getMinValue()];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public double getVisitCount(int attrValue) {
        final Double count = getAttributeVisitCount(attrValue);
        if (count == null) {
            return 0;
        }
        return count;
    }

    public int getVisits() {
        return visits;
    }

    public int getAttrsCount() {
        return attrsCount;
    }

    public Double[] getVisitCounts() {
        return visitCounts;
    }

    public Integer[] getAttrValues() {
        Integer[] attrValues = new Integer[attrsCount];

        int j = 0;
        for (int i = 0; i < visitCounts.length; i++) {
            if (visitCounts[i] != null) {
                attrValues[j++] = i + attribute.getMinValue();
            }
        }

        return attrValues;
    }

    public double getAvgVisitCount() {
        int j = 0;
        for (int i = 0; i < visitCounts.length; i++) {
            if (visitCounts[i] != null && visitCounts[i] > 0) {
                j++;
            }
        }
        return visits / j;
    }
}
