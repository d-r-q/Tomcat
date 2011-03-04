package kc.serpent.utils;

import robocode.AdvancedRobot;

import java.awt.geom.Point2D;

//Based off of PEZ's RobotPredictor
public class RobotPredictor {
    double ahead = 0;
    double turnRightRadians = 0;
    double maxVelocity = 8;

    public void setAhead(double d) {
        ahead = d;
    }

    public void setTurnRightRadians(double turn) {
        turnRightRadians = turn;
    }

    public void setMaxVelocity(double v) {
        maxVelocity = v;
    }

    public Point2D.Double getNextLocation(AdvancedRobot robot) {
        return KUtils.projectMotion(new Point2D.Double(robot.getX(), robot.getY()),
                robot.getHeadingRadians() + turnIncrement(turnRightRadians, robot.getVelocity()),
                nextVelocity(robot.getVelocity(), KUtils.sign(ahead), maxVelocity));
    }

    public static double maxTurn(double v) {
        return Math.PI / 18 - Math.abs(v) * Math.PI / 240;
    }

    public static double turnIncrement(double t, double v) {
        double max = maxTurn(v);
        return KUtils.minMax(t, -max, max);
    }

    public static double nextVelocity(double v, int d, double maxV) {
        return Math.abs(v) > maxV ?
                v + (KUtils.sign(v) * Math.max(maxV - Math.abs(v), -2)) :
                KUtils.minMax(v + (d * (d * v >= 0 ? 1 : 2)), -maxV, maxV);
    }
}
