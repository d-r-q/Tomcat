package lxx.targeting.mg4;

import java.awt.*;
import java.util.*;

/**
 * User: jdev
 * Date: 13.05.2010
 */
public class DVCanvas extends Canvas {

    private Pattern predicat;

    public void paint(Graphics g) {
        if (predicat == null) {
            return;
        }
        System.out.println(predicat.getFsCount());
        java.util.List<AttributeValuesRange> ranges = predicat.getRanges();
        int height = getHeight();
        int width = getWidth();
        int rangeHeight = height / ranges.size();
        int idx = 0;
        g.drawString(Integer.toString(predicat.getFsCount()), 10, rangeHeight * 2 - 10);
        for (AttributeValuesRange range : ranges) {
            g.setColor(Color.BLACK);
            g.drawLine(0, rangeHeight * idx, width, rangeHeight * idx);
            int maxVisitCount = 0;
            final SortedMap<Integer, AttributeVisitCount> attrValues = null;
            for (Integer attrValue : attrValues.keySet()) {
                if (attrValues.get(attrValue).visitCount > maxVisitCount) {
                    maxVisitCount = attrValues.get(attrValue).visitCount;
                }
            }
            int barWidth = width / (range.getAttribute().getMaxValue() - range.getAttribute().getMinValue());
            int barIdx = 0;
            g.drawString(range.getAttribute().getName(), 30, rangeHeight * (idx + 1) - 15);
            if (idx % 2 == 0) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.RED);
            }
            for (int attrValue = range.getAttribute().getMinValue(); attrValue <= range.getAttribute().getMaxValue(); attrValue++) {
                final AttributeVisitCount count = attrValues.get(attrValue);
                int barHeight = count == null ? 0 : (int) (rangeHeight * ((double) count.visitCount / maxVisitCount));
                g.fillRect(barWidth * barIdx, rangeHeight * idx, barWidth, barHeight - 1);
                g.setColor(Color.BLACK);
                g.drawRect(barWidth * barIdx, rangeHeight * idx, barWidth, barHeight - 1);
                if (idx % 2 == 0) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.RED);
                }
                /*if (barIdx % 2 == 0) {
                    g.drawString(attrValue + "", barWidth * barIdx + 2, rangeHeight * (idx + 1) - 2);
                }*/
                barIdx++;
            }
            idx++;
        }
    }

    public void setPredicat(Pattern predicat) {
        this.predicat = predicat;
    }
}
