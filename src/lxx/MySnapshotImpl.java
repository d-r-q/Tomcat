package lxx;

import lxx.utils.LXXPoint;
import robocode.util.Utils;

import java.util.LinkedList;

import static java.lang.Math.signum;

public class MySnapshotImpl extends RobotSnapshot2 {

    private final LinkedList<LXXPoint> last10Positions;
    private final double turnRateRadians;

    public MySnapshotImpl(LXXRobot2 currentState) {
        super(currentState);
        last10Positions = new LinkedList<LXXPoint>();
        turnRateRadians = 0;
    }

    public MySnapshotImpl(MySnapshotImpl prevState, LXXRobot2 currentState) {
        super(prevState, currentState);
        last10Positions = new LinkedList<LXXPoint>(prevState.getLast10Positions());
        last10Positions.add(new LXXPoint(currentState.getPosition()));
        if (last10Positions.size() > 10) {
            last10Positions.removeFirst();
        }

        this.turnRateRadians = prevState.getHeadingRadians() - currentState.getHeadingRadians();
    }

    public MySnapshotImpl(MySnapshotImpl state1, MySnapshotImpl state2, double interpolationK) {
        super(state1, state2, interpolationK);
        last10Positions = state2.getLast10Positions();
        turnRateRadians = state1.getTurnRateRadians() + (state2.getTurnRateRadians() - state1.getTurnRateRadians()) * interpolationK;
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

    public double getTurnRateRadians() {
        return turnRateRadians;
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
}
