/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.office.Office;
import lxx.office.PropertiesManager;
import lxx.strategies.challenges.MCChallengerStrategy;
import lxx.strategies.challenges.TCChallengerStrategy;
import lxx.strategies.duel.DuelFirePowerSelector;
import lxx.strategies.duel.DuelStrategy;
import lxx.strategies.duel.WaveSurfingMovement;
import lxx.strategies.find_enemies.FindEnemiesStrategy;
import lxx.strategies.win.WinStrategy;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_claws.TomcatClaws;
import lxx.targeting.tomcat_eyes.TomcatEyes;

import java.util.ArrayList;
import java.util.List;

public class StrategySelector {

    private final List<Strategy> strategies = new ArrayList<Strategy>();

    public StrategySelector(Tomcat robot, Office office) {
        final TargetManager targetManager = office.getTargetManager();
        final EnemyBulletManager enemyBulletManager = office.getEnemyBulletManager();
        final TomcatEyes tomcatEyes = office.getTomcatEyes();
        targetManager.addListener(tomcatEyes);
        enemyBulletManager.addListener(tomcatEyes);

        final TomcatClaws tomcatClaws = new TomcatClaws(robot, office.getTurnSnapshotsLog(), office.getDataViewManager().getDuelDataView());
        final WaveSurfingMovement wsm = new WaveSurfingMovement(office);
        office.getPaintManager().addPainter(wsm);

        strategies.add(new FindEnemiesStrategy(robot, targetManager, robot.getInitialOthers()));
        if ("TCc".equals(PropertiesManager.getDebugProperty("lxx.Tomcat.mode"))) {
            strategies.add(new TCChallengerStrategy(robot, tomcatClaws, targetManager));
        }
        if ("MCc".equals(PropertiesManager.getDebugProperty("lxx.Tomcat.mode"))) {
            strategies.add(new MCChallengerStrategy(robot, wsm, targetManager, enemyBulletManager));
        }

        final DuelStrategy duelStrategy = new DuelStrategy(robot,
                wsm,
                tomcatClaws,
                new DuelFirePowerSelector(office.getStatisticsManager()), targetManager, enemyBulletManager);
        strategies.add(duelStrategy);

        final WinStrategy winStrategy = new WinStrategy(robot, targetManager, enemyBulletManager);
        office.getPaintManager().addPainter(winStrategy);
        strategies.add(winStrategy);
    }

    public Strategy selectStrategy() {
        for (Strategy s : strategies) {
            if (s.match()) {
                return s;
            }
        }

        return null;
    }

}
