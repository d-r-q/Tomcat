package lxx.utils;

import java.awt.*;

import static java.lang.Math.round;
import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 19.06.11
 */
public class ColorSet {

    private final double minValue;
    private final double maxValue;
    private final Color minValueColor;
    private final Color maxValueColor;

    public ColorSet(double minValue, double maxValue, Color minValueColor, Color maxValueColor) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minValueColor = minValueColor;
        this.maxValueColor = maxValueColor;
    }

    public Color getColor(double value) {
        final double normalizedValue = (value - minValue) / (maxValue - minValue);
        return new Color(
                (int)round(minValueColor.getRed() + (maxValueColor.getRed() - minValueColor.getRed()) * normalizedValue),
                (int)round(minValueColor.getGreen() + (maxValueColor.getGreen() - minValueColor.getGreen()) * normalizedValue),
                (int)round(minValueColor.getBlue() + (maxValueColor.getBlue() - minValueColor.getBlue()) * normalizedValue));
    }

}
