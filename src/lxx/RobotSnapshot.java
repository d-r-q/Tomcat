package lxx;

import lxx.utils.*;

import static java.lang.Math.signum;

public abstract class RobotSnapshot implements LXXRobotSnapshot {

    private final LXXPoint position;
    private final double speed;
    private final BattleField battleField;
    private final double energy;
    private final String name;
    protected final double headingRadians;
    protected final double velocity;
    protected final int lastDirection;
    protected final double acceleration;
    protected final long snapshotTime;
    protected double gunHeat;

    public RobotSnapshot(LXXRobot currentState) {
        headingRadians = currentState.getHeadingRadians();
        speed = currentState.getSpeed();
        velocity = currentState.getVelocity();
        position = new LXXPoint(currentState);
        battleField = currentState.getBattleField();
        energy = currentState.getEnergy();
        name = currentState.getName();
        snapshotTime = currentState.getTime();
        gunHeat = currentState.getGunHeat();

        lastDirection = 1;
        acceleration = 0;
    }

    public RobotSnapshot(LXXRobotSnapshot prevState, LXXRobot currentState) {
        snapshotTime = currentState.getTime();
        headingRadians = currentState.getHeadingRadians();

        speed = currentState.getSpeed();
        velocity = currentState.getVelocity();
        position = new LXXPoint(currentState);
        battleField = currentState.getBattleField();
        energy = currentState.getEnergy();
        name = currentState.getName();
        gunHeat = currentState.getGunHeat();

        if (velocity != 0) {
            lastDirection = (int) signum(velocity);
        } else {
            lastDirection = prevState.getLastDirection();
        }

        acceleration = LXXUtils.calculateAcceleration(prevState, currentState);
    }

    public RobotSnapshot(LXXRobotSnapshot state1, LXXRobotSnapshot state, double interpolationK) {
        snapshotTime = (long) (state1.getSnapshotTime() + (state.getSnapshotTime() - state1.getSnapshotTime()) * interpolationK);
        headingRadians = state1.getHeadingRadians() + (state.getHeadingRadians() - state1.getHeadingRadians()) * interpolationK;
        speed = state1.getSpeed() + (state.getSpeed() - state1.getSpeed()) * interpolationK;
        velocity = state1.getVelocity() + (state.getVelocity() - state1.getVelocity()) * interpolationK;
        energy = state1.getEnergy() + (state.getEnergy() - state1.getEnergy()) * interpolationK;
        gunHeat = state1.getGunHeat() + (state.getGunHeat() - state1.getGunHeat()) * interpolationK;
        position = new LXXPoint(state1.getX() + (state.getX() - state1.getX()) * interpolationK,
                state1.getY() + (state.getY() - state1.getY()) * interpolationK);

        acceleration = state1.getAcceleration() + (state.getAcceleration() - state1.getAcceleration()) * interpolationK;

        battleField = state1.getBattleField();
        name = state1.getName();

        lastDirection = state.getLastDirection();
    }

    public double getVelocity() {
        return velocity;
    }

    public double getSpeed() {
        return speed;
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public double aDistance(APoint p) {
        return position.aDistance(p);
    }

    public double angleTo(APoint pnt) {
        return position.angleTo(pnt);
    }

    public APoint project(double alpha, double distance) {
        return position.project(alpha, distance);
    }

    public APoint project(DeltaVector dv) {
        return position.project(dv);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobotSnapshot that = (RobotSnapshot) o;

        return snapshotTime == that.snapshotTime && name.equals(that.name);
    }

    public int hashCode() {
        int result = (int) (snapshotTime ^ (snapshotTime >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    public String getName() {
        return name;
    }

    public double getHeadingRadians() {
        return headingRadians;
    }

    public BattleField getBattleField() {
        return battleField;
    }

    public double getEnergy() {
        return energy;
    }

    public LXXPoint getPosition() {
        return position;
    }

    public double getGunHeat() {
        return gunHeat;
    }

    public double getWidth() {
        throw new UnsupportedOperationException();
    }

    public double getHeight() {
        throw new UnsupportedOperationException();
    }

    public double getAcceleration() {
        return acceleration;
    }

    public int getLastDirection() {
        return lastDirection;
    }

    public long getSnapshotTime() {
        return snapshotTime;
    }

}
