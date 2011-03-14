/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;

import java.awt.*;
import java.util.List;

import static java.lang.Math.*;

public class TCPredictionData implements AimingPredictionData {

    private final List<TomcatClaws.TurnPrediction> predictedPoses;
    private final APoint predictedFirePosition;

    public TCPredictionData(List<TomcatClaws.TurnPrediction> predictedPoses, APoint predictedFirePosition) {
        this.predictedPoses = predictedPoses;
        this.predictedFirePosition = predictedFirePosition;
    }

    public void paint(LXXGraphics g, LXXBullet bullet) {
        if (!bullet.getTarget().isAlive()) {
            return;
        }
        if (predictedPoses.size() > 0) {
            drawPredictedPath(g);
        }

        final APoint firePosition = bullet.getFirePosition();
        drawFirePositions(g, firePosition);

        final LXXRobot target = bullet.getTarget();
        final double travelledDistance = bullet.getTravelledDistance();
        final double bulletFlightTime = (firePosition.aDistance(target) - travelledDistance) / bullet.getSpeed();
        final APoint targetPredictedPos = target.project(target.getState().getAbsoluteHeadingRadians(), target.getState().getVelocityModule() * bulletFlightTime);
        final double bulletHeadingRadians = bullet.getHeadingRadians();

        g.setColor(getColor(bullet, targetPredictedPos, bulletHeadingRadians));
        final double angleToTarget = firePosition.angleTo(target);
        final double angleToTargetAtFireTime = bullet.noBearingOffset();
        final double robotWidthAtRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), target);
        final double robotHalfWidthAtRadians = robotWidthAtRadians / 2;
        final double baseArcDistance = travelledDistance - 10;
        final double lowArcDistance = baseArcDistance - 3;
        final double highArcDistance = baseArcDistance + 3;

        final double anglesRange = min(LXXConstants.RADIANS_45, max(abs(angleToTarget - angleToTargetAtFireTime), abs(bulletHeadingRadians - angleToTargetAtFireTime)) + LXXConstants.RADIANS_5);
        g.drawArc(firePosition,
                firePosition.project(angleToTargetAtFireTime - anglesRange, baseArcDistance),
                firePosition.project(angleToTargetAtFireTime + anglesRange, baseArcDistance));
        g.drawLine(firePosition.project(angleToTargetAtFireTime, lowArcDistance), firePosition.project(angleToTargetAtFireTime, highArcDistance));

        g.setStroke(new BasicStroke(2));

        g.setColor(new Color(168, 255, 21, 200));
        final double targetMinAngle = angleToTarget - robotHalfWidthAtRadians;
        final double targetMaxAngle = angleToTarget + robotHalfWidthAtRadians;
        drawArc(g, firePosition, baseArcDistance, lowArcDistance, highArcDistance, targetMinAngle, targetMaxAngle);

        g.setColor(new Color(255, 109, 21, 200));
        final double bulletMinAngle = bulletHeadingRadians - LXXConstants.RADIANS_0_5;
        final double bulletMaxAngle = bulletHeadingRadians + LXXConstants.RADIANS_0_5;
        drawArc(g, firePosition, baseArcDistance, lowArcDistance, highArcDistance, bulletMinAngle, bulletMaxAngle);
    }

    private void drawArc(LXXGraphics g, APoint firePosition, double baseArcDistance, double lowArcDistance, double highArcDistance, double targetMinAngle, double targetMaxAngle) {
        g.drawLine(firePosition.project(targetMinAngle, lowArcDistance), firePosition.project(targetMinAngle, highArcDistance));
        g.drawLine(firePosition.project(targetMaxAngle, lowArcDistance), firePosition.project(targetMaxAngle, highArcDistance));
        g.drawLine(firePosition.project(targetMinAngle, baseArcDistance), firePosition.project(targetMaxAngle, baseArcDistance));
    }

    private void drawFirePositions(LXXGraphics g, APoint firePosition) {
        g.setColor(Color.BLUE);
        g.drawCircle(predictedFirePosition, 4);

        g.setColor(Color.GREEN);
        g.drawCircle(firePosition, 4);
    }

    private void drawPredictedPath(LXXGraphics g) {
        g.setColor(new Color(255, 0, 0, 175));
        for (final TomcatClaws.TurnPrediction pnt : predictedPoses) {
            g.fillCircle(pnt.enemyPos, 3);
        }
    }

    private Color getColor(LXXBullet bullet, APoint targetPredictedPos, double bulletHeadingRadians) {
        final double anglesDiff = LXXUtils.anglesDiff(bulletHeadingRadians, bullet.getFirePosition().angleTo(targetPredictedPos));
        double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), targetPredictedPos);
        final float missProbability = anglesDiff > robotWidthInRadians ? 1 : (float) (anglesDiff < robotWidthInRadians / 3 ? 0 : anglesDiff / robotWidthInRadians);
        return new Color(Color.HSBtoRGB(28F / 255F, 255F / 255F, (1630F - 100F * missProbability) / 255F));
    }

}
