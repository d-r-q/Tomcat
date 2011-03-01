package lxx.wave;

import lxx.utils.LXXRobot;
import lxx.utils.LXXPoint;
import lxx.RobotListener;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.abs;

import robocode.*;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class WaveManager implements RobotListener {

    private Map<String, List<Wave>> waves = new HashMap<String, List<Wave>>();

    public Wave launchWave(LXXRobot source, LXXRobot target, long time, double heading, double speed, WaveCallback callback) {
        Wave w = new Wave(callback, source, target, time, speed, heading);
        addWave(source.getName(), w);

        return w;
    }

    private void addWave(String owener, Wave w) {
        List<Wave> waves = getWaves(owener);
        waves.add(w);
    }

    private List<Wave> getWaves(String owener) {
        List<Wave> waves = this.waves.get(owener);
        if (waves == null) {
            waves = new ArrayList<Wave>();
            this.waves.put(owener, waves);
        }
        return waves;
    }

    private void removeWave(String owener, LXXPoint pos) {
        List<Wave> waves = getWaves(owener);
        double minDist = Double.MAX_VALUE;
        Wave res = null;
        for (Wave w : waves) {
            if (w.distance(pos) < minDist && abs(w.distance(pos)) < 20) {
                minDist = w.distance(pos);
                res = w;
            }
        }

        waves.remove(res);
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        Bullet bullet = e.getBullet();
        removeWave(bullet.getName(), new LXXPoint(bullet.getX(), bullet.getY()));
        bullet = e.getHitBullet();
        removeWave(bullet.getName(), new LXXPoint(bullet.getX(), bullet.getY()));
    }

    public void onHitByBullet(HitByBulletEvent e) {
        Bullet bullet = e.getBullet();
        removeWave(bullet.getVictim(), new LXXPoint(bullet.getX(), bullet.getY()));
    }

    public void onBulletHit(BulletHitEvent e) {
        Bullet bullet = e.getBullet();
        removeWave(bullet.getName(), new LXXPoint(bullet.getX(), bullet.getY()));
    }

    public void onBulletMissed(BulletMissedEvent e) {
        Bullet bullet = e.getBullet();
        removeWave(bullet.getName(), new LXXPoint(bullet.getX(), bullet.getY()));
    }

    public void tick() {
        for (List<Wave> ws : waves.values()) {
            List<Wave> toRemove = new ArrayList<Wave>();
            for (Wave w : ws) {
                if (w.check()) {
                    toRemove.add(w);
                    w.getCallback().waveBroken(w);
                }
            }
            ws.removeAll(toRemove);
        }
    }

    public void paint(Graphics2D g) {

        /*g.setColor(Color.DARK_GRAY);
        for (List<Wave> ws : waves.values()) {
            for (Wave w : ws) {
                g.fillOval((int) w.sourcePos.getX() - 3, (int) w.sourcePos.getY() - 3, 6, 6);

                g.drawOval((int) w.sourcePos.getX() - (int) w.getTraveledDistance(),
                        (int) w.sourcePos.getY() - (int) w.getTraveledDistance(),
                        (int) w.getTraveledDistance() * 2, (int) w.getTraveledDistance() * 2);

                g.drawLine((int) w.sourcePos.getX(), (int) w.sourcePos.getY(),
                        (int) (w.sourcePos.getX() + sin(w.heading) * w.getTraveledDistance()),
                        (int) (w.sourcePos.getY() + cos(w.heading) * w.getTraveledDistance()));
            }
        }*/
    }

}
