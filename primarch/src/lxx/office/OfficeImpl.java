/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Primarch;
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
    private final PatternManager patternManager;

    private final Primarch primarch;

    public OfficeImpl(Primarch primarch) {
        this.primarch = primarch;

        attributesManager = new AttributesManager(this, primarch);

        timer = new Timer(staticDataManager, primarch.getRoundNum());
        primarch.addListener(timer);

        targetManager = new TargetManager(primarch);
        primarch.addListener(targetManager);

        battleSnapshotManager = new BattleSnapshotManager(this);
        targetManager.addListener(battleSnapshotManager);

        waveManager = new WaveManager();
        primarch.addListener(waveManager);

        enemyBulletManager = new EnemyBulletManager(this, primarch);
        primarch.addListener(enemyBulletManager);
        targetManager.addListener(enemyBulletManager);

        bulletManager = new BulletManager();
        primarch.addListener(bulletManager);

        final StatisticsManager statisticsManager = new StatisticsManager(this, primarch);
        primarch.addListener(statisticsManager);

        patternManager = new PatternManager(this);

        final PaintManager paintManager = new PaintManager();
        primarch.addListener(paintManager);

        DebugManager debugManager = new DebugManager(this);
        primarch.addListener(debugManager);
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

    public PatternManager getPatternManager() {
        return patternManager;
    }

    public long getTime() {
        return primarch.getTime();
    }

    public Primarch getPrimarch() {
        return primarch;
    }

    public boolean isDebugMode() {
        return true;
    }
}
