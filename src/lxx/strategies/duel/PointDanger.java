/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import lxx.bullets.LXXBullet;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.StrictMath.sqrt;

class PointDanger {

    private final LXXBullet bullet;
    private final double dangerOnFirstWave;
    private final double distanceToCenter;

    private double distToEnemySq = Integer.MAX_VALUE;
    private PointDanger minDangerOnSecondWave;
    private double danger;
    private double dangerMultiplier = 1;

    PointDanger(LXXBullet bullet, double dangerOnFirstWave, double distanceToCenter) {
        this.bullet = bullet;
        this.dangerOnFirstWave = dangerOnFirstWave;
        this.distanceToCenter = distanceToCenter;
        calculateDanger();
    }

    public double getDanger() {
        return danger;
    }

    public void setMinDistToEnemySq(double distToEnemy) {
        this.distToEnemySq = min(this.distToEnemySq, distToEnemy);
    }

    public void setMinDangerOnSecondWave(PointDanger minDangerOnSecondWave) {
        this.minDangerOnSecondWave = minDangerOnSecondWave;
        calculateDanger();
    }

    public void setDangerMultiplier(double dangerMultiplier) {
        this.dangerMultiplier = dangerMultiplier;
    }

    public void calculateDanger() {
        final double distToEnemy = sqrt(distToEnemySq);
        double thisDanger = dangerOnFirstWave * 120 +
                distanceToCenter / 800 * 5 +
                max(0, (500 - distToEnemy)) / distToEnemy * 15;
        if (bullet != null) {
            thisDanger = thisDanger * bullet.getBullet().getPower();
        }

        double secondDanger;
        if (minDangerOnSecondWave != null) {
            secondDanger = minDangerOnSecondWave.getDanger();
        } else {
            secondDanger = 0;
        }

        danger = (thisDanger + (secondDanger / 10)) * dangerMultiplier;
    }

}
