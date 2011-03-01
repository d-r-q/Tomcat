package lxx.strat.duel;

import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.TargetManager;
import lxx.utils.Utils;
import lxx.utils.LXXRobot;
import lxx.utils.LXXPoint;
import lxx.utils.kd_tree.KDTree;
import lxx.utils.kd_tree.KDNode;
import lxx.wave.Wave;
import lxx.wave.WaveManager;
import lxx.wave.WaveCallback;
import lxx.RobotListener;
import lxx.BasicRobot;
import robocode.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import static java.lang.StrictMath.round;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.toRadians;
import java.awt.*;

/**
 * User: jdev
 * Date: 09.01.2010
 */
public class FireAngleMonitor implements WaveCallback, RobotListener, TargetManagerListener {

    private final WaveManager waveManager;
    private final BasicRobot robot;
    private final TargetManager targetManager;

    private final Map<String, Set<Wave>> waves = new HashMap<String, Set<Wave>>();
    private static KDTree<FACData, Object[]> guessFactors = new KDTree<FACData, Object[]>(new FACKeyExtractor(), new FACData());

    private boolean hitInThisTick = false;

    public FireAngleMonitor(WaveManager waveManager, BasicRobot robot, TargetManager targetManager) {
        this.waveManager = waveManager;
        this.robot = robot;
        this.targetManager = targetManager;
    }

    public void targetUpdated(Target oldState, Target newState, robocode.Event source) {
        final Target closest = targetManager.getClosestTergetToT(newState);
        if ((closest == null || robot.aDistance(newState) < closest.aDistance(newState))
                && newState.getEnergyDelta() < -0.09 && newState.getEnergyDelta() > -3.1) {
            Set ws = waves.get(newState.getName());
            if (ws == null) {
                ws = new HashSet();
                waves.put(newState.getName(), ws);
            }
            ws.add(waveManager.launchWave(oldState, robot, robot.getTime(), Utils.angle(oldState, robot),
                    Rules.getBulletSpeed(-newState.getEnergyDelta()), this));
        }
    }

    public void waveBroken(Wave w) {
        guessFactors.addStat(
                new Object[]{w.getSource().getName(),
                        robot.getCornerManager().isInCorner(new LXXPoint(w.getTargetPos().getX(),
                                w.getTargetPos().getY())), w.targetVelocity,
                        w.targetVelocity * Math.sin(w.targetHeading - Utils.angle(w.sourcePos, w.targetPos))},
                w, hitInThisTick);

        waves.get(w.getSource().getName()).remove(w);
        hitInThisTick = false;
    }

    public Map<Double, Double> getRisks(Wave w) {
        KDNode<FACData, Object[]> n = guessFactors.getNode(
                new Object[]{w.getSource().getName(),
                        robot.getCornerManager().isInCorner(new LXXPoint(w.getTargetPos().getX(),
                                w.getTargetPos().getY())), w.targetVelocity,
                        w.targetVelocity * Math.sin(w.targetHeading - Utils.angle(w.sourcePos, w.targetPos))}
        );
        return n.getData().getAngleRisks();
    }

    public void paint(Graphics2D g) {
        Wave w = getClosestWave();
        if (w == null) {
            return;
        }
        KDNode<FACData, Object[]> n = guessFactors.getNode(
                new Object[]{w.getSource().getName(),
                        robot.getCornerManager().isInCorner(new LXXPoint(w.getTargetPos().getX(),
                                w.getTargetPos().getY())), w.targetVelocity,
                        w.targetVelocity * Math.sin(w.targetHeading - Utils.angle(w.sourcePos, w.targetPos))}
        );
        Map<Double, Double> angleRisks = n.getData().getAngleRisks();
        double maxRisk = 0;
        double minRisk = Double.MAX_VALUE;
        for (Double angle : angleRisks.keySet()) {
            if (angleRisks.get(angle) > maxRisk) {
                maxRisk = angleRisks.get(angle);
            }
        }
        for (Double angle : angleRisks.keySet()) {
            Point p = new Point((int) w.sourcePos.getX() + (int) (sin(toRadians(angle * 2) + w.heading) * w.getTraveledDistance()),
                    (int) w.sourcePos.getY() + (int) (cos(toRadians(angle * 2) + w.heading) * w.getTraveledDistance()));
            Double risk = angleRisks.get(angle);
            if (risk < 0) {
                risk = 0D;
            }
            try {
                g.setColor(new Color((int) (255 - 255 * (maxRisk - risk) / maxRisk), 0,
                        (int) (255 * (maxRisk - risk) / maxRisk)));
            } catch (IllegalArgumentException e) {
                System.out.println((int) (255 - 255 * (maxRisk - risk) / maxRisk) + ", " +
                        (int) (255 * (maxRisk - risk) / maxRisk));
            }

            g.fillOval(p.x - 2, p.y - 2, 4, 4);
        }

    }

    public Wave getClosestWave() {
        Wave res = null;
        double minTime = Double.MAX_VALUE;
        for (Set<Wave> ws : waves.values()) {
            Set<Wave> toRemove = new HashSet<Wave>();
            for (Wave w : ws) {
                double dist = w.sourcePos.aDistance(robot) - w.getTraveledDistance();
                if (dist < 0) {
                    toRemove.add(w);
                }
                if (dist / w.speed < minTime) {
                    minTime = dist / w.speed;
                    res = w;
                }
            }

            ws.removeAll(toRemove);
        }

        return res;
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
    }

    public void onHitByBullet(HitByBulletEvent e) {
        hitInThisTick = true;
    }

    public void onBulletHit(BulletHitEvent event) {
    }

    public void onBulletMissed(BulletMissedEvent event) {
    }

    public void tick() {
    }
}
