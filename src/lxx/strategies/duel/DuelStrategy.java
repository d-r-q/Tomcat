/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.Office;
import lxx.strategies.*;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.LXXConstants;
import robocode.util.Utils;

import static java.lang.Math.signum;

public class DuelStrategy extends AbstractStrategy implements Radar, TargetSelector {

    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final Gun gun;
    private final FirePowerSelector firePowerSelector;
    private final Movement withBulletsMovement;

    private Target target;

    public DuelStrategy(Tomcat robot,
                        Movement withBulletsMovement,
                        Gun gun, FirePowerSelector firePowerSelector, TargetManager targetManager,
                        EnemyBulletManager enemyBulletManager, Office office) {
        super(robot, office);

        this.withBulletsMovement = withBulletsMovement;
        this.gun = gun;
        this.firePowerSelector = firePowerSelector;

        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
    }

    public boolean match() {
        final boolean match = targetManager.hasDuelOpponent() || enemyBulletManager.getBulletsOnAir(1).size() > 0;
        if (match) {
            target = targetManager.getDuelOpponent();
        }
        return match;
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
        return withBulletsMovement.getMovementDecision();
    }

    protected GunDecision getGunDecision(Target target, double firePower) {
        if (target == null) {
            return new GunDecision(Utils.normalRelativeAngle(-robot.getGunHeadingRadians()), null);
        }
        return gun.getGunDecision(target, firePower);
    }

    protected double selectFirePower(Target target) {
        return firePowerSelector.selectFirePower(robot, target);
    }

}
