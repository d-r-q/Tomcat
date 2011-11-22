/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies.duel;

import static java.lang.Math.max;

class PointDanger {

    public final double dangerOnFirstWave;
    public final double dangerOnSecondWave;
    public double distToEnemy;
    public final double distanceToCenter;
    public final double enemyAttackAngle;
    public double danger;
    public double minDangerOnSecondWave;

    PointDanger(double dangerOnFirstWave, double dangerOnSecondWave, double distToEnemy, double distanceToWall,
                double enemyAttackAngle) {
        this.dangerOnFirstWave = dangerOnFirstWave;
        this.dangerOnSecondWave = dangerOnSecondWave;
        this.distToEnemy = distToEnemy;
        this.distanceToCenter = distanceToWall;
        this.enemyAttackAngle = enemyAttackAngle;

        calculateDanger();
    }

    public void calculateDanger() {
        this.danger = dangerOnFirstWave * 120 +
                distanceToCenter / 800 * 5 +
                max(0, (500 - distToEnemy)) / distToEnemy * 15 +
                minDangerOnSecondWave / 10;
    }

    @Override
    public String toString() {
        return String.format("PointDanger (%s #1, %s #2, %3.3f, %3.3f)", dangerOnFirstWave, dangerOnSecondWave, distToEnemy, distanceToCenter);
    }
}
