/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.challenges;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.strategies.Movement;
import lxx.strategies.Strategy;
import lxx.strategies.TurnDecision;
import lxx.targeting.TargetManager;

public class MCChallengerStrategy implements Strategy {

    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;
    private final Movement movement;

    public MCChallengerStrategy(Tomcat robot,
                                Movement movement,
                                TargetManager targetManager,
                                EnemyBulletManager enemyBulletManager) {
        this.movement = movement;

        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
        robot.addListener(new RaikoGun(robot));
    }

    @Override
    public boolean match() {
        final boolean match = targetManager.hasDuelOpponent() || enemyBulletManager.getBulletsOnAir(1).size() > 0;
        if (match) {
            return true;
        }
        return false;
    }

    @Override
    public TurnDecision makeDecision() {
        return new TurnDecision(movement.getMovementDecision(), null, 0, null, null, null);
    }

}
