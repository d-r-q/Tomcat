package kc.serpent.utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class KUtils {
    public static Rectangle2D makeField(double width, double height, double margin) {
        return new Rectangle2D.Double(margin, margin, width - (margin * 2), height - (margin * 2));
    }

    public static Point2D.Double projectMotion(Point2D source, double heading, double distance) {
        return new Point2D.Double(source.getX() + (Math.sin(heading) * distance), source.getY() + (Math.cos(heading) * distance));
    }

    public static double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    public static double bulletSpeed(double power) {
        return 20 - (3 * power);
    }

    public static double maxEscapeAngle(double speed) {
        return Math.asin(8 / speed);
    }

    public static double toGF(int factor, int totalFactors) {
        return (((double) (factor) / ((double) (totalFactors - 1))) * 2) - 1;
    }

    public static int toFactor(double GF, int middleFactor, int totalFactors) {
        return minMax((int) (Math.round((GF + 1) * middleFactor)), 0, totalFactors - 1);
    }

    public static double botWidthAngle(double width, double distance) {
        return Math.atan(width / distance);
    }

    public static double windowFactor(double width, double distance, double maxEscapeAngle) {
        return botWidthAngle(width, distance) / maxEscapeAngle;
    }

    public static int index(double v, double[] slices) {
        for (int i = 0; i < slices.length; i++) {
            if (v < slices[i]) {
                return i;
            }
        }
        return slices.length;
    }

    public static boolean inBounds(double v, double[] bounds) {
        return v >= bounds[0] && v <= bounds[1];
    }

    public static boolean inBounds(double v, double tolerance, double[] bounds) {
        return v >= bounds[0] - tolerance && v <= bounds[1] + tolerance;
    }

    public static int minMax(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public static double minMax(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int sign(double v) {
        return v > 0 ? 1 : -1;
    }

    public static double frthrt(double v) {
        return Math.sqrt(Math.sqrt(v));
    }

    public static double sqr(double v) {
        return (v * v);
    }

    public static double cube(double v) {
        return (v * v * v);
    }

    public static double fourth(double v) {
        return sqr(sqr(v));
    }

    public static double sixth(double v) {
        return sqr(cube(v));
    }

    public static double eighth(double v) {
        return sqr(fourth(v));
    }

    public static double sixteenth(double v) {
        return fourth(fourth(v));
    }

    public static double twentyfourth(double v) {
        return eighth(cube(v));
    }

    public static double thirtysecond(double v) {
        return eighth(fourth(v));
    }

    public static double quadratic(double x, double a, double b, double c) {
        return (a * sqr(x)) + (b * x) + c;
    }

    public static double cubic(double x, double a, double b, double c, double d) {
        return (a * cube(x)) + (b * sqr(x)) + (c * x) + d;
    }

    public static double quartic(double x, double a, double b, double c, double d, double e) {
        return (a * fourth(x)) + (b * cube(x)) + (c * sqr(x)) + (d * x) + e;
    }
}
