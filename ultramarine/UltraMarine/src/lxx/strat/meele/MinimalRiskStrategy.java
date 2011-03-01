package lxx.strat.meele;

import lxx.movement.Strategy;
import lxx.movement.minimal_risk.MinimalRiskModel;
import lxx.utils.LXXPoint;
import lxx.targeting.TargetChooser;
import lxx.BasicRobot;
import lxx.UltraMarine;

import java.awt.*;

/**
 * User: jdev
 * Date: 03.11.2009
 */
public class MinimalRiskStrategy implements Strategy {

    private final MinimalRiskModel minimalRiskModel;
    private final BasicRobot robot;
    private LXXPoint dest;

    public MinimalRiskStrategy(UltraMarine robot, MinimalRiskModel minimalRiskModel) {
        this.robot = robot;
        this.minimalRiskModel = minimalRiskModel;        
    }

    public boolean match() {
        minimalRiskModel.recalculate();
        return minimalRiskModel.getSafestPoint() != null;
    }

    public LXXPoint getDestination(boolean newSession) {
        if (dest != null && !newSession && robot.getDistanceRemaining() > 25 && dest.equals(robot.getDestination())) {
            return null;
        }
        dest = minimalRiskModel.getSafestPoint();
        return dest;
    }

    public void paint(Graphics2D g) {
        minimalRiskModel.paint(g);
    }

    public TargetChooser getTargetChooser() {
        return null;
    }
}
