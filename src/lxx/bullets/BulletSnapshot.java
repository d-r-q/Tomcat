package lxx.bullets;

import lxx.LXXRobotSnapshot2;
import lxx.utils.APoint;
import robocode.util.Utils;

public class BulletSnapshot {

    private final LXXRobotSnapshot2 ownerState;
    private final LXXRobotSnapshot2 targetState;
    private final double noBearingOffsetRadians;
    private final double travelledDistance;
    private final double speed;
    private final long launchTime;

    public BulletSnapshot(LXXRobotSnapshot2 ownerState, LXXRobotSnapshot2 targetState, double noBearingOffsetRadians, double travelledDistance,
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

    public LXXRobotSnapshot2 getTargetState() {
        return targetState;
    }

    public LXXRobotSnapshot2 getOwnerState() {
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
