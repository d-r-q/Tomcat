/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx;

import lxx.bullets.LXXBullet;
import lxx.events.FireEvent;
import lxx.events.LXXPaintEvent;
import lxx.events.TickEvent;
import lxx.office.Office;
import lxx.office.OfficeImpl;
import lxx.office.PropertiesManager;
import lxx.paint.Painter;
import lxx.strategies.MovementDecision;
import lxx.strategies.Strategy;
import lxx.strategies.StrategySelector;
import lxx.strategies.TurnDecision;
import lxx.utils.LXXUtils;
import lxx.utils.wave.Wave;
import robocode.Bullet;
import robocode.DeathEvent;
import robocode.Rules;
import robocode.io.RobocodeObjectInputStream;
import robocode.security.SecureInputStream;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.lang.Math.abs;
import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 01.03.2011
 */
public class Tomcat extends BasicRobot {

    private boolean isAlive = true;

    private boolean isPaintEnabled = false;

    private Office office;
    private TurnDecision turnDecision;
    private StrategySelector strategySelector;

    public void run() {
        if (getBattleFieldWidth() > 1200 || getBattleFieldHeight() > 1200) {
            System.out.println("Tomcat isn't support battle fields greater than 1200x1200");
            return;
        }
        if (getOthers() > 1) {
            System.out.println("Tomcat isn't support battles with more than 1 opponents");
            return;
        }
        if (PropertiesManager.getDebugProperty("lxx.Tomcat.mode") == null) {
            final Properties props = new Properties();
            try {
                props.load(new FileInputStream(getDataFile("Tomcat.properties")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            PropertiesManager.setDebugProperty("lxx.Tomcat.mode", props.getProperty("lxx.Tomcat.mode"));
        }

        init();

        while (isAlive) {
            doTurn();
            execute();
            notifyListeners();
        }

    }

    private void notifyListeners() {
        if (isAlive) {
            notifyListeners(new TickEvent(getTime()));
            if (isPaintEnabled) {
                notifyListeners(new LXXPaintEvent(getLXXGraphics()));
            }
        }
    }

    private void doTurn() {
        try {
            Strategy currentStrategy = strategySelector.selectStrategy();
            if (currentStrategy instanceof Painter) {
                ((Painter) currentStrategy).paint(getLXXGraphics());
            }

            turnDecision = currentStrategy.makeDecision();

            handleGun();
            move();
            turnRadar();
        } catch (Throwable t) {
            System.out.println("Round time: " + getTime());
            t.printStackTrace();
        }
        isPaintEnabled = false;
    }

    public void init() {
        super.init();
        office = new OfficeImpl(this);
        strategySelector = new StrategySelector(this, office);

        setColors(new Color(255, 67, 0), new Color(255, 144, 66), new Color(255, 192, 66),
                new Color(255, 192, 66), new Color(255, 192, 66));
    }

    private void turnRadar() {
        setTurnRadarRightRadians(turnDecision.getRadarTurnRate());
    }

    private void handleGun() {

        if (getGunHeat() == 0) {
            if (abs(getGunTurnRemaining()) > 1) {
                System.out.printf("[WARN] gun turn remaining is %3.2f when gun is cool\n", getGunTurnRemaining());
            } else if (turnDecision.getFirePower() > 0) {
                fire();
            }
        } else {
            aimGun();
        }
    }

    private void fire() {

        final Bullet bullet = setFireBullet(turnDecision.getFirePower());
        if (bullet != null && turnDecision.getTarget() != null) {
            final Wave bulletWave = office.getWaveManager().launchWaveOnNextTick(getState(), turnDecision.getTarget().getState(), Rules.getBulletSpeed(turnDecision.getFirePower()));
            final LXXBullet lxxBullet = new LXXBullet(bullet, bulletWave, turnDecision.getAimAimPredictionData());

            notifyListeners(new FireEvent(lxxBullet));
        }
    }

    private void aimGun() {
        setTurnGunRightRadians(turnDecision.getGunTurnRate());
    }

    public int getTurnsToGunCool() {
        return (int) round(getGunHeat() / getGunCoolingRate());
    }

    private void move() {
        final MovementDecision movDecision = turnDecision.getMovementDecision();
        final double nextVelocityModule = LXXUtils.limit(0, getVelocityModule() + movDecision.getAcceleration(), Rules.MAX_VELOCITY);
        setTurnRightRadians(movDecision.getTurnRateRadians());
        setMaxVelocity(nextVelocityModule);
        setAhead(100 * movDecision.getMovementDirection().sign);
    }

    public void onDeath(DeathEvent event) {
        isAlive = false;
        notifyListeners(event);
    }

    public void onPaint(Graphics2D g) {
        isPaintEnabled = true;
    }

    public double firePower() {
        return turnDecision.getFirePower();
    }
}
