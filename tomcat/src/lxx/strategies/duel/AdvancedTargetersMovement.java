/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.office.EnemyBulletManager;
import lxx.office.TargetManager;
import lxx.targeting.Target;
import lxx.targeting.tomcat_eyes.TomcatEyes;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class AdvancedTargetersMovement extends WaveSurfingMovement {

    private static final int DEFAULT_DISTANCE_AGAINST_ADVANCED = 500;
    private static final int MIN_DISTANCE = 200;

    public AdvancedTargetersMovement(Tomcat robot, TargetManager targetManager, EnemyBulletManager enemyBulletManager, TomcatEyes tomcatEyes) {
        super(robot, targetManager, enemyBulletManager, tomcatEyes);
    }

    protected double getEnemyPreferredDistance(Target opponent) {
        double enemyPreferredDistance = tomcatEyes.getEnemyPreferredDistance(opponent);
        if (enemyPreferredDistance == -1) {
            enemyPreferredDistance = DEFAULT_DISTANCE_AGAINST_ADVANCED;
        }
        enemyPreferredDistance = (int) max(min(enemyPreferredDistance - 75, opponent.getPosition().distanceToWall(robot.battleField, opponent.angleTo(robot)) - 75), MIN_DISTANCE);

        return enemyPreferredDistance;
    }

}
