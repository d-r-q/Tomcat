/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.bullets.PastBearingOffset;

import java.util.List;
import java.util.Map;

public class AEGMPredictionData extends EnemyBulletPredictionData {

    private final Map<AdvancedEnemyGunModel.Log, List<PastBearingOffset>> allLogsPredictions;

    public AEGMPredictionData(List<PastBearingOffset> predictedBearingOffsets,
                              int enemyWavesCollected, long predictionTime, Map<AdvancedEnemyGunModel.Log, List<PastBearingOffset>> allLogsPredictions) {
        super(predictedBearingOffsets, enemyWavesCollected, predictionTime);
        this.allLogsPredictions = allLogsPredictions;
    }

    public List<PastBearingOffset> getBearingOffset(AdvancedEnemyGunModel.Log log) {
        return allLogsPredictions.get(log);
    }

}
