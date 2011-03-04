package kc.serpent.utils;

import robocode.util.Utils;

import java.awt.geom.Point2D;

public abstract class Wave {
    public Point2D.Double source;
    public boolean isReal;
    public long fireTime;
    public int orbitDirection;
    public double absoluteBearing;
    public double maxEscapeAngle;
    public double speed;
    public double radius;
    public double distance;

    public double normalizedDistance;
    public double latVelocity;
    public double accel;
    public double vChangeTimer;
    public double lastDTraveled;
    public double wallAhead;
    public double wallReverse;

    public double getGF(Point2D.Double location) {
        double angleOffset = Utils.normalRelativeAngle(KUtils.absoluteBearing(source, location) - absoluteBearing);
        return angleOffset / maxEscapeAngle * orbitDirection;
    }

    public double getGF(double angle) {
        double angleOffset = Utils.normalRelativeAngle(angle - absoluteBearing);
        return angleOffset / maxEscapeAngle * orbitDirection;
    }

    public void setRadius(long gameTime) {
        radius = (gameTime - fireTime) * speed;
    }
}
