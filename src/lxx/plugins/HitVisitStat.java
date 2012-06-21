package lxx.plugins;

import lxx.Tomcat;
import lxx.bullets.BulletManagerListener;
import lxx.bullets.LXXBullet;
import lxx.office.Office;
import lxx.paint.LXXGraphics;
import lxx.utils.LXXPoint;
import lxx.utils.LXXUtils;
import lxx.utils.wave.Wave;
import lxx.utils.wave.WaveCallback;
import lxx.utils.wave.WaveManager;

import java.awt.*;

import static java.lang.Math.*;

public class HitVisitStat implements Plugin, BulletManagerListener, WaveCallback {

    private static final int BINS = 201;

    private static final double[] meHits = new double[BINS];
    private static final double[] meVisits = new double[BINS];

    private static final double[] eHits = new double[BINS];
    private static final double[] eVisits = new double[BINS];

    private WaveManager waveManager;
    private Tomcat robot;

    public void roundStarted(Office office) {
        waveManager = office.getWaveManager();
        robot = office.getRobot();
        office.getEnemyBulletManager().addListener(this);
        office.getBulletManager().addListener(this);
    }

    public void tick() {
        drawBins(meHits, robot.getLXXGraphics(), new LXXPoint(0, 0), 100, Color.GREEN);
        drawBins(meVisits, robot.getLXXGraphics(), new LXXPoint(0, 110), 100, Color.RED);

        drawBins(eHits, robot.getLXXGraphics(), new LXXPoint(BINS + 12, 0), 100, Color.BLUE);
        drawBins(eVisits, robot.getLXXGraphics(), new LXXPoint(BINS + 12, 110), 100, Color.YELLOW);
    }

    public void bulletFired(LXXBullet bullet) {
        waveManager.addCallback(this, bullet.getWave());
    }

    public void bulletHit(LXXBullet bullet) {
        final double gf = bullet.getRealBearingOffsetRadians() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed()) * bullet.getTargetState().getLastDirection();
        if (bullet.getTarget().getName().equals(robot.getName())) {
            registerBinVisit(meHits, gf);
        } else {
            registerBinVisit(eHits, gf);
        }
    }

    public void bulletIntercepted(LXXBullet bullet) {
        /*final double gf = bullet.getRealBearingOffsetRadians() / LXXUtils.getMaxEscapeAngle(bullet.getSpeed()) * bullet.getTargetState().getLastDirection();
        if (bullet.getTarget().getName().equals(robot.getName())) {
            meHits[toBin(gf)]++;
        } else {
            eHits[toBin(gf)]++;
        }*/
    }

    public void waveBroken(Wave w) {
        final double gf = w.getHitBearingOffsetInterval().center() / LXXUtils.getMaxEscapeAngle(w.getSpeed()) * w.getTargetState().getLastDirection();
        if (robot.getName().equals(w.getTarget().getName())) {
            registerBinVisit(meVisits, gf);
        } else {
            registerBinVisit(eVisits, gf);
        }
    }

    private void registerBinVisit(double[] bins, double gf) {
        final int center = toBin(gf);
        final int from = max(0, center - 2);
        final int to = min(bins.length, center + 3);
        for (int i = from; i < to; i++) {
            bins[i] += 1d / (abs(center - i) + 1);
        }

    }

    private static int toBin(double gf) {
        return (int) round((LXXUtils.limit(-1, gf, 1) + 0.99) / 2 * BINS);
    }

    private static void drawBins(double[] bins, LXXGraphics g, LXXPoint base, int height, Color c) {
        int maxIdx = 0;
        for (int i = 1; i < bins.length; i++) {
            if (bins[i] > bins[maxIdx]) {
                maxIdx = i;
            }
        }

        g.setColor(c.darker());
        g.drawRect(base.x, base.y, bins.length + 2, height);
        final double max = bins[maxIdx];
        if (max > 0) {
            g.setColor(c);
            for (int i = 0; i < bins.length; i++) {
                final double h = height * bins[i] / max;
                g.drawLine(base.x + i + 1, base.y, base.x + i + 1, base.y + h);
            }
        }
    }

    public void battleEnded() {
    }

    public void bulletMiss(LXXBullet bullet) {
    }
}
