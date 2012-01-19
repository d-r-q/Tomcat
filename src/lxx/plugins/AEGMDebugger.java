/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.plugins;

import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.bullets.enemy.EnemyBulletPredictionData;
import lxx.office.Office;
import lxx.office.PropertiesManager;
import lxx.utils.AvgValue;

public class AEGMDebugger implements Plugin, BulletManagerListener {

    private static final AvgValue avgBearingOffsets = new AvgValue(5000);
    private static final AvgValue avgLogs = new AvgValue(5000);

    public void roundStarted(Office office) {
        office.getEnemyBulletManager().addListener(this);
    }

    public void battleEnded() {
    }

    public void tick() {
    }

    public void bulletFired(LXXBullet bullet) {
        avgBearingOffsets.addValue(((EnemyBulletPredictionData) bullet.getAimPredictionData()).getPredictedBearingOffsets().size());
        avgLogs.addValue(((EnemyBulletPredictionData) bullet.getAimPredictionData()).getLogs().size());
        PropertiesManager.setDebugProperty("[DEBUG] avg BOs", String.valueOf(avgBearingOffsets.getCurrentValue()));
        PropertiesManager.setDebugProperty("[DEBUG] avg logss", String.valueOf(avgLogs.getCurrentValue()));
    }

    public void bulletHit(LXXBullet bullet) {
    }

    public void bulletMiss(LXXBullet bullet) {
    }

    public void bulletIntercepted(LXXBullet bullet) {
    }

    public void bulletPassing(LXXBullet bullet) {
    }
}
