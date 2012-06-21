package lxx;

public interface LXXRobotSnapshot extends LXXRobotState {

    double getAcceleration();

    double getAbsoluteHeadingRadians();

    int getLastDirection();

    long getSnapshotTime();

    double getLast10TicksDist();

}
