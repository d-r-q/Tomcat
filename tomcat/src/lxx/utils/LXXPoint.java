/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 25.07.2009
 */
public class LXXPoint extends Point2D.Double implements APoint, Serializable {

    private static final NumberFormat format = new DecimalFormat();

    static {
        format.setMaximumFractionDigits(2);
    }

    public LXXPoint(double x, double y) {
        super(x, y);
    }

    public LXXPoint(LXXPoint point) {
        super(point.x, point.y);
    }

    public LXXPoint() {
        super();
    }

    public LXXPoint(APoint original) {
        this(original.getX(), original.getY());
    }

    public double aDistance(APoint p) {
        return distance(p.getX(), p.getY());
    }

    public double aDistance(LXXPoint p) {
        return distance(p.x, p.y);
    }

    public double aDistanceSq(LXXPoint p) {
        return distanceSq(p.x, p.y);
    }

    public String toString() {
        return "[" + format.format(x) + ", " + format.format(y) + "]";
    }

    public LXXPoint project(double alpha, double dist) {
        return new LXXPoint(x + QuickMath.sin(alpha) * dist, y + QuickMath.cos(alpha) * dist);
    }

    public double angleTo(APoint another) {
        return LXXUtils.angle(this, another);
    }

    public double angleTo(LXXPoint another) {
        return LXXUtils.angle(this.x, this.y, another.x, another.y);
    }

    public APoint project(DeltaVector result) {
        return project(result.getAlphaRadians(), result.getLength());
    }

    public double distanceToWall(BattleField battleField, double direction) {
        return distanceToWall(this, battleField, direction);
    }

    public static double distanceToWall(LXXPoint pnt, BattleField battleField, double direction) {
        final BattleField.Wall w = battleField.getWall(pnt, direction);
        return battleField.getDistanceToWall(w, pnt) / abs(QuickMath.cos(direction - w.wallType.fromCenterAngle));
    }
}