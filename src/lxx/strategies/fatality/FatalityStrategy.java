package lxx.strategies.fatality;

import lxx.LXXRobotSnapshot;
import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.strategies.MovementDecision;
import lxx.strategies.Strategy;
import lxx.strategies.TurnDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.signum;

/**
 * User: Aleksey Zhidkov
 * Date: 25.06.12
 */
public class FatalityStrategy implements Strategy {

    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final Tomcat robot;

    private Target target;

    public FatalityStrategy(TargetManager targetManager, EnemyBulletManager enemyBulletManager, Tomcat robot) {
        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
        this.robot = robot;
    }

    public boolean match() {
        if (robot.getRound() < 2) {
            return false;
        }
        if (!targetManager.hasDuelOpponent()) {
            return false;
        }
        if (enemyBulletManager.getAllBulletsOnAir().size() > 0) {
            return false;
        }
        target = targetManager.getDuelOpponent();
        if (Rules.getBulletDamage(target.getEnergy()) > robot.getEnergy()) {
            return false;
        }
        if (target.getEnergy() >= target.getTargetData().getMinFireEnergy()) {
            return false;
        }
        if (target.getTargetData().getAvgFireDelay() > 5) {
            return false;
        }
        if (robot.getTime() - robot.getLastFireTime() >= LXXConstants.INACTIVITY_TIMER - 2) {
            return false;
        }
        if (robot.getEnergy() <= target.getEnergy() + 0.1) {
            return false;
        }

        return true;
    }

    public TurnDecision makeDecision() {
        final double velocity = getVelocity();
        return new TurnDecision(
                new MovementDecision(velocity, getTurnRate(
                        velocity > 0
                                ? robot.getHeadingRadians()
                                : Utils.normalAbsoluteAngle(robot.getHeadingRadians() + LXXConstants.RADIANS_180)
                )),
                getTurnRate(robot.getGunHeadingRadians()), 0,
                getRadarTurnAngleRadians(), null, null
        );
    }

    private double getVelocity() {
        if (LXXUtils.anglesDiff(robot.getHeadingRadians(), robot.angleTo(target)) < LXXConstants.RADIANS_90) {
            return Rules.MAX_VELOCITY;
        } else {
            return -Rules.MAX_VELOCITY;
        }
    }

    private double getRadarTurnAngleRadians() {
        if (target == null) {
            return Utils.normalRelativeAngle(-robot.getRadarHeadingRadians());
        }
        final double angleToTarget = robot.angleTo(target);
        final double sign = (angleToTarget != robot.getRadarHeadingRadians())
                ? signum(Utils.normalRelativeAngle(angleToTarget - robot.getRadarHeadingRadians()))
                : 1;

        return Utils.normalRelativeAngle(angleToTarget - robot.getRadarHeadingRadians() + LXXConstants.RADIANS_5 * sign);
    }

    private double getTurnRate(double heading) {
        final APoint pnt = target.project(Utils.normalAbsoluteAngle(target.getAbsoluteHeadingRadians() + target.getCurrentSnapshot().getTurnRateRadians()),
                Rules.MAX_VELOCITY);
        return Utils.normalRelativeAngle(robot.angleTo(pnt) - heading);
    }

}
