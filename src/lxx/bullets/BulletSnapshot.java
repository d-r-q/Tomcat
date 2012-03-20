package lxx.bullets;

import lxx.LXXRobotSnapshot;
import lxx.utils.APoint;
import robocode.util.Utils;

public class BulletSnapshot {

    private final LXXRobotSnapshot ownerState;
    private final LXXRobotSnapshot targetState;
    private final double noBearingOffsetRadians;
    private final double travelledDistance;
    private final double speed;
    private final long launchTime;

    public BulletSnapshot(LXXRobotSnapshot ownerState, LXXRobotSnapshot targetState, double noBearingOffsetRadians, double travelledDistance,
                          double speed, long launchTime) {
        this.ownerState = ownerState;
        this.targetState = targetState;
        this.noBearingOffsetRadians = noBearingOffsetRadians;
        this.travelledDistance = travelledDistance;
        this.speed = speed;
        this.launchTime = launchTime;
    }

    public double getFlightTime(APoint pnt) {
        return (ownerState.aDistance(pnt) - travelledDistance) / speed;
    }

    public LXXRobotSnapshot getTargetState() {
        return targetState;
    }

    public LXXRobotSnapshot getOwnerState() {
        return ownerState;
    }

    public double getBearingOffsetRadians(APoint pnt) {
        return Utils.normalRelativeAngle(ownerState.angleTo(pnt) - noBearingOffsetRadians);
    }

    public double getTravelledDistance() {
        return travelledDistance;
    }

    public double getSpeed() {
        return speed;
    }

    public long getLaunchTime() {
        return launchTime;
    }
}
