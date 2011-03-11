/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.debug;

import lxx.BasicRobot;
import lxx.office.Office;
import lxx.office.TargetManager;
import lxx.targeting.Target;
import lxx.utils.LXXGraphics;

import java.awt.*;

import static java.lang.Math.toDegrees;

public class NoBearingOffsetPainter implements Debugger {

    private BasicRobot robot;
    private TargetManager targetManager;

    public void roundStarted(Office office) {
        robot = office.getRobot();
        targetManager = office.getTargetManager();
    }

    public void roundEnded() {
    }

    public void battleEnded() {
    }

    public void tick() {
        if (targetManager.hasDuelOpponent()) {
            final Target duelOpponent = targetManager.getDuelOpponent();
            final LXXGraphics g = robot.getLXXGraphics();
            g.setColor(Color.BLUE);
            g.drawLine(duelOpponent, robot);
            double angleDegrees = toDegrees(duelOpponent.angleTo(robot));
            g.drawString((duelOpponent.getX() + robot.getX()) / 2,
                    (duelOpponent.getY() + robot.getY()) / 2,
                    String.format("%3.2f", angleDegrees));
        }
    }
}
