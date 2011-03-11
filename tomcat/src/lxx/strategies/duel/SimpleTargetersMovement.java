/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.Tomcat;
import lxx.office.EnemyBulletManager;
import lxx.office.TargetManager;
import lxx.targeting.Target;
import lxx.targeting.tomcat_eyes.TomcatEyes;

public class SimpleTargetersMovement extends WaveSurfingMovement {

    public SimpleTargetersMovement(Tomcat robot, TargetManager targetManager, EnemyBulletManager enemyBulletManager, TomcatEyes tomcatEyes) {
        super(robot, targetManager, enemyBulletManager, tomcatEyes);
    }

    protected double getEnemyPreferredDistance(Target opponent) {
        return DEFAULT_DISTANCE_AGAINST_SIMPLE;
    }

}
