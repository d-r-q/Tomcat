/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.enemy_bullets;

import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

/**
 * User: jdev
 * Date: 07.09.2010
 */
public class EnemyAimingPredictionData implements AimingPredictionData {

    private static final NumberFormat format = new DecimalFormat("###.###");

    private final List<SegmentDanger<Double>> dangers = new ArrayList<SegmentDanger<Double>>();
    private final double step;

    private double maxDanger;
    private double maxBearingOffset = 0;

    public EnemyAimingPredictionData(Map<Double, Double> matches) {
        for (Double bearingOffset : matches.keySet()) {
            double danger = matches.get(bearingOffset);
            if (danger == -1) {
                continue;
            }
            if (danger > maxDanger) {
                maxDanger = danger;
            }

            dangers.add(new SegmentDanger<Double>(bearingOffset, danger));
            maxBearingOffset = max(maxBearingOffset, bearingOffset);
        }
        step = (maxBearingOffset * 2 + LXXConstants.RADIANS_1) / matches.size();
    }

    public double getAverangeDanger(double baseBearingOffset, double botWidthRadians) {
        double totalDanger = 0;
        for (double delta = -botWidthRadians / 2; delta <= botWidthRadians / 2 + 0.01; delta += botWidthRadians / 10) {
            totalDanger += getDanger(baseBearingOffset + delta);
        }
        return totalDanger / 10;
    }

    public double getMaxDanger(double baseBearingOffset, double botWidthRadians) {
        return max(max(getDanger(baseBearingOffset), getDanger(baseBearingOffset - botWidthRadians / 2)), getDanger(baseBearingOffset + botWidthRadians / 2));
    }

    public double getDanger(double bearingOffset) {
        final int idx = LXXUtils.limit(0, (int) ((bearingOffset + maxBearingOffset) / step), dangers.size() - 1);
        final SegmentDanger<Double> segmentDanger = dangers.get(idx);
        final SegmentDanger<Double> prevSegmentDanger = idx > 0 ? dangers.get(idx - 1) : null;
        final SegmentDanger<Double> nextSegmentDanger = idx < dangers.size() - 1 ? dangers.get(idx + 1) : null;

        if (bearingOffset < segmentDanger.bearingOffset) {
            if (prevSegmentDanger == null) {
                return segmentDanger.match;
            }

            return (prevSegmentDanger.match * abs(segmentDanger.bearingOffset - bearingOffset) / step +
                    segmentDanger.match * abs(bearingOffset - prevSegmentDanger.bearingOffset) / step);
        } else {
            if (nextSegmentDanger == null) {
                return segmentDanger.match;
            }

            return (nextSegmentDanger.match * abs(bearingOffset - segmentDanger.bearingOffset) / step +
                    segmentDanger.match * abs(nextSegmentDanger.bearingOffset - bearingOffset) / step);
        }
    }


    public void paint(LXXGraphics g, LXXBullet bullet) {
        double baseAlpha = bullet.getFirePosition().angleTo(bullet.getTargetPosAtFireTime());
        final double currentBearingOffset = bullet.getFirePosition().angleTo(bullet.getTarget());
        float currentBearingOffsetDanger = 0;
        for (SegmentDanger<Double> danger : dangers) {
            double alpha = baseAlpha + (danger.bearingOffset);

            float match = (float) danger.match;
            final Color rgb = new Color(Color.HSBtoRGB((float) (0.33F - 0.33F * min(match / maxDanger, 1F)), 1F, 1F));
            g.setColor(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 240));

            int length = (int) (8 * match / maxDanger);

            APoint pnt1 = bullet.getFirePosition().project(alpha, bullet.getTravelledDistance() - 5 - length / 2);
            APoint pnt2 = bullet.getFirePosition().project(alpha, bullet.getTravelledDistance() - 5 + length / 2);
            g.drawLine(pnt1, pnt2);

            if (LXXUtils.anglesDiff(currentBearingOffset, alpha) < LXXConstants.RADIANS_1) {
                currentBearingOffsetDanger = match;
            }
        }

        final Color rgb = new Color(Color.HSBtoRGB((float) (0.33F - 0.33F * currentBearingOffsetDanger / maxDanger), 1F, 1F));
        g.setColor(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 240));
        final APoint pnt1 = bullet.getFirePosition().project(currentBearingOffset, bullet.getTravelledDistance() - 5 + 6);
        final APoint pnt2 = bullet.getFirePosition().project(currentBearingOffset, bullet.getTravelledDistance() - 5 - 6);
        g.drawLine(pnt1, pnt2);

        final Font oldFont = g.getFont();
        g.setFont(new Font("Arial", Font.PLAIN, 10));

        final APoint dangerLabelPos = bullet.getFirePosition().project(currentBearingOffset, bullet.getTravelledDistance() - 5 - 6 - 20);
        g.drawString(dangerLabelPos, format.format(currentBearingOffsetDanger));

        g.setColor(Color.WHITE);
        final double bulletFlightTime = (bullet.getDistanceToTarget() - bullet.getTravelledDistance()) / bullet.getSpeed();
        final APoint bftLabelPos = bullet.getFirePosition().project(bullet.angleToTargetPos() - LXXConstants.RADIANS_50, bullet.getTravelledDistance() - 5 - 6 - 20);
        g.drawString(bftLabelPos, format.format(bulletFlightTime));

        g.setFont(oldFont);
    }

}