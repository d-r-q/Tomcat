package lxx;

public interface LXXRobotSnapshot2 extends LXXRobotState2 {

    double getAcceleration();

    double getTurnRateRadians();

    double getAbsoluteHeadingRadians();

    int getLastDirection();

    long getSnapshotTime();

}
