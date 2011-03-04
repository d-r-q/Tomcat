package kc.serpent.utils;

import kc.serpent.utils.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;


public class PreciseUtils {
    public static double minWaveDistance(double angle, Point2D.Double botLocation, Wave w) {
        double[] distanceSq = new double[]{w.source.distanceSq(new Point2D.Double(botLocation.x - 18, botLocation.y - 18)),
                w.source.distanceSq(new Point2D.Double(botLocation.x - 18, botLocation.y + 18)),
                w.source.distanceSq(new Point2D.Double(botLocation.x + 18, botLocation.y - 18)),
                w.source.distanceSq(new Point2D.Double(botLocation.x + 18, botLocation.y + 18))};
        return Math.min(botLocation.distance(w.source) - realBotWidth(angle, 18.0), Math.sqrt(Math.min(Math.min(distanceSq[0], distanceSq[1]), Math.min(distanceSq[2], distanceSq[3]))));
    }

    public static double maxWaveDistance(double angle, Point2D.Double botLocation, Wave w) {
        double[] distanceSq = new double[]{w.source.distanceSq(new Point2D.Double(botLocation.x - 18, botLocation.y - 18)),
                w.source.distanceSq(new Point2D.Double(botLocation.x - 18, botLocation.y + 18)),
                w.source.distanceSq(new Point2D.Double(botLocation.x + 18, botLocation.y - 18)),
                w.source.distanceSq(new Point2D.Double(botLocation.x + 18, botLocation.y + 18))};
        return Math.max(botLocation.distance(w.source) + realBotWidth(angle, 18.0), Math.sqrt(Math.max(Math.max(distanceSq[0], distanceSq[1]), Math.max(distanceSq[2], distanceSq[3]))));
    }

    static double realBotWidth(double angle, double length) {
        return length / Math.max(Math.abs(Math.cos(angle)), Math.abs(Math.sin(angle)));
    }

    //Following methods are based off of Skilgannon's PreciseUtils
    public static double[] getInterceptRange(Point2D.Double botLocation, double absoluteBearing, double waveRadius, Wave w) {
        double[] yBounds = new double[]{botLocation.y - 18, botLocation.y + 18};
        double[] xBounds = new double[]{botLocation.x - 18, botLocation.x + 18};
        double[] radiusSquared = new double[]{KUtils.sqr(waveRadius - w.speed), KUtils.sqr(waveRadius)};

        ArrayList intercepts = new ArrayList();

        for (int i = 0; i < 2; i++) {
            for (int ii = 0; ii < 2; ii++) {
                Point2D.Double[] testPoints = xIntercepts(yBounds[i], radiusSquared[ii], w);
                for (int iii = 0; iii < testPoints.length; iii++) {
                    if (KUtils.inBounds(testPoints[iii].x, xBounds)) {
                        intercepts.add(testPoints[iii]);
                    }
                }
                testPoints = yIntercepts(xBounds[i], radiusSquared[ii], w);
                for (int iii = 0; iii < testPoints.length; iii++) {
                    if (KUtils.inBounds(testPoints[iii].y, yBounds)) {
                        intercepts.add(testPoints[iii]);
                    }
                }

                Point2D.Double corner = new Point2D.Double(xBounds[i], yBounds[ii]);
                double cornerDistance = corner.distanceSq(w.source);
                if (cornerDistance > radiusSquared[0] && cornerDistance <= radiusSquared[1]) {
                    intercepts.add(corner);
                }
            }
        }

        double[] largestDiff = new double[2];

        Iterator i = intercepts.iterator();
        while (i.hasNext()) {
            Point2D.Double intercept = (Point2D.Double) (i.next());
            double angleDiff = Utils.normalRelativeAngle(KUtils.absoluteBearing(w.source, intercept) - absoluteBearing);
            if (angleDiff < largestDiff[0]) {
                largestDiff[0] = angleDiff;
            } else if (angleDiff > largestDiff[1]) {
                largestDiff[1] = angleDiff;
            }
        }

        return largestDiff;
    }

    static Point2D.Double[] xIntercepts(double y, double radiusSquared, Wave w) {
        double root = radiusSquared - KUtils.sqr(y - w.source.y);
        if (root < 0) {
            return new Point2D.Double[]{};
        } else {
            root = Math.sqrt(root);
            return new Point2D.Double[]{new Point2D.Double(w.source.x + root, y),
                    new Point2D.Double(w.source.x - root, y)};
        }
    }

    static Point2D.Double[] yIntercepts(double x, double radiusSquared, Wave w) {
        double root = radiusSquared - KUtils.sqr(x - w.source.x);
        if (root < 0) {
            return new Point2D.Double[]{};
        } else {
            root = Math.sqrt(root);
            return new Point2D.Double[]{new Point2D.Double(x, w.source.y + root),
                    new Point2D.Double(x, w.source.y - root)};
        }
    }
}
