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
import lxx.targeting.TargetManager;

public class MCChallengerStrategy implements Strategy {

    private final TargetManager targetManager;
    private final Tomcat robot;
    private final EnemyBulletManager enemyBulletManager;
    private final Movement movement;

    public MCChallengerStrategy(Tomcat robot,
                                Movement movement,
                                TargetManager targetManager,
                                EnemyBulletManager enemyBulletManager) {
        this.movement = movement;
        this.robot = robot;
        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
    }

    @Override
    public boolean match() {
        final boolean match = targetManager.hasDuelOpponent() || enemyBulletManager.getBulletsOnAir(1).size() > 0;
        if ("MCc".equals(PropertiesManager.getDebugProperty("lxx.Tomcat.mode")) &&
                match) {
            robot.addListener(new RaikoGun(robot));
            return true;
        }
        return false;
    }

    @Override
    public TurnDecision makeDecision() {
        return new TurnDecision(movement.getMovementDecision(), null, 0, null, null, null);
    }

}
