/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.targeting.TargetManager;
import lxx.strategies.*;
import lxx.targeting.GunType;
import lxx.targeting.Target;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.utils.LXXConstants;
import robocode.Rules;
import robocode.util.Utils;

import static java.lang.Math.signum;

public class DuelStrategy extends AbstractStrategy implements Radar, TargetSelector {

    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final TomcatEyes tomcatEyes;

    private final Movement simpleMovement;
    private final Movement advancedMovement;
    private final Gun gun;
    private final FirePowerSelector firePowerSelector;

    protected Target target;

    private GunType enemyGunType;

    public DuelStrategy(Tomcat robot, Movement simpleMovement, Movement advancedMovement,
                        Gun gun, FirePowerSelector firePowerSelector, TargetManager targetManager,
                        EnemyBulletManager enemyBulletManager, TomcatEyes tomcatEyes) {
        super(robot);

        this.simpleMovement = simpleMovement;
        this.advancedMovement = advancedMovement;
        this.gun = gun;
        this.firePowerSelector = firePowerSelector;

        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
        this.tomcatEyes = tomcatEyes;
    }

    public boolean match() {
        final boolean match = targetManager.hasDuelOpponent() || enemyBulletManager.hasBulletsOnAir();
        if (match) {
            target = targetManager.getDuelOpponent();
            enemyGunType = tomcatEyes.getEnemyGunType(target);
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
        if (enemyGunType == GunType.ADVANCED) {
            return advancedMovement.getMovementDecision();
        } else {
            return simpleMovement.getMovementDecision();
        }
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
