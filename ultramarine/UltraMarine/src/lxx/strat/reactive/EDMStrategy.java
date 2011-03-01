package lxx.strat.reactive;

import lxx.utils.LXXPoint;
import lxx.BasicRobot;
import lxx.RobotListener;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import static lxx.utils.Utils.angle;
import lxx.targeting.TargetManager;
import lxx.targeting.Target;
import lxx.targeting.TargetChooser;
import lxx.movement.edm.EnemyDodgingMovement;
import lxx.movement.Strategy;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import static java.lang.Math.abs;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class EDMStrategy implements Strategy {

    private final EnemyDodgingMovement enemyDodgingStrategy;
    private final TargetManager targetManager;
    private BasicRobot robot;

    public EDMStrategy(BasicRobot robot, TargetManager targetManager) {
        this.targetManager = targetManager;
        this.robot = robot;
        enemyDodgingStrategy = new EnemyDodgingMovement(robot);
    }

    public boolean match() {
        double t = targetManager.getClosestTargetDistance();
        return t < 140 &&
                abs(robot.getHeadingRadians() - angle(robot, targetManager.getClosestTarget())) < LXXConstants.RADIANS_45;
    }

    public LXXPoint getDestination(boolean newSession) {
        List<APoint> pts = getEnemies();
        LXXPoint point = enemyDodgingStrategy.getDestination(pts);
        LXXPoint dest = robot.getDestination();
        if (dest == null) {
            dest = new LXXPoint();
        }
        point.x = (point.x + dest.x) / 2;
        point.y = (point.y + dest.y) / 2;
        return point;
    }

    private List<APoint> getEnemies() {
        List<APoint> pts = new ArrayList<APoint>();
        for (Target t : targetManager.getAliveTargets()) {
            pts.add(t);
        }
        return pts;
    }

    public void paint(Graphics2D g) {
        enemyDodgingStrategy.paint(g, getEnemies());
    }

    public TargetChooser getTargetChooser() {
        return null;
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
    }

    public void onHitByBullet(HitByBulletEvent e) {
    }
}
