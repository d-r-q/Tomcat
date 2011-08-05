/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.bullets.AbstractGFAimingPredictionData;
import lxx.bullets.LXXBullet;
import lxx.bullets.PastBearingOffset;
import lxx.paint.LXXGraphics;
import lxx.ts_log.attributes.AttributesManager;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EnemyBulletPredictionData extends AbstractGFAimingPredictionData {

    private static final double A = 0.02;
    private static final int B = 20;
    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_1;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private final List<PastBearingOffset> predictedBearingOffsets;
    private final int enemyWavesCollected;

    public EnemyBulletPredictionData(List<PastBearingOffset> predictedBearingOffsets, int enemyWavesCollected) {
        this.predictedBearingOffsets = predictedBearingOffsets;
        this.enemyWavesCollected = enemyWavesCollected;
    }

    @Override
    public void paint(LXXGraphics g, LXXBullet bullet) {
        super.paint(g, bullet);

        final double dist = bullet.getTravelledDistance() - 15;
        g.setColor(Color.WHITE);
        for (PastBearingOffset bo : predictedBearingOffsets) {
            final double alpha = bullet.noBearingOffset() + bo.bearingOffset;
            final APoint bulletPos = bullet.getFirePosition().project(alpha, dist);
            g.fillCircle(bulletPos, 3);
        }
    }

    @Override
    protected Map<Double, Double> getMatches() {
        final int bearingOffsetsCount = predictedBearingOffsets.size();
        final Map<Double, Double> bearingOffsetDangers = new TreeMap<Double, Double>();
        for (double wavePointBearingOffset = -MAX_BEARING_OFFSET; wavePointBearingOffset <= MAX_BEARING_OFFSET + LXXConstants.RADIANS_0_1; wavePointBearingOffset += BEARING_OFFSET_STEP) {
            double bearingOffsetDanger = 0;
            if (bearingOffsetsCount > 0) {
                for (PastBearingOffset bulletBearingOffset : predictedBearingOffsets) {
                    // this is empirical selected formula, which
                    // produce smooth peaks for bearing offsets
                    final double difference = bulletBearingOffset.bearingOffset - wavePointBearingOffset;
                    final double differenceSquare = difference * difference;
                    final double bearingOffsetsDifference = differenceSquare + A;
                    final double d = enemyWavesCollected == -1
                            ? 1
                            : (bulletBearingOffset.source.getAttrValue(AttributesManager.enemyOutgoingWavesCollected) / enemyWavesCollected);
                    bearingOffsetDanger += (d * d * d) / (bearingOffsetsDifference * B);
                }
            }
            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
        }

        return bearingOffsetDangers;
    }

    public List<PastBearingOffset> getPredictedBearingOffsets() {
        return predictedBearingOffsets;
    }
}
