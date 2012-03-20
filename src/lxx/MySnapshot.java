package lxx;

import lxx.bullets.BulletSnapshot;
import lxx.utils.LXXPoint;
import robocode.util.Utils;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.round;
import static java.lang.Math.signum;

public class MySnapshot extends RobotSnapshot {

    // todo(zhidkov): store only distance
    private final LinkedList<LXXPoint> last10Positions;
    private List<BulletSnapshot> bullets;
    private final double gunCoolingRate;

    public MySnapshot(BasicRobot currentState) {
        super(currentState);
        last10Positions = new LinkedList<LXXPoint>();
        bullets = currentState.getBulletsInAir();
        gunCoolingRate = currentState.getGunCoolingRate();
    }

    public MySnapshot(MySnapshot prevState, BasicRobot currentState) {
        super(prevState, currentState);
        last10Positions = new LinkedList<LXXPoint>(prevState.getLast10Positions());

        bullets = currentState.getBulletsInAir();
        gunCoolingRate = currentState.getGunCoolingRate();
    }

    public MySnapshot(MySnapshot state1, MySnapshot state2, double interpolationK) {
        super(state1, state2, interpolationK);
        last10Positions = state2.getLast10Positions();
        bullets = state2.getBulletsInAir();
        gunCoolingRate = state2.gunCoolingRate;
    }

    public double getLast10TicksDist() {
        if (last10Positions.size() == 0) {
            return 0;
        }
        return last10Positions.getFirst().aDistance(last10Positions.getLast());
    }

    public LinkedList<LXXPoint> getLast10Positions() {
        return last10Positions;
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
