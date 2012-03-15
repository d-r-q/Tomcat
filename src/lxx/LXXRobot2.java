package lxx;

public interface LXXRobot2 extends LXXRobotState2 {

    long getTime();

    boolean isAlive();

    LXXRobotSnapshot2 getPrevSnapshot();

    LXXRobotSnapshot2 getCurrentSnapshot();

    double getFirePower();

    int getRound();

}
