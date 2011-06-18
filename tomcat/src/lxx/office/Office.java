/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.bullets.my.BulletManager;
import lxx.targeting.TargetManager;
import lxx.ts_log.TurnSnapshotsLog;
import lxx.ts_log.attributes.AttributesManager;
import lxx.targeting.tomcat_claws.data_analise.DataViewManager;
import lxx.utils.wave.WaveManager;

public interface Office {

    TargetManager getTargetManager();

    TurnSnapshotsLog getTurnSnapshotsLog();

    WaveManager getWaveManager();

    EnemyBulletManager getEnemyBulletManager();

    AttributesManager getAttributesManager();

    BulletManager getBulletManager();

    DataViewManager getDataViewManager();

    long getTime();

    Tomcat getRobot();

    boolean isDebugMode();
}
