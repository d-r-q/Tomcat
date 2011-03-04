package jk.mega;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PreciseUtils {
    public static final int INTERSECTION = 3, PASSED = 1, NOT_REACHED = 2;


    //high speed test to determine if the full method should be run this tick
    public static int intersects(Point2D.Double botLocation, PreciseWave wave) {
        double[] distSq = new double[]{
                wave.fireLocation.distanceSq(botLocation.x - 18, botLocation.y + 18),
                wave.fireLocation.distanceSq(botLocation.x + 18, botLocation.y + 18),
                wave.fireLocation.distanceSq(botLocation.x + 18, botLocation.y - 18),
                wave.fireLocation.distanceSq(botLocation.x - 18, botLocation.y - 18)};

        //faster? I'm not sure
        // Arrays.sort(distSq);
        // return (sqr(wave.distanceTraveled + bulletVelocity) > distSq[0]
        // && sqr(wave.distanceTraveled) < distSq[3]);
        int score = 0;
        if (sqr(wave.distanceTraveled) >
                Math.min(Math.min(distSq[0], distSq[1]), Math.min(distSq[2], distSq[3])))
            score++;
        if (sqr(wave.distanceTraveled - wave.bulletVelocity) < Math.max(Math.max(distSq[0], distSq[1]), Math.max(distSq[2], distSq[3])))
            score += 2;
        return score;
    }

    public static double[] getIntersectionRange(Point2D.Double botLocation, PreciseWave wave) {
        double[] yBounds = new double[]{botLocation.y - 18, botLocation.y + 18};
        double[] xBounds = new double[]{botLocation.x - 18, botLocation.x + 18};

        double[] radii = new double[]{wave.distanceTraveled, wave.distanceTraveled - wave.bulletVelocity};

        ArrayList<Point2D.Double> intersects = new ArrayList<Point2D.Double>();
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) {
                Point2D.Double[] testPoints = vertIntersect(wave.fireLocation.x, wave.fireLocation.y, radii[i], xBounds[j]);
                for (int k = 0; k < testPoints.length; k++)
                    if (inBounds(testPoints[k].y, yBounds))
                        intersects.add(testPoints[k]);
            }

        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) {
                Point2D.Double[] testPoints = horizIntersect(wave.fireLocation.x, wave.fireLocation.y, radii[i], yBounds[j]);
                for (int k = 0; k < testPoints.length; k++)
                    if (inBounds(testPoints[k].x, xBounds))
                        intersects.add(testPoints[k]);
            }
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) {
                Point2D.Double testCorner = new Point2D.Double(xBounds[i], yBounds[j]);
                double distSq = testCorner.distanceSq(wave.fireLocation);
                if (distSq <= sqr(radii[0]) && distSq > sqr(radii[1]))
                    intersects.add(testCorner);
            }
        double antiClockAngle = 1;
        double clockAngle = -1;
        Point2D.Double antiClock = null, clock = null;
        double absBearing = angle(wave.fireLocation, botLocation);
        for (Point2D.Double p : intersects) {
            double angDiff = fastRelativeAngle(angle(wave.fireLocation, p) - absBearing);
            if (angDiff > clockAngle) {
                clockAngle = angDiff;
                clock = p;
            }
            if (angDiff < antiClockAngle) {
                antiClockAngle = angDiff;
                antiClock = p;
            }
        }
        // return new Point2D.Double[]{antiClock,clock};
        return new double[]{fastAbsoluteAngle(antiClockAngle + absBearing), fastAbsoluteAngle(clockAngle + absBearing)};
    }

    static boolean inBounds(double q, double[] bounds) {
        return bounds[0] <= q && q <= bounds[1];
    }

    //assumes between -PI*2 and PI*2
    public static double fastRelativeAngle(double angle) {
        return angle < -Math.PI ? angle + Math.PI * 2 : angle > Math.PI ? angle - Math.PI * 2 : angle;
    }

    //assumes between -PI*2 and PI*4
    public static double fastAbsoluteAngle(double angle) {
        return angle > Math.PI * 2 ? angle - Math.PI * 2 : angle < 0 ? angle + Math.PI * 2 : angle;
    }

    static Point2D.Double[] vertIntersect(double centerX, double centerY, double r, double intersectX) {
        double deltaX = centerX - intersectX;
        double sqrtVal = r * r - deltaX * deltaX;
        if (sqrtVal < 0)
            return new Point2D.Double[]{};

        // if(sqrtVal == 0)
        // return new Point2D.Double[]{
        // new Point2D.Double(intersectX, centerY)};

        sqrtVal = Math.sqrt(sqrtVal);
        return new Point2D.Double[]{
                new Point2D.Double(intersectX, centerY + sqrtVal),
                new Point2D.Double(intersectX, centerY - sqrtVal)};
    }

    static Point2D.Double[] horizIntersect(double centerX, double centerY, double r, double intersectY) {
        double deltaY = centerY - intersectY;
        double sqrtVal = r * r - deltaY * deltaY;
        if (sqrtVal < 0)
            return new Point2D.Double[]{};

        // if(sqrtVal == 0)
        // return new Point2D.Double[]{
        // new Point2D.Double(centerX, intersectY)};

        sqrtVal = Math.sqrt(sqrtVal);
        return new Point2D.Double[]{
                new Point2D.Double(centerX + sqrtVal, intersectY),
                new Point2D.Double(centerX - sqrtVal, intersectY)};
    }

    public static double sqr(double d) {
        return d * d;
    }

    public static double angle(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }


}
