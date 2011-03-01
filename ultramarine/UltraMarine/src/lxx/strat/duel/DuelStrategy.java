package lxx.strat.duel;

import lxx.BasicRobot;
import lxx.movement.Strategy;
import lxx.targeting.Target;
import lxx.targeting.TargetChooser;
import lxx.targeting.TargetManager;
import lxx.utils.LXXConstants;
import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.wave.Wave;
import robocode.Rules;

import java.awt.*;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * User: jdev
 * Date: 10.11.2009
 */
public class DuelStrategy implements Strategy {

    private final TargetManager targetManager;
    private final BasicRobot robot;
    private final FireAngleMonitor fireAngleMonitor;

    public DuelStrategy(TargetManager targetManager, BasicRobot robot, FireAngleMonitor fireAngleMonitor) {
        this.targetManager = targetManager;
        this.robot = robot;
        this.fireAngleMonitor = fireAngleMonitor;
    }

    public boolean match() {
        return robot.getOthers() == 1;
    }

    public LXXPoint getDestination(boolean newSession) {
        /*Wave closest = fireAngleMonitor.getClosestWave();
        if (closest == null) {
            return null;
        }
        Map<Double, Double> risks = fireAngleMonitor.getRisks(closest);

        double safestAngle = 0;
        double minRisk = Double.MAX_VALUE;

        final APoint pos = closest.getSourcePos();
        final double dist = max(400, closest.sourcePos.aDistance(robot));

        final Rectangle activityArea = new Rectangle(30, 30, robot.battleField.width - 60, robot.battleField.height - 60);

        for (Map.Entry<Double, Double> e: risks.entrySet()) {
            final double angle = toRadians(e.getKey());
            if (activityArea.contains(new LXXPoint(pos.getX() + sin(closest.heading + angle) * dist,
                pos.getY() + cos(closest.heading + angle) * dist)) &&
                    e.getValue() < minRisk) {
                minRisk = e.getValue();
                safestAngle = angle;
            }
        }*/

        Wave closest = fireAngleMonitor.getClosestWave();
        if (closest == null) {
            return null;
        }
        Map<Double, Double> risks = fireAngleMonitor.getRisks(closest);
        List<Map.Entry<Double, Double>> entries = new ArrayList<Map.Entry<Double, Double>>();

        for (Map.Entry<Double, Double> e : risks.entrySet()) {
            entries.add(e);
        }

        Collections.sort(entries, new Comparator<Map.Entry<Double, Double>>() {

            public int compare(Map.Entry<Double, Double> o1, Map.Entry<Double, Double> o2) {
                return (int)(o2.getValue() - o1.getValue());
            }
        });

        double angle = Utils.angle(closest.sourcePos, robot);
        LXXPoint center = new LXXPoint(robot.battleField.width / 2, robot.battleField.height / 2);
        LXXPoint res = null;
        int dist = 400;
        do {
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<Double, Double> e = entries.get(i);
                if (e.getValue() < 0.5) {
                    break;
                }

                if (closest.heading + toRadians(e.getKey()) - LXXConstants.RADIANS_20 < angle &&
                        closest.heading + toRadians(e.getKey()) + LXXConstants.RADIANS_20 > angle) {
                    LXXPoint res1 = new LXXPoint(closest.sourcePos.getX() + sin(closest.heading + toRadians(e.getKey()) + LXXConstants.RADIANS_20) * dist,
                            closest.sourcePos.getY() + cos(closest.heading + toRadians(e.getKey()) + LXXConstants.RADIANS_20) * dist);
                    LXXPoint res2 = new LXXPoint(closest.sourcePos.getX() + sin(closest.heading + toRadians(e.getKey()) - LXXConstants.RADIANS_20) * dist,
                            closest.sourcePos.getY() + cos(closest.heading + toRadians(e.getKey()) - LXXConstants.RADIANS_20) * dist);
                    if (center.aDistance(res1) < center.aDistance(res2) && robot.getBattleField().contains(res1.getX(), res1.getY())) {
                        res = res1;
                    } else if (robot.getBattleField().contains(res1.getX(), res1.getY())) {
                        res = res2;
                    }
                }
            }
            dist -= 50;
        } while ((res == null || !robot.getBattleField().contains(res.getX(), res.getY())) && dist > 0);

        return res;
    }

    public void paint(Graphics2D g) {

        /*Wave closest = fireAngleMonitor.getClosestWave();
        if (closest == null) {
            return;
        }
        Map<Double, Double> risks = fireAngleMonitor.getRisks(closest);
        List<Map.Entry<Double, Double>> entries = new ArrayList<Map.Entry<Double, Double>>();

        for (Map.Entry<Double, Double> e : risks.entrySet()) {
            entries.add(e);
        }

        Collections.sort(entries, new Comparator<Map.Entry<Double, Double>>() {

            public int compare(Map.Entry<Double, Double> o1, Map.Entry<Double, Double> o2) {
                return (int) (o2.getValue() - o1.getValue());
            }
        });
        g.setColor(Color.RED);
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Double, Double> e = entries.get(i);
            if (e.getValue() < 0.5) {
                break;
            }

            if (e.getValue() > 0) {
                g.fillOval((int) (closest.sourcePos.getX() + sin(closest.heading + toRadians(e.getKey())) * closest.getTraveledDistance() - 3),
                        (int) (closest.sourcePos.getY() + cos(closest.heading + toRadians(e.getKey())) * closest.getTraveledDistance() - 3),
                        6, 6);
            }
        }*/

        /*g.setColor(Color.BLUE);
        for (int i = 0; i < 3; i++) {
            Map.Entry<Double, Double> e = entries.get(entries.size() - 1 - i);

            g.fillOval((int) (closest.sourcePos.getX() + sin(closest.heading + toRadians(e.getKey())) * closest.getTraveledDistance() - 3),
                    (int) (closest.sourcePos.getY() + cos(closest.heading + toRadians(e.getKey())) * closest.getTraveledDistance() - 3),
                    6, 6);
        }*/
    }

    public TargetChooser getTargetChooser() {
        return new DuelTargetChooser();
    }

    private class DuelTargetChooser implements TargetChooser {


        public Target getBestTarget() {
            return targetManager.getClosestTarget();
        }

        public double firePower() {
            final Target bestTarget = getBestTarget();
            if (bestTarget == null) {
                return 0;
            }
            double bulletPower = 3;
            if (Rules.getBulletDamage(bulletPower) > bestTarget.getEnergy()) {
                bulletPower = bestTarget.getEnergy() / 4;
            }

            if (robot.getEnergy() * 3 < bestTarget.getEnergy() || robot.getEnergy() < 5) {
                bulletPower = 0.1;
            }

            return bulletPower;
        }

    }

}
