/**
 * $Id$
 *
 * Copyright (c) 2009 Zodiac Interactive. All Rights Reserved.
 */
package lxx.targeting.mg4;

import lxx.autosegmentation.AttributeFactory;
import lxx.autosegmentation.model.Attribute;
import lxx.autosegmentation.model.FireSituation;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

public class Pattern {

    private final List<AttributeValuesRange> ranges = new LinkedList<AttributeValuesRange>();

    private final AttributeFactory attributeFactory;

    private int fsCount = 0;

    public Pattern(AttributeFactory attributeFactory) {
        this.attributeFactory = attributeFactory;
    }

    public void addPredicat(FireSituation fs) {
        enusreRanges();

        for (AttributeValuesRange range : ranges) {
            range.addVisit(fs.getAttributeValue(range.getAttribute()));
        }
        fsCount++;
    }

    private void enusreRanges() {
        if (ranges.size() == 0) {
            for (Attribute a : attributeFactory.getAttributes()) {
                ranges.add(new AttributeValuesRange(a));
            }
        }
    }

    public List<AttributeValuesRange> getRanges() {
        return ranges;
    }

    public int getFsCount() {
        return fsCount;
    }

    public double match(FireSituation fs) {
        double res = 0;

        for (AttributeValuesRange range : ranges) {
            int attrValue = fs.getAttributeValue(range.getAttribute());
            double visitCount = range.getVisitCount(attrValue);
            if (visitCount == 0) {
                return -1;
            }
            final double avgVisits = ((double)range.getVisits() / range.getAttrsCount());
            //if (avgVisits < range.maxVisitCount * 0.75)
            {
                if (range.maxVisitCount > 0) {
                    res += visitCount / (range.getAvgVisitCount())/* * (range.maxVisitCount / range.getAvgVisitCount()) / ((double)range.getAttrsCount() / range.getAttribute().getActualRange())*/;
                    if (Double.isNaN(res)) {
                        throw new RuntimeException("Something wrong");
                    }
                }
            }
        }

        return res/* / ranges.size()*/;
    }

}
