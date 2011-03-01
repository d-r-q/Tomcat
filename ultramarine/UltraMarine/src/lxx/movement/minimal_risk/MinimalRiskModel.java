package lxx.movement.minimal_risk;

import lxx.utils.LXXPoint;
import static lxx.StaticData.robot;
import lxx.utils.Utils;
import lxx.targeting.Target;
import lxx.targeting.TargetManager;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.lang.Math.abs;

public class MinimalRiskModel {

    private final double bfWidth;
    private final double bfHeight;
    private final TargetManager targetManager;
    
    private List<LXXPoint> points = new LinkedList<LXXPoint>();
    private Map<LXXPoint, Double> risks = new HashMap<LXXPoint, Double>();

    private LXXPoint mostSafePoint;
    private LXXPoint mostRiskPoint;
    private double maxRisk;
    private double minRisk;

    public MinimalRiskModel(double bfWidth, double bfHeight, TargetManager targetManager) {
        this.bfWidth = bfWidth;
        this.bfHeight = bfHeight;
        this.targetManager = targetManager;
    }

    public LXXPoint getSafestPoint() {
        return mostSafePoint;
    }

    public LXXPoint getMostRiskPoint() {
        return mostRiskPoint;
    }

    public void recalculate() {

        points = new LinkedList<LXXPoint>();
        risks = new HashMap<LXXPoint, Double>();
        minRisk = Double.MAX_VALUE;
        maxRisk = Double.MIN_VALUE;

        final double robotX = robot.getX();
        final double robotY = robot.getY();

        List<Target> enemies = targetManager.getAliveTargets();

        final double idealDistToEnemies = Utils.scale(bfWidth * 1.41, bfWidth * 1.41, 15);
        final double idealBbearingToEnemies = Utils.scale(Math.PI / 2, Math.PI, 7);
        final double idealTravelDistance = Utils.scale(150, bfWidth * 1.41, 4);

        final double[] idealPoint = {idealDistToEnemies, idealBbearingToEnemies, idealTravelDistance};

        final double xStep = (bfWidth - 80) / 10;
        final double yStep = (bfWidth - 80) / 10;
        for (int i = 40; i <= bfWidth - 40; i += xStep) {
            for (int j = 40; j <= bfHeight - 40; j += yStep) {
                LXXPoint point = new LXXPoint();
                point.x = i + Math.random() * 20 - 10;
                point.y = j + Math.random() * 20 - 10;                

                double distToEnemiesCur = 0;
                double bearingToEnemiesCur = 0;

                final double angleToPoint = robot.angle(robotX, robotY, point.x, point.y);

                int tCount = 0;
                for (Target t : enemies) {
                    final LXXPoint pos = t.getPosition();
                    double distToTarget = point.aDistance(t);
                    final double angleToEnemy = Utils.angle(robotX, robotY, pos.x, pos.y);
                    double bearingAbs = abs(Utils.normalizeBearing(angleToPoint - angleToEnemy));
                    if (distToTarget < 240) {
                        distToEnemiesCur += distToTarget / t.getEnergy();
                        bearingToEnemiesCur += bearingAbs;
                    } else {
                        distToEnemiesCur += bfWidth * 1.41;
                    }
                    tCount++;
                }
                distToEnemiesCur = Utils.scale(distToEnemiesCur / tCount, bfWidth * 1.41, 15);
                bearingToEnemiesCur /= tCount;
                bearingToEnemiesCur = Utils.scale(bearingToEnemiesCur, Math.PI, 0);
                final double[] p = {distToEnemiesCur, bearingToEnemiesCur, Utils.scale(robot.distance(point), bfWidth * 1.41, 4)};

                double risk = Utils.distance(idealPoint, p);

                points.add(point);
                risks.put(point, risk);
                if (risk < minRisk || (risk == minRisk && Math.random() > 0.5)) {
                    mostSafePoint = point;
                    minRisk = risk;
                } else if (risk >= maxRisk) {
                    mostRiskPoint = point;
                    maxRisk = risk;
                }
            }
        }
    }

    public void paint(Graphics2D g) {
        for (LXXPoint p : points) {
            double risk = risks.get(p);

            int radius = 6;
            int gb = (int) (255 * (1D - (risk - minRisk) / (maxRisk - minRisk)));
            if (gb < 0) {
                gb = 0;
            } else if (gb > 255) {
                gb = 255;
            }
            g.setColor(new Color(255, gb, gb));
            g.fillOval((int) Math.round(p.x - radius / 2), (int) Math.round(p.y - radius / 2), radius, radius);
            if (risk <= minRisk) {
                radius = 12;
                g.drawOval((int) Math.round(p.x - radius / 2), (int) Math.round(p.y - radius / 2), radius, radius);
            }
        }
    }

}
