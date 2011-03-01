package lxx.strat.meele;

import lxx.utils.LXXPoint;
import lxx.BasicRobot;
import lxx.movement.CornerManager;
import lxx.utils.LXXConstants;
import lxx.utils.Utils;
import static lxx.utils.Utils.angle;
import lxx.movement.Strategy;
import lxx.targeting.TargetManager;
import lxx.targeting.TargetChooser;
import lxx.targeting.Target;

import java.awt.*;
import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class HoldCornerStrategy implements Strategy {

    private final BasicRobot robot;
    private final TargetManager targetManager;
    private final TargetChooser targetChooser;
    private final CornerManager cornerManager;

    public HoldCornerStrategy(BasicRobot robot, TargetManager targetManager, CornerManager cornerManager) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.cornerManager = cornerManager;
        this.targetChooser = new HCTargetChooser();
    }

    public boolean match() {
        Rectangle corner = cornerManager.getCorner();
        if (corner == null) {
            return false;
        }

        int tcIn = targetManager.getTargetCountIn(corner);
        return (tcIn == 0 || tcIn == 1 && targetManager.getTargetsIn(corner).iterator().next().getEnergy() < robot.getEnergy() * 0.4);

    }

    public LXXPoint getDestination(boolean newSession) {
        if (abs(robot.getDistanceRemaining()) > 10 && !newSession) {
            return null;
        }

        Rectangle corner = cornerManager.getCorner();
        LXXPoint cCenter = cornerManager.getCornerCenter(corner);

        LXXPoint res = new LXXPoint();
        res.x = cCenter.x + (corner.width / 2 - 20) * (Math.random() * 2 - 1);
        res.y = cCenter.y + (corner.height / 2 - 20) * (Math.random() * 2 - 1);
        return res;
    }

    public void paint(Graphics2D g) {
        g.setColor(Color.WHITE);
        for (Rectangle corner : cornerManager.getCorners()) {
            if (corner == null) {
                return;
            }
            cornerManager.getCornerCenter(corner);
            g.drawRect(corner.x, corner.y, corner.width, corner.height);
        }
    }

    public TargetChooser getTargetChooser() {
        return targetChooser.getBestTarget() != null ? targetChooser : null;
    }

    private class HCTargetChooser implements TargetChooser {

        public Target getBestTarget() {
            Target target = targetManager.getClosestTarget();
            // todo: fix
            Rectangle corner = cornerManager.getCorner();
            if (target != null && corner != null &&
                    abs(angle(target, cornerManager.getCornerCenter(corner)) - target.getAbsoluteHeading()) < LXXConstants.RADIANS_45) {
                return target;
            }
            return null;
        }

        public double firePower() {
            if (robot.getOthers() < 6) {
                return 1200 / robot.distance(getBestTarget());
            }
            return 3;
        }
    }

}
