package lxx.movement;

import lxx.BasicRobot;
import lxx.utils.LXXPoint;
import lxx.targeting.TargetManager;
import lxx.targeting.Target;

import java.awt.*;
import static java.lang.Math.round;
import static java.lang.Math.abs;
import java.util.Collection;

/**
 * User: jdev
 * Date: 04.11.2009
 */
public class CornerManager {

    private final BasicRobot robot;
    private final TargetManager targetManager;

    private final Rectangle[] corners = new Rectangle[4];

    public CornerManager(BasicRobot robot, TargetManager targetManager) {
        this.robot = robot;
        this.targetManager = targetManager;

        int bfw = (int) robot.getBattleFieldWidth();
        int cornerWidth = (int) round(bfw / 3.5D);
        int bfh = (int) robot.getBattleFieldHeight();
        int cornerHeight = (int) round(bfh / 3.5D);

        corners[0] = new Rectangle(0, 0, cornerWidth, cornerHeight);
        corners[1] = new Rectangle(0, bfh - cornerHeight, cornerWidth, cornerHeight);
        corners[2] = new Rectangle(bfw - cornerWidth, bfh - cornerHeight, cornerWidth, cornerHeight);
        corners[3] = new Rectangle(bfw - cornerWidth, 0, cornerWidth, cornerHeight);
    }

    public Rectangle getMostSafeCorner() {
        double maxPower = -1;
        Rectangle res = null;

        for (Rectangle corner : corners) {
            double cornerPower = calculateCornerPower(getCornerCenter(corner), targetManager.getAliveTargets());
            if (maxPower < cornerPower) {
                res = corner;
                maxPower = cornerPower;
            }
        }

        return res;
    }

    public LXXPoint getCornerCenter(Rectangle corner) {
        int ccX = corner.x + corner.width / 2;
        int ccY = corner.y + corner.height / 2;
        return new LXXPoint(ccX, ccY);
    }

    public double calculateCornerPower(LXXPoint corner, Collection<Target> enemies) {
        double res = 0;

        for (Target t : enemies) {
            double v = corner.aDistance(t) / t.getEnergy();
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                continue;
            }
            res += v;
        }
        return res / robot.distance(corner);
    }

    public Rectangle[] getCorners() {
        return corners;
    }

    public Rectangle getCorner() {
        Rectangle corner = null;
        for (Rectangle r : corners) {
            if (r.contains(robot.getPosition())) {
                corner = r;
                break;
            }
        }
        return corner;
    }

    public Rectangle[] getNeigh(Rectangle corner) {
        Rectangle[] res = new Rectangle[2];

        for (int i = 0; i < corners.length; i++) {
            if (corners[i] == corner) {
                int prevIdx;
                int nextIdx;
                if (i == 0) {
                    prevIdx = 3;
                } else {
                    prevIdx = i - 1;
                }

                if (i == corners.length - 1) {
                    nextIdx = 0;
                } else {
                    nextIdx = i + 1;
                }

                res[0] = corners[prevIdx];
                res[1] = corners[nextIdx];
                break;
            }
        }

        return res;
    }

    public boolean isInCorner(LXXPoint position) {
        for (Rectangle corner : corners) {
            if (corner.contains(position)) {
                return true;
            }
        }

        return false;
    }
}
