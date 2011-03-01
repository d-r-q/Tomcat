package lxx.utils;

import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * User: jdev
 * Date: 25.07.2009
 */
public class LXXPoint extends Point2D.Double implements APoint {

    private static NumberFormat format = new DecimalFormat();

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

    public LXXPoint(Double ethalon) {
        this(ethalon.x, ethalon.y);
    }

    public APoint getPosition() {
        return this;
    }

    public double aDistance(APoint p) {
        return distance(p.getX(), p.getY());
    }

    public String toString() {
        return "[" + format.format(x) + ", " + format.format(y) + "]";
    }
}