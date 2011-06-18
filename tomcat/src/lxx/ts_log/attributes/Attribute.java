/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.ts_log.attributes;

import lxx.ts_log.attributes.attribute_extractors.AttributeValueExtractor;
import lxx.utils.Interval;
import lxx.utils.IntervalDouble;

import static java.lang.StrictMath.round;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class Attribute {

    private static int idSequence = 0;

    private final String name;
    private final double minValue;
    private final double maxValue;
    private final AttributeValueExtractor extractor;
    private final int id;

    private double actualMin = Integer.MAX_VALUE;
    private double actualMax = Integer.MIN_VALUE;

    public Attribute(String name, double minValue, double maxValue, AttributeValueExtractor extractor) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.extractor = extractor;
        this.id = idSequence++;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public AttributeValueExtractor getExtractor() {
        return extractor;
    }

    public int getId() {
        return id;
    }

    public double getActualMin() {
        return actualMin;
    }

    public void setActualMin(double actualMin) {
        if (actualMin < minValue) {
            throw new RuntimeException(this + ": " + actualMin + "/" + minValue);
        }
        this.actualMin = actualMin;
    }

    public double getActualMax() {
        return actualMax;
    }

    public void setActualMax(double actualMax) {
        if (actualMax > maxValue) {
            throw new RuntimeException(this + ": " + actualMax + "/" + maxValue);
        }
        this.actualMax = actualMax;
    }

    public String toString() {
        return name;
    }

    public double getActualRange() {
        return actualMax - actualMin + 1;
    }

    public IntervalDouble getRange() {
        return new IntervalDouble(minValue, maxValue);
    }

    public Interval getRoundedRange() {
        return new Interval((int) round(minValue), (int) round(maxValue));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attribute attribute = (Attribute) o;

        if (id != attribute.id) return false;

        return true;
    }

    public int hashCode() {
        return id;
    }
}
