package lxx;

import java.util.List;

public interface EnemySnapshot extends LXXRobot {

    void addVisit(double guessFactor);

    List<Double> getVisitedGuessFactors();

    long getLastDirChangeTime();

}
