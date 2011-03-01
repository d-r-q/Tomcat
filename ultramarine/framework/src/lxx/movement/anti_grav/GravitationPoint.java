package lxx.movement.anti_grav;

import lxx.utils.LXXPoint;

/**
 * User: jdev
 * Date: 25.07.2009
 */
public class GravitationPoint extends LXXPoint {

    public static final double DEFAULT_POWER = 1;

    public double power;
    public double effectiveDistance = 200;
    public boolean inner = true;

    public GravitationPoint(double x, double y, double power) {
        super(x, y);
        this.power = power;
    }

    public GravitationPoint(double x, double y) {
        super(x, y);
        this.power = DEFAULT_POWER;
    }


    public GravitationPoint(LXXPoint point, double power) {
        super(point);

        this.power = power;
    }

    public GravitationPoint(double x, double y, double power, double effectiveDistance) {
        this(x, y, power);
        this.effectiveDistance = effectiveDistance;
    }

    public GravitationPoint(LXXPoint pos, double power, int effectiveDistance) {
        this(pos.x, pos.y, power, effectiveDistance);
    }

    public String toString() {
        return super.toString() + ", " + power;
    }
}
