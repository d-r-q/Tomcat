/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting;

import lxx.BasicRobot;
import lxx.RobotListener;
import lxx.events.LXXPaintEvent;
import lxx.events.TickEvent;
import lxx.paint.LXXGraphics;
import lxx.utils.LXXPoint;
import robocode.*;
import robocode.Event;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: jdev
 * Date: 25.07.2009
 */

@SuppressWarnings({"UnusedDeclaration"})
public class TargetManager implements RobotListener {

    private static final Map<String, TargetData> targetDatas = new HashMap<String, TargetData>();

    private final Map<String, Target> targets = new HashMap<String, Target>();

    private final List<Target> aliveTargets = new ArrayList<Target>();
    private final List<TargetManagerListener> listeners = new LinkedList<TargetManagerListener>();
    private final Set<Target> updatedTargets = new HashSet<Target>();

    private final BasicRobot robot;

    private boolean isAliveTargetsDirty = true;

    public TargetManager(BasicRobot robot) {
        this.robot = robot;
    }

    public void updateTarget(Event e, String name) {
        Target t = getTarget(name);
        updatedTargets.add(t);
        t.addEvent(e);
        isAliveTargetsDirty = true;
    }

    public void onTargetKilled(String targetName) {
        if (targets.get(targetName).isAlive()) {
            targets.get(targetName).setNotAlive();
            isAliveTargetsDirty = true;
        }
    }

    public void onPaint(LXXGraphics g) {
        for (Target t : getAliveTargets()) {
            g.setColor(Color.WHITE);
            final LXXPoint coords = t.getState().getPosition();
            g.drawSquare(coords, robot.getWidth());
            g.drawRect((int) coords.x - (int) robot.getWidth() / 2,
                    (int) coords.y - (int) robot.getHeight() / 2,
                    (int) robot.getWidth(), (int) robot.getHeight());
        }
    }

    public void onTick() {
        for (Target updatedTarget : updatedTargets) {
            updatedTarget.update();
            notifyListeners(updatedTarget);
        }
        updatedTargets.clear();
    }

    public Target getTarget(String name) {
        Target t;
        if ((t = targets.get(name)) == null) {
            TargetData td = targetDatas.get(name);
            if (td == null) {
                td = new TargetData();
                targetDatas.put(name, td);
            }
            t = new Target(robot, name, td);
            targets.put(name, t);
        }
        return t;
    }

    // todo(zhidkov): will be used in melee strategies
    public Target getLastUpdatedTarget() {
        Target res = null;

        for (Target t : getAliveTargets()) {
            if (res == null || (t.getUpdateTime() < res.getUpdateTime())) {
                res = t;
            }
        }

        return res;
    }

    public int getAliveTargetCount() {
        return getAliveTargets().size();
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

    // todo(zhidkov): will be used in melee strategies
    public Target getClosestTarget() {
        Target res = null;
        double minDist = Double.MAX_VALUE;

        for (Target t : getAliveTargets()) {
            final double dist = robot.aDistance(t.getState().getPosition());
            if (dist < minDist) {
                res = t;
                minDist = dist;
            }
        }

        return res;
    }

    public boolean hasDuelOpponent() {
        return getAliveTargetCount() == 1;
    }

    private void notifyListeners(Target target) {
        for (TargetManagerListener listener : listeners) {
            listener.targetUpdated(target);
        }
    }

    public void addListener(TargetManagerListener listener) {
        listeners.add(listener);
    }

    public void onEvent(robocode.Event event) {
        if (event instanceof TickEvent) {
            onTick();
        } else if (event instanceof LXXPaintEvent) {
            onPaint(((LXXPaintEvent) event).getGraphics());
        } else if (event instanceof RobotDeathEvent) {
            onTargetKilled(((RobotDeathEvent) event).getName());
            updateTarget(event, ((RobotDeathEvent) event).getName());
        } else if (event instanceof HitByBulletEvent) {
            updateTarget(event, ((HitByBulletEvent) event).getName());
        } else if (event instanceof BulletHitEvent) {
            updateTarget(event, ((BulletHitEvent) event).getName());
        } else if (event instanceof HitRobotEvent) {
            updateTarget(event, ((HitRobotEvent) event).getName());
        } else if (event instanceof ScannedRobotEvent) {
            updateTarget(event, ((ScannedRobotEvent) event).getName());
        }
    }

    // todo(zhidkov): will be used in melee strategies
    public int getTargetsCount() {
        return targets.size();
    }

    public Target getDuelOpponent() {
        if (aliveTargets.size() == 0) {
            return null;
        }
        return aliveTargets.get(0);
    }

    public Target getAnyDuelOpponent() {
        return targets.values().iterator().next();
    }

    public String getDuelOpponentName() {
        return targets.keySet().iterator().next();
    }

    public boolean isNoAliveEnemies() {
        return aliveTargets.size() == 0;
    }
}
