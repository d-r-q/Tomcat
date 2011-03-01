package lxx.targeting;

import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.BasicRobot;
import lxx.movement.minimal_risk.MinimalRiskModel;
import robocode.Rules;

/**
 * Created by IntelliJ IDEA.
 * User: jdev
 * Date: 30.10.2009
 */

public class TargetChooserImpl implements TargetChooser {

    private static final double[] IDEAL_TARGET;
    static {
        final int distance = 0;
        final int bearing = 0;
        final int energy = 0;
        final int gunBearing = 0;
        final int latency = 0;
        final int velocity = 0;
        final int lastHitBullet = 0;
        IDEAL_TARGET = new double[]{distance, bearing, energy, gunBearing, latency, velocity, lastHitBullet};
    }

    private final BasicRobot robot;
    private final TargetManager targetManager;
    private final MinimalRiskModel minimalRiskModel;

    public TargetChooserImpl(BasicRobot robot, TargetManager targetManager, MinimalRiskModel minimalRiskModel) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.minimalRiskModel = minimalRiskModel;
    }

    public Target getBestTarget() {
        final long time = robot.getTime();
        if (time == 0) {
            return null;
        }

        final Target closestTarget = targetManager.getClosestTarget();
        if (closestTarget != null && closestTarget.getPosition().distance(robot.getX(), robot.getY()) < 150) {
            return closestTarget;
        }

        Target res = null;
        double minDist = Double.MAX_VALUE;

        final double bfWidth = robot.getBattleField().width;
        final double heading = robot.getHeadingRadians();
        final double gunHeading = robot.getGunHeadingRadians();

        LXXPoint mostSafe = minimalRiskModel.getSafestPoint();
        double minDistToMostSafe = Double.MAX_VALUE;

        for (Target t : targetManager.getAliveTargets()) {
            final LXXPoint pos = t.getPosition();
            final double distance = robot.distance(pos) * (250D / bfWidth);
            final double bearing = 0 * Math.abs(robot.normalizeBearing(heading - t.getHeading())) * (25D / Math.PI);
            final double energy = Utils.scale(t.getEnergy(), 100, 26);
            final double gunBearing = Math.abs(robot.normalizeBearing(gunHeading - robot.angleTo(pos))) * (100D / Math.PI);
            final double latency = t.getLatency() * (100D / 32D);
            final double velocity = 0;
            final long lastHit = (time - t.getLastHitTime()) * (0 / time);
            final long lastHitRobot = (time - t.getLastHitRobotTime()) * (0 / time);
            final double[] target = new double[]{distance, bearing, energy, gunBearing, latency, velocity, lastHit, lastHitRobot};
            if (Utils.distance(IDEAL_TARGET, target) < minDist) {
                res = t;
                minDist = Utils.distance(IDEAL_TARGET, target);
            }

            if (mostSafe != null && mostSafe.distance(t.getX(), t.getY()) < minDistToMostSafe) {
                minDistToMostSafe = mostSafe.distance(t.getX(), t.getY());
            }
        }
        return res;
    }

    public double firePower() {
        Target bestTarget = getBestTarget();
        if (bestTarget == null) {
            return 0;
        }
        double bulletPower = 3;

        if (Rules.getBulletDamage(bulletPower) > bestTarget.getEnergy()) {
            bulletPower = bestTarget.getEnergy() / 4;
        }

        if (bulletPower == 0) {
            bulletPower = Math.max(0.1, Math.max(bestTarget.getEnergy() / 4, 0.1));
        }

        if (bulletPower > 3) {
            bulletPower = 3;
        }

        if (bulletPower == 0) {
            bulletPower = 1.1;
        }

        return bulletPower;
    }
}
