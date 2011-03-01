/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.Primarch;
import lxx.targeting.bullets.BulletManager;

public interface Office {

    TargetManager getTargetManager();

    Timer getBattleTimeManager();

    BattleSnapshotManager getBattleSnapshotManager();

    WaveManager getWaveManager();

    EnemyBulletManager getEnemyBulletManager();

    AttributesManager getAttributesManager();

    BulletManager getBulletManager();

    PatternManager getPatternManager();

    long getTime();

    Primarch getPrimarch();

    boolean isDebugMode();
}
