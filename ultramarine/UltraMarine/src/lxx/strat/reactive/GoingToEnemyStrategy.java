package lxx.strat.reactive;

import lxx.movement.Strategy;
import lxx.utils.LXXPoint;
import lxx.utils.LXXConstants;
import lxx.targeting.TargetChooser;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import lxx.BasicRobot;

import java.awt.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 06.01.2010
 */
public class GoingToEnemyStrategy implements Strategy {
    private final TargetManager targetManager;
    private final BasicRobot robot;
    private final LXXPoint center;

    public GoingToEnemyStrategy(BasicRobot robot, TargetManager targetManager) {
        this.robot = robot;
        this.targetManager = targetManager;

        center = new LXXPoint(robot.battleField.width / 2, robot.battleField.height / 2);
    }

    public boolean match() {
        boolean res = false;

        for (Target t : targetManager.getAliveTargets()) {
            if (robot.aDistance(t) > 300) {
                continue;
            }

            if (abs(robot.angleTo(t) - robot.getAbsoluteHeading()) < LXXConstants.RADIANS_10) {
                res = true;
                break;
            }
        }

        return res;
    }

    public LXXPoint getDestination(boolean newSession) {
        LXXPoint candidate1 = new LXXPoint(robot.getX() + sin(robot.getAbsoluteHeading() + LXXConstants.RADIANS_45),
                robot.getY() + cos(robot.getAbsoluteHeading() + LXXConstants.RADIANS_45));
        LXXPoint candidate2 = new LXXPoint(robot.getX() + sin(robot.getAbsoluteHeading() - LXXConstants.RADIANS_45),
                robot.getY() + cos(robot.getAbsoluteHeading() - LXXConstants.RADIANS_45));

        if (candidate1.distance(center) < candidate2.distance(center)) {
            return candidate1;
        } else {
            return candidate2;
        }
    }

    public void paint(Graphics2D g) {
    }

    public TargetChooser getTargetChooser() {
        return null;
    }
}
