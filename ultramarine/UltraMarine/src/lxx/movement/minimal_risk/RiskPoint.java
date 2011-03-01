package lxx.movement.minimal_risk;

import lxx.utils.LXXPoint;

/**
 * User: jdev
 * Date: 17.10.2009
 */
public class RiskPoint extends LXXPoint {

    public final double risk;

    public RiskPoint(double x, double y, double risk) {
        super(x, y);
        this.risk = risk;
    }

    public RiskPoint(LXXPoint point, double risk) {
        super(point);
        this.risk = risk;
    }

    public RiskPoint(double risk) {
        this.risk = risk;
    }
}
