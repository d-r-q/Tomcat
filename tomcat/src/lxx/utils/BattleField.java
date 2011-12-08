/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static java.lang.Math.max;

/**
 * User: jdev
 * Date: 17.02.2011
 */
public class BattleField {

    public static final double WALL_STICK = 140;

    public final APoint availableLeftBottom;
    public final APoint availableLeftTop;
    public final APoint availableRightTop;
    public final APoint availableRightBottom;

    private final LXXPoint leftTop;
    private final LXXPoint rightTop;
    private final LXXPoint rightBottom;

    public final Wall bottom;
    public final Wall left;
    public final Wall top;
    public final Wall right;

    public final int availableBottomY;
    public final int availableTopY;
    public final int availableLeftX;
    public final int availableRightX;

    public final Rectangle battleField;
    public final Rectangle2D.Double availableBattleFieldRectangle;
    public final Rectangle2D.Double exactAvailableBattleFieldRectangle;

    public final Interval noSmoothX;
    public final Interval noSmoothY;

    public final LXXPoint center;

    public final int width;
    public final int height;

    public BattleField(int x, int y, int width, int height) {
        availableBottomY = y;
        availableTopY = y + height;
        availableLeftX = x;
        availableRightX = x + width;

        availableLeftBottom = new LXXPoint(availableLeftX, availableBottomY);
        availableLeftTop = new LXXPoint(availableLeftX, availableTopY);
        availableRightTop = new LXXPoint(availableRightX, availableTopY);
        availableRightBottom = new LXXPoint(availableRightX, availableBottomY);

        final int bottomY = 0;
        final int topY = y * 2 + height;
        final int leftX = 0;
        final int rightX = x * 2 + width;

        leftTop = new LXXPoint(leftX, topY);
        rightTop = new LXXPoint(rightX, topY);
        rightBottom = new LXXPoint(rightX, bottomY);

        bottom = new Wall(WallType.BOTTOM, availableRightBottom, availableLeftBottom);
        left = new Wall(WallType.LEFT, availableLeftBottom, availableLeftTop);
        top = new Wall(WallType.TOP, availableLeftTop, availableRightTop);
        right = new Wall(WallType.RIGHT, availableRightTop, availableRightBottom);
        bottom.clockwiseWall = left;
        bottom.counterClockwiseWall = right;
        left.clockwiseWall = top;
        left.counterClockwiseWall = bottom;
        top.clockwiseWall = right;
        top.counterClockwiseWall = left;
        right.clockwiseWall = bottom;
        right.counterClockwiseWall = top;

        battleField = new Rectangle(0, 0, width + x * 2, height + y * 2);
        availableBattleFieldRectangle = new Rectangle2D.Double(x - 1, y - 1, width + 2, height + 2);
        exactAvailableBattleFieldRectangle = new Rectangle2D.Double(x, y, width, height);

        center = new LXXPoint(rightX / 2, topY / 2);

        this.width = width;
        this.height = height;

        noSmoothX = new Interval((int) WALL_STICK, width - (int) WALL_STICK);
        noSmoothY = new Interval((int) WALL_STICK, height - (int) WALL_STICK);
    }

    // this method is called very often, so keep it optimal
    public Wall getWall(LXXPoint pos, double heading) {
        final double normalHeadingTg = QuickMath.tan(heading % LXXConstants.RADIANS_90);
        if (heading < LXXConstants.RADIANS_90) {
            final double rightTopTg = (rightTop.x - pos.x) / (rightTop.y - pos.y);
            if (normalHeadingTg < rightTopTg) {
                return top;
            } else {
                return right;
            }
        } else if (heading < LXXConstants.RADIANS_180) {
            final double rightBottomTg = pos.y / (rightBottom.x - pos.x);
            if (normalHeadingTg < rightBottomTg) {
                return right;
            } else {
                return bottom;
            }
        } else if (heading < LXXConstants.RADIANS_270) {
            final double leftBottomTg = pos.x / pos.y;
            if (normalHeadingTg < leftBottomTg) {
                return bottom;
            } else {
                return left;
            }
        } else if (heading < LXXConstants.RADIANS_360) {
            final double leftTopTg = (leftTop.y - pos.y) / pos.x;
            if (normalHeadingTg < leftTopTg) {
                return left;
            } else {
                return top;
            }
        }
        throw new IllegalArgumentException("Invalid heading: " + heading);
    }

    public double getBearingOffsetToWall(LXXPoint pnt, double heading) {
        return Utils.normalRelativeAngle(getWall(pnt, heading).wallType.fromCenterAngle - heading);
    }

    public double getDistanceToWall(Wall wall, LXXPoint pnt) {
        switch (wall.wallType) {
            case TOP:
                return availableTopY - pnt.y;
            case RIGHT:
                return availableRightX - pnt.x;
            case BOTTOM:
                return pnt.y - availableBottomY;
            case LEFT:
                return pnt.x - availableLeftX;
            default:
                throw new IllegalArgumentException("Unknown wallType: " + wall.wallType);
        }
    }

    public double smoothWalls(LXXPoint pnt, double desiredHeading, boolean isClockwise) {
        return smoothWall(getWall(pnt, desiredHeading), pnt, desiredHeading, isClockwise);
    }

    private double smoothWall(Wall wall, LXXPoint pnt, double desiredHeading, boolean isClockwise) {
        final double adjacentLeg = max(0, getDistanceToWall(wall, pnt) - 4);
        if (WALL_STICK < adjacentLeg) {
            return desiredHeading;
        }
        double smoothAngle;
        smoothAngle = (QuickMath.acos(adjacentLeg / WALL_STICK) + LXXConstants.RADIANS_4) * (isClockwise ? 1 : -1);
        final double baseAngle = wall.wallType.fromCenterAngle;
        double smoothedAngle = Utils.normalAbsoluteAngle(baseAngle + smoothAngle);
        final Wall secondWall = isClockwise ? wall.clockwiseWall : wall.counterClockwiseWall;
        return smoothWall(secondWall, pnt, smoothedAngle, isClockwise);
    }

    public boolean contains(APoint point) {
        return availableBattleFieldRectangle.contains(point.getX(), point.getY());
    }

    public class Wall {

        public final WallType wallType;
        public final APoint ccw;
        public final APoint cw;

        private Wall clockwiseWall;
        private Wall counterClockwiseWall;

        private Wall(WallType wallType, APoint ccw, APoint cw) {
            this.wallType = wallType;
            this.ccw = ccw;
            this.cw = cw;
        }
    }

    public static enum WallType {

        TOP(LXXConstants.RADIANS_0, LXXConstants.RADIANS_90, LXXConstants.RADIANS_270),
        RIGHT(LXXConstants.RADIANS_90, LXXConstants.RADIANS_180, LXXConstants.RADIANS_0),
        BOTTOM(LXXConstants.RADIANS_180, LXXConstants.RADIANS_270, LXXConstants.RADIANS_90),
        LEFT(LXXConstants.RADIANS_270, LXXConstants.RADIANS_0, LXXConstants.RADIANS_180);

        public final double fromCenterAngle;

        public final double clockwiseAngle;
        public final double counterClockwiseAngle;

        private WallType(double fromCenterAngle, double clockwiseAngle, double counterClockwiseAngle) {
            this.fromCenterAngle = fromCenterAngle;


            this.clockwiseAngle = clockwiseAngle;
            this.counterClockwiseAngle = counterClockwiseAngle;
        }

    }
}
