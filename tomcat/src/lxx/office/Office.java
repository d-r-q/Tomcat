/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Tomcat;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.tomcat_claws.data_analise.DataViewManager;

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
