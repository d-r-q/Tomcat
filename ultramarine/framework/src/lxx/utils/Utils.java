/**
 * $Id$
 *
 * Copyright (c) 2009 Zodiac Interactive. All Rights Reserved.
 */
package lxx.utils;

import static java.lang.Math.*;

public class Utils {

    public static double angle(double baseX, double baseY, double x, double y) {
        // todo: use quick math get ange
        double theta = QuickMath.asin((y - baseY) / LXXPoint.distance(x, y, baseX, baseY)) - Math.PI / 2;
        if (x >= baseX && y >= baseY) {
            theta = abs(theta);
        } else if (x >= baseX && y <= baseY) {
            theta = abs(theta);
        } else if (x <= baseX && y <= baseY) {
            theta = Math.PI * 2 + theta;
        } else {
            theta = Math.PI * 2 + theta;
        }
        return theta;
    }

    public static double normalizeBearing(double angle) {
        while (angle > Math.PI) {
            angle -= Math.PI * 2;
        }
        while (angle < -Math.PI) {
            angle += Math.PI * 2;
        }

        return angle;
    }

    public static double distance(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.pow(p1[i] - p2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static Double distance(Double[] p1, Double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.pow(p1[i] - p2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static double scale(double value, double maxValue, double scaledMaxValue) {
        return scaledMaxValue * value / maxValue;
    }

    public static double angle(APoint p1, APoint p2) {
        return angle(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public static void move(LXXPoint p, double angle, double distance) {
        p.x += sin(angle) * distance;
        p.y += sin(angle) * distance;
    }

    public static double factoredManhettanDistance(int[] a, int[] b, double[] factors, double[] scales) {
        double res = 0;

        for (int i = 0; i < a.length; i++) {
            final int dif = b[i] - a[i];
            res += dif * dif * factors[i] * scales[i];
        }

        return sqrt(res);
    }

}
