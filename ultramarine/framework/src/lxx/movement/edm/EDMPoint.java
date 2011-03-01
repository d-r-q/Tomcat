package lxx.movement.edm;

import lxx.utils.APoint;
import lxx.utils.LXXPoint;

/**
 * Class to represent Point in EDM method. Externds Point2D.Double and adds field <code>avgDistance</code>,
 * which means averegene distance to enemies in EDM
 *
 * @author jdev
 */
public class EDMPoint extends LXXPoint {

    public double avgDistance;

    public EDMPoint(double x, double y) {
        super(x, y);
    }

    public double distance(APoint p) {
        return distance(p.getX(), p.getY());
    }

    public String toString() {
        return super.toString() + ": " + avgDistance;
    }
}
