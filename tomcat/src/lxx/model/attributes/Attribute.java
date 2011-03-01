/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.model.attributes;

import lxx.model.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.Interval;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class Attribute {

    private static int idSequence = 0;

    private final String name;
    private final int minValue;
    private final int maxValue;
    private final AttributeValueExtractor extractor;
    private final int id;

    private int actualMin = Integer.MAX_VALUE;
    private int actualMax = Integer.MIN_VALUE;

    public Attribute(String name, int minValue, int maxValue, AttributeValueExtractor extractor) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.extractor = extractor;
        this.id = idSequence++;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public AttributeValueExtractor getExtractor() {
        return extractor;
    }

    public int getId() {
        return id;
    }

    public int getActualMin() {
        return actualMin;
    }

    public void setActualMin(int actualMin) {
        if (actualMin < minValue) {
            throw new RuntimeException(this + ": " + actualMin + "/" + minValue);
        }
        this.actualMin = actualMin;
    }

    public int getActualMax() {
        return actualMax;
    }

    public void setActualMax(int actualMax) {
        if (actualMax > maxValue) {
            throw new RuntimeException(this + ": " + actualMax + "/" + maxValue);
        }
        this.actualMax = actualMax;
    }

    public String toString() {
        return name;
    }

    public int getActualRange() {
        return actualMax - actualMin + 1;
    }

    public Interval getRange() {
        return new Interval(minValue, maxValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attribute attribute = (Attribute) o;

        if (id != attribute.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
