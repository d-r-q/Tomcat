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
    public final IntervalDouble maxRange;
    public final AttributeValueExtractor extractor;
    public final int id;

    public final IntervalDouble actualRange = new IntervalDouble(Integer.MAX_VALUE, Integer.MIN_VALUE);

    public Attribute(String name, double minValue, double maxValue, AttributeValueExtractor extractor) {
        this.name = name;
        this.maxRange = new IntervalDouble(minValue, maxValue);
        this.extractor = extractor;
        this.id = idSequence++;
    }

    public AttributeValueExtractor getExtractor() {
        return extractor;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return name;
    }

    public Interval getRoundedRange() {
        return new Interval((int) round(maxRange.a), (int) round(maxRange.b));
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
