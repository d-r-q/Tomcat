/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.bullets.AbstractGFAimingPredictionData;
import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;
import lxx.utils.APoint;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class EnemyBulletsPredictionData extends AbstractGFAimingPredictionData {

    private final List<Double> predictedBearingOffsets;

    public EnemyBulletsPredictionData(Map<Double, Double> matches, List<Double> predictedBearingOffsets) {
        super(matches);

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
}
