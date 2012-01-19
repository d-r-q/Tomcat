/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.LXXRobotState;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.ts_log.attributes.AttributesManager;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;

public class LXXUtils {

    private static final int FIFTEEN_BITS = 0x7FFF;

    private static final double ROBOT_SQUARE_DIAGONAL = LXXConstants.ROBOT_SIDE_SIZE * sqrt(2);
    private static final double HALF_PI = Math.PI / 2;
    private static final double DOUBLE_PI = Math.PI * 2;

    public static double angle(double baseX, double baseY, double x, double y) {
        double theta = QuickMath.asin((y - baseY) / LXXPoint.distance(x, y, baseX, baseY)) - HALF_PI;
        if (x >= baseX && theta < 0) {
            theta = -theta;
        }
        return (theta %= DOUBLE_PI) >= 0 ? theta : (theta + DOUBLE_PI);
    }

    public static double angle(APoint p1, APoint p2) {
        return angle(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public static double anglesDiff(double alpha1, double alpha2) {
        return abs(Utils.normalRelativeAngle(alpha1 - alpha2));
    }

    public static int limit(int minValue, int value, int maxValue) {
        if (value < minValue) {
            return minValue;
        }

        if (value > maxValue) {
            return maxValue;
        }

        return value;
    }

    public static double limit(double minValue, double value, double maxValue) {
        if (value < minValue) {
            return minValue;
        }

        if (value > maxValue) {
            return maxValue;
        }

        return value;
    }

    public static double limit(Attribute a, double value) {
        return limit(a.actualRange.a, value, a.actualRange.b);
    }

    public static double lateralDirection(APoint center, LXXRobotState robotState) {
        return lateralDirection(center, robotState, robotState.getSpeed(), robotState.getAbsoluteHeadingRadians());
    }

    private static double lateralDirection(APoint center, APoint pos, double velocity, double heading) {
        if (Utils.isNear(0, velocity)) {
            return 1;
        }
        return signum(lateralVelocity(center, pos, velocity, heading));
    }

    public static double lateralVelocity(APoint center, LXXRobotState robotState) {
        return lateralVelocity(center, robotState, robotState.getSpeed(), robotState.getAbsoluteHeadingRadians());
    }

    private static double lateralVelocity(APoint center, APoint pos, double velocity, double heading) {
        return velocity * Math.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
    }

    public static double getBulletPower(double bulletSpeed) {
        // speed = 20 - 3 * firepower
        // - 3 * firepower = speed - 20
        // firepower = (20 - speed) / 3
        return (20 - bulletSpeed) / 3;
    }

    public static double getReturnedEnergy(double bulletPower) {
        return 3 * bulletPower;
    }

    public static Rectangle2D getBoundingRectangleAt(APoint point) {
        return getBoundingRectangleAt(point, LXXConstants.ROBOT_SIDE_HALF_SIZE);
    }

    public static Rectangle2D getBoundingRectangleAt(APoint point, final int sideHalfSize) {
        return new Rectangle.Double(point.getX() - sideHalfSize, point.getY() - sideHalfSize,
                sideHalfSize * 2, sideHalfSize * 2);
    }

    public static double bearingOffset(APoint source, APoint dest1, APoint dest2) {
        return Utils.normalRelativeAngle(angle(source, dest2) - angle(source, dest1));
    }

    public static double getRobotWidthInRadians(APoint center, APoint robotPos) {
        return getRobotWidthInRadians(angle(center, robotPos), center.aDistance(robotPos));
    }

    public static double getRobotWidthInRadians(double angle, double distance) {
        final double alpha = abs(LXXConstants.RADIANS_45 - (angle % LXXConstants.RADIANS_90));
        if (distance < ROBOT_SQUARE_DIAGONAL) {
            distance = ROBOT_SQUARE_DIAGONAL;
        }
        return QuickMath.asin(QuickMath.cos(alpha) * ROBOT_SQUARE_DIAGONAL / distance);
    }

    public static double getMaxEscapeAngle(double bulletSpeed) {
        return QuickMath.asin(Rules.MAX_VELOCITY / bulletSpeed);
    }

    // from robowiki
    public static double getMaxEscapeAngle(APoint center, LXXRobotState state, double bulletSpeed) {
        // Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to robot
        final double eAbsBearing = center.angleTo(state);
        final double rX = center.getX();
        final double rY = center.getY();
        final double eX = state.getX();
        final double eY = state.getY();
        final double eV = state.getVelocity();
        final double eHd = state.getHeadingRadians();
        // These constants make calculating the quadratic coefficients below easier
        final double A = (eX - rX) / bulletSpeed;
        final double B = eV / bulletSpeed * Math.sin(eHd);
        final double C = (eY - rY) / bulletSpeed;
        final double D = eV / bulletSpeed * Math.cos(eHd);
        // Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
        final double a = A * A + C * C;
        final double b = 2 * (A * B + C * D);
        final double c = (B * B + D * D - 1);
        final double discrim = b * b - 4 * a * c;
        if (discrim >= 0) {
            // Reciprocal of quadratic formula
            final double t1 = 2 * a / (-b - Math.sqrt(discrim));
            final double t2 = 2 * a / (-b + Math.sqrt(discrim));
            final double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);
            // Assume enemy stops at walls
            final BattleField battleField = state.getBattleField();
            final double endX = limit(
                    eX + eV * t * Math.sin(eHd),
                    battleField.availableLeftX, battleField.availableRightX);
            final double endY = limit(
                    eY + eV * t * Math.cos(eHd),
                    battleField.availableBottomY, battleField.availableTopY);

            return abs(Utils.normalRelativeAngle(center.angleTo(new LXXPoint(endX, endY)) - eAbsBearing));
        }

        return 0;
    }

    public static double calculateAcceleration(LXXRobotState prevState, LXXRobotState curState) {
        if (prevState == null) {
            return 0;
        }

        double acceleration;
        if (signum(curState.getVelocity()) == signum(prevState.getVelocity()) || abs(curState.getVelocity()) < 0.001) {
            acceleration = abs(curState.getVelocity()) - abs(prevState.getVelocity());
        } else {
            acceleration = abs(curState.getVelocity());
        }

        return limit(-Rules.MAX_VELOCITY, acceleration, Rules.ACCELERATION);
    }

    public static DeltaVector getEnemyDeltaVector(TurnSnapshot ts1, TurnSnapshot ts2) {
        final double enemyHeading = ts1.getEnemyAbsoluteHeading();
        final double x1 = ts1.getAttrValue(AttributesManager.enemyX);
        final double y1 = ts1.getAttrValue(AttributesManager.enemyY);
        final double x2 = ts2.getAttrValue(AttributesManager.enemyX);
        final double y2 = ts2.getAttrValue(AttributesManager.enemyY);
        final double alpha = angle(x1, y1, x2, y2);

        return new DeltaVector(Utils.normalRelativeAngle(alpha - enemyHeading), Point2D.Double.distance(x1, y1, x2, y2));
    }

    @SuppressWarnings({"unchecked"})
    public static <K, V> Map<K, V> toMap(Object... data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("data length: " + data.length);
        }
        Map map = new HashMap();

        for (int i = 0; i < data.length; i += 2) {
            map.put(data[i], data[i + 1]);
        }

        return map;
    }

    public static double getStopDistance(double speed) {
        double distance = 0;
        while (speed > 0) {
            speed -= Rules.DECELERATION;
            distance += speed;
        }
        return distance;
    }

    public static double getStopTime(double speed) {
        int time = 0;
        while (speed > 0) {
            speed -= Rules.DECELERATION;
            time++;
        }
        return time;
    }

    // we solve this problem in coordinate system with center in farest pnt and y direction equals to segment angle
    // in this cs following set of equations taking place:
    // / x = 0 - equation of segment
    // \ (x - cx)^2 + (y - cy)^2 = r^2 - equation of circle
    // then y = +/- sqrt(r^2 - cx^2) + cy;
    // because x = 0, y - it's distance from farest pnt to intersection pnt
    // so intersection point it's projection from farest point in direction on segment on y distance
    public static APoint[] intersection(APoint pnt1, APoint pnt2, final APoint center, double r) {
        final APoint farest;
        final APoint closest;
        if (center.aDistance(pnt1) > center.aDistance(pnt2)) {
            farest = pnt1;
            closest = pnt2;
        } else {
            farest = pnt2;
            closest = pnt1;
        }
        final double segmentAlpha = farest.angleTo(closest);
        final double segmentDist = farest.aDistance(closest);
        // calculate circle center in new cs
        final APoint newCircleCenter = new LXXPoint().project(abs(Utils.normalRelativeAngle(farest.angleTo(center) - segmentAlpha)), farest.aDistance(center));

        if (r < newCircleCenter.getX()) {
            // no intersection
            return new LXXPoint[0];
        }

        final double y1 = sqrt(r * r - newCircleCenter.getX() * newCircleCenter.getX()) + newCircleCenter.getY();
        final double y2 = -sqrt(r * r - newCircleCenter.getX() * newCircleCenter.getX()) + newCircleCenter.getY();

        final List<APoint> res = new ArrayList<APoint>();
        if (y2 > 0 && y2 < segmentDist) {
            res.add(farest.project(segmentAlpha, y2));
        }
        if (y1 > 0 && y1 < segmentDist) {
            res.add(farest.project(segmentAlpha, y1));
        }

        return res.toArray(new APoint[res.size()]);
    }

    public static int getRoundTime(long time, int round) {
        if (round > FIFTEEN_BITS || time > FIFTEEN_BITS) {
            throw new IllegalArgumentException("Too large round-time: " + round + " - " + time);
        }

        return (int) (((round & FIFTEEN_BITS) << 15) | (time & FIFTEEN_BITS));
    }

    public static <T> List<T> asModifiableList(T... items) {
        return new ArrayList<T>(Arrays.asList(items));
    }

}
