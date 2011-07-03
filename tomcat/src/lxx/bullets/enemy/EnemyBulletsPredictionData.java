/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.bullets.AbstractGFAimingPredictionData;
import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EnemyBulletsPredictionData extends AbstractGFAimingPredictionData {

    private static final double A = 0.02;
    private static final int B = 20;
    private static final double BEARING_OFFSET_STEP = LXXConstants.RADIANS_1;
    private static final double MAX_BEARING_OFFSET = LXXConstants.RADIANS_45;

    private final List<Double> predictedBearingOffsets;

    public EnemyBulletsPredictionData(List<Double> predictedBearingOffsets) {
        this.predictedBearingOffsets = predictedBearingOffsets;
    }

    public List<Double> getPredictedBearingOffsets() {
        return predictedBearingOffsets;
    }

    @Override
    public void paint(LXXGraphics g, LXXBullet bullet) {
        super.paint(g, bullet);

        final double dist = bullet.getTravelledDistance() - 15;
        g.setColor(Color.WHITE);
        for (Double bo : predictedBearingOffsets) {
            final double alpha = bullet.noBearingOffset() + bo;
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
                for (Double bulletBearingOffset : predictedBearingOffsets) {
                    // this is empirical selected formula, which
                    // produce smooth peaks for bearing offsets
                    final double difference = bulletBearingOffset - wavePointBearingOffset;
                    final double differenceSquare = difference * difference;
                    final double bearingOffsetsDifference = differenceSquare + A;
                    bearingOffsetDanger += 1D / (bearingOffsetsDifference * B);
                }
            }
            bearingOffsetDangers.put(wavePointBearingOffset, bearingOffsetDanger);
        }

        return bearingOffsetDangers;
    }
}
