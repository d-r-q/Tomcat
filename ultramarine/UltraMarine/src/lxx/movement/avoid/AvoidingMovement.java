package lxx.movement.avoid;

import lxx.UltraMarine;
import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.movement.minimal_risk.RiskPoint;
import lxx.movement.minimal_risk.MinimalRiskModel;
import lxx.targeting.TargetManager;
import lxx.targeting.Target;

import java.awt.*;
import java.util.*;
import java.util.List;
import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 17.10.2009
 */
public class AvoidingMovement {

    private static final double DIST_TO_OTHERS_SCALE = 156777;
    private static final double BEARING_SCALE = 100;
    private static final double DIST_FROM_CENTER_SCALE = 130500;
    private static final double DIST_TO_DEST_SCALE = 17800;
    private static final double BEARING_LAST_HIT_SCALE = 90000;
    private static final double DIST_TO_MIN_RISK_SCALE = 155000;
    private static final double DIST_TO_MAX_RISK_SCALE = 60000;

    private final UltraMarine robot;
    private final TargetManager targetManager;
    private final MinimalRiskModel minimalRiskModel;
    private double[] idealPoint;
    private final double bfWidth;

    public AvoidingMovement(UltraMarine robot, TargetManager targetManager, MinimalRiskModel minimalRiskModel) {
        this.robot = robot;
        this.targetManager = targetManager;
        this.minimalRiskModel = minimalRiskModel;

        bfWidth = this.robot.getBattleFieldWidth();
        idealPoint = new double[]{DIST_TO_OTHERS_SCALE, 0, Utils.scale(bfWidth / 3, bfWidth / 2, DIST_FROM_CENTER_SCALE), 0,
                BEARING_LAST_HIT_SCALE * (Math.PI / 2) / (Math.PI / 2), 0, DIST_TO_MAX_RISK_SCALE};
    }

    public LXXPoint getDestination() {
        List<RiskPoint> points = getPoints(125, minimalRiskModel.getSafestPoint(), minimalRiskModel.getMostRiskPoint());
        double minRisk = Double.MAX_VALUE;
        RiskPoint res = null;
        for (RiskPoint rp : points) {
            if (rp.risk < minRisk) {
                minRisk = rp.risk;
                res = rp;
            }
        }

        return res;
    }

    public void paint(Graphics2D g) {
        g.setColor(Color.WHITE);
        List<RiskPoint> points = getPoints(125, minimalRiskModel.getSafestPoint(), minimalRiskModel.getMostRiskPoint());
        double maxRisk = 0;
        double minRisk = Double.MAX_VALUE;
        for (RiskPoint rp : points) {
            if (rp.risk > maxRisk) {
                maxRisk = rp.risk;
            } else {
                if (rp.risk < minRisk) {
                    minRisk = rp.risk;
                }
            }
        }

        for (RiskPoint rp : points) {

            int radius = 6;
            int gb = (int) (255 * (1D - (rp.risk - minRisk) / (maxRisk - minRisk)));
            if (gb < 0) {
                gb = 0;
            } else if (gb > 255) {
                gb = 255;
            }
            g.setColor(new Color(255, gb, gb));
            g.fillOval((int) Math.round(rp.x - radius / 2), (int) Math.round(rp.y - radius / 2), radius, radius);
            if (rp.risk <= minRisk) {
                radius = 12;
                g.drawOval((int) Math.round(rp.x - radius / 2), (int) Math.round(rp.y - radius / 2), radius, radius);
            }
        }
    }

    private List<RiskPoint> getPoints(double dist, LXXPoint safestPoint, LXXPoint mostRiskPoint) {
        List<RiskPoint> points = new LinkedList<RiskPoint>();
        LXXPoint pos = new LXXPoint(robot.getX(), robot.getY());
        List<Target> targets = targetManager.getAliveTargets();
        // todo(zhidkov) : fix
        if (safestPoint == null) {
            safestPoint = new LXXPoint(0, 0);
        }
        if (mostRiskPoint == null) {
            mostRiskPoint = new LXXPoint(0, 0);
        }

        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 9) {
            LXXPoint p = new LXXPoint(pos.x + Math.sin(angle) * dist,
                    pos.y + Math.cos(angle) * dist);

            // todo: fix
            /*if (!robot.isInBounds20(p)) {
                continue;
            }*/

            LXXPoint p2 = new LXXPoint(pos.x + Math.sin(angle) * dist *  1.5,
                    pos.y + Math.cos(angle) * dist * 1.5);
            final double risk = calculateRisk(p, targets, angle, safestPoint, mostRiskPoint) +
                    calculateRisk(p2, targets, angle, safestPoint, mostRiskPoint) / 2;
            points.add(new RiskPoint(p, risk));
        }

        return points;
    }

    private double calculateRisk (LXXPoint p, List<Target> targets, double angle, LXXPoint safestPoint,
                                  LXXPoint mostRiskPoint) {
        double distToTargets = 0;
        double tCount = 0;
        for (Target t : targets) {
            final double distToT = p.aDistance(t);
            if (distToT < 200) {
                distToTargets = distToT;
                tCount++;                                                
            }
        }
        if (tCount > 0) {
            distToTargets /= tCount;
        } else {
            distToTargets = 200;
        }
        final double heading = robot.getHeadingRadians() + robot.getVelocity() < 0 ? Math.PI : 0;
        double bearing = abs(Utils.normalizeBearing(heading - angle));
        /*if (bearing > Math.PI / 2D) {
            bearing = (Math.PI - bearing) + (Math.PI / 36D);
        }*/

        LXXPoint dest = robot.getDestination();
        if (dest == null) {
            dest = p;
        }

        final double distToSafest = p.distance(safestPoint.getX(), safestPoint.getY());
        final double distToMostRisk = p.distance(mostRiskPoint.getX(), mostRiskPoint.getY());

        final double distToOthers = DIST_TO_OTHERS_SCALE * (distToTargets / 200);
        final double bearingScaled = BEARING_SCALE * (abs(bearing) / Math.PI);
        final double distFromCenter = DIST_FROM_CENTER_SCALE * p.distance(bfWidth / 2D, robot.getBattleFieldHeight() / 2D) / (bfWidth * 1.42D / 2D);
        final double distToDest = DIST_TO_DEST_SCALE * p.distance(dest.getX(), dest.getY()) / (75D);

        final double lastHitBearing;
        final double hitBulletHeading = robot.getLastHitBulletHeading();
        if (hitBulletHeading == 500) {
            lastHitBearing = Math.PI / 2D;
        } else {
            lastHitBearing = abs(Utils.normalizeBearing(hitBulletHeading - angle * signum(robot.getVelocity())));
        }
        final double bearingToLastHit = DIST_TO_DEST_SCALE * lastHitBearing / Math.PI;
        final double distToSafestScaled = Utils.scale(distToSafest, bfWidth, DIST_TO_MIN_RISK_SCALE);
        final double distToMostRiskScaled = Utils.scale(distToMostRisk, bfWidth, DIST_TO_MAX_RISK_SCALE);
        return Utils.distance(idealPoint, new double[]{distToOthers, bearingScaled, distFromCenter, distToDest, bearingToLastHit,
                distToSafestScaled, distToMostRiskScaled});
    }

}
