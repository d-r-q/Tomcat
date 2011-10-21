/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.bullets.PastBearingOffset;
import lxx.ts_log.TurnSnapshot;

import java.util.List;
import java.util.Map;

public class AEGMPredictionData extends EnemyBulletPredictionData {

    private final Map<AdvancedEnemyGunModel.Log, List<PastBearingOffset>> allLogsPredictions;
    private TurnSnapshot ts;

    public AEGMPredictionData(List<PastBearingOffset> predictedBearingOffsets,
                              int enemyWavesCollected, long predictionTime, Map<AdvancedEnemyGunModel.Log, List<PastBearingOffset>> allLogsPredictions,
                              TurnSnapshot ts) {
        super(predictedBearingOffsets, enemyWavesCollected, predictionTime);
        this.allLogsPredictions = allLogsPredictions;
        this.ts  = ts;
    }

    public TurnSnapshot getTs() {
        return ts;
    }

    public List<PastBearingOffset> getBearingOffset(AdvancedEnemyGunModel.Log log) {
        return allLogsPredictions.get(log);
    }

}
