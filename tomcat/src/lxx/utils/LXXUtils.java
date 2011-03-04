/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.model.BattleSnapshot;
import lxx.office.AttributesManager;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

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

    public static double weightedManhattanDistance(int[] indexes, int[] a, int[] b, double[] weights) {
        double res = 0;

        final int len = indexes.length;
        for (int i = 0; i < len; i++) {
            res += ((b[indexes[i]] > a[indexes[i]])
                    ? b[indexes[i]] - a[indexes[i]]
                    : a[indexes[i]] - b[indexes[i]])
                    * weights[indexes[i]];
        }

        return res;
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

    public static double lateralVelocity2(APoint center, APoint pos, double velocity, double heading) {
        if (Utils.isNear(0, velocity)) {
            return 1;
        }
        return abs(velocity) * Math.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
    }

    public static double lateralVelocity(APoint center, APoint pos, double velocity, double heading) {
        return abs(velocity) * Math.sin(Utils.normalRelativeAngle(heading - center.angleTo(pos)));
    }

    public static APoint getMyPos(BattleSnapshot bs) {
        return new LXXPoint(bs.getAttrValue(AttributesManager.myX), bs.getAttrValue(AttributesManager.myY));
    }

    public static APoint getEnemyPos(BattleSnapshot bs) {
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


    public static double manhattanDistance(double[] pnt1, double[] pnt2, double[] weights) {
        double res = 0;

        for (int i = 0; i < pnt1.length; i++) {
            res += abs(pnt1[i] - pnt2[i]) * weights[i];
        }

        return res;
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

    public static long getAccelerationTime(double startVel, double targetVel) {
        final double velocityDelta = startVel < targetVel ? Rules.ACCELERATION : Rules.DECELERATION;
        if (startVel > targetVel) {
            startVel = -startVel;
            targetVel = -targetVel;
        }
        long res = 0;
        while (startVel < targetVel) {
            res++;
            startVel += min(velocityDelta, abs(startVel - targetVel));
        }
        return res;
    }

    public static long getAccelerationDistance(double startVel, double targetVel) {
        final double velocityDelta = startVel < targetVel ? Rules.ACCELERATION : Rules.DECELERATION;
        if (startVel > targetVel) {
            startVel = -startVel;
            targetVel = -targetVel;
        }
        long res = 0;
        while (startVel < targetVel) {
            startVel += min(velocityDelta, abs(startVel - targetVel));
            res += abs(startVel);
        }
        return res;
    }

    public static double getMaxEscapeAngle(double bulletSpeed) {
        return QuickMath.asin(Rules.MAX_VELOCITY / bulletSpeed);
    }

    public static double lateralVelocity(APoint center, LXXRobotState robotState) {
        return lateralVelocity(center, robotState, robotState.getVelocityModule(), robotState.getAbsoluteHeadingRadians());
    }
}
