/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting;

import lxx.BasicRobot;
import lxx.EnemySnapshot;
import lxx.LXXRobot;
import lxx.utils.*;
import robocode.*;
import robocode.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * User: jdev
 * Date: 25.07.2009
 */

public class Target implements Serializable, LXXRobot {

    private final List<Event> eventsList = new ArrayList<Event>(15);

    private final String name;
    private transient final BasicRobot owner;

    private boolean isAlive = true;

    private TargetData targetData;
    private boolean isRammingNow;
    private EnemySnapshot prevSnapshot;
    private EnemySnapshot currentSnapshot;

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
    private long fireReadyTime;

    public Target(BasicRobot owner, String name, TargetData targetData) {
        this.owner = owner;
        this.name = name;
        this.targetData = targetData;
    }

    public void addEvent(Event e) {
        eventsList.add(e);
    }

    public void update() {
        ensureValid();
        prevSnapshot = currentSnapshot;
        updateRobotState();
        if (prevSnapshot == null) {
            currentSnapshot = new EnemySnapshot(this, targetData.getVisitedGuessFactors());
        } else {
            currentSnapshot = new EnemySnapshot(prevSnapshot, this);
        }
        updateState();

        isRammingNow = ((LXXUtils.anglesDiff(angleTo(owner), getAbsoluteHeadingRadians()) < LXXConstants.RADIANS_30 &&
                getSpeed() > 0) || (owner.aDistance(this) < 50));

        eventsList.clear();
    }

    private void updateState() {
        enemyHitRobotEnergyLoss = 0;
        if (prevSnapshot == null) {
            gunHeat = LXXConstants.INITIAL_GUN_HEAT - owner.getGunCoolingRate() * owner.getTime();
        } else if (isFireLastTick()) {
            final double firePower = getExpectedEnergy() - energy;
            gunHeat = Rules.getGunHeat(firePower);
            enemyLastFirePower = firePower;
            targetData.addFireDelay(owner.getTime() - fireReadyTime);
            targetData.setMinFireEnergy(prevSnapshot.getEnergy());
        }

        gunHeat = max(0, gunHeat - owner.getGunCoolingRate());
        if (gunHeat == 0) {
            fireReadyTime = owner.getTime();
        }
        currentSnapshot.setGunHeat(gunHeat);
    }

    private void updateRobotState() {

        for (Event event : eventsList) {
            if (event instanceof ScannedRobotEvent) {
                final ScannedRobotEvent e = (ScannedRobotEvent) event;
                processScannedRobotEvent(e);
            } else if (event instanceof HitRobotEvent) {
                final HitRobotEvent e = (HitRobotEvent) event;
                processHitRobotEvent(e);
            } else if (event instanceof BulletHitEvent) {
                final BulletHitEvent e = (BulletHitEvent) event;
                processBulletHitEvent(e);
            } else if (event instanceof HitByBulletEvent) {
                final HitByBulletEvent e = (HitByBulletEvent) event;
                processHitByBulletEvent(e);
            } else if (event instanceof RobotDeathEvent) {
                processRobotDeathEvent();
            }
        }
    }

    private void processRobotDeathEvent() {
        energy = 0;
    }

    private void processHitByBulletEvent(HitByBulletEvent e) {
        final double bulletPower = e.getBullet().getPower();
        energy = prevSnapshot.getEnergy() + LXXUtils.getReturnedEnergy(bulletPower);
        enemyLastHitTime = e.getTime();
        enemyLastCollectedEnergy = LXXUtils.getReturnedEnergy(bulletPower);
    }

    private void processBulletHitEvent(BulletHitEvent e) {
        myLastHitTime = e.getTime();
        myLastDamage = Rules.getBulletDamage(e.getBullet().getPower());

        position = new LXXPoint(e.getBullet().getX(), e.getBullet().getY());
        energy = e.getEnergy();
    }

    private void processHitRobotEvent(HitRobotEvent e) {
        double absoluteBearing = owner.getHeadingRadians() + e.getBearingRadians();
        position = (LXXPoint) owner.project(absoluteBearing, LXXConstants.ROBOT_SIDE_SIZE);
        energy = e.getEnergy();
        enemyHitRobotEnergyLoss += LXXConstants.ROBOT_HIT_DAMAGE;
    }

    private void processScannedRobotEvent(ScannedRobotEvent e) {
        final double absoluteBearing = owner.getHeadingRadians() + e.getBearingRadians();

        position = (LXXPoint) owner.project(absoluteBearing, e.getDistance());
        headingRadians = e.getHeadingRadians();
        velocity = e.getVelocity();
        energy = e.getEnergy();

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

    public EnemySnapshot getPrevSnapshot() {
        return prevSnapshot;
    }

    public EnemySnapshot getCurrentSnapshot() {
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
        if (prevSnapshot != null && prevSnapshot.getGunHeat() >= owner.getGunCoolingRate()) {
            return false;
        }
        double energyDiff = getExpectedEnergy() - energy;
        return energyDiff > 0 && energyDiff < 3.1;
    }

    public double getExpectedEnergy() {
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

        if (isHitWall()) {
            expectedEnergy -= Rules.getWallHitDamage(abs(prevSnapshot.getVelocity()) + prevSnapshot.getAcceleration());
        }

        expectedEnergy -= enemyHitRobotEnergyLoss;

        return expectedEnergy;
    }

    public boolean isHitWall() {
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

    public void addVisit(double guessFactor) {
        targetData.addVisit(guessFactor);
    }

    public double getFirePower() {
        return enemyLastFirePower;
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

    public LXXPoint getPosition() {
        return position;
    }

    public TargetData getTargetData() {
        return targetData;
    }
}
