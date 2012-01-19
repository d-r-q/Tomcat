/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.win;

import lxx.Tomcat;
import lxx.bullets.enemy.EnemyBulletManager;
import lxx.paint.LXXGraphics;
import lxx.paint.Painter;
import lxx.strategies.MovementDecision;
import lxx.strategies.Strategy;
import lxx.strategies.TurnDecision;
import lxx.targeting.TargetManager;
import lxx.utils.LXXConstants;
import robocode.util.Utils;

import java.awt.*;

import static java.lang.Math.*;

public class WinStrategy implements Strategy, Painter {

    private final Tomcat robot;
    private final TargetManager targetManager;
    private final EnemyBulletManager enemyBulletManager;

    private Long winTime = -1L;
    private static final double PARADE_HEADING = LXXConstants.RADIANS_90;

    public WinStrategy(Tomcat robot, TargetManager targetManager,
                       EnemyBulletManager enemyBulletManager) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.enemyBulletManager = enemyBulletManager;
    }

    public boolean match() {
        boolean match = robot.getTime() > 10 && enemyBulletManager.getBulletsOnAir(1).size() == 0 &&
                targetManager.isNoAliveEnemies();

        if (match && winTime == -1L) {
            winTime = robot.getTime();
        }

        return match;
    }

    public TurnDecision makeDecision() {
        return new TurnDecision(
                new MovementDecision(0, getTurnRemaining()),
                Utils.normalRelativeAngle(-robot.getGunHeadingRadians()), 0.1,
                Utils.normalRelativeAngle(-robot.getRadarHeadingRadians()),
                targetManager.getAnyDuelOpponent(), null);
    }

    public double getTurnRemaining() {
        double turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - robot.getHeadingRadians());
        if (abs(turnRemaining) > LXXConstants.RADIANS_90) {
            turnRemaining = Utils.normalRelativeAngle(PARADE_HEADING - Utils.normalAbsoluteAngle(robot.getHeadingRadians() + Math.PI));
        }
        return turnRemaining;
    }

    public void paint(LXXGraphics lxxGraphics) {
        if (winTime == -1L) {
            return;
        }
        final int flagLength = 46;
        final int flagHeight = (int) min((robot.getTime() - winTime) * 3, 24);
        final int lineLength = flagHeight / 3;
        int x = (int) (robot.getX()) + 2;
        final int baseY = (int) (robot.getY()) + 20;

        for (int i = 0; i < flagLength; i++) {
            final double deltaFunctionValue = cos((i + robot.getTime() * 3D) / (flagLength / 5D));
            final double yDelta = 3D / ((flagLength - i) / 3);
            double lineY = baseY + deltaFunctionValue * yDelta;
            lxxGraphics.setColor(Color.RED);
            lxxGraphics.drawLine(x, lineY, x, lineY + lineLength);

            lineY += lineLength;
            lxxGraphics.setColor(Color.BLUE);
            lxxGraphics.drawLine(x, lineY, x, lineY + lineLength);

            lineY += lineLength;
            lxxGraphics.setColor(Color.WHITE);
            lxxGraphics.drawLine(x, lineY, x, lineY + lineLength);

            if (deltaFunctionValue > 0.5) {
                lxxGraphics.setColor(new Color(255, 255, 255, (int) (105 * (deltaFunctionValue - 0.5) * 2)));
                lxxGraphics.drawLine(x, baseY + deltaFunctionValue * yDelta, x, baseY + deltaFunctionValue * yDelta + flagHeight);
            }
            if (deltaFunctionValue < -0.5) {
                lxxGraphics.setColor(new Color(0, 0, 0, (int) (105 * (abs(deltaFunctionValue) - 0.5) * 2)));
                lxxGraphics.drawLine(x, baseY + deltaFunctionValue * yDelta, x, baseY + deltaFunctionValue * yDelta + flagHeight);
            }

            x++;
        }

        x = (int) (robot.getX()) + 2;
        lxxGraphics.setColor(new Color(128, 128, 128, 175));
        lxxGraphics.drawLine(x - 2, baseY - 20, x - 2, baseY + flagHeight + 2);
        lxxGraphics.drawLine(x - 1, baseY - 20, x - 1, baseY + flagHeight + 2);

        lxxGraphics.setColor(new Color(192, 192, 192, 175));
        lxxGraphics.drawLine(x, baseY - 20, x, baseY + flagHeight + 2);
        lxxGraphics.drawLine(x + 1, baseY - 20, x + 1, baseY + flagHeight + 2);

        lxxGraphics.setColor(new Color(64, 64, 64, 175));
        lxxGraphics.drawLine(x + 2, baseY - 20, x + 2, baseY + flagHeight + 2);
        lxxGraphics.drawLine(x + 3, baseY - 20, x + 3, baseY + flagHeight + 2);
    }
}
