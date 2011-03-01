package lxx.autosegmentation.test;

import lxx.autosegmentation.model.Extremum;
import lxx.autosegmentation.model.Segment;
import lxx.autosegmentation.model.Segmentation;

import java.awt.*;
import java.text.DecimalFormat;
import static java.lang.Math.abs;
import java.util.*;

/**
 * User: jdev
 * Date: 18.02.2010
 */
public class SegmentationView extends Canvas {

    private Segmentation segmentation;
    private DecimalFormat format = new DecimalFormat();

    public SegmentationView() {
        setBackground(Color.WHITE);
        format.setMaximumFractionDigits(1);
    }

    public void paint(Graphics g) {
        final int minAttrValue = segmentation.getMinAttrValue();
        final int maxAttrValue = segmentation.getMaxAttrValue();
        double minME = segmentation.getMinGuessFactor();
        double maxME = segmentation.getMaxGuessFactor();

        /*for (AttributeValue av : segmentation.getAttributeValues()) {
            double me = av.getMathExpection();
            if (me < minME) {
                minME = me;
            }

            if (me > maxME) {
                maxME = me;
            }
        }*/

        /*double scaleX = (double)getWidth() / (maxAttrValue - minAttrValue);
        double scaleY = (double)getHeight() / (maxME - minME);*/
        int pntFreq = 20;
        if (maxAttrValue - minAttrValue < 20) {
            pntFreq = 2;
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int step = (maxAttrValue - minAttrValue) / 800;
        if (step == 0) {
            step = 1;
        }
        int delta = step / 5;
        for (int attributeValue = minAttrValue + delta * 0; attributeValue < segmentation.getMaxAttrValue() - 1; attributeValue += step) {
            final int avX1 = (getWidth() * (attributeValue - minAttrValue) / (maxAttrValue - minAttrValue));
            final Double me1 = segmentation.getMathExpection(attributeValue);
            if (me1 == null) {
                continue;
            }
            final int avY1 = (int) (200 * (int) (maxME - me1) / (maxME - minME)) + (getHeight() / 2 - 100);
            g.setColor(Color.ORANGE);
            //g.fillOval(avX1 - 1, avY1 - 1, 2, 2);

            int nextAttributeValue = attributeValue + step;
            final int avX2 = (getWidth() * (nextAttributeValue - minAttrValue) / (maxAttrValue - minAttrValue));
            final Double me2 = segmentation.getMathExpection(nextAttributeValue);
            if (me2 == null) {
                continue;
            }
            final int avY2 = (int) (200 * (int) (maxME - me2) / (maxME - minME)) + (getHeight() / 2 - 100);
            // g.setColor(new Color(200, 200, 127));
            g.drawLine(avX1, avY1, avX2, avY2);
            g.setColor(Color.BLACK);
            //g.fillOval(avX2 - 2, avY2 - 2, 4, 4);

        }

        for (int attributeValue = minAttrValue + delta * 0; attributeValue < segmentation.getMaxAttrValue() - 1; attributeValue += step) {
            final int avX1 = (getWidth() * (attributeValue - minAttrValue) / (maxAttrValue - minAttrValue));
            final Double me1 = segmentation.getAvgGuessFactor(attributeValue);
            if (me1 == null) {
                continue;
            }
            final int avY1 = (int) (200 * (int) (maxME - me1) / (maxME - minME)) + (getHeight() / 2 - 100);
            g.setColor(Color.GREEN);
            //g.fillOval(avX1 - 1, avY1 - 1, 2, 2);

            int nextAttributeValue = attributeValue + step;
            final int avX2 = (getWidth() * (nextAttributeValue - minAttrValue) / (maxAttrValue - minAttrValue));
            final Double me2 = segmentation.getAvgGuessFactor(nextAttributeValue);
            if (me2 == null) {
                continue;
            }
            final int avY2 = (int) (200 * (int) (maxME - me2) / (maxME - minME)) + (getHeight() / 2 - 100);
            //g.setColor(new Color(127, 200, 127));
            g.drawLine(avX1, avY1, avX2, avY2);
            g.setColor(Color.BLACK);
            //g.fillOval(avX2 - 2, avY2 - 2, 4, 4);

        }

        for (int attributeValue = minAttrValue; attributeValue < segmentation.getMaxAttrValue() - 1; attributeValue += 1) {
            final int avX1 = (getWidth() * (attributeValue - minAttrValue) / (maxAttrValue - minAttrValue));
            final Double me1 = segmentation.getSrKVOtkl(attributeValue);
            if (me1 == null) {
                continue;
            }
            final int avY1 = (int) (200 * (int) (maxME - me1) / (maxME - minME)) + (getHeight() / 2 - 100);
            g.setColor(Color.RED);
            //g.fillOval(avX1 - 1, avY1 - 1, 2, 2);

            int nextAttributeValue = attributeValue + 1;
            final int avX2 = (getWidth() * (nextAttributeValue - minAttrValue) / (maxAttrValue - minAttrValue));
            final Double me2 = segmentation.getSrKVOtkl(nextAttributeValue);
            if (me2 == null) {
                continue;
            }
            final int avY2 = (int) (200 * (int) (maxME - me2) / (maxME - minME)) + (getHeight() / 2 - 100);
            //g.setColor(new Color(200, 127, 127));
            g.drawLine(avX1, avY1, avX2, avY2);
            g.setColor(Color.BLACK);
            //g.fillOval(avX2 - 2, avY2 - 2, 4, 4);

            if (nextAttributeValue % 50 == 0) {
                if (avY1 < 0) {
                    g.drawString("(" + nextAttributeValue + ", " + format.format((me2 * 100) / 100D) + ")", avX2, avY2 - g.getFontMetrics().getHeight());
                } else {
                    g.drawString("(" + nextAttributeValue + ", " + format.format((me2 * 100) / 100D) + ")", avX2, avY2);
                }
            }

        }

        g.setColor(Color.LIGHT_GRAY);
        final java.util.List<Extremum> extremums = segmentation.getExtremums();
        Collections.sort(extremums, new Comparator<Extremum>() {

            public int compare(Extremum o1, Extremum o2) {
                return (int) (abs(o2.y) - abs(o1.y));
            }
        });
        for (int i = 0; i < extremums.size(); i++) {
            final int x = (int) (getWidth() * (extremums.get(i).x - minAttrValue) / (maxAttrValue - minAttrValue));
            g.drawLine(x, 0, x, getHeight());
        }

        g.setColor(Color.BLUE);
        for (Segment s : segmentation.getSegments()) {
            final int avX1 = (getWidth() * (s.getActualMinValue() - minAttrValue) / (maxAttrValue - minAttrValue));
            final int avX2 = (getWidth() * (s.getActualMaxValue() - minAttrValue) / (maxAttrValue - minAttrValue));
            final Double me1 = s.getMathExpection();
            if (me1 == null) {
                continue;
            }
            final int avY = (int) (200 * (int) (maxME - me1) / (maxME - minME)) + (getHeight() / 2 - 100);
            g.drawLine(avX1, avY, avX2, avY);
            final String freq = format.format((segmentation.getFreq(s) * 100) / 100D);
            final String mathExpection = format.format(((abs(s.getMathExpection()) * segmentation.getFreq(s) * s.getAttributeValues().length) * 100) / 100D);
            final String plotnost = format.format(s.getPlotnost() * 100);
            g.drawString(freq, avX1 + 10, avY + 10);
            g.drawString(mathExpection, avX1 + 10, avY - 10);
            g.drawString(plotnost, avX1 + 10, avY - 20);
        }

        g.drawString(format.format(segmentation.getSegmentsMESum()), 100, 100);

        g.setColor(Color.BLACK);
        g.drawLine(0, (int) (getHeight() * (maxME - minME) / 2 / (maxME - minME)),
                getWidth(), (int) (getHeight() * (maxME - minME) / 2 / (maxME - minME)));

        g.drawLine((getWidth() * (maxAttrValue - minAttrValue) / 2 / (maxAttrValue - minAttrValue)), 0,
                (getWidth() * (maxAttrValue - minAttrValue) / 2 / (maxAttrValue - minAttrValue)), getHeight());

        g.setColor(Color.YELLOW);
        g.drawLine(0, (int) (getHeight() * (maxME - segmentation.getMathExpection()) / (maxME - minME)),
                getWidth(), (int) (getHeight() * (maxME - segmentation.getMathExpection()) / (maxME - minME)));
    }


    public void setSegmentation(Segmentation segmentation) {
        this.segmentation = segmentation;
    }
}
