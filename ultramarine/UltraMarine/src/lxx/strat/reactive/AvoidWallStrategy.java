package lxx.strat.reactive;

import lxx.movement.Strategy;
import lxx.utils.LXXPoint;
import lxx.targeting.TargetChooser;
import lxx.StaticData;
import lxx.BasicRobot;

import java.awt.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * User: jdev
 * Date: 05.01.2010
 */
public class AvoidWallStrategy implements Strategy {

    private final Rectangle battleField;
    private final BasicRobot robot;

    public AvoidWallStrategy(BasicRobot robot) {
        this.robot = robot;
        battleField = new Rectangle(20, 20, robot.getBattleField().width - 40, robot.getBattleField().height - 40);
    }

    public boolean match() {
        robot.setMaxVelocity(0);
        return !battleField.contains(getFuturePos());
    }

    private LXXPoint getFuturePos() {
        final double angle = robot.getHeadingRadians();
        final double velocity = robot.getVelocity();
        return new LXXPoint(robot.getX() + sin(angle) * velocity * 3,
                robot.getY() + cos(angle) * velocity * 3);
    }

    public LXXPoint getDestination(boolean newSession) {
        final double angle = robot.angleTo(robot.battleField.width / 2, robot.battleField.height / 2);
        final double velocity = 50;
        return new LXXPoint(robot.getX() + sin(angle) * velocity * 2,
                robot.getY() + cos(angle) * velocity * 2);
    }

    public void paint(Graphics2D g) {
        LXXPoint futurePos = getFuturePos();
        g.drawOval((int)futurePos.x - 5, (int)futurePos.y - 5, 10, 10);
    }

    public TargetChooser getTargetChooser() {
        return null;
    }
}
