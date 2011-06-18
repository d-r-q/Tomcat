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
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;

public class LXXUtils {

    private static final double HALF_PI = Math.PI / 2;
    private static final double DOUBLE_PI = Math.PI * 2;

    public static double angle(double baseX, double baseY, double x, double y) {
        double theta = QuickMath.asin((y - baseY) / LXXPoint.distance(x, y, baseX, baseY)) - HALF_PI;
        if (x >= baseX && y >= baseY) {
            theta = abs(theta);
        } else if (x >= baseX && y <= baseY) {
            theta = abs(theta);
        } else if (x <= baseX && y <= baseY) {
            theta = DOUBLE_PI + theta;
        } else {
            theta = DOUBLE_PI + theta;
        }
        return Utils.normalAbsoluteAngle(theta);
    }

    public static double angle(APoint p1, APoint p2) {
        return angle(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public static double weightedManhattanDistance(double[] a, double[] b, double[] weights) {
        double res = 0;

        for (int i = 0; i < a.length; i++) {
            res += ((b[i] > a[i])
                    ? b[i] - a[i]
                    : a[i] - b[i])
                    * weights[i];
        }

        return res;
    }


    public static double factoredManhettanDistance(int[] indexes, double[] a, double[] b, double[] factors) {
        double res = 0;

        final int len = indexes.length;
        for (int i = 0; i < len; i++) {
            res += ((b[indexes[i]] > a[indexes[i]])
                    ? b[indexes[i]] - a[indexes[i]]
                    : a[indexes[i]] - b[indexes[i]])
                    * factors[indexes[i]];
        }

        return res;
    }


    public static double getBulletPower(double bulletSpeed) {
        // speed = 20 - 3 * firepower
        // - 3 * firepower = speed - 20
        // firepower = (20 - speed) / 3
        return (20 - bulletSpeed) / 3;
    }

    public static double getStopDistance(double speed) {
        double res = 0;

        while (true) {
            speed -= Rules.DECELERATION;
            if (speed < 0) {
                break;
            }
            res += speed;
        }

        return res;
    }

    public static double anglesDiff(double alpha1, double alpha2) {
        return abs(Utils.normalRelativeAngle(alpha1 - alpha2));
    }

    public static double getAttackAngle(APoint attackerPos, APoint victimPos, double victimHeading) {
        double attackAngle = anglesDiff(attackerPos.angleTo(victimPos), victimHeading);
        if (attackAngle > Math.PI / 2) {
            attackAngle = Math.PI - attackAngle;
        }
        return attackAngle;
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
        return limit(a.getActualMin(), value, a.getActualMax());
    }

    public static double lateralVelocity2(APoint center, APoint pos, double velocity, double heading) {
        if (Utils.isNear(0, velocity)) {
            return 1;
        }
        return abs(velocity) * Math.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
    }

    public static double lateralVelocity(APoint center, APoint pos, double velocity, double heading) {
        return abs(velocity) * Math.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
    }

    public static APoint getMyPos(TurnSnapshot bs) {
        return new LXXPoint(bs.getRoundedAttrValue(AttributesManager.myX), bs.getRoundedAttrValue(AttributesManager.myY));
    }

    public static APoint getEnemyPos(TurnSnapshot bs) {
        return new LXXPoint(bs.getAttrValue(AttributesManager.enemyX), bs.getAttrValue(AttributesManager.enemyY));
    }

    public static double getReturnedEnergy(double bulletPower) {
        return 3 * bulletPower;
    }

    public static double getTurnDistance(double initialAngle, double targetAngle) {
        final double anglesDiff = anglesDiff(initialAngle, targetAngle);
        final double turnTime = anglesDiff / Rules.getTurnRateRadians(Rules.MAX_VELOCITY);

        return Rules.MAX_VELOCITY * turnTime;
    }

    public static Rectangle2D getBoundingRectangleAt(APoint point) {
        return new Rectangle.Double(point.getX() - LXXConstants.ROBOT_SIDE_HALF_SIZE, point.getY() - LXXConstants.ROBOT_SIDE_HALF_SIZE,
                LXXConstants.ROBOT_SIDE_SIZE, LXXConstants.ROBOT_SIDE_SIZE);
    }

    public static double bearingOffset(APoint source, APoint dest1, APoint dest2) {
        return Utils.normalRelativeAngle(source.angleTo(dest2) - source.angleTo(dest1));
    }

    public static APoint[] toPoints(Rectangle2D rect) {
        return new APoint[]{new LXXPoint(rect.getX(), rect.getY()), new LXXPoint(rect.getX(), rect.getY() + rect.getHeight()),
                new LXXPoint(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight()), new LXXPoint(rect.getX() + rect.getWidth(), rect.getY())};
    }

    public static double getRobotWidthInRadians(APoint center, APoint robotPos) {
        double minAngle = Integer.MAX_VALUE;
        double maxAngle = Integer.MIN_VALUE;
        for (APoint pnt : toPoints(getBoundingRectangleAt(robotPos))) {
            double angle = center.angleTo(pnt);
            minAngle = min(minAngle, angle);
            maxAngle = max(maxAngle, angle);
        }
        return maxAngle - minAngle;
    }

    public static double getMaxEscapeAngle(double bulletSpeed) {
        return QuickMath.asin(Rules.MAX_VELOCITY / bulletSpeed);
    }

    public static double lateralVelocity(APoint center, LXXRobotState robotState) {
        return lateralVelocity(center, robotState, robotState.getVelocityModule(), robotState.getAbsoluteHeadingRadians());
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
    public static<K, V> Map<K, V> toMap(Object ... data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("data length: " + data.length);
        }
        Map map = new HashMap();

        for (int i = 0; i < data.length; i += 2) {
            map.put(data[i], data[i + 1]);
        }

        return map;
    }

}
