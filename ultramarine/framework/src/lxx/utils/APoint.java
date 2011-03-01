package lxx.utils;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public interface APoint {

    APoint getPosition();
    double getX();
    double getY();
    double aDistance(APoint p);
    double distance(double x, double y);
}
