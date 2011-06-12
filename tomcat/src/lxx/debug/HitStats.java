/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.debug;

import lxx.Tomcat;
import lxx.office.Office;
import lxx.targeting.bullets.BulletManager;
import lxx.targeting.bullets.BulletManagerListener;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.APoint;
import lxx.utils.LXXConstants;
import lxx.utils.LXXGraphics;
import lxx.utils.LXXUtils;

import java.awt.*;

import static java.lang.StrictMath.*;

public class HitStats implements Debugger, BulletManagerListener {

    private static double[][] hitStats = new double[3][91];

    private BulletManager bulletManager;
    private Tomcat robot;

    public void roundStarted(Office office) {
        office.getBulletManager().addListener(this);
        bulletManager = office.getBulletManager();
        robot = office.getRobot();
    }

    public void roundEnded() {
    }

    public void battleEnded() {
    }

    public void tick() {
        LXXBullet firstBullet = bulletManager.getFirstBullet();
        if (firstBullet == null) {
            return;
        }

        LXXGraphics g = robot.getLXXGraphics();

        final int latVelIdx = getLatVelIdx(firstBullet);
        double maxHits = 0;
        double minHits = Integer.MAX_VALUE;
        for (int i = 0; i < hitStats[latVelIdx].length - 1; i++) {
            maxHits = max(maxHits, hitStats[latVelIdx][i]);
            minHits = min(minHits, hitStats[latVelIdx][i]);
        }

        final double initialAlpha = firstBullet.noBearingOffset() - LXXConstants.RADIANS_45;
        for (int i = 0; i < hitStats[latVelIdx].length - 1; i++) {
            final double alpha1 = initialAlpha + toRadians(i);
            final double alpha2 = initialAlpha + toRadians(i + 1);

            final APoint pnt1 = firstBullet.getFirePosition().project(alpha1, firstBullet.getTravelledDistance() - 5);
            final APoint pnt2 = firstBullet.getFirePosition().project(alpha2, firstBullet.getTravelledDistance() - 5);
            if (maxHits > 0) {
                g.setColor(new Color(255,
                        (int) round(255 - 255 * hitStats[latVelIdx][i] / maxHits),
                        (int) round(255 - 255 * hitStats[latVelIdx][i] / maxHits)));
            } else {
                g.setColor(new Color(255, 255, 255));
            }
            g.drawLine(pnt1, pnt2);

            if (hitStats[latVelIdx][i] == minHits) {
                final APoint pnt3 = firstBullet.getFirePosition().project(alpha1 + LXXConstants.RADIANS_0_5, firstBullet.getTravelledDistance() - 5);
                g.fillCircle(pnt3, 5);
            }
        }
    }

    public void bulletFired(LXXBullet bullet) {
    }

    public void bulletHit(LXXBullet bullet) {
        registerHit(bullet);
    }

    private void registerHit(LXXBullet bullet) {
        final int latVelIdx = getLatVelIdx(bullet);

        final double bearingOffset = toDegrees(bullet.getRealBearingOffsetRadians() *
                (latVelIdx == 0
                        ? -1
                        : 1));

        for (int i = 0; i < hitStats[latVelIdx].length; i++) {
            final double dist = round(abs(bearingOffset + 45 - i));
            hitStats[latVelIdx][i] = hitStats[latVelIdx][i] * 0.9 + (1 / (dist + 1));
        }
    }

    private int getLatVelIdx(LXXBullet bullet) {
        final double lateralVelocity = LXXUtils.lateralVelocity(bullet.getFirePosition(), bullet.getTargetStateAtFireTime());
        final int latVelIdx;
        if (abs(lateralVelocity) < 3) {
            latVelIdx = 1;
        } else if (lateralVelocity < -3) {
            latVelIdx = 0;
        } else {
            latVelIdx = 2;
        }
        return latVelIdx;
    }

    public void bulletMiss(LXXBullet bullet) {
    }

    public void bulletIntercepted(LXXBullet bullet) {
        registerHit(bullet);
    }

    public void bulletPassing(LXXBullet bullet) {
    }
}
