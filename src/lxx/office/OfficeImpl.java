/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.bullets.my.BulletManager;
import lxx.paint.PaintManager;
import lxx.plugins.PluginManager;
import lxx.targeting.TargetManager;
import lxx.targeting.tomcat_claws.data_analise.DataViewManager;
import lxx.targeting.tomcat_eyes.TomcatEyes;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.time_profiling.TimeProfiler;
import lxx.utils.wave.WaveManager;

public class OfficeImpl implements Office {

    private final TargetManager targetManager;
    private final TurnSnapshotsLog turnSnapshotsLog;
    private final WaveManager waveManager;
    private final EnemyBulletManager enemyBulletManager;
    private final AttributesManager attributesManager;
    private final BulletManager bulletManager;
    private final DataViewManager dataViewManager;

    private final Tomcat tomcat;
    private StatisticsManager statisticsManager;
    private TomcatEyes tomcatEyes;
    private PaintManager paintManager;
    private TimeProfiler timeProfiler;

    public OfficeImpl(Tomcat tomcat) {
        this.tomcat = tomcat;
        this.tomcatEyes = new TomcatEyes(tomcat);

        timeProfiler = new TimeProfiler();
        tomcat.addListener(timeProfiler);

        targetManager = new TargetManager(tomcat);
        tomcat.addListener(targetManager);

        waveManager = new WaveManager();
        tomcat.addListener(waveManager);

        bulletManager = new BulletManager(waveManager);
        tomcat.addListener(bulletManager);

        attributesManager = new AttributesManager(this, tomcat);

        turnSnapshotsLog = new TurnSnapshotsLog(this);
        targetManager.addListener(turnSnapshotsLog);

        enemyBulletManager = new EnemyBulletManager(this, tomcat);
        tomcat.addListener(enemyBulletManager);
        targetManager.addListener(enemyBulletManager);

        statisticsManager = new StatisticsManager(this, tomcat);
        tomcat.addListener(statisticsManager);

        paintManager = new PaintManager();
        tomcat.addListener(paintManager);

        final PluginManager pluginManager = new PluginManager(this);
        tomcat.addListener(pluginManager);

        dataViewManager = new DataViewManager(targetManager, turnSnapshotsLog);
        tomcat.addListener(dataViewManager);

        final PropertiesManager propertiesManager = new PropertiesManager();
        tomcat.addListener(propertiesManager);
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

    public TurnSnapshotsLog getTurnSnapshotsLog() {
        return turnSnapshotsLog;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public BulletManager getBulletManager() {
        return bulletManager;
    }

    public DataViewManager getDataViewManager() {
        return dataViewManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public PaintManager getPaintManager() {
        return paintManager;
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

    public TomcatEyes getTomcatEyes() {
        return tomcatEyes;
    }

    public TimeProfiler getTimeProfiler() {
        return timeProfiler;
    }
}
