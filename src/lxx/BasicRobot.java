/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx;

import lxx.bullets.BulletSnapshot;
import lxx.events.LXXKeyEvent;
import lxx.paint.LXXGraphics;
import lxx.utils.*;
import robocode.*;
import robocode.util.Utils;

import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 24.10.2009
 */
public abstract class BasicRobot extends TeamRobot implements APoint, LXXRobot {

    static {
        QuickMath.init();
    }

    private final Set<RobotListener> listeners = new LinkedHashSet<RobotListener>();
    private final LXXPoint position = new LXXPoint();
    private final LinkedList<LXXPoint> last10Positions = new LinkedList<LXXPoint>();

    private int initialOthers;
    public BattleField battleField;

    private MySnapshot prevSnapshot;
    private MySnapshot currentSnapshot;
    private MySnapshot correctSnapshot;

    private int lastDirection = 1;

    protected void init() {
        initialOthers = getOthers();
        battleField = new BattleField(LXXConstants.ROBOT_SIDE_HALF_SIZE, LXXConstants.ROBOT_SIDE_HALF_SIZE,
                (int) getBattleFieldWidth() - LXXConstants.ROBOT_SIDE_SIZE, (int) getBattleFieldHeight() - LXXConstants.ROBOT_SIDE_SIZE);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
    }

    public double angleTo(APoint point) {
        return LXXUtils.angle(position, point);
    }

    public APoint project(double alpha, double distance) {
        return position.project(alpha, distance);
    }

    public APoint project(DeltaVector dv) {
        return position.project(dv);
    }

    public double aDistance(APoint p) {
        return position.aDistance(p);
    }

    public void onBulletHitBullet(BulletHitBulletEvent event) {
        notifyListeners(event);
    }

    public void onHitByBullet(HitByBulletEvent event) {
        notifyListeners(event);
    }

    public void onBattleEnded(BattleEndedEvent event) {
        notifyListeners(event);
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        notifyListeners(event);
    }

    public void onRobotDeath(RobotDeathEvent event) {
        notifyListeners(event);
    }

    public void onHitRobot(HitRobotEvent event) {
        notifyListeners(event);
    }

    public void onBulletHit(BulletHitEvent event) {
        notifyListeners(event);
    }

    public void onBulletMissed(BulletMissedEvent event) {
        notifyListeners(event);
    }

    public void onWin(WinEvent event) {
        notifyListeners(event);
    }

    public void onSkippedTurn(SkippedTurnEvent event) {
        notifyListeners(event);
    }

    public void onKeyTyped(KeyEvent e) {
        notifyListeners(new LXXKeyEvent(e.getKeyChar()));
    }

    public void addListener(RobotListener listener) {
        listeners.add(listener);
    }

    protected void notifyListeners(Event event) {
        for (RobotListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void onHitWall(HitWallEvent event) {
        notifyListeners(event);
    }

    public double getAbsoluteHeadingRadians() {
        if (signum(getVelocity()) == 1) {
            return getHeadingRadians();
        } else if (signum(getVelocity()) == -1) {
            return Utils.normalAbsoluteAngle(getHeadingRadians() + Math.PI);
        } else if (lastDirection == 1) {
            return getHeadingRadians();
        } else {
            return Utils.normalAbsoluteAngle(getHeadingRadians() + Math.PI);
        }
    }

    public double getSpeed() {
        return abs(getVelocity());
    }

    public boolean isAlive() {
        return true;
    }

    public int getInitialOthers() {
        return initialOthers;
    }

    public boolean isDuel() {
        return initialOthers == 1;
    }

    public LXXGraphics getLXXGraphics() {
        return new LXXGraphics(getGraphics());
    }

    public void onStatus(StatusEvent e) {

        prevSnapshot = currentSnapshot != null
                ? currentSnapshot
                : new MySnapshot(this);

        last10Positions.add(new LXXPoint(e.getStatus().getX(), e.getStatus().getY()));
        if (last10Positions.size() > 10) {
            last10Positions.removeFirst();
        }
        
        currentSnapshot = new MySnapshot(prevSnapshot, this, last10Positions.getFirst().aDistance(last10Positions.getLast()));

        // performance enhancing bug - because some reason using old position gives best results
        position.x = e.getStatus().getX();
        position.y = e.getStatus().getY();

        correctSnapshot = new MySnapshot(prevSnapshot, this, last10Positions.getFirst().aDistance(last10Positions.getLast()));

        if (abs(e.getStatus().getVelocity()) >= 0.1) {
            lastDirection = (int) signum(e.getStatus().getVelocity());
        }

        notifyListeners(e);
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public LXXPoint getPosition() {
        return position;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicRobot basicRobot = (BasicRobot) o;

        return getName().equals(basicRobot.getName());
    }

    public MySnapshot getPrevSnapshot() {
        return prevSnapshot;
    }

    public MySnapshot getCurrentSnapshot() {
        return currentSnapshot;
    }

    public MySnapshot getCorrectSnapshot() {
        return correctSnapshot;
    }

    public int getRound() {
        return getRoundNum();
    }

    public BattleField getBattleField() {
        return battleField;
    }

    public abstract List<BulletSnapshot> getBulletsInAir();
}