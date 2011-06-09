/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws;

import lxx.model.TurnSnapshot;
import lxx.office.TurnSnapshotsLog;
import lxx.simulator.RobotProxy;
import lxx.strategies.MovementDecision;
import lxx.targeting.Target;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;

public class TCPredictionData implements AimingPredictionData {

    private final List<LXXPoint> predictedPoses;
    private final APoint predictedFirePosition;
    private final TurnSnapshotsLog turnSnapshotsLog;
    private LinkedList<MovementDecision> predictedDecs;
    private LinkedList<LXXRobotState> predictedStates;
    private LXXRobotState duelOpponent;
    private LinkedList<TurnSnapshot> predictedTSs;
    private LXXRobotState enemyProxy;

    public TCPredictionData(List<LXXPoint> predictedPoses, LinkedList<MovementDecision> predictedDecs, LinkedList<LXXRobotState> predictedStates, APoint predictedFirePosition, TurnSnapshotsLog turnSnapshotsLog, LXXRobotState duelOpponent, LinkedList<TurnSnapshot> predictedTSs, LXXRobotState enemyProxy) {
        this.predictedPoses = predictedPoses;
        this.predictedFirePosition = predictedFirePosition;
        this.turnSnapshotsLog = turnSnapshotsLog;
        this.predictedDecs = predictedDecs;
        this.predictedStates = predictedStates;
        this.duelOpponent = duelOpponent;
        this.predictedTSs = predictedTSs;
        this.enemyProxy = enemyProxy;
    }

    public void paint(LXXGraphics g, LXXBullet bullet) {
        if (!bullet.getTarget().isAlive()) {
            return;
        }
        if (predictedPoses.size() > 0) {
            drawPredictedPath(g, bullet);
        }

        final APoint firePosition = bullet.getFirePosition();
        drawFirePositions(g, firePosition);

        final LXXRobot target = bullet.getTarget();
        final double travelledDistance = bullet.getTravelledDistance();
        final double bulletHeadingRadians = bullet.getHeadingRadians();

        g.setColor(getColor(bullet, target, bulletHeadingRadians));
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

    private void drawPredictedPath(LXXGraphics g, LXXBullet bullet) {
        int idx = 0;
        for (final LXXPoint pnt : predictedPoses) {
            g.setColor(new Color(255,
                    (int) (255 * (1 - ((double) idx / predictedPoses.size()))),
                    (int) (255 * (1 - ((double) idx / predictedPoses.size()))), 175));
            g.fillCircle(pnt, 3);
            idx++;
        }

        int delta = (int) (bullet.getTarget().getTime() - bullet.getWave().getLaunchTime());
        if (delta == 0) {
            return;
        }
        int initDelta = delta;
        for (; delta >= 0; delta--) {
            final TurnSnapshot ts = turnSnapshotsLog.getLastSnapshot((Target) bullet.getTarget(), delta);
            g.setColor(new Color(
                    (int) (255 * (1 - ((double) (initDelta - delta) / initDelta))),
                    (int) (255 * (1 - ((double) (initDelta - delta) / initDelta))),
                    255, 175));
            g.fillCircle(LXXUtils.getEnemyPos(ts), 3);
        }
    }

    private Color getColor(LXXBullet bullet, APoint targetPos, double bulletHeadingRadians) {
        final double anglesDiff = LXXUtils.anglesDiff(bulletHeadingRadians, bullet.getFirePosition().angleTo(targetPos));
        final double robotWidthInRadians = LXXUtils.getRobotWidthInRadians(bullet.getFirePosition(), targetPos);
        if (anglesDiff < robotWidthInRadians / 2) {
            return new Color(255, 200, 200, 170);
        } else {
            return new Color(255, 0, 0, 25);
        }
    }

}
