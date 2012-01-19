/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.bullets.AbstractGFAimingPredictionData;
import lxx.bullets.LXXBullet;
import lxx.paint.LXXGraphics;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXUtils;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class TCPredictionData extends AbstractGFAimingPredictionData {

    private final Map<Double, Double> matches;
    private final List<APoint> predictedPoses;
    private final APoint robotPos;
    private final APoint initialPos;

    public TCPredictionData(Map<Double, Double> matches, List<APoint> predictedPoses, APoint robotPos, APoint initialPos) {
        super(null, -1);
        this.matches = matches;
        this.predictedPoses = predictedPoses;
        this.robotPos = robotPos;
        this.initialPos = initialPos;
    }

    public Map<Double, Double> getMatches() {
        return matches;
    }

    @Override
    public void paint(LXXGraphics g, LXXBullet bullet) {
        super.paint(g, bullet);

        for (APoint predictedPos : predictedPoses) {
            final double pntDanger = getDangerInt(LXXUtils.bearingOffset(robotPos, initialPos, predictedPos), LXXConstants.RADIANS_0_5);
            final Color rgb = new Color(Color.HSBtoRGB((float) (0.33F - 0.33F * min(pntDanger / maxDanger, 1F)), 1F, 1F));
            Color borderColor = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 155);
            g.setColor(borderColor);
            g.drawRect(predictedPos, LXXConstants.ROBOT_SIDE_HALF_SIZE);

            final Color fillColor = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), borderColor.getAlpha() / 10);
            g.setColor(fillColor);
            g.fillSquare(predictedPos, LXXConstants.ROBOT_SIDE_HALF_SIZE);
        }
    }
}
