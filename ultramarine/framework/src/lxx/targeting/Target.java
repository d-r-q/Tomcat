package lxx.targeting;

import lxx.BasicRobot;
import static lxx.StaticData.robot;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.LXXRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import static java.lang.Math.*;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;

/**
 * User: jdev
 * Date: 25.07.2009
 */

public class Target implements LXXRobot {

    protected final BasicRobot owener;
    protected final String name;

    protected long updateTime;
    protected long fullUpdateTime = 0;

    protected LXXPoint position = new LXXPoint(0, 0);
    protected LXXPoint prevPosition = new LXXPoint(0, 0);

    protected double heading;
    protected double headingDelta;

    protected double velocity;
    protected double velocityDelta;

    protected double lateralVelocity;

    protected double energy;
    protected double energyDelta;

    protected double fireCount = 1;
    protected double hitCount = 1;

    protected boolean isAlive = true;
    protected long lastHitTime;
    protected long lastHitRobotTime;

    protected long lastStopTime;
    protected long lastTrevelTime;
    protected long lastLateralDirChange;

    protected int lastVisitedGF;

    private Map<Integer, Double> maxEscapeAngles = new HashMap<Integer, Double>();
    private double distTravelledLastWave;

    public Target(Target ethalon) {
        this.owener = ethalon.owener;
        this.name = ethalon.name;
        this.updateTime = ethalon.updateTime;
        this.fullUpdateTime = ethalon.fullUpdateTime;
        this.position = new LXXPoint(ethalon.position);
        this.prevPosition = new LXXPoint(ethalon.prevPosition);
        this.heading = ethalon.heading;
        this.headingDelta = ethalon.headingDelta;
        this.velocity = ethalon.velocity;
        this.velocityDelta = ethalon.velocityDelta;
        this.energy = ethalon.energyDelta;
        this.lastStopTime = ethalon.lastStopTime;
        this.lastTrevelTime = ethalon.lastTrevelTime;
        this.lateralVelocity = ethalon.lateralVelocity;
    }

    public Target(BasicRobot owener, String name) {
        this.owener = owener;
        this.name = name;
    }

    public void update(ScannedRobotEvent e) {
        double absoluteBearing = owener.getHeadingRadians() + e.getBearingRadians();
        LXXPoint coords = owener.getCoords(absoluteBearing, round(e.getDistance()));

        update(new TargetUpdatedEvent(owener.getTime(), coords,
                e.getHeadingRadians(), e.getVelocity(), e.getEnergy()));
        fullUpdateTime = e.getTime();
        isAlive = true;
    }

    public void update(HitRobotEvent e) {
        double absoluteBearing = owener.getHeadingRadians() + e.getBearingRadians();
        lastHitRobotTime = e.getTime();
        update(new TargetUpdatedEvent(owener.getTime(), owener.getCoords(absoluteBearing, owener.width),
                heading, velocity, e.getEnergy()));
    }


    public void update(BulletHitEvent e) {
        hitCount++;
        final LXXPoint point = new LXXPoint(e.getBullet().getX(), e.getBullet().getY());
        update(new TargetUpdatedEvent(owener.getTime(), point,
                heading, velocity, e.getEnergy()));
    }

    public void update(HitByBulletEvent e) {
        lastHitTime = e.getTime();
        update(new TargetUpdatedEvent(owener.getTime(), position,
                heading, velocity, energy));
    }

    public void update(TargetUpdatedEvent e) {

        if (e.getPosition().x < 0) {
            e.getPosition().x = 0;
        }
        if (e.getPosition().x > owener.getBattleFieldWidth()) {
            e.getPosition().x = owener.getBattleFieldWidth();
        }
        if (e.getPosition().y < 0) {
            e.getPosition().y = 0;
        }
        if (e.getPosition().y > owener.getBattleFieldHeight()) {
            e.getPosition().y = owener.getBattleFieldHeight();
        }

        prevPosition = position;
        position = e.getPosition();

        if (updateTime == 0) {
            headingDelta = 0;
        } else {
            headingDelta = e.getHeading() - heading;
            if (headingDelta > Rules.MAX_TURN_RATE_RADIANS) {
                headingDelta = heading - e.getHeading() - Math.PI * 2;
            } else if (headingDelta < -Rules.MAX_TURN_RATE_RADIANS) {
                headingDelta = Math.PI * 2 - heading - e.getHeading();
            }
            // todo: fix me
            if (abs(headingDelta) > Rules.MAX_TURN_RATE_RADIANS) {
                headingDelta = 0;
            }
        }
        heading = e.getHeading();
        updateTime = e.getTime();
        velocityDelta = -(velocity - e.getVelocity());
        if (velocityDelta > Rules.ACCELERATION) {
            velocityDelta = Rules.ACCELERATION;
        } else if (velocityDelta < Rules.DECELERATION) {
            velocityDelta = Rules.DECELERATION;
        }
        velocity = e.getVelocity();
        if (velocity == 0) {
            lastStopTime = updateTime;
        } else {
            lastTrevelTime = updateTime;
        }
        double prevLatVel = lateralVelocity;
        lateralVelocity = (velocity * Math.sin(Utils.normalRelativeAngle(heading - owener.angleTo(this))));
        if (signum(prevLatVel) != signum(lateralVelocity)) {
            lastLateralDirChange = updateTime;
        }

        energyDelta = -(energy - e.getEnergy());
        energy = e.getEnergy();

        maxEscapeAngles.clear();
    }

    public void endRaund() {
        updateTime = 0;
        lastHitTime = 0;
        lastTrevelTime = 0;
        lastStopTime = 0;
        isAlive = false;
        position = new LXXPoint(0, 0);
        prevPosition = new LXXPoint(0, 0);
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public long getFullUpdateTime() {
        return fullUpdateTime;
    }

    public int getLatency() {
        return (int)(owener.getTime() - updateTime);
    }

    public LXXPoint getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return owener.getTime();
    }

    public double getEnergy() {
        return energy;
    }

    public double getEnergyDelta() {
        return energyDelta;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getHeading() {
        return heading;
    }

    public double getHeadingDelta() {
        return headingDelta;
    }

    public double getVelocityDelta() {
        return velocityDelta;
    }

    public boolean isAlive() {
        return isAlive && getLatency() < 20;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public double getHitProbability() {
        return hitCount / fireCount;
    }

    public long getLastHitTime() {
        return lastHitTime;
    }

    public long getLastHitRobotTime() {
        return lastHitRobotTime;
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public double aDistance(APoint p) {
        return position.distance(p.getX(), p.getY());
    }

    public double distance(double x, double y) {
        return position.distance(x, y);
    }

    public double getAbsoluteHeading() {
        if (velocity >= 0) {
            return heading;
        } else {
            return Utils.normalAbsoluteAngle(heading - Math.PI);
        }
    }

    public double getAbsoluteHeadingD() {
        if (velocity >= 0) {
            return toDegrees(heading);
        } else {
            return toDegrees(Utils.normalAbsoluteAngle(heading - Math.PI));
        }
    }

    public long getLastStopTime() {
        return lastStopTime;
    }

    public long getLastTrevelTime() {
        return lastTrevelTime;
    }

    public double distanceToHeadOnWall() {
        switch ((int)(getAbsoluteHeadingD() / 90D) * 90) {
            case 0:
            case 360:
                return (owener.getBattleField().height - getY());
            case 90:
                return (owener.getBattleField().width - getX());
            case 180:
                return getY();
            case 270:
                return getX();
        }

        throw new RuntimeException("Something wrong: " + (int)((getAbsoluteHeadingD() / 90D) * 90));
    }

    public double distanceToClosestWall() {
        return min(min(getX(), getY()), min(owener.getBattleField().height - getY(), owener.getBattleField().width - getX()));
    }

    public double bearingToClosestWall() {
        double angle = 0;
        if (getX() < getY()) {
            if (getX() < robot.getBattleFieldWidth()) {
                angle = LXXConstants.RADIANS_270;
            } else {
                angle = LXXConstants.RADIANS_90;
            }
        } else {
            if (getY() < robot.getBattleFieldWidth()) {
                angle = LXXConstants.RADIANS_180;
            } else {
                angle = LXXConstants.RADIANS_0;
            }
        }

        return robocode.util.Utils.normalRelativeAngle(angle - getAbsoluteHeading());
    }

    public double bearingToHeadOnWall() {
        return (int)((getAbsoluteHeadingD() / 90) * 90) - getAbsoluteHeadingD();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target)o;

        if (!name.equals(target.name)) return false;

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public double getLateralVelocity() {
        return lateralVelocity;
    }

    public long getLastLateralDirChange() {
        return lastLateralDirChange;
    }

    public double maxEscapeAngle(int dir) {

        final double v = Rules.MAX_VELOCITY / Rules.getBulletSpeed(3);
        return asin(v) * signum(dir);
    }

    public int getLastVisitedGF() {
        return lastVisitedGF;
    }

    public void setLastVisitedGF(int lastVisitedGF) {
        this.lastVisitedGF = lastVisitedGF;
    }

    public double getDistTravelledLastWave() {
        return distTravelledLastWave;
    }

    public void setDistTravelledLastWave(double distTravelledLastWave) {
        this.distTravelledLastWave = distTravelledLastWave;
    }

    public double getMaxTurnAngle() {
        TargetImage ti = new TargetImage(this);
        long time = 0;
        while (owener.distance(ti) > Rules.getBulletSpeed(owener.firePower()) * time++ &&
                Utils.normalRelativeAngle(ti.getAbsoluteHeading() - getAbsoluteHeading()) < Math.PI &&
                Utils.normalRelativeAngle(ti.getAbsoluteHeading() - getAbsoluteHeading()) >= 0) {
            ti.setHeadingDelta(Rules.getTurnRateRadians(ti.getVelocity()));
            if (abs(ti.getVelocity()) > 0) {
                ti.setVelocityDelta(-signum(ti.getVelocity()) * min(2, abs(ti.getVelocity())));
            } else {
                ti.setVelocityDelta(0);
            }
            ti.tick();
        }
        if (Utils.normalRelativeAngle(ti.getAbsoluteHeading() - getAbsoluteHeading()) < 0) {
            return Math.PI;
        }
        return Utils.normalRelativeAngle(ti.getAbsoluteHeading() - getAbsoluteHeading());
    }

    public double getMaxEscapeDistance(double angle, APoint sourcePos) {
        TargetImage ti = new TargetImage(this);
        ti.setHeading(getAbsoluteHeading() + angle);
        ti.setVelocity(8);
        ti.setVelocityDelta(0);
        ti.setHeadingDelta(0);
        long time = 0;
        while (time * Rules.getBulletSpeed(2) < sourcePos.aDistance(ti)) {
            time++;
            ti.tick();
        }
        /*Graphics g = robot.getGraphics();
        g.setColor(Color.GREEN);
        g.drawOval((int)getX(), (int)getY(), 10, 10);
        g.setColor(Color.BLUE);
        g.drawOval((int)ti.getX(), (int)ti.getY(), 10, 10);
        g.setColor(Color.GREEN);
        g.drawLine((int)getX(), (int)getY(), (int)ti.getX(), (int)ti.getY());*/
        return aDistance(ti);
    }

}
