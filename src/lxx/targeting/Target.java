/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting;

import lxx.*;
import lxx.utils.*;
import robocode.*;
import robocode.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 25.07.2009
 */

public class Target implements EnemySnapshot, Serializable, LXXRobot2 {

    private final List<Event> eventsList = new ArrayList<Event>(15);

    private final String name;
    private transient final BasicRobot owner;

    private boolean isAlive = true;

    private TargetState prevState;
    private TargetState state;
    private final TargetInfo info;
    private TargetData targetData;
    private boolean isRammingNow;
    private EnemySnapshotImpl prevSnapshot;
    private EnemySnapshotImpl currentSnapshot;

    private LXXPoint position = new LXXPoint();
    private double energy = 100;
    private long enemyLastHitTime;
    private double enemyLastCollectedEnergy;
    private long myLastHitTime;
    private double myLastDamage;
    private double headingRadians;
    private double velocity;
    private int enemyHitRobotEnergyLoss;
    private double enemyLastFirePower;
    private double gunHeat;

    public Target(BasicRobot owner, String name, TargetData targetData) {
        this.owner = owner;
        this.name = name;
        this.targetData = targetData;

        info = new TargetInfo();
    }

    public void addEvent(Event e) {
        eventsList.add(e);
    }

    public void update() {
        ensureValid();
        prevState = state;
        prevSnapshot = currentSnapshot;

        state = mergeEvents();
        if (prevSnapshot == null) {
            currentSnapshot = new EnemySnapshotImpl(this, targetData.getVisitedGuessFactors());
        } else {
            currentSnapshot = new EnemySnapshotImpl(prevSnapshot, this);
        }
        if (prevState != null) {
            if (isFireLastTick() != isFireLastTick2()) {
                isFireLastTick2();
            }
        }
        state.calculateState(prevState);
        updateState();

        if (abs(currentSnapshot.getGunHeat() - state.gunHeat) > 0.01) {
            updateState();
        }
        if (getExpectedEnergy() != getExpectedEnergy2()) {
            assert getExpectedEnergy() != getExpectedEnergy2();
        }
        if (isHitWall() != isHitWall2()) {
            assert isHitWall() != isHitWall2();
        }


        if (getExpectedEnergy() != getExpectedEnergy2()) {
            assert getExpectedEnergy() != getExpectedEnergy2();
        }

        if (prevState != null) {
            if (isFireLastTick() != isFireLastTick2()) {
                assert isFireLastTick() == isFireLastTick2();
            }
        }


        if (prevState != null && prevState.time + 1 != state.time && prevState.time >= 10) {
            // todo (zhidkov): notify listeners
            System.out.println("[WARN]: scans for " + getName() + " skipped: " + (state.time - prevState.time));
        }

        isRammingNow = ((LXXUtils.anglesDiff(angleTo(owner), getAbsoluteHeadingRadians()) < LXXConstants.RADIANS_30 &&
                getSpeed() > 0) || (owner.aDistance(this) < 50));

        if (prevSnapshot != null && prevState != null && (prevSnapshot.getEnergy() != prevState.getEnergy())) {
            assert prevSnapshot.getEnergy() == prevState.getEnergy();
        }
        assert currentSnapshot.getEnergy() == state.getEnergy();
        if (abs(currentSnapshot.getGunHeat() - state.gunHeat) > 0.01) {
            assert currentSnapshot.getGunHeat() == state.gunHeat;
        }
        assert currentSnapshot.getHeadingRadians() == state.getHeadingRadians();
        if (currentSnapshot.getLastDirChangeTime() != getLastDirChangeTime()) {
            assert currentSnapshot.getLastDirChangeTime() == getLastDirChangeTime();
        }
        assert currentSnapshot.getPosition().equals(getPosition());
        assert currentSnapshot.getSpeed() == state.getSpeed();
        if (currentSnapshot.getTurnRateRadians() != state.getTurnRateRadians()) {
            prevState.calculateState(state);
            assert currentSnapshot.getTurnRateRadians() == state.getTurnRateRadians();
        }
        assert currentSnapshot.getVelocity() == state.getVelocity();

        if (getFirePower() != enemyLastFirePower) {
            assert getFirePower() == enemyLastFirePower;
        }


        eventsList.clear();
    }

    private void updateState() {
        enemyHitRobotEnergyLoss = 0;
        if (prevSnapshot == null) {
            gunHeat = LXXConstants.INITIAL_GUN_HEAT - owner.getGunCoolingRate() * owner.getTime();
        } else if (isFireLastTick2()) {
            final double firePower = getExpectedEnergy2() - energy;
            gunHeat = Rules.getGunHeat(firePower);
            enemyLastFirePower = firePower;
        }

        gunHeat = max(0, gunHeat - owner.getGunCoolingRate());
        currentSnapshot.setGunHeat(gunHeat);
    }

    private TargetState mergeEvents() {
        final TargetState newState = createState();
        info.enemyHitRobotEnergyLoss = 0;

        for (Event event : eventsList) {
            if (event instanceof ScannedRobotEvent) {
                final ScannedRobotEvent e = (ScannedRobotEvent) event;
                processScannedRobotEvent(newState, e);
            } else if (event instanceof HitRobotEvent) {
                final HitRobotEvent e = (HitRobotEvent) event;
                processHitRobotEvent(newState, e);
            } else if (event instanceof BulletHitEvent) {
                final BulletHitEvent e = (BulletHitEvent) event;
                processBulletHitEvent(newState, e);
            } else if (event instanceof HitByBulletEvent) {
                final HitByBulletEvent e = (HitByBulletEvent) event;
                processHitByBulletEvent(newState, e);
            } else if (event instanceof RobotDeathEvent) {
                processRobotDeathEvent(newState);
            }
        }

        return newState;
    }

    private void processRobotDeathEvent(TargetState newState) {
        newState.energy = 0;
        energy = 0;
    }

    private TargetState createState() {
        final TargetState newState = new TargetState();

        if (prevState != null) {
            newState.headingRadians = prevState.headingRadians;
            newState.velocity = prevState.velocity;
            newState.position = prevState.position;
            newState.energy = prevState.energy;
        } else {
            newState.headingRadians = 0;
            newState.velocity = 0;
            newState.position = new LXXPoint();
            newState.energy = 100;
        }

        return newState;
    }

    private void processHitByBulletEvent(TargetState newState, HitByBulletEvent e) {
        final double bulletPower = e.getBullet().getPower();
        newState.energy = prevState.energy + LXXUtils.getReturnedEnergy(bulletPower);
        energy = prevSnapshot.getEnergy() + LXXUtils.getReturnedEnergy(bulletPower);
        info.enemyLastHitTime = e.getTime();
        enemyLastHitTime = e.getTime();
        info.enemyLastCollectedEnergy = LXXUtils.getReturnedEnergy(bulletPower);
        enemyLastCollectedEnergy = LXXUtils.getReturnedEnergy(bulletPower);
    }

    private void processBulletHitEvent(TargetState newState, BulletHitEvent e) {
        info.myLastHitTime = e.getTime();
        myLastHitTime = e.getTime();
        info.myLastDamage = Rules.getBulletDamage(e.getBullet().getPower());
        myLastDamage = Rules.getBulletDamage(e.getBullet().getPower());

        newState.position = new LXXPoint(e.getBullet().getX(), e.getBullet().getY());
        position = new LXXPoint(e.getBullet().getX(), e.getBullet().getY());
        newState.energy = e.getEnergy();
        energy = e.getEnergy();
    }

    private void processHitRobotEvent(TargetState newState, HitRobotEvent e) {
        double absoluteBearing = owner.getHeadingRadians() + e.getBearingRadians();

        newState.position = (LXXPoint) owner.project(absoluteBearing, LXXConstants.ROBOT_SIDE_SIZE);
        position = newState.position;
        newState.energy = e.getEnergy();
        energy = e.getEnergy();
        info.enemyHitRobotEnergyLoss += LXXConstants.ROBOT_HIT_DAMAGE;
        enemyHitRobotEnergyLoss += LXXConstants.ROBOT_HIT_DAMAGE;
    }

    private void processScannedRobotEvent(TargetState newState, ScannedRobotEvent e) {
        final double absoluteBearing = owner.getHeadingRadians() + e.getBearingRadians();
        final APoint coords = owner.project(absoluteBearing, e.getDistance());

        newState.position = (LXXPoint) coords;
        position = newState.position;
        newState.headingRadians = e.getHeadingRadians();
        headingRadians = e.getHeadingRadians();
        newState.velocity = e.getVelocity();
        velocity = e.getVelocity();
        newState.energy = e.getEnergy();
        energy = e.getEnergy();
        if (state != null && signum(getAcceleration()) != signum(LXXUtils.calculateAcceleration(state, newState)) &&
                abs(e.getVelocity()) > 0.1 && abs(e.getVelocity()) < 7.9) {
            info.lastDirChangeTime = e.getTime() - 1;
        }

        isAlive = true;
    }

    public long getUpdateTime() {
        ensureValid();
        return currentSnapshot.getSnapshotTime();
    }

    // todo(zhidkov): will be used in melee radar
    @SuppressWarnings({"UnusedDeclaration"})
    public int getLatency() {
        ensureValid();
        return (int) (owner.getTime() - currentSnapshot.getSnapshotTime());
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        ensureValid();
        return owner.getTime();
    }

    public double getEnergy() {
        ensureValid();
        return energy;
    }

    public double getVelocity() {
        ensureValid();
        return velocity;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public EnemySnapshotImpl getPrevSnapshot() {
        return prevSnapshot;
    }

    public EnemySnapshotImpl getCurrentSnapshot() {
        return currentSnapshot;
    }

    public void setNotAlive() {
        ensureValid();
        isAlive = false;
    }

    public double getX() {
        ensureValid();
        return position.x;
    }

    public double getY() {
        ensureValid();
        return position.y;
    }

    public double aDistance(APoint p) {
        ensureValid();
        return position.aDistance(p);
    }

    public double angleTo(APoint target) {
        return position.angleTo(target);
    }

    public APoint project(double alpha, double distance) {
        return position.project(alpha, distance);
    }

    public APoint project(DeltaVector dv) {
        return position.project(dv);
    }

    public double getAbsoluteHeadingRadians() {
        ensureValid();
        if (velocity >= 0) {
            return headingRadians;
        } else {
            return Utils.normalAbsoluteAngle(headingRadians + Math.PI);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        return name.equals(target.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public double getAcceleration() {
        return currentSnapshot.getAcceleration();
    }

    private void ensureValid() {
        if (!owner.isAlive() && currentSnapshot != null) {
            throw new RuntimeException("Something wrong");
        }
    }

    public double getWidth() {
        ensureValid();
        return owner.getWidth();
    }

    public double getHeight() {
        ensureValid();
        return owner.getHeight();
    }

    public double getHeadingRadians() {
        return headingRadians;
    }

    public boolean isFireLastTick() {
        ensureValid();
        if (prevState != null && prevState.gunHeat >= owner.getGunCoolingRate()) {
            return false;
        }
        double energyDiff = getExpectedEnergy() - state.energy;
        return energyDiff > 0 && energyDiff < 3.1;
    }

    public double getExpectedEnergy() {
        ensureValid();
        if (prevState == null) {
            return state.energy;
        }

        double expectedEnergy = prevState.energy;
        if (owner.getTime() == info.myLastHitTime) {
            expectedEnergy -= info.myLastDamage;
        }

        if (owner.getTime() == info.enemyLastHitTime) {
            expectedEnergy += info.enemyLastCollectedEnergy;
        }

        if (isHitWall()) {
            expectedEnergy -= Rules.getWallHitDamage(abs(prevState.velocity) + prevState.acceleration);
        }

        expectedEnergy -= info.enemyHitRobotEnergyLoss;

        return expectedEnergy;
    }

    public boolean isHitWall() {
        ensureValid();
        if (prevState == null) {
            return false;
        }

        if (abs(prevState.velocity) - abs(state.velocity) > Rules.DECELERATION) {
            return true;
        }

        return prevState.position.aDistance(state.position) -
                prevState.position.aDistance(prevState.position.project(getAbsoluteHeadingRadians(), abs(state.velocity))) < -1.1;
    }

    public boolean isFireLastTick2() {
        ensureValid();
        if (prevSnapshot != null && prevSnapshot.getGunHeat() >= owner.getGunCoolingRate()) {
            return false;
        }
        double energyDiff = getExpectedEnergy2() - energy;
        return energyDiff > 0 && energyDiff < 3.1;
    }

    public double getExpectedEnergy2() {
        ensureValid();
        if (prevSnapshot == null) {
            return energy;
        }

        double expectedEnergy = prevSnapshot.getEnergy();
        if (owner.getTime() == myLastHitTime) {
            expectedEnergy -= myLastDamage;
        }

        if (owner.getTime() == enemyLastHitTime) {
            expectedEnergy += enemyLastCollectedEnergy;
        }

        if (isHitWall2()) {
            expectedEnergy -= Rules.getWallHitDamage(abs(prevSnapshot.getVelocity()) + prevSnapshot.getAcceleration());
        }

        expectedEnergy -= enemyHitRobotEnergyLoss;

        return expectedEnergy;
    }

    public boolean isHitWall2() {
        ensureValid();
        if (prevSnapshot == null) {
            return false;
        }

        if (abs(prevSnapshot.getVelocity()) - abs(velocity) > Rules.DECELERATION) {
            return true;
        }

        return prevSnapshot.getPosition().aDistance(position) -
                prevSnapshot.getPosition().aDistance(prevSnapshot.getPosition().project(currentSnapshot.getAbsoluteHeadingRadians(), abs(velocity))) < -1.1;
    }

    public double getSpeed() {
        ensureValid();
        return abs(velocity);
    }

    public BattleField getBattleField() {
        return owner.getBattleField();
    }

    public long getLastDirChangeTime() {
        ensureValid();
        return info.lastDirChangeTime;
    }

    public void addVisit(double guessFactor) {
        targetData.addVisit(guessFactor);
    }

    public List<Double> getVisitedGuessFactors() {
        return targetData.getVisitedGuessFactors();
    }

    public LXXRobotState getPrevState() {
        return prevState;
    }

    public TargetState getState() {
        return state;
    }

    public double getFirePower() {
        return info.enemyLastFirePower;
    }

    public int getRound() {
        return owner.getRound();
    }

    public double getGunHeat() {
        return gunHeat;
    }

    public boolean isRammingNow() {
        return isRammingNow;
    }

    public class TargetState implements LXXRobotState {

        private final Long time;

        private LXXPoint position;
        private double headingRadians;
        private double velocity;
        private double energy;

        private double turnRateRadians;
        private double acceleration;
        private double gunHeat;

        public TargetState() {
            time = owner.getTime();
        }

        private void calculateState(TargetState prevState) {

            turnRateRadians = calculateTurnRate(prevState);
            acceleration = LXXUtils.calculateAcceleration(prevState, this);
            if (prevState == null) {
                gunHeat = LXXConstants.INITIAL_GUN_HEAT - owner.getGunCoolingRate() * owner.getTime();
            } else if (isFireLastTick()) {
                final double firePower = getExpectedEnergy() - state.energy;
                gunHeat = Rules.getGunHeat(firePower);
                info.enemyLastFirePower = firePower;
            } else {
                gunHeat = prevState.gunHeat;
            }

            gunHeat = max(0, gunHeat - owner.getGunCoolingRate());
        }

        private double calculateTurnRate(TargetState prevState) {
            if (prevState == null) {
                return 0;
            }

            double turnRate = Utils.normalRelativeAngle(state.headingRadians - prevState.headingRadians);

            if (abs(turnRate) > Rules.MAX_TURN_RATE_RADIANS + 0.01) {
                if (state.time == prevState.time + 1) {
                    System.out.println("[WARN] new headingRadians delta: " + toDegrees(turnRate));
                    turnRate = Rules.MAX_TURN_RATE_RADIANS * signum(turnRate);
                } else {
                    turnRate = turnRate / (time - prevState.time);
                    if (abs(turnRate) > Rules.MAX_TURN_RATE_RADIANS) {
                        turnRate = Rules.MAX_TURN_RATE_RADIANS * signum(turnRate);
                    }
                }
            }

            return turnRate;
        }

        public double getX() {
            return position.getX();
        }

        public double getY() {
            return position.getY();
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

        public LXXPoint getPosition() {
            return position;
        }

        public double getAbsoluteHeadingRadians() {
            if (velocity >= 0) {
                return headingRadians;
            } else {
                return Utils.normalAbsoluteAngle(headingRadians + Math.PI);
            }
        }

        public double getVelocity() {
            return velocity;
        }

        public double getSpeed() {
            return abs(velocity);
        }

        public LXXRobot getRobot() {
            return Target.this;
        }

        public double getHeadingRadians() {
            return headingRadians;
        }

        public BattleField getBattleField() {
            return owner.battleField;
        }

        public double getTurnRateRadians() {
            return turnRateRadians;
        }

        public double getEnergy() {
            return energy;
        }
    }

    public class TargetInfo {

        private long myLastHitTime;
        private double myLastDamage;
        public long lastDirChangeTime;

        private long enemyLastHitTime;
        private double enemyLastCollectedEnergy;
        private double enemyLastFirePower;
        private double enemyHitRobotEnergyLoss;

    }

    public LXXPoint getPosition() {
        return state.position;
    }
}
