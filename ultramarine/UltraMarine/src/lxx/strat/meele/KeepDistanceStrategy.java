package lxx.strat.meele;

import lxx.movement.Strategy;
import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.utils.LXXConstants;
import lxx.targeting.TargetChooser;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;
import static lxx.StaticData.robot;
import lxx.StaticData;

import java.awt.*;
import java.util.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 04.01.2010
 */
public class KeepDistanceStrategy implements Strategy {

    private final TargetManager targetManager;
    private final Rectangle activityArea;

    private LXXPoint dest;

    public KeepDistanceStrategy(TargetManager targetManager) {
        this.targetManager = targetManager;
        this.activityArea = new Rectangle(40, 40, robot.battleField.width - 80, robot.battleField.height - 80);
    }

    public boolean match() {
        if (robot.getOthers() > 5 || targetManager.getAliveTargetCount() == 0) {
            return false;
        }
        Map<Target, Double> dists = new HashMap<Target, Double>();
        final java.util.List<Target> aliveTargets = targetManager.getAliveTargets();
        for (Target t : aliveTargets) {
            final Target closest = targetManager.getClosestTergetToT(t);
            if (closest == null) {
                return false;
            }
            dists.put(t, closest.aDistance(t) + 80);
        }

        LXXPoint candidate = dest;
        Target closest = targetManager.getClosestTarget();
        for (int i = 0; i < 20; i++) {
            double angle = Utils.angle(closest.getPosition(), robot.getPosition()) + Math.random() * LXXConstants.RADIANS_20 - LXXConstants.RADIANS_10;
            double dist = dists.get(closest);

            if (candidate == null) {
                candidate = new LXXPoint(closest.getX() + sin(angle) * dist,
                        closest.getY() + cos(angle) * dist);
            }

            for (Target t : aliveTargets) {
                final double d = t.aDistance(candidate);
                if (d < dists.get(t)) {
                    candidate = null;
                    break;
                }
            }
            if (candidate != null && robot.getBattleField().contains(candidate)) {
                break;
            }

            candidate = new LXXPoint(closest.getX() + sin(angle) * dist,
                    closest.getY() + cos(angle) * dist);
        }
        
        dest = candidate;

        return candidate != null && robot.getBattleField().contains(candidate) && StaticData.robot.getDestination().aDistance(dest) > 20;
    }

    public LXXPoint getDestination(boolean newSession) {
        if (abs(robot.getDistanceRemaining()) > 10 && !newSession) {
            return null;
        }

        return dest;
    }

    public void paint(Graphics2D g) {
        if (targetManager.getAliveTargetCount() == 0) {
            return;
        }

        g.setColor(Color.RED);
        for (Target t : targetManager.getAliveTargets()) {
            final Target another = targetManager.getClosestTergetToT(t);
            final double dist = another.aDistance(t) * 2 + 20;
            g.drawOval((int) (t.getX() - dist / 2), (int) (t.getY() - dist / 2), (int) dist, (int) dist);
            g.drawLine((int) t.getX(), (int) t.getY(), (int) another.getX(), (int) another.getY());
        }
    }

    public TargetChooser getTargetChooser() {
        return new KDTargetChooser();
    }

    public class KDTargetChooser implements TargetChooser {

        public Target getBestTarget() {
            return targetManager.getClosestTarget();
        }

        public double firePower() {
            return 0.25;
        }
    }

}
