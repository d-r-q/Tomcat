package lxx.movement.anti_grav;

import lxx.utils.LXXPoint;

/**
 * User: jdev
 * Date: 25.07.2009
 */
public class OldingPoint extends GravitationPoint {

    public final double oldingRate;

    public OldingPoint(double x, double y, double power, double oldingRate, double effectiveDistance) {
        super(x, y, power, effectiveDistance);
        this.oldingRate = oldingRate;
    }

    public OldingPoint(double x, double y, double power, double oldingRate) {
        super(x, y, power);
        this.oldingRate = oldingRate;
    }

    public OldingPoint(LXXPoint p, double power, double oldingRate) {
        super(p, power);
        this.oldingRate = oldingRate;
    }

    public OldingPoint(LXXPoint destionation, int power, double oldingRate, double effectiveDistance) {
        this(destionation.x, destionation.y, power, oldingRate, effectiveDistance);
    }

    public String toString() {
        return super.toString() + ", " + oldingRate;
    }
}
