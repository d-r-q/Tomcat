package lxx.autosegmentation.model;

import lxx.UltraMarine;
import lxx.autosegmentation.model.Attribute;
import lxx.autosegmentation.attribute_extractors.AttributeValueExtractor;
import lxx.targeting.Target;
import lxx.targeting.bullets.BulletManager;
import lxx.utils.LXXConstants;

import java.awt.*;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: jdev
 * Date: 18.02.2010
 */
public class Segmentation {

    private final List<Segment> segments = new ArrayList<Segment>();

    private final String attributeName;
    private final AttributeValueExtractor extractor;

    private final int minAttributeValue;
    private final int maxAttributeValue;

    private int minGuessFactor = Integer.MAX_VALUE;
    private int maxGuessFactor = Integer.MIN_VALUE;
    private Attribute attribute;

    private double segmentsIntersection = 0;

    public Segmentation(Attribute attribute) {
        this.attributeName = attribute.getName();
        this.extractor = attribute.getExtractor();
        this.minAttributeValue = attribute.getMinValue();
        this.maxAttributeValue = attribute.getMaxValue();
        this.attribute = attribute;

        segments.add(new Segment(attribute.getMinValue(), attribute.getMaxValue()));
    }

    public void addEntry(int attributeValue, int guessFactor) {
        if (attributeValue < minAttributeValue || attributeValue > maxAttributeValue) {
            //System.out.println("Entry ignored: " + attributeName + " = " + attributeValue + "(" + minAttributeValue + " - " + maxAttributeValue + ")");
            return;
        }
        final Segment segment = getSegment(attributeValue);
        segment.addEntry(attributeValue, guessFactor);

        if (guessFactor < minGuessFactor) {
            minGuessFactor = guessFactor;
        }
        if (guessFactor > maxGuessFactor) {
            maxGuessFactor = guessFactor;
        }
    }

    public int getSegmentIdx(int attributeValue) {
        if (attributeValue < segments.get(0).getActualMinValue()) {
            return 0;
        }

        if (attributeValue > segments.get(segments.size() - 1).getActualMaxValue()) {
            return segments.size() - 1;
        }

        for (int i = 0; i < segments.size(); i++) {
            Segment s = segments.get(i);
            if (s.getActualMinValue() <= attributeValue && s.getActualMaxValue() >= attributeValue) {
                return i;
            }
        }
        return -1;
    }

    public int getMinAttrValue() {
        return segments.get(0).getActualMinValue();
    }

    public int getMaxAttrValue() {
        return segments.get(segments.size() - 1).getActualMaxValue();
    }

    public Double getMathExpection(int attributeValue) {
        final AttributeValue av = getAttributeValue(attributeValue);
        if (av == null) {
            return null;
        }

        return av.getMathExpection();
    }


    public Integer getMediana(int attributeValue) {
        final AttributeValue av = getAttributeValue(attributeValue);
        if (av == null) {
            return null;
        }

        return av.getMediana();
    }


    public Double getAvgGuessFactor(int attributeValue) {
        final AttributeValue av = getAttributeValue(attributeValue);
        if (av == null) {
            return null;
        }

        return av.getAvgGuessFactor();
    }

    public AttributeValue getAttributeValue(int attributeValue) {
        final Segment segment = getSegment(attributeValue);
        if (attributeValue < segment.getActualMinValue() || attributeValue > segment.getActualMaxValue()) {
            return null;
        }
        return segment.getAttributeValue(attributeValue);
    }

    public Segment getSegment(int attributeValue) {
        if (attributeValue < segments.get(0).getActualMinValue()) {
            return segments.get(0);
        }

        if (attributeValue > segments.get(segments.size() - 1).getActualMaxValue()) {
            return segments.get(segments.size() - 1);
        }

        for (Segment s : segments) {
            if (s.getActualMinValue() <= attributeValue && s.getActualMaxValue() >= attributeValue) {
                return s;
            }
        }
        return null;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public int getMinGuessFactor() {
        return minGuessFactor;
    }

    public int getMaxGuessFactor() {
        return maxGuessFactor;
    }

    public List<AttributeValue> getAttributeValues() {
        List<AttributeValue> res = new LinkedList<AttributeValue>();
        for (Segment s : segments) {
            for (AttributeValue av : s.getAttributeValues()) {
                if (av != null) {
                    res.add(av);
                }
            }
        }
        return res;
    }

    public AttributeValue[] getAllEntries() {
        AttributeValue[] allEntries = new AttributeValue[maxAttributeValue - minAttributeValue + 1];

        for (Segment s : segments) {
            AttributeValue[] attrs = s.getAttributeValues();
            if (attrs.length != allEntries.length) {
                throw new RuntimeException("Something wrong");
            }

            for (int i = 0; i < allEntries.length; i++) {
                if (attrs[i] != null) {
                    if (allEntries[i] != null) {
                        throw new RuntimeException("Something wrong");
                    }

                    allEntries[i] = attrs[i];
                }
            }
        }

        return allEntries;
    }

    public double getMathExpection() {
        double res = 0;

        for (Segment s : segments) {
            res += s.getMathExpection();
        }

        return res / segments.size();
    }

    public double getSrKVOtkl() {
        double res = 0;

        for (Segment s : segments) {
            res += s.getSrKVOtkl();
        }

        return res / segments.size();
    }

    public double getSegmentSrKVOtkl(int attributeValue) {
        return getSegment(attributeValue).getSrKVOtkl();
    }

    public Double getSrKVOtkl(int attributeValue) {
        AttributeValue av = getAttributeValue(attributeValue);
        if (av == null) {
            return null;
        }

        return av.getSrKVOtkl();
    }

    public Double getMESum() {
        double res = 0;

        for (Segment s : segments) {
            res += s.getMESum();
        }

        return res / segments.size();
    }

    public List<Extremum> getExtremums() {
        List<Extremum> res = new ArrayList<Extremum>();

        for (Segment s : segments) {
            res.addAll(s.getExtremums());
        }

        return res;
    }

    public void splitIntoSegments() {
        int segmentationLength = segments.get(0).getLength();
        List<Segment> segmentsStack = new ArrayList<Segment>();
        segmentsStack.add(segments.get(0));

        while (segmentsStack.size() > 0 && segments.size() < 10) {
            Segment s = segmentsStack.remove(0);
            List<Segment> newSegemnts = s.split();
            if (newSegemnts.get(0).getAttributeValuesCount() + newSegemnts.get(1).getAttributeValuesCount() !=
                    s.getAttributeValuesCount()) {
                System.out.println("AAAAAAAAa");
            }
            if (newSegemnts != null && abs(newSegemnts.get(0).getMathExpection() - newSegemnts.get(1).getMathExpection()) >
                    min(abs(newSegemnts.get(0).getMathExpection()), abs(newSegemnts.get(1).getMathExpection()) * 0.08) &&
                    s.getVisitCount() > 0 && s.getAttributeValuesCount() > 0) {
                int idx = segments.indexOf(s);
                segments.remove(idx);
                segments.addAll(idx, newSegemnts);
                if (newSegemnts.get(0).getLength() >= 4 && segments.get(0).getLength() > segmentationLength * 0.1) {
                    segmentsStack.add(newSegemnts.get(0));
                }
                if (newSegemnts.get(1).getLength() >= 4 && segments.get(1).getLength() > segmentationLength * 0.1) {
                    segmentsStack.add(newSegemnts.get(1));
                }
            }
        }

        segmentsIntersection = 0;
        if (segments.size() > 1) {
            Segment[] mvs = getMostVisitedSegments(2);
            final Interval i1 = mvs[0].getGFInteval(0.7);
            final Interval i2 = mvs[1].getGFInteval(0.7);
            if (mvs[0].getLength() == 0 || mvs[0].getVisitCount() == 0 || mvs[1].getLength() == 0 || mvs[1].getVisitCount() == 0) {
                segmentsIntersection = Integer.MAX_VALUE;
            } else {
                segmentsIntersection += i1.getIntersection(i2) /
                        (min((double) mvs[0].getVisitCount() / mvs[1].getVisitCount(), (double) mvs[1].getVisitCount() / mvs[0].getVisitCount()));
            }
        } else {
            segmentsIntersection = Integer.MAX_VALUE;
        }

    }

    public List<Segment> getSegments() {
        return segments;
    }

    public Double getSegmentsMESum() {
        double res = 0;

        for (Segment s : segments) {
            res += abs(s.getMESum())/* * ((s.getMaxValue() - s.getMinValue()) / attributeLength)*/ * getFreq(s);
        }

        return res;
    }

    public double getFreq(Segment segment) {
        int totalVisitCount = 0;
        for (Segment s : segments) {
            totalVisitCount += s.getVisitCount();
        }

        if (totalVisitCount == 0) {
            return 0;
        }

        return (double) segment.getVisitCount() / totalVisitCount;
    }

    public void resegment() {
        if (getVisitCount() == 0) {
            return;
        }
        if (segments.size() == 1) {
            splitIntoSegments();
            return;
        }

        Segment root = new Segment(minAttributeValue, maxAttributeValue);
        root.setEntries(getAllEntries());

        segments.clear();
        segments.add(root);

        splitIntoSegments();
    }

    public double getSegmentME(Integer attributeValue) {
        return getSegment(attributeValue).getMathExpection();
    }

    public double getPlotnost() {
        double res = 0;

        for (Segment s : segments) {
            res += s.getPlotnost();
        }

        return res / segments.size();
    }

    public int getVisitCount() {
        int res = 0;

        for (Segment s : segments) {
            res += s.getVisitCount();
        }

        return res;
    }

    public AttributeValueExtractor getExtractor() {
        return extractor;
    }

    public boolean contains(Integer attrValue) {
        return attrValue >= minAttributeValue && attrValue <= maxAttributeValue;
    }

    public int normalize(int base, int value) {
        return base + value;
    }

    public void paint(Graphics2D g, int width, int height, int baseX, int baseY, BulletManager bulletManager, UltraMarine um, Target t) {
        final int minAttrValue = segments.get(0).getActualMinValue();
        final int maxAttrValue = segments.get(segments.size() - 1).getActualMaxValue();
        final int centerGF = (maxGuessFactor - minGuessFactor) / 2;
        double minME = (centerGF - minGuessFactor) / 2;
        double maxME = (maxGuessFactor - centerGF) / 2;

        g.setColor(Color.GRAY);
        g.drawLine(normalize(baseX, 0), normalize(baseY, height / 2), normalize(baseX, width), normalize(baseY, height / 2));
        g.drawLine(normalize(baseX, width / 2), normalize(baseY, 0), normalize(baseX, width / 2), normalize(baseY, height));

        g.setColor(Color.WHITE);
        g.drawLine(normalize(baseX, 0), normalize(baseY, 0), normalize(baseX, 0), normalize(baseY, height));
        g.drawLine(normalize(baseX, 0), normalize(baseY, height), normalize(baseX, width), normalize(baseY, height));
        g.drawLine(normalize(baseX, width), normalize(baseY, height), normalize(baseX, width), normalize(baseY, 0));

        final Font font = new Font("Arial", Font.PLAIN, 10);
        g.setFont(font);
        g.drawString(attributeName, normalize(baseX, width + 2), normalize(baseY, height / 2));

        if (minAttrValue == maxAttrValue) {
            return;
        }

        int step = (maxAttrValue - minAttrValue) / (width / 3);
        if (step == 0) {
            step = 1;
        }
        g.setColor(Color.BLUE);
        for (int attributeValue = minAttrValue; attributeValue < getMaxAttrValue() - step; attributeValue += step) {
            final int avX1 = normalize(baseX, (width * (attributeValue - minAttrValue) / (maxAttrValue - minAttrValue)));
            final Double me1 = getMathExpection(attributeValue);
            if (me1 == null) {
                continue;
            }
            final int avY1 = normalize(baseY, (int) (height * (me1 - minGuessFactor) / (maxGuessFactor - minGuessFactor)));

            int nextAttributeValue = attributeValue + step;
            final int avX2 = normalize(baseX, (width * (nextAttributeValue - minAttrValue) / (maxAttrValue - minAttrValue)));
            final Double me2 = getMathExpection(nextAttributeValue);
            if (me2 == null) {
                continue;
            }
            final int avY2 = normalize(baseY, (int) (height * (me2 - minGuessFactor) / (maxGuessFactor - minGuessFactor)));

            g.drawLine(avX1, avY1, avX2, avY2);
        }

        final DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(2);

        int curAttrValue = extractor.getAttributeValue(t, um, bulletManager);
        for (Segment s : getSegments()) {
            if (curAttrValue >= s.getActualMinValue() && curAttrValue <= s.getActualMaxValue()) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.RED);
            }
            final int avX1 = normalize(baseX, (width * (s.getActualMinValue() - minAttrValue) / (maxAttrValue - minAttrValue)));
            final int avX2 = normalize(baseX, (width * (s.getActualMaxValue() - minAttrValue) / (maxAttrValue - minAttrValue)));
            final Double me1 = s.getMathExpection();
            if (me1 == null) {
                continue;
            }
            final int avY = normalize(baseY, (int) (height * (s.getMathExpection() - minGuessFactor) / (maxGuessFactor - minGuessFactor)));
            g.drawLine(avX1, avY, avX2, avY);
            //final String freq = format.format((getFreq(s) * 100) / 100D);
            //final String mathExpection = format.format(((abs(s.getMathExpection()) * getFreq(s) * s.getAttributeValues().length) * 100) / 100D);
            //final String plotnost = format.format(s.getPlotnost() * 100);
            //g.drawString("F = " + freq + ", ME = " + mathExpection + ", P = " + plotnost, avX1 + 10, avY - 20);
            //g.drawString(mathExpection, avX1 + 10, avY - 20);
        }

    }

    public double getSegmentsSrKvOtkl() {
        double res = 0;
        int segsCount = 0;
        for (Segment s : segments) {
            if (s.getVisitCount() == 0) {
                continue;
            }
            res += s.getSrKVOtkl();
            segsCount++;
        }
        if (segsCount == 0) {
            return LXXConstants.MAX_GUESS_FACTOR * 4;
        }
        return res / segsCount;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public double getSegmentsMEDiff() {
        if (segments.size() == 1) {
            return 0;
        }
        double res = 0;

        for (int i = 0; i < segments.size() - 1; i++) {
            res += abs(segments.get(i).getMathExpection() - segments.get(i + 1).getMathExpection()) * ((segments.get(i).getPlotnost() + segments.get(i + 1).getPlotnost()) / 2);
        }

        return res / (segments.size());
    }

    public double getSegmentsIntersection() {
        return segmentsIntersection;
    }

    public Segment[] getMostVisitedSegments(int sCount) {
        Segment[] res = new Segment[min(sCount, segments.size())];
        if (res.length == segments.size()) {
            segments.toArray(res);
            Arrays.sort(res, new Comparator<Segment>() {

                public int compare(Segment o1, Segment o2) {
                    return o2.getVisitCount() - o1.getVisitCount();
                }
            });
        }

        List<Segment> sortedSegments = new ArrayList<Segment>(segments);
        Collections.sort(sortedSegments, new Comparator<Segment>() {

            public int compare(Segment o1, Segment o2) {
                return o2.getVisitCount() - o1.getVisitCount();
            }
        });

        sortedSegments = sortedSegments.subList(0, sCount);
        sortedSegments.toArray(res);

        return res;
    }

}