package lxx.strat;

import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.utils.LXXConstants;
import lxx.BasicRobot;
import lxx.targeting.TargetChooser;
import lxx.movement.Strategy;

import static java.lang.Math.random;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import java.awt.*;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class RandomStrategy implements Strategy {

    private final BasicRobot robot;
    private final int bfWidth;
    private final int bfHeight;

    public RandomStrategy(BasicRobot robot) {
        this.robot = robot;

        this.bfWidth = (int) robot.getBattleFieldWidth();
        this.bfHeight = (int) robot.getBattleFieldHeight();
    }

    public boolean match() {
        return true;
    }

    public LXXPoint getDestination(boolean newSession) {
        if (abs(robot.getDistanceRemaining()) > 5 && !newSession) {
            return null;
        }

        double angle = Math.PI * 2 * random();
        double dist = 50 + 150 * random();

        double x = robot.getX();
        double y = robot.getY();
        LXXPoint res = new LXXPoint(x + sin(angle) * dist,
                y + cos(angle) * dist);
        while (!robot.getBattleField().contains(res)) {
            angle = Math.PI * 2 * random();
            res.x = x + sin(angle) * dist;
            res.y = y + cos(angle) * dist;
        }
        return res;
    }

    public void paint(Graphics2D g) {
    }

    public TargetChooser getTargetChooser() {
        return null;
    }
}
