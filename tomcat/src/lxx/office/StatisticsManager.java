/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.RobotListener;
import lxx.Tomcat;
import lxx.events.TickEvent;
import lxx.targeting.bullets.BulletManagerListener;
import lxx.targeting.bullets.LXXBullet;
import lxx.utils.HitRate;
import robocode.DeathEvent;
import robocode.Event;
import robocode.RobotDeathEvent;
import robocode.WinEvent;

/**
 * User: jdev
 * Date: 16.06.2010
 */
public class StatisticsManager implements RobotListener, BulletManagerListener {

    private static final int[] placeDeathCount = new int[11];
    private static final double[] placeEnergyCount = new double[11];
    private static final int[] placePassed = new int[11];

    private static HitRate myHitRate;
    private static HitRate enemyHitRate;

    private final Office office;
    private final Tomcat tomcat;

    public StatisticsManager(Office office, Tomcat tomcat) {
        this.office = office;
        this.tomcat = tomcat;
        if (myHitRate == null) {
            myHitRate = new HitRate();
            enemyHitRate = new HitRate();
        }

        office.getBulletManager().addListener(this);
        office.getEnemyBulletManager().addListener(this);

    }

    public void onTick() {
        tomcat.setDebugProperty(tomcat.getName() + " static hit rate", String.valueOf(myHitRate));
        tomcat.setDebugProperty(tomcat.getName() + " miss count", String.valueOf(myHitRate.getMissCount()));

        if (tomcat.isDuel() && office.getTargetManager().hasDuelOpponent()) {
            tomcat.setDebugProperty(office.getTargetManager().getDuelOpponentName() + " static hit rate", String.valueOf(enemyHitRate));
        }
    }

    public void onRobotDeath() {
        placeEnergyCount[tomcat.getOthers() + 2] = (placeEnergyCount[tomcat.getOthers() + 2] * placePassed[tomcat.getOthers() + 2] + tomcat.getEnergy()) / (placePassed[tomcat.getOthers() + 2] + 1);
        placePassed[tomcat.getOthers() + 2]++;
    }

    public void onDeath() {
        placeDeathCount[tomcat.getOthers() + 1]++;
    }

    public void onWin() {
        placeDeathCount[1]++;
        placePassed[1]++;
    }

    public void onEvent(Event event) {
        if (tomcat.getOthers() > 10) {
            return;
        }
        if (event instanceof WinEvent) {
            onWin();
        } else if (event instanceof DeathEvent) {
            onDeath();
        } else if (event instanceof RobotDeathEvent) {
            onRobotDeath();
        } else if (event instanceof TickEvent) {
            onTick();
        }
    }

    public void bulletHit(LXXBullet bullet) {
        if (bullet.getOwner().getName().equals(tomcat.getName())) {
            myHitRate.hit();
        } else {
            enemyHitRate.hit();
        }
    }

    public void bulletMiss(LXXBullet bullet) {
        if (bullet.getOwner().getName().equals(tomcat.getName())) {
            myHitRate.miss();
        } else {
            enemyHitRate.miss();
        }
    }

}
