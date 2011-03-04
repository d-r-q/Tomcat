package voidious.utils;

import robocode.AdvancedRobot;
import robocode.util.Utils;
import voidious.utils.geom.Circle;
import voidious.utils.geom.LineSeg;
import voidious.utils.trace.TraceLogger;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2009-2010 - Voidious
 * <p/>
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * <p/>
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software.
 * <p/>
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * <p/>
 * 3. This notice may not be removed or altered from any source
 * distribution.
 */

public class DiaUtils {
    public static MovSim moveSimulator;
    public static boolean traceEnabled = false;

    public static final boolean IGNORE_WALLS = true;
    public static final boolean OBSERVE_WALL_HITS = false;
    protected static final double HALF_PI = Math.PI / 2;
    protected static final double TWO_PI = Math.PI * 2;
    protected static final double BOT_HALF_WIDTH = 18;
    protected static TraceLogger _traceLogger;

    public static Point2D.Double project(Point2D.Double sourceLocation,
                                         double angle, double length) {

        return project(sourceLocation, Math.sin(angle), Math.cos(angle), length);
    }

    public static Point2D.Double project(Point2D.Double sourceLocation,
                                         double sinAngle, double cosAngle, double length) {

        return new Point2D.Double(sourceLocation.x + sinAngle * length,
                sourceLocation.y + cosAngle * length);
    }

    public static double absoluteBearing(Point2D.Double sourceLocation,
                                         Point2D.Double target) {

        return Math.atan2(target.x - sourceLocation.x,
                target.y - sourceLocation.y);
    }

    public static int nonZeroSign(double d) {
        if (d < 0) {
            return -1;
        }
        return 1;
    }

    public static double square(double d) {
        return d * d;
    }

    public static double cube(double d) {
        return d * d * d;
    }

    public static double power(double d, int exp) {
        double r = 1;
        for (int x = 0; x < exp; x++) {
            r *= d;
        }

        return r;
    }

    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }


    public static double botWidthAimAngle(double distance) {
        return Math.abs(18.0 / distance);
    }

    public static int bulletTicksFromPower(double distance, double power) {
        return (int) Math.ceil(distance / (20 - (3 * power)));
    }

    public static int bulletTicksFromSpeed(double distance, double speed) {
        return (int) Math.ceil(distance / speed);
    }

    public static void setBackAsFront(AdvancedRobot robot,
                                      double goAngleRadians) {

        double angle = Utils.normalRelativeAngle(
                goAngleRadians - robot.getHeadingRadians());

        if (Math.abs(angle) > (HALF_PI)) {
            if (angle < 0) {
                robot.setTurnRightRadians(Math.PI + angle);
            } else {
                robot.setTurnLeftRadians(Math.PI - angle);
            }

            robot.setBack(100);
        } else {
            if (angle < 0) {
                robot.setTurnLeftRadians(-1 * angle);
            } else {
                robot.setTurnRightRadians(angle);
            }
            robot.setAhead(100);
        }
    }

    public static double rollingAverage(double previousValue, double newValue,
                                        double depth) {

        return ((previousValue * depth) + newValue) / (depth + 1);
    }

    public static double round(double d, int i) {
        long powerTen = 1;

        for (int x = 0; x < i; x++) {
            powerTen *= 10;
        }

        return ((double) Math.round(d * powerTen)) / powerTen;
    }

    public static Point2D.Double nextLocation(AdvancedRobot robot) {
        if (moveSimulator == null) {
            moveSimulator = new MovSim();
        }

        MovSimStat[] next = moveSimulator.futurePos(1, robot);

        return new Point2D.Double(next[0].x, next[0].y);
    }

    public static Point2D.Double nextLocation(Point2D.Double botLocation,
                                              double velocity, double heading) {

        return new Point2D.Double(
                botLocation.x + (Math.sin(heading) * velocity),
                botLocation.y + (Math.cos(heading) * velocity));
    }

    public static RobotState nextLocation(Point2D.Double botLocation,
                                          double velocity, double maxVelocity,
                                          double headingRadians, double goAngleRadians, long currentTime,
                                          boolean isSmoothing, boolean ignoreWallHits, double battleFieldWidth,
                                          double battleFieldHeight) {

        MovSim movSim = DiaUtils.getMovSim();

        double futureTurn = Utils.normalRelativeAngle(
                goAngleRadians - headingRadians);
        double futureDistance;

        if (Math.abs(futureTurn) > (HALF_PI)) {
            if (futureTurn < 0) {
                futureTurn = Math.PI + futureTurn;
            } else {
                futureTurn = -1 * (Math.PI - futureTurn);
            }

            futureDistance = -1000;
        } else {
            futureDistance = 1000;
        }

        int extraWallSize = 0;

        if (ignoreWallHits) {
            extraWallSize = 50000;
        }

        MovSimStat[] futureMoves = movSim.futurePos(
                1, extraWallSize + botLocation.x,
                extraWallSize + botLocation.y, velocity, maxVelocity,
                headingRadians, futureDistance,
                futureTurn, 10.0, extraWallSize * 2 + battleFieldWidth,
                extraWallSize * 2 + battleFieldHeight);

        return new RobotState(
                new Point2D.Double(DiaUtils.round(futureMoves[0].x - extraWallSize, 4),
                        DiaUtils.round(futureMoves[0].y - extraWallSize, 4)), futureMoves[0].h,
                futureMoves[0].v, currentTime + 1, isSmoothing);
    }

    public static RobotState nextPerpendicularLocation(
            Point2D.Double targetLocation, double absBearingRadians,
            double enemyVelocity, double enemyHeadingRadians,
            boolean clockwise, long currentTime, boolean ignoreWallHits) {

        int purelyPerpendicularOffset = 0;

        return nextPerpendicularLocation(targetLocation, absBearingRadians,
                enemyVelocity, enemyHeadingRadians, purelyPerpendicularOffset,
                clockwise, currentTime, ignoreWallHits);
    }

    public static RobotState nextPerpendicularLocation(
            Point2D.Double targetLocation, double absBearingRadians,
            double enemyVelocity, double enemyHeadingRadians, double attackAngle,
            boolean clockwise, long currentTime, boolean ignoreWallHits) {

        return nextPerpendicularWallSmoothedLocation(targetLocation,
                absBearingRadians, enemyVelocity, 8.0, enemyHeadingRadians,
                attackAngle, clockwise, currentTime, null, 0, 0, 0,
                ignoreWallHits);
    }

    public static RobotState nextPerpendicularWallSmoothedLocation(
            Point2D.Double targetLocation, double absBearingRadians,
            double enemyVelocity, double maxVelocity, double enemyHeadingRadians,
            double attackAngle, boolean clockwise, long currentTime,
            Rectangle2D.Double battleField, double bfWidth, double bfHeight,
            double wallStick, boolean ignoreWallHits) {

        int orientation;
        if (clockwise) {
            orientation = 1;
        } else {
            orientation = -1;
        }

        double goAngleRadians = Utils.normalRelativeAngle(
                absBearingRadians + (orientation * ((HALF_PI) + attackAngle)));

        boolean isSmoothing = false;

        if (wallStick != 0 && battleField != null) {
            double smoothedAngle = DiaUtils.wallSmoothing(
                    battleField, bfWidth, bfHeight, targetLocation,
                    goAngleRadians, orientation, wallStick);

            if (DiaUtils.round(smoothedAngle, 4)
                    != DiaUtils.round(goAngleRadians, 4)) {
                isSmoothing = true;
            }

            goAngleRadians = smoothedAngle;
        }

        return nextLocation(targetLocation, enemyVelocity, maxVelocity,
                enemyHeadingRadians, goAngleRadians, currentTime,
                isSmoothing, bfWidth, bfHeight, ignoreWallHits);
    }

    public static RobotState nextLocation(
            Point2D.Double targetLocation, double enemyVelocity, double maxVelocity,
            double enemyHeadingRadians, double goAngleRadians, long currentTime,
            boolean isSmoothing, double battleFieldWidth, double battleFieldHeight,
            boolean ignoreWallHits) {

        MovSim movSim = DiaUtils.getMovSim();

        double futureTurn = Utils.normalRelativeAngle(
                goAngleRadians - enemyHeadingRadians);
        double futureDistance;

        if (Math.abs(futureTurn) > (HALF_PI)) {
            if (futureTurn < 0) {
                futureTurn = Math.PI + futureTurn;
            } else {
                futureTurn = -1 * (Math.PI - futureTurn);
            }

            futureDistance = -1000;
        } else {
            futureDistance = 1000;
        }

        int extraWallSize = 0;

        if (ignoreWallHits) {
            extraWallSize = 50000;
        }

        MovSimStat[] futureMoves = movSim.futurePos(
                1, extraWallSize + targetLocation.x,
                extraWallSize + targetLocation.y, enemyVelocity, maxVelocity,
                enemyHeadingRadians, futureDistance,
                futureTurn, 10.0, extraWallSize * 2 + battleFieldWidth,
                extraWallSize * 2 + battleFieldHeight);

        return new RobotState(
                new Point2D.Double(futureMoves[0].x - extraWallSize,
                        futureMoves[0].y - extraWallSize), futureMoves[0].h,
                futureMoves[0].v, currentTime + 1, isSmoothing);
    }

    public static MovSim getMovSim() {
        if (moveSimulator == null) {
            moveSimulator = new MovSim();
        }

        return moveSimulator;
    }

    public static double orbitalWallDistance(Point2D.Double sourceLocation,
                                             Point2D.Double targetLocation, double bulletPower, int direction,
                                             Rectangle2D.Double fieldRect) {

        return orbitalWallDistance(sourceLocation, targetLocation, bulletPower,
                direction, fieldRect, 1);
    }

    public static double orbitalWallDistance(Point2D.Double sourceLocation,
                                             Point2D.Double targetLocation, double bulletPower, int direction,
                                             Rectangle2D.Double fieldRect, double fudge) {

        double absBearing =
                DiaUtils.absoluteBearing(sourceLocation, targetLocation);
        double distance = sourceLocation.distance(targetLocation) * fudge;
        double maxAngleRadians = Math.asin(8.0 / (20 - 3.0 * bulletPower));


        // 1.0 means the max range of orbital movement exactly reaches bounds
        // of battle field
        double wallDistance = 2.0;
        for (int x = 0; x < 200; x++) {
            if (!fieldRect.contains(
                    sourceLocation.x + (Math.sin(absBearing +
                            (direction * (x / 100.0) * maxAngleRadians)) * distance),
                    sourceLocation.y + (Math.cos(absBearing +
                            (direction * (x / 100.0) * maxAngleRadians)) * distance))) {
                wallDistance = x / 100.0;
                break;
            }
        }

        return wallDistance;
    }

    public static double directToWallDistance(Point2D.Double targetLocation,
                                              double distance, double heading, double bulletPower,
                                              Rectangle2D.Double fieldRect) {

        int bulletTicks = DiaUtils.bulletTicksFromPower(distance, bulletPower);

        double wallDistance = 2.0;
        double sinHeading = Math.sin(heading);
        double cosHeading = Math.cos(heading);
        for (int x = 0; x < 2 * bulletTicks; x++) {
            if (!fieldRect.contains(
                    targetLocation.x + (sinHeading * 8.0 * x),
                    targetLocation.y + (cosHeading * 8.0 * x))) {
                wallDistance = ((double) x) / bulletTicks;
                break;
            }
        }

        return wallDistance;
    }

    /**
     * wallSmoothing: do some Voodoo and wall smooth in a very efficient way.
     * - ...in terms of CPU cycles, not amount of code.
     * - used to be iterative, which was a lot simpler and more readable,
     * but far too slow with how much it was called during precise
     * prediction.
     */
    public static double wallSmoothing(Rectangle2D.Double field, double bfWidth,
                                       double bfHeight, Point2D.Double startLocation, double startAngleRadians,
                                       int orientation, double wallStick) {

/*
        double angle = startAngle;
        _lastWallSmoothAway = false;
        while (!field.contains(x + Math.sin(Math.toRadians(angle))*WALL_STICK,
            y+Math.cos(Math.toRadians(angle))*WALL_STICK)) {
            angle += orientation*smoothNormal*7.0;
            if (smoothNormal == -1) { _lastWallSmoothAway = true; }
        }

        return angle;
*/
        // Trying to do almost exactly the equivalent of the above in more
        // code but less CPU time. The above needs a low increment to work
        // perfectly smoothly, which results in very slow execution.
        //
        // NOTE: The two algorithms can give slightly different results,
        //       but that is mainly because the iterative one never tests a
        //       very specific angle in a corner that would turn up "in bounds";
        //       if it increased the angle var by (1/INFINITY), they'd be the
        //       same (as far as I can tell).

        double angle = startAngleRadians;
        double wallDistanceX = Math.min(startLocation.x - 18,
                bfWidth - startLocation.x - 18);
        double wallDistanceY = Math.min(startLocation.y - 18,
                bfHeight - startLocation.y - 18);

        if (wallDistanceX > wallStick && wallDistanceY > wallStick) {
            return startAngleRadians;
        }

        double testX = startLocation.x + (Math.sin(angle) * wallStick);
        double testY = startLocation.y + (Math.cos(angle) * wallStick);
        double testDistanceX = Math.min(testX - 18, bfWidth - testX - 18);
        double testDistanceY = Math.min(testY - 18, bfHeight - testY - 18);

        double adjacent = 0;
        int g = 0;

        while ((testDistanceX < 0 || testDistanceY < 0) && g++ < 25) {
            while (angle < 0) {
                angle += (2 * Math.PI);
            }
            if (testDistanceY < 0 && testDistanceY < testDistanceX) {
                // wall smooth North or South wall
                angle = ((int) ((angle + (Math.PI / 2)) / Math.PI)) * Math.PI;
                adjacent = Math.abs(wallDistanceY);
            } else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
                // wall smooth East or West wall
                angle = (((int) (angle / Math.PI)) * Math.PI) + (Math.PI / 2);
                adjacent = Math.abs(wallDistanceX);
            }

            angle += orientation *
                    (Math.abs(Math.acos(adjacent / wallStick)) + 0.0005);

            testX = startLocation.x + (Math.sin(angle) * wallStick);
            testY = startLocation.y + (Math.cos(angle) * wallStick);
            testDistanceX = Math.min(testX - 18, bfWidth - testX - 18);
            testDistanceY = Math.min(testY - 18, bfHeight - testY - 18);
        }

        return angle;
    }

    public static DiaWave findClosestWave(List<DiaWave> waveList,
                                          Point2D.Double targetLocation, long currentTime, boolean onlySurfable,
                                          double matchDistanceThreshold) {

        return findClosestWave(waveList, targetLocation, currentTime,
                onlySurfable, DiaWave.ANY_WAVE, matchDistanceThreshold, null);
    }

    public static DiaWave findClosestWave(List<DiaWave> waveList,
                                          Point2D.Double targetLocation, long currentTime, boolean onlySurfable,
                                          double matchDistanceThreshold, double bulletPower) {

        return findClosestWave(waveList, targetLocation, currentTime,
                onlySurfable, DiaWave.ANY_WAVE, matchDistanceThreshold, null,
                bulletPower);
    }

    public static DiaWave findClosestWave(List<DiaWave> waveList,
                                          Point2D.Double targetLocation, long currentTime, boolean onlySurfable,
                                          boolean onlyFiring, double matchDistanceThreshold, String botName) {

        return findClosestWave(waveList, targetLocation, currentTime,
                onlySurfable, DiaWave.ANY_WAVE, matchDistanceThreshold, null,
                DiaWave.ANY_BULLET_POWER);
    }

    public static DiaWave findClosestWave(List<DiaWave> waveList,
                                          Point2D.Double targetLocation, long currentTime, boolean onlySurfable,
                                          boolean onlyFiring, double matchDistanceThreshold, String botName,
                                          double bulletPower) {

        double closestDistance = Double.POSITIVE_INFINITY;
        DiaWave closestWave = null;
        Iterator<DiaWave> wavesIterator = waveList.iterator();

        while (wavesIterator.hasNext()) {
            DiaWave w = wavesIterator.next();
            if (!w.altWave && (botName == null || botName.equals(w.botName) ||
                    botName.equals(""))) {
                double distanceFromTargetToWave =
                        (w.sourceLocation.distance(targetLocation) -
                                w.distanceTraveled(currentTime));

                if (Math.abs(distanceFromTargetToWave) < matchDistanceThreshold
                        && Math.abs(distanceFromTargetToWave) < closestDistance &&
                        (!onlySurfable || distanceFromTargetToWave > 0) &&
                        (!onlyFiring || w.firingWave) &&
                        (bulletPower == DiaWave.ANY_BULLET_POWER ||
                                Math.abs(bulletPower - w.bulletPower) < 0.001)) {

                    closestDistance = Math.abs(distanceFromTargetToWave);
                    closestWave = w;
                }
            }
        }

        return closestWave;
    }

    public static double standardDeviation(double[] values) {
        double avg = average(values);

        double sumSquares = 0;

        for (int x = 0; x < values.length; x++) {
            sumSquares += square(avg - values[x]);
        }

        return Math.sqrt(sumSquares / values.length);
    }

    public static double average(double[] values) {
        double sum = 0;
        for (int x = 0; x < values.length; x++) {
            sum += values[x];
        }

        return (sum / values.length);
    }

    public static double accel(double velocity, double previousVelocity) {
        double accel = velocity - previousVelocity;
        // TODO: check if this could hit rounding errors
        if (previousVelocity == 0.0) {
            accel = Math.abs(accel);
        } else {
            accel *= Math.signum(previousVelocity);
        }

        return accel;
    }

    public static Point2D.Double translateToField(Point2D.Double p,
                                                  double width, double height) {

        return new Point2D.Double(
                limit(18, p.x, width - 18),
                limit(18, p.y, height - 18));
    }

    public static double preciseFrontBumperOffset(Point2D.Double sourceLocation,
                                                  Point2D.Double botLocation) {

        return sourceLocation.distance(botLocation) -
                distancePointToBot(sourceLocation, botLocation);
    }

    public static Rectangle2D.Double botRect(Point2D.Double botLocation) {
        Rectangle2D.Double botRect = new Rectangle2D.Double(botLocation.x - 18,
                botLocation.y - 18, 36, 36);

        return botRect;
    }

    public static ArrayList<Line2D.Double> botSides(Point2D.Double botLocation) {
        ArrayList<Line2D.Double> botSides = new ArrayList<Line2D.Double>();
        botSides.add(new Line2D.Double(botLocation.x - 18,
                botLocation.y - 18, botLocation.x + 18, botLocation.y - 18));
        botSides.add(new Line2D.Double(botLocation.x + 18,
                botLocation.y - 18, botLocation.x + 18, botLocation.y + 18));
        botSides.add(new Line2D.Double(botLocation.x + 18,
                botLocation.y + 18, botLocation.x - 18, botLocation.y + 18));
        botSides.add(new Line2D.Double(botLocation.x - 18,
                botLocation.y + 18, botLocation.x - 18, botLocation.y - 18));

        return botSides;
    }

    public static double distancePointToBot(
            Point2D.Double sourceLocation, Point2D.Double botLocation) {

        if (sourceLocation.x > botLocation.x - 18 &&
                sourceLocation.x < botLocation.x + 18 &&
                sourceLocation.y > botLocation.y - 18 &&
                sourceLocation.y < botLocation.y + 18) {

            return 0;
        } else {
            ArrayList<Line2D.Double> botSides = new ArrayList<Line2D.Double>();
            botSides.add(new Line2D.Double(botLocation.x - 18,
                    botLocation.y - 18, botLocation.x + 18, botLocation.y - 18));
            botSides.add(new Line2D.Double(botLocation.x + 18,
                    botLocation.y - 18, botLocation.x + 18, botLocation.y + 18));
            botSides.add(new Line2D.Double(botLocation.x + 18,
                    botLocation.y + 18, botLocation.x - 18, botLocation.y + 18));
            botSides.add(new Line2D.Double(botLocation.x - 18,
                    botLocation.y + 18, botLocation.x - 18, botLocation.y - 18));

            double distance = Double.POSITIVE_INFINITY;

            Iterator<Line2D.Double> sideIterator = botSides.iterator();
            while (sideIterator.hasNext()) {
                distance = Math.min(distance,
                        sideIterator.next().ptSegDist(sourceLocation));
            }

            return distance;
        }
    }

    public static ArrayList<Point2D.Double> botCorners(Point2D.Double p) {
        ArrayList<Point2D.Double> corners = new ArrayList<Point2D.Double>();
        corners.add(new Point2D.Double(p.x - BOT_HALF_WIDTH, p.y - BOT_HALF_WIDTH));
        corners.add(new Point2D.Double(p.x - BOT_HALF_WIDTH, p.y + BOT_HALF_WIDTH));
        corners.add(new Point2D.Double(p.x + BOT_HALF_WIDTH, p.y - BOT_HALF_WIDTH));
        corners.add(new Point2D.Double(p.x + BOT_HALF_WIDTH, p.y + BOT_HALF_WIDTH));

        return corners;
    }

    public static double[] preciseBotWidth(DiaWave w,
                                           ArrayList<RobotState> dangerStates) {

        ArrayList<Double> aimAngles = new ArrayList<Double>();

        Iterator<RobotState> dangerIterator = dangerStates.iterator();
        while (dangerIterator.hasNext()) {
            RobotState dangerState = dangerIterator.next();

            ArrayList<Point2D.Double> corners = botCorners(dangerState.location);
            Circle waveStart = new Circle(w.sourceLocation.x,
                    w.sourceLocation.y,
                    w.bulletSpeed * (dangerState.time - w.fireTime));
            Circle waveEnd = new Circle(w.sourceLocation.x, w.sourceLocation.y,
                    w.bulletSpeed * (dangerState.time - w.fireTime + 1));

            Iterator<Point2D.Double> cornerIterator = corners.iterator();
            while (cornerIterator.hasNext()) {
                Point2D.Double corner = cornerIterator.next();
                if (waveEnd.contains(corner) && !waveStart.contains(corner)) {
                    aimAngles.add(DiaUtils.absoluteBearing(w.sourceLocation,
                            corner));
                }
            }

            Iterator<Line2D.Double> sidesIterator = dangerState.botSides().iterator();
            while (sidesIterator.hasNext()) {
                Line2D.Double side = sidesIterator.next();
                LineSeg seg = new LineSeg(side.x1, side.y1, side.x2, side.y2);
                Point2D.Double[] intersects = waveStart.intersectsLineSeg(seg);
                for (int x = 0; x < intersects.length; x++) {
                    if (intersects[x] != null) {
                        aimAngles.add(DiaUtils.absoluteBearing(w.sourceLocation,
                                intersects[x]));
                    }
                }
                intersects = waveEnd.intersectsLineSeg(seg);
                if (intersects != null) {
                    for (int x = 0; x < intersects.length; x++) {
                        if (intersects[x] != null) {
                            aimAngles.add(DiaUtils.absoluteBearing(
                                    w.sourceLocation, intersects[x]));
                        }
                    }
                }
            }
        }

        if (aimAngles.size() == 0) {
            // TODO: Caused when the second wave is past any danger zone by the
            //       time we finish surfing the first. This can be caused by
            //       chase bullets or the like. Sorting waves by time to impact
            //       instead of distance in 1.5.10 mostly eliminated this
            //       (tested vs PrairieWolf), but lost points. Reverted.
            return new double[]{
                    DiaUtils.absoluteBearing(w.sourceLocation,
                            dangerStates.get(0).location),
                    DiaUtils.botWidthAimAngle(
                            dangerStates.get(0).location.distance(w.sourceLocation))
                            / Math.asin(8.0 / w.bulletSpeed)};
        }

        double normalizeReference = aimAngles.get(0);

        double minAngle = normalizeReference;
        double maxAngle = normalizeReference;

        Iterator<Double> anglesIterator = aimAngles.iterator();
        while (anglesIterator.hasNext()) {
            double thisAngle = anglesIterator.next();
            double normDiff = normalizeReference - thisAngle;
            while (Math.abs(normDiff) > Math.PI) {
                thisAngle += Math.signum(normDiff) * TWO_PI;
                normDiff = normalizeReference - thisAngle;
            }

            if (thisAngle > maxAngle) {
                maxAngle = thisAngle;
            }

            if (thisAngle < minAngle) {
                minAngle = thisAngle;
            }
        }

        double centerAngle = (maxAngle + minAngle) / 2;
        double bandwidth = maxAngle - centerAngle;

        return new double[]{centerAngle, bandwidth};
    }


    // TODO: figure out if KdBucketTree utility methods should be here
    //       or in KdBucketTree.java ... in both for now.

    public static double distanceSq(double[] p1, double[] p2,
                                    double[] weights) {

        double sum = 0;
        for (int x = 0; x < p1.length; x++) {
            double z = (p1[x] - p2[x]) * weights[x];
            sum += z * z;
        }

        return sum;
    }

    public static double manhattanDistance(double[] p1, double[] p2,
                                           double[] weights) {

        double sum = 0;
        for (int x = 0; x < p1.length; x++) {
            sum += Math.abs(p1[x] - p2[x]) * weights[x];
        }

        return sum;
    }

    public static double findLongestDistance(double[][] points,
                                             double[] testPoint, double[] weights, boolean manhattan) {

        double longestDistance = 0;
        for (int x = 0; x < points.length; x++) {
            double distance;
            if (manhattan) {
                distance = manhattanDistance(points[x], testPoint, weights);
            } else {
                distance = distanceSq(points[x], testPoint, weights);
            }
            if (distance > longestDistance) {
                longestDistance = distance;
            }
        }

        return longestDistance;
    }

    public static double findAndReplaceLongestDistance(double[][] points,
                                                       double[] nearestDistances, double[] newPoint,
                                                       double newPointDistance) {

        double longestDistance = 0;
        double newLongestDistance = 0;
        int longestIndex = 0;
        for (int x = 0; x < points.length; x++) {
            double distance = nearestDistances[x];
            if (distance > longestDistance) {
                newLongestDistance = longestDistance;
                longestDistance = distance;
                longestIndex = x;
            } else if (distance > newLongestDistance) {
                newLongestDistance = distance;
            }
        }
        points[longestIndex] = newPoint;
        nearestDistances[longestIndex] = newPointDistance;

        return Math.max(newLongestDistance, newPointDistance);
    }

    public static double[][] nearestNeighbors(double[][] dataSet,
                                              double[] searchPoint, double[] weights, int numNeighbors,
                                              boolean manhattan) {

        if (dataSet.length <= numNeighbors) {
            return dataSet;
        }

        double[][] closestPoints =
                new double[numNeighbors][searchPoint.length];
        double[] nearestDistances = new double[numNeighbors];
        for (int y = 0; y < numNeighbors; y++) {
            closestPoints[y] = dataSet[y];
            if (manhattan) {
                nearestDistances[y] = manhattanDistance(
                        closestPoints[y], searchPoint, weights);
            } else {
                nearestDistances[y] = distanceSq(
                        closestPoints[y], searchPoint, weights);
            }
        }

        double closestDistanceThreshold = findLongestDistance(
                closestPoints, searchPoint, weights, manhattan);
        for (int y = numNeighbors; y < dataSet.length; y++) {
            double[] point = dataSet[y];
            double thisDistance;
            if (manhattan) {
                thisDistance = manhattanDistance(searchPoint, point, weights);
            } else {
                thisDistance = distanceSq(searchPoint, point, weights);
            }
            if (thisDistance < closestDistanceThreshold) {
                closestDistanceThreshold =
                        findAndReplaceLongestDistance(closestPoints,
                                nearestDistances, point, thisDistance);
            }
        }

        return closestPoints;
    }

    public static void log(String className, String methodName, String params,
                           boolean entering) {

        if (traceEnabled) {
            _traceLogger.log(className, methodName, params, entering);
        }
    }

    public static void initTrace(String[] include, String[] exclude) {
        if (traceEnabled) {
            _traceLogger = new TraceLogger(include, exclude);
        }
    }
}
