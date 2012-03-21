package lxx;

public interface LXXRobot extends LXXRobotState {

    long getTime();

    boolean isAlive();

    LXXRobotSnapshot getPrevSnapshot();

    LXXRobotSnapshot getCurrentSnapshot();

    double getFirePower();

    int getRound();

}
