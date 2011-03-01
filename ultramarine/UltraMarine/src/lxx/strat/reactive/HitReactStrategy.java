package lxx.strat.reactive;

import lxx.utils.LXXPoint;
import lxx.BasicRobot;
import lxx.RobotListener;
import lxx.utils.LXXConstants;
import lxx.targeting.TargetChooser;
import lxx.movement.Strategy;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class HitReactStrategy implements Strategy, RobotListener {

    private final List<Hit> hits = new ArrayList<Hit>();
    private final BasicRobot robot;

    public HitReactStrategy(BasicRobot robot) {
        this.robot = robot;
        robot.addListener(this);
    }

    public boolean match() {
        for (Iterator<Hit> iter = hits.iterator(); iter.hasNext();) {
            Hit h = iter.next();
            if (robot.getTime() - h.time > 20) {
                iter.remove();
            }
        }
        double avgBearing = 0;
        for (Hit h : hits) {
            avgBearing += h.heading;
        }
        avgBearing /= hits.size();

        // todo (zhidkov): fix name
        double maxHz = 0;
        for (Hit h : hits) {
            if (abs(h.heading - avgBearing) > maxHz) {
                maxHz = abs(h.heading - avgBearing);
            }
        }
        return abs(avgBearing - maxHz) < LXXConstants.RADIANS_45 && hits.size() > 1;
    }

    public LXXPoint getDestination(boolean newSession) {
        double angle = robot.getHeadingRadians() + Math.PI / 2;
        LXXPoint res1 = new LXXPoint(robot.getX(), robot.getY());
        res1.x += sin(angle) * 85;
        res1.y += cos(angle) * 85;
        double d1 = res1.distance(robot.getBattleFieldWidth() / 2, robot.getBattleFieldHeight() / 2);

        angle = robot.getHeadingRadians() - Math.PI / 2;
        LXXPoint res2 = new LXXPoint(robot.getX(), robot.getY());
        res2.x += sin(angle) * 85;
        res2.y += cos(angle) * 85;
        double d2 = res2.distance(robot.getBattleFieldWidth() / 2, robot.getBattleFieldHeight() / 2);

        return d1 < d2 ? res1 : res2;
    }

    public void paint(Graphics2D g) {
    }

    public TargetChooser getTargetChooser() {
        return null;
    }

    public void onHitByBullet(HitByBulletEvent e) {
        Hit h = new Hit();
        h.heading = e.getHeadingRadians();
        h.time = e.getTime();
        hits.add(h);
    }

    public void onBulletHit(BulletHitEvent event) {
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
    }

    public void onBulletMissed(BulletMissedEvent event) {        
    }

    public void tick() {        
    }

    private class Hit {
        public double heading;
        public long time;
    }

}
