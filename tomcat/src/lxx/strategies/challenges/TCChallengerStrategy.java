package lxx.strategies.challenges;

import lxx.Tomcat;
import lxx.office.PropertiesManager;
import lxx.strategies.*;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.LXXConstants;
import robocode.util.Utils;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 19.06.11
 */
public class TCChallengerStrategy extends AbstractStrategy {

    private final Gun gun;
    private final TargetManager targetManager;

    protected Target target;

    public TCChallengerStrategy(Tomcat robot, Gun gun, TargetManager targetManager) {
        super(robot);

        this.gun = gun;
        this.targetManager = targetManager;
    }

    public boolean match() {
        if ("TCc".equals(PropertiesManager.getDebugProperty("lxx.Tomcat.mode"))) {
            target = targetManager.getDuelOpponent();
            return true;
        }
        return false;
    }

    public double getRadarTurnAngleRadians() {
        if (target == null) {
            return Utils.normalRelativeAngle(-robot.getRadarHeadingRadians());
        }
        final double angleToTarget = robot.angleTo(target);
        final double sign = (angleToTarget != robot.getRadarHeadingRadians())
                ? signum(Utils.normalRelativeAngle(angleToTarget - robot.getRadarHeadingRadians()))
                : 1;

        return Utils.normalRelativeAngle(angleToTarget - robot.getRadarHeadingRadians() + LXXConstants.RADIANS_5 * sign);
    }

    public Target selectTarget() {
        return target;
    }

    protected MovementDecision getMovementDecision() {
        return new MovementDecision(0, 0, MovementDecision.MovementDirection.FORWARD);
    }

    protected GunDecision getGunDecision(Target target, double firePower) {
        if (target == null) {
            return new GunDecision(Utils.normalRelativeAngle(-robot.getGunHeadingRadians()), null);
        }
        return gun.getGunDecision(target, firePower);
    }

    protected double selectFirePower(Target target) {
        return 3;
    }

}
