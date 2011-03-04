package kc.serpent.movement;

import kc.serpent.utils.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

//Walking stick wall smoothing by David Alves
//Fancy stick wall smoothing by Simonton
public class WallSmoother {
    static final double WALKING_STICK_LENGTH = 170.0;
    static final double WALKING_SMOOTH_MARGIN = 25.0;
    static final double FANCY_SMOOTH_MARGIN = 18.01;
    static final double TURNING_RADIUS = 114.5450131316624;

    double battleFieldWidth;
    double battleFieldHeight;
    Rectangle2D smoothingField;
    double N, S, E, W;

    public void init(double battleFieldWidth, double battleFieldHeight) {
        this.battleFieldWidth = battleFieldWidth;
        this.battleFieldHeight = battleFieldHeight;
        smoothingField = KUtils.makeField(battleFieldWidth, battleFieldHeight, WALKING_SMOOTH_MARGIN);

        N = battleFieldHeight - FANCY_SMOOTH_MARGIN;
        S = FANCY_SMOOTH_MARGIN;
        E = battleFieldWidth - FANCY_SMOOTH_MARGIN;
        W = FANCY_SMOOTH_MARGIN;
    }

    public double walkingStickSmooth(Point2D.Double location, double heading, double distance, int direction, int orbitDirection) {
        double stick = Math.min(WALKING_STICK_LENGTH, distance);
        boolean top = false;
        boolean right = false;
        boolean bottom = false;
        boolean left = false;
        int smoothedWall = 0;//1 = top, 2 = right, 3 = bottom, 4 = left

        Point2D.Double projectedLocation = KUtils.projectMotion(location, heading, stick * direction);
        if (projectedLocation.getX() < WALKING_SMOOTH_MARGIN) {
            left = true;
        } else if (projectedLocation.getX() > battleFieldWidth - WALKING_SMOOTH_MARGIN) {
            right = true;
        }
        if (projectedLocation.getY() < WALKING_SMOOTH_MARGIN) {
            bottom = true;
        } else if (projectedLocation.getY() > battleFieldHeight - WALKING_SMOOTH_MARGIN) {
            top = true;
        }

        if (top) {
            if (right) {
                if (orbitDirection == 1) {
                    smoothedWall = 2;
                } else {
                    smoothedWall = 1;
                }
            } else if (left) {
                if (orbitDirection == 1) {
                    smoothedWall = 1;
                } else {
                    smoothedWall = 4;
                }
            } else {
                smoothedWall = 1;
            }
        } else if (bottom) {
            if (right) {
                if (orbitDirection == 1) {
                    smoothedWall = 3;
                } else {
                    smoothedWall = 2;
                }
            } else if (left) {
                if (orbitDirection == 1) {
                    smoothedWall = 4;
                } else {
                    smoothedWall = 3;
                }
            } else {
                smoothedWall = 3;
            }
        } else {
            if (right) {
                smoothedWall = 2;
            } else if (left) {
                smoothedWall = 4;
            }
        }

        if (smoothedWall == 1) {
            heading = orbitDirection * Math.acos((battleFieldHeight - WALKING_SMOOTH_MARGIN - location.getY()) / stick);
        } else if (smoothedWall == 2) {
            heading = (Math.PI / 2) + (orbitDirection * Math.acos((battleFieldWidth - WALKING_SMOOTH_MARGIN - location.getX()) / stick));
        } else if (smoothedWall == 3) {
            heading = Math.PI + (orbitDirection * Math.acos((location.getY() - WALKING_SMOOTH_MARGIN) / stick));
        } else if (smoothedWall == 4) {
            heading = (3 * Math.PI / 2) + (orbitDirection * Math.acos((location.getX() - WALKING_SMOOTH_MARGIN) / stick));
        }
        if (direction == -1 && smoothedWall != 0) {
            heading += Math.PI;
        }

        return heading;
    }

    public double fancyStickSmooth(double heading, double speed, double x, double y, int orbitDirection) {
        if (heading > Math.PI) {
            if (shouldSmooth(heading - Math.PI, speed, x - W, orbitDirection)) {
                heading = smoothAngle(speed, x - W, orbitDirection) + Math.PI;
            }
        } else if (heading < Math.PI) {//right
            if (shouldSmooth(heading, speed, E - x, orbitDirection)) {
                heading = smoothAngle(speed, E - x, orbitDirection);
            }
        }
        if (heading < Math.PI / 2 || heading > 3 * Math.PI / 2) {//top
            if (shouldSmooth(heading + Math.PI / 2, speed, N - y, orbitDirection)) {
                heading = smoothAngle(speed, N - y, orbitDirection) - Math.PI / 2;
            }
        } else if (heading > Math.PI / 2 && heading < 3 * Math.PI / 2) {//bottom
            if (shouldSmooth(heading - Math.PI / 2, speed, y - S, orbitDirection)) {
                heading = smoothAngle(speed, y - S, orbitDirection) + Math.PI / 2;
            }
        }

        return heading;
    }

    double smoothAngle(double speed, double wallDistance, int orbitDirection) {
        if (wallDistance < 0.01) {
            return (orbitDirection == 1 ? Math.PI : 0);
        }

        double heading = Math.acos((wallDistance - TURNING_RADIUS) / Math.sqrt(KUtils.sqr(TURNING_RADIUS) + KUtils.sqr(speed))) + Math.atan(speed / TURNING_RADIUS);

        return (orbitDirection == 1 ? heading : Math.PI - heading);
    }

    boolean shouldSmooth(double heading, double speed, double wallDistance, int orbitDirection) {
        wallDistance -= (speed * Math.sin(heading));

        if (wallDistance < 0) {
            return true;
        }

        wallDistance -= (1 + Math.sin(heading + (orbitDirection * Math.PI / 2))) * TURNING_RADIUS;

        return wallDistance < 0;
    }
}