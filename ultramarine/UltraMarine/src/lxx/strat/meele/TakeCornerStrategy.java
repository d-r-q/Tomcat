package lxx.strat.meele;

import lxx.utils.LXXPoint;
import lxx.BasicRobot;
import lxx.movement.CornerManager;
import lxx.utils.LXXConstants;
import lxx.movement.Strategy;
import lxx.targeting.TargetManager;
import lxx.targeting.TargetChooser;
import lxx.targeting.Target;

import java.awt.*;
import static java.lang.Math.round;
import static java.lang.Math.min;
import static java.lang.Math.random;
import static java.lang.Math.cos;
import static java.lang.Math.abs;
import static java.lang.StrictMath.sin;
import java.util.Collection;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class TakeCornerStrategy implements Strategy {

    private final BasicRobot robot;
    private final TargetManager targetManager;

    private final int cornerHyp;
    private final TargetChooser targetChooser;
    private final CornerManager cornerManager;

    private Rectangle currentCorner;

    public TakeCornerStrategy(BasicRobot robot, TargetManager targetManager, CornerManager cornerManager) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.cornerManager = cornerManager;
        this.targetChooser = new TCTargetChooser();

        int bfw = (int) robot.getBattleFieldWidth();
        int cornerWidth = (int) round(bfw / 3.75D);
        int bfh = (int) robot.getBattleFieldHeight();
        int cornerHeight = (int) round(bfh / 3.75D);
        cornerHyp = (int) round(min(cornerWidth, cornerHeight) * 0.7D);
    }

    public boolean match() {
        Rectangle safeCorner = cornerManager.getMostSafeCorner();
        int tcIn = targetManager.getTargetCountIn(safeCorner);
        return cornerManager.getCorner() == null && (tcIn == 0 || (tcIn == 1 && targetManager.getTargetsIn(safeCorner).iterator().next().getEnergy() < robot.getEnergy() * 0.5));
    }

    public LXXPoint getDestination(boolean newSession) {
        Rectangle corner = cornerManager.getMostSafeCorner();
        if (corner != currentCorner) {
            currentCorner = corner;
        } else if (abs(robot.getDistanceRemaining()) > 10) {
            return null;
        }
        LXXPoint cCenter = cornerManager.getCornerCenter(corner);
        double angle = random() * LXXConstants.RADIANS_360;
        double distance = cornerHyp / 3 + cornerHyp / 3 * random();
        LXXPoint res = new LXXPoint();
        res.x = cCenter.x + sin(angle) * distance;
        res.y = cCenter.y + cos(angle) * distance;
        return res;
    }

    public void paint(Graphics2D g) {
    }

    public TargetChooser getTargetChooser() {
        return targetChooser;
    }

    private class TCTargetChooser implements TargetChooser {

        public Target getBestTarget() {
            Target closestTarget = targetManager.getClosestTarget();
            if (closestTarget != null && closestTarget.aDistance(robot) < 150) {
                return closestTarget;
            }
            Rectangle corner = cornerManager.getMostSafeCorner();
            Collection<Target> targets = targetManager.getTargetsIn(corner);
            if (targets.size() == 0) {
                return targetManager.getClosestTergetTo(cornerManager.getCornerCenter(cornerManager.getMostSafeCorner()));
            }

            Target res = null;
            double minEnergy = Double.MAX_VALUE;
            for (Target t : targets) {
                if (t.getEnergy() < minEnergy) {
                    res = t;
                    minEnergy = t.getEnergy();
                }
            }
            return res;
        }

        public double firePower() {
            final Target target = getBestTarget();
            if (target == null) {
                return 0;
            }
            return 1200 / robot.distance(target);
        }
    }

}
