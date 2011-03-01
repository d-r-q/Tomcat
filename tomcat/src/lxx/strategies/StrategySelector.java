/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Tomcat;
import lxx.office.EnemyBulletManager;
import lxx.office.Office;
import lxx.office.TargetManager;
import lxx.strategies.duel.DuelFirePowerSelector;
import lxx.strategies.duel.DuelStrategy;
import lxx.strategies.duel.WaveSurfingMovement;
import lxx.strategies.find_enemies.FindEnemiesStrategy;
import lxx.strategies.win.WinStrategy;
import lxx.targeting.tomcat_claws.TomcatClaws;
import lxx.targeting.tomcat_eyes.TomcatEyes;

import java.util.ArrayList;
import java.util.List;

public class StrategySelector {

    private final List<Strategy> strategies = new ArrayList<Strategy>();

    public StrategySelector(Tomcat robot, Office office) {
        final TargetManager targetManager = office.getTargetManager();
        final EnemyBulletManager enemyBulletManager = office.getEnemyBulletManager();

        strategies.add(new FindEnemiesStrategy(robot, targetManager, robot.getInitialOthers()));

        final DuelStrategy waveSurfingDuelStrategy = new DuelStrategy(robot,
                new WaveSurfingMovement(robot, targetManager, enemyBulletManager),
                createDuelGun(robot, office, targetManager),
                new DuelFirePowerSelector(), targetManager, enemyBulletManager);
        strategies.add(waveSurfingDuelStrategy);

        strategies.add(new WinStrategy(robot, targetManager, enemyBulletManager));
    }

    private Gun createDuelGun(Tomcat robot, Office office, TargetManager targetManager) {
        final TomcatEyes tomcatEyes = new TomcatEyes(robot, office.getBulletManager());
        targetManager.addListener(tomcatEyes);
        final TomcatClaws tomcatClaws = new TomcatClaws(office, tomcatEyes);
        robot.addListener(tomcatClaws);
        return tomcatClaws;
    }

    public Strategy selectStrategy() {
        for (Strategy s : strategies) {
            if (s.match()) {
                return s;
            }
        }

        for (Strategy s : strategies) {
            s.match();
        }

        return null;
    }

}
