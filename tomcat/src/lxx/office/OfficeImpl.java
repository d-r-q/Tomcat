/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Tomcat;
import lxx.debug.DebugManager;
import lxx.targeting.bullets.BulletManager;

public class OfficeImpl implements Office {

    private static final StaticDataManager staticDataManager = new StaticDataManager();

    private final TargetManager targetManager;
    private final Timer timer;
    private final BattleSnapshotManager battleSnapshotManager;
    private final WaveManager waveManager;
    private final EnemyBulletManager enemyBulletManager;
    private final AttributesManager attributesManager;
    private final BulletManager bulletManager;

    private final Tomcat tomcat;

    public OfficeImpl(Tomcat tomcat) {
        this.tomcat = tomcat;

        attributesManager = new AttributesManager(this, tomcat);

        timer = new Timer(staticDataManager, tomcat.getRoundNum());
        tomcat.addListener(timer);

        targetManager = new TargetManager(tomcat);
        tomcat.addListener(targetManager);

        battleSnapshotManager = new BattleSnapshotManager(this);
        targetManager.addListener(battleSnapshotManager);

        waveManager = new WaveManager();
        tomcat.addListener(waveManager);

        enemyBulletManager = new EnemyBulletManager(this, tomcat);
        tomcat.addListener(enemyBulletManager);
        targetManager.addListener(enemyBulletManager);

        bulletManager = new BulletManager();
        tomcat.addListener(bulletManager);

        final StatisticsManager statisticsManager = new StatisticsManager(this, tomcat);
        tomcat.addListener(statisticsManager);

        final PaintManager paintManager = new PaintManager();
        tomcat.addListener(paintManager);

        DebugManager debugManager = new DebugManager(this);
        tomcat.addListener(debugManager);
    }

    public EnemyBulletManager getEnemyBulletManager() {
        return enemyBulletManager;
    }

    public AttributesManager getAttributesManager() {
        return attributesManager;
    }

    public TargetManager getTargetManager() {
        return targetManager;
    }

    public Timer getBattleTimeManager() {
        return timer;
    }

    public BattleSnapshotManager getBattleSnapshotManager() {
        return battleSnapshotManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public BulletManager getBulletManager() {
        return bulletManager;
    }

    public long getTime() {
        return tomcat.getTime();
    }

    public Tomcat getRobot() {
        return tomcat;
    }

    public boolean isDebugMode() {
        return true;
    }
}
