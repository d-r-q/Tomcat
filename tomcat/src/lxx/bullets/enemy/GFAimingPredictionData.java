/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets.enemy;

import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;
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
public class GFAimingPredictionData implements AimingPredictionData {

    private static final NumberFormat format = new DecimalFormat("###.###");

    private final List<BearingOffsetDanger> dangers = new ArrayList<BearingOffsetDanger>();
    private final double step;

    private double maxDanger;
    private double maxBearingOffset = 0;

    public GFAimingPredictionData(Map<Double, Double> matches) {
        for (Double bearingOffset : matches.keySet()) {
            double danger = matches.get(bearingOffset);
            if (danger == -1) {
                continue;
            }
            if (danger > maxDanger) {
                maxDanger = danger;
            }

            dangers.add(new BearingOffsetDanger(bearingOffset, danger));
            maxBearingOffset = max(maxBearingOffset, bearingOffset);
        }
        step = (maxBearingOffset * 2 + LXXConstants.RADIANS_1) / matches.size();
    }

    public double getDanger(double baseBearingOffset, double botWidthRadians) {
        final int fromIdx = (int) LXXUtils.limit(0, floor((baseBearingOffset - botWidthRadians / 2 + maxBearingOffset) / step), dangers.size() - 1);
        final int toIdx = (int) LXXUtils.limit(0, ceil((baseBearingOffset + botWidthRadians / 2 + maxBearingOffset) / step), dangers.size() - 1);
        double danger = 0;
        for (int i = fromIdx; i <= toIdx; i++) {
            danger += dangers.get(i).danger;
        }

        return danger;
    }


    public void paint(LXXGraphics g, LXXBullet bullet) {
        final APoint firePosition = bullet.getFirePosition();
        final double baseDistance = bullet.getTravelledDistance() - 5;

        g.setColor(new Color(255, 255, 255, 240));
        final double baseAlpha = bullet.noBearingOffset();
        g.drawLine(firePosition, baseAlpha, baseDistance, 12);

        final double currentAngle = firePosition.angleTo(bullet.getTarget());
        float currentBearingOffsetDanger = 0;
        for (BearingOffsetDanger danger : dangers) {
            double alpha = baseAlpha + (danger.bearingOffset);

            float match = (float) danger.danger;
            final Color rgb = new Color(Color.HSBtoRGB((float) (0.33F - 0.33F * min(match / maxDanger, 1F)), 1F, 1F));
            g.setColor(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 240));

            int length = (int) (8 * match / maxDanger);
            g.drawLine(firePosition, alpha, baseDistance, length);

            if (LXXUtils.anglesDiff(currentAngle, alpha) < LXXConstants.RADIANS_1) {
                currentBearingOffsetDanger = match;
            }
        }

        final Color rgb = new Color(Color.HSBtoRGB((float) (0.33F - 0.33F * currentBearingOffsetDanger / maxDanger), 1F, 1F));
        g.setColor(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 240));
        g.drawLine(firePosition, currentAngle, baseDistance, 6);

        final Font oldFont = g.getFont();
        g.setFont(new Font("Arial", Font.PLAIN, 10));

        final APoint dangerLabelPos = firePosition.project(currentAngle, baseDistance - 6 - 20);
        g.drawString(dangerLabelPos, format.format(currentBearingOffsetDanger));

        g.setColor(Color.WHITE);
        final double bulletFlightTime = (bullet.getDistanceToTarget() - bullet.getTravelledDistance()) / bullet.getSpeed();
        final APoint bftLabelPos = firePosition.project(baseAlpha - LXXConstants.RADIANS_50, baseDistance - 6 - 20);
        g.drawString(bftLabelPos, format.format(bulletFlightTime));

        g.setFont(oldFont);
    }

}