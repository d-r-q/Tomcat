package lxx;

import robocode.Rules;
import robocode.util.Utils;

import java.util.List;

import static java.lang.Math.*;

public class EnemySnapshot extends RobotSnapshot {

    private final long lastDirChangeTime;
    private final List<Double> visits;
    private final double turnRateRadians;

    public EnemySnapshot(LXXRobot currentState, List<Double> visits) {
        super(currentState);
        this.visits = visits;
        lastDirChangeTime = 0;
        turnRateRadians = 0;
    }

    public EnemySnapshot(EnemySnapshot prevState, LXXRobot currentState) {
        super(prevState, currentState);

        if (currentState.isAlive() && signum(prevState.getAcceleration()) != signum(getAcceleration()) &&
                getSpeed() > 0.1 && getSpeed() < 7.9) {
            lastDirChangeTime = currentState.getTime() - 1;
        } else {
            lastDirChangeTime = prevState.getLastDirChangeTime();
        }

        double turnRateRadians = Utils.normalRelativeAngle(currentState.getHeadingRadians() - prevState.getHeadingRadians());

        if (abs(turnRateRadians) > Rules.MAX_TURN_RATE_RADIANS + 0.01) {
            if (currentState.getTime() == prevState.getSnapshotTime() + 1) {
                System.out.println("[WARN] new headingRadians delta: " + toDegrees(turnRateRadians));
                turnRateRadians = Rules.MAX_TURN_RATE_RADIANS * signum(turnRateRadians);
            } else {
                turnRateRadians = turnRateRadians / (snapshotTime - prevState.getSnapshotTime());
                if (abs(turnRateRadians) > Rules.MAX_TURN_RATE_RADIANS) {
                    turnRateRadians = Rules.MAX_TURN_RATE_RADIANS * signum(turnRateRadians);
                }
            }
        }
        this.turnRateRadians = turnRateRadians;

        visits = prevState.getVisitedGuessFactors();
    }

    public EnemySnapshot(EnemySnapshot state1, EnemySnapshot state2, double interpolationK) {
        super(state1, state2, interpolationK);
        lastDirChangeTime = (long) (state1.getLastDirChangeTime() + (state2.getLastDirChangeTime() - state1.getLastDirChangeTime()) * interpolationK);
        visits = state2.getVisitedGuessFactors();
        turnRateRadians = state1.getTurnRateRadians() + (state2.getTurnRateRadians() - state1.getTurnRateRadians()) * interpolationK;
    }

    public long getLastDirChangeTime() {
        return lastDirChangeTime;
    }

    public void addVisit(double guessFactor) {
        visits.add(guessFactor);
    }

    public List<Double> getVisitedGuessFactors() {
        return visits;
    }

    public double getTurnRateRadians() {
        return turnRateRadians;
    }

    public double getAbsoluteHeadingRadians() {
        if (velocity >= 0) {
            return headingRadians;
        } else {
            return Utils.normalAbsoluteAngle(headingRadians + Math.PI);
        }
    }

    public void setGunHeat(double gunHeat) {
        this.gunHeat = gunHeat;
    }
}
