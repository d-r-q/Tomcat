package lxx;

import lxx.bullets.BulletSnapshot;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.round;
import static java.lang.Math.signum;

public class MySnapshot extends RobotSnapshot {

    private List<BulletSnapshot> bullets;
    private final double gunCoolingRate;

    public MySnapshot(BasicRobot currentState) {
        super(currentState);
        bullets = currentState.getBulletsInAir();
        gunCoolingRate = currentState.getGunCoolingRate();
    }

    public MySnapshot(MySnapshot prevState, BasicRobot currentState, double last10TicksDist) {
        super(prevState, currentState, last10TicksDist);

        bullets = currentState.getBulletsInAir();
        gunCoolingRate = currentState.getGunCoolingRate();
    }

    public MySnapshot(MySnapshot state1, MySnapshot state2, double interpolationK) {
        super(state1, state2, interpolationK);
        bullets = state2.getBulletsInAir();
        gunCoolingRate = state2.gunCoolingRate;
    }

    public double getAbsoluteHeadingRadians() {
        if (signum(velocity) == 1) {
            return headingRadians;
        } else if (signum(velocity) == -1) {
            return Utils.normalAbsoluteAngle(headingRadians + Math.PI);
        } else if (lastDirection == 1) {
            return headingRadians;
        } else {
            return Utils.normalAbsoluteAngle(headingRadians + Math.PI);
        }
    }

    public List<BulletSnapshot> getBulletsInAir() {
        return bullets;
    }

    public int getTurnsToGunCool() {
        return (int) round(gunHeat / gunCoolingRate);
    }

    public void setBullets(List<BulletSnapshot> bullets) {
        this.bullets = bullets;
    }
}
