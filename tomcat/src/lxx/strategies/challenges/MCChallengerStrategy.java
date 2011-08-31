/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.challenges;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.PropertiesManager;
import lxx.strategies.Movement;
import lxx.strategies.Strategy;
import lxx.strategies.TurnDecision;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.utils.LXXConstants;
import robocode.util.Utils;

import static java.lang.Math.signum;

public class MCChallengerStrategy implements Strategy {

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final Movement movement;

    private Target target;

    public MCChallengerStrategy(Tomcat robot,
                                Movement movement,
                                TargetManager targetManager,
                                EnemyBulletManager enemyBulletManager) {
        this.robot = robot;
        this.movement = movement;

        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
    }

    @Override
    public boolean match() {
        final boolean match = targetManager.hasDuelOpponent() || enemyBulletManager.getBulletsOnAir(1).size() > 0;
        if ("MCc".equals(PropertiesManager.getDebugProperty("lxx.Tomcat.mode")) &&
                match) {
            target = targetManager.getDuelOpponent();
            return true;
        }
        return false;
    }

    @Override
    public TurnDecision makeDecision() {
        return new TurnDecision(movement.getMovementDecision(), 0, 0, getRadarTurnAngleRadians(), null, null);
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

}
