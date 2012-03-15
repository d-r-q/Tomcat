package lxx;

import lxx.utils.APoint;
import lxx.utils.BattleField;
import lxx.utils.LXXPoint;

public interface LXXRobotState2 extends APoint {

    String getName();

    double getWidth();

    double getHeight();

    double getHeadingRadians();

    double getVelocity();

    double getSpeed();

    BattleField getBattleField();

    double getEnergy();

    LXXPoint getPosition();

    double getGunHeat();

    boolean equals(Object another);

    int hashCode();

}
