package lxx.targeting;

import robocode.*;
import robocode.Event;
import static lxx.StaticData.robot;
import lxx.utils.LXXPoint;
import lxx.utils.APoint;

import java.awt.*;
import java.util.*;
import java.util.List;
import static java.lang.Math.cos;
import static java.lang.Math.sin;import static java.lang.Math.abs;

/**
 * User: jdev
 * Date: 25.07.2009
 */

public class TargetManager {
    
    private final Map<String, Target> targets = new HashMap<String, Target>();

    private final List<Target> aliveTargets = new ArrayList<Target>();
    private boolean isAliveTargetsDirty = true;

    private final List<TargetManagerListener> listeners = new ArrayList<TargetManagerListener>();

    public void updateTarget(ScannedRobotEvent e) {
        Target t = getTarget(e.getName());
        Target oldState = new Target(t);
        t.update(e);
        isAliveTargetsDirty = true;

        notifyListeners(t, oldState, e);
    }

    private void notifyListeners(Target t, Target oldState, Event source) {
        for (TargetManagerListener listener : listeners) {
            listener.targetUpdated(oldState, t, source);
        }
    }


    public void updateTarget(HitRobotEvent e) {
        Target t = getTarget(e.getName());
        Target oldState = new Target(t);
        t.update(e);
        isAliveTargetsDirty = true;

        notifyListeners(t, oldState, e);
    }

    public void updateTarget(BulletHitEvent e) {
        Target t = getTarget(e.getName());
        Target oldState = new Target(t);
        t.update(e);
        isAliveTargetsDirty = true;

        notifyListeners(t, oldState, e);
    }

    public void updateTarget(HitByBulletEvent e) {
        Target t = getTarget(e.getName());
        Target oldState = new Target(t);
        t.update(e);
        isAliveTargetsDirty = true;

        notifyListeners(t, oldState, e);
    }

    public Target getTarget(String name) {
        Target t;
        if ((t = targets.get(name)) == null) {
            t = new Target(robot, name);
            targets.put(name, t);
        }
        return t;
    }

    public Target getLastUpdatedTarget() {
        Target res = null;

        for (Target t : getAliveTargets()) {
            if (res == null || (t.getUpdateTime() < res.getUpdateTime())) {
                res = t;
            }
        }

        return res;
    }

    public List<Target> getAllTargets() {
        return new ArrayList<Target>(targets.values());
    }

    public int getAliveTargetCount() {
        return getAliveTargets().size();
    }

    public void onTargetKilled(String targetName) {
        targets.get(targetName).setAlive(false);
        isAliveTargetsDirty = true;
    }

    public void endRound() {
        for (Target t : targets.values()) {
            t.setAlive(false);
            t.endRaund();
        }
        isAliveTargetsDirty = true;
    }

    public List<Target> getAliveTargets() {
        if (isAliveTargetsDirty) {
            aliveTargets.clear();
            for (Target t : targets.values()) {
                if (t.isAlive()) {
                    aliveTargets.add(t);
                }
            }
        }
        isAliveTargetsDirty = false;
        return aliveTargets;
    }

    public void paint(Graphics2D g) {
        for (Target t : getAliveTargets()) {
            g.setColor(Color.WHITE);
            final LXXPoint coords = t.getPosition();
            g.drawRect((int)coords.x - (int) robot.getWidth() / 2,
                    (int)coords.y - (int) robot.getHeight() / 2,
                    (int) robot.getWidth(), (int) robot.getHeight());
            g.drawLine((int)coords.x, (int)coords.y,
                    (int)(coords.x + sin(t.getHeading()) * (robot.getWidth() / 2 + abs(t.getVelocity() * 2))),
                    (int)(coords.y + cos(t.getHeading()) * (robot.getWidth() / 2 + abs(t.getVelocity() * 2))));
            g.fillOval((int)(coords.x + sin(t.getHeading()) * (robot.getWidth() / 2 + abs(t.getVelocity() * 2))) - 5,
                    (int)(coords.y + cos(t.getHeading()) * (robot.getWidth() / 2 + abs(t.getVelocity() * 2))) - 5,
                    10, 10);
        }
    }

    public Target getClosestTarget() {
        Target res = null;
        double minDist = Double.MAX_VALUE;

        for (Target t : getAliveTargets()) {
            final double dist = robot.distance(t.getPosition());
            if (dist < minDist) {
                res = t;
                minDist = dist;
            }
        }

        return res;
    }

    public void addListener(TargetManagerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TargetManagerListener listener) {
        listeners.remove(listener);
    }

    public int getTargetCountIn(Rectangle corner) {
        int res = 0;

        for (Target t : getAliveTargets()) {
            if (corner.contains(t.getPosition())) {
                res++;
            }
        }

        return res;
    }

    public double getClosestTargetDistance() {
        Target target = getClosestTarget();
        if (target == null) {
            return Double.MAX_VALUE;
        }
        return target.getPosition().distance(robot.getPosition());
    }

    public Collection<Target> getTargetsIn(Rectangle corner) {
        List<Target> res = new ArrayList<Target>();

        for (Target t : getAliveTargets()) {
            if (corner.contains(t.getPosition())) {
                res.add(t);
            }
        }

        return res;
    }

    public Target getClosestTergetTo(APoint p) {
        double minDistance = Double.MAX_VALUE;
        Target res = null;

        for (Target t : getAliveTargets()) {
            if (t.aDistance(p) < minDistance) {
                res = t;
                minDistance = t.aDistance(p);
            }
        }

        return res;
    }

    public Target getClosestTergetToT(Target target) {
        Target res = null;
        double minDist = Double.MAX_VALUE;
        for (Target t : getAliveTargets()) {
            if (t.equals(target)) {
                continue;
            }

            if (target.aDistance(t) < minDist) {
                minDist = target.aDistance(t);
                res = t;
            }
        }

        return res;
    }

    public Map<String, Target> getTargets() {
        return targets;
    }
}
