package kc.serpent.gun;

import kc.serpent.utils.*;
import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GunBase {
    public static boolean isTC = false;
    public static boolean isMelee = false;
    AdvancedRobot robot;
    RobotPredictor robotPredictor;

    public GunBase(AdvancedRobot robot, RobotPredictor robotPredictor) {
        this.robot = robot;
        this.robotPredictor = robotPredictor;
    }

    public static final double FULL_POWER_THRESHOLD = 133.0;
    public static final double WALL_MARGIN = 17.999;
    public static final int RECORDED_POSITION_TICKS = 10;
    public static double MAX_DISTANCE;
    public static double NORMAL_BULLET_SPEED;

    public static boolean isAntiBulletShielding;

    static Rectangle2D battleField;
    Point2D.Double myLocation;
    Point2D.Double enemyLocation;
    double damageDealt;
    int hits;
    int shots;
    double myEnergy;
    double enemyEnergy;
    double enemyDistance;
    double lastEnemyVelocity;
    double lastGF;
    int enemyOrbitDirection;
    long gameTime;
    long lastEnemyVChangeTime;
    long lastEnemyAccelTime;
    long lastEnemyDeccelTime;

    double absoluteBearing;
    double enemyVelocity;
    double enemyDeltaH;
    double enemyHeading;
    double lastEnemyHeading;
    int deltaHSign;
    int velocitySign;

    ArrayList enemyPositionHistory = new ArrayList();
    ArrayList waves = new ArrayList();

    GunSystem currentGun;
    GunSystem mainGun;
    PatternMatcher patternMatcher;
    ArrayList virtualGuns = new ArrayList();
    HashMap virtualGunHits = new HashMap();

    public void init() {
        battleField = KUtils.makeField(robot.getBattleFieldWidth(), robot.getBattleFieldHeight(), WALL_MARGIN);
        MAX_DISTANCE = Math.max(robot.getBattleFieldWidth(), robot.getBattleFieldHeight());
        NORMAL_BULLET_SPEED = isTC ? 11 : KUtils.bulletSpeed(1.999);

        virtualGuns.add(mainGun = new VCSGunStandard());
        virtualGuns.add(new VCSGunAS());
        virtualGuns.add(patternMatcher = new PatternMatcher());
        currentGun = mainGun;

        Iterator i = virtualGuns.iterator();
        while (i.hasNext()) {
            GunSystem current = (GunSystem) i.next();
            current.init(this);
            virtualGunHits.put(current, new Integer(0));
        }
    }

    public void reset() {
        enemyEnergy = 100;
        lastEnemyVelocity = 0;
        lastEnemyHeading = -1;
        deltaHSign = 0;
        velocitySign = 0;
        lastEnemyVChangeTime = 0;
        lastEnemyAccelTime = 0;
        lastEnemyDeccelTime = 0;
        waves.clear();
        enemyPositionHistory.clear();
        isAntiBulletShielding = false;

        Iterator i = virtualGuns.iterator();
        while (i.hasNext()) {
            ((GunSystem) i.next()).reset();
        }

        setCurrentGun();
        System.out.println("Using " + currentGun.getName());
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        gameTime = robot.getTime();
        myLocation = new Point2D.Double(robot.getX(), robot.getY());
        myEnergy = robot.getEnergy();
        enemyEnergy = e.getEnergy();
        enemyDistance = e.getDistance();
        enemyVelocity = e.getVelocity();
        enemyHeading = e.getHeadingRadians();
        absoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
        enemyLocation = KUtils.projectMotion(myLocation, absoluteBearing, enemyDistance);

        double bulletPower = findBulletPower();
        double bulletSpeed = KUtils.bulletSpeed(bulletPower);
        double bulletImpactTime = enemyDistance / bulletSpeed;
        double maxEscapeAngle = KUtils.maxEscapeAngle(bulletSpeed);

        double accel = Math.abs(enemyVelocity - lastEnemyVelocity) * (Math.abs(enemyVelocity) < Math.abs(lastEnemyVelocity) ? -1 : 1);
        if (Math.abs(accel) > 0.01) {
            lastEnemyVChangeTime = gameTime;
            if (accel < 0) {
                lastEnemyAccelTime = gameTime;
            } else {
                lastEnemyDeccelTime = gameTime;
            }
        }

        double vChangeTimer = (double) (gameTime - lastEnemyVChangeTime) / bulletImpactTime;
        double accelTimer = (double) (gameTime - lastEnemyAccelTime) / bulletImpactTime;
        double deccelTimer = (double) (gameTime - lastEnemyDeccelTime) / bulletImpactTime;

        double nextAccel = 0;
        if (accel != 0) {
            boolean deccelAccelSwitch = false;
            if (enemyVelocity == 0 || (lastEnemyVelocity != 0 && KUtils.sign(lastEnemyVelocity) != KUtils.sign(enemyVelocity))) {
                deccelAccelSwitch = true;
            }

            nextAccel = deccelAccelSwitch ? KUtils.sign(accel) * Math.min(Math.abs(accel), 1) : accel;
        }
        double nextVelocity = KUtils.minMax(enemyVelocity + nextAccel, -8, 8);
        double nextHeading = enemyHeading + Utils.normalRelativeAngle(enemyHeading - lastEnemyHeading);
        Point2D.Double nextEnemyLocation = KUtils.projectMotion(enemyLocation, nextHeading, nextVelocity);

        double deltaH = Utils.normalRelativeAngle(enemyHeading - lastEnemyHeading);
        int deltaHSignChange = 1;
        int velocitySignChange = 1;
        int latVelocitySignChange = 1;
        double latVelocity = enemyVelocity * Math.sin(enemyHeading - absoluteBearing);
        if (latVelocity != 0 && KUtils.sign(latVelocity) != enemyOrbitDirection) {
            enemyOrbitDirection = KUtils.sign(latVelocity);
            latVelocitySignChange = -1;
        }
        if (enemyVelocity != 0 && KUtils.sign(enemyVelocity) != velocitySign) {
            velocitySign = KUtils.sign(enemyVelocity);
            velocitySignChange = -1;
        }
        if (deltaH != 0 && KUtils.sign(deltaH) != deltaHSign) {
            deltaHSign = KUtils.sign(deltaH);
            deltaHSignChange = -1;
        }

        if (lastEnemyHeading != -1 && Math.abs(deltaH) < 0.0001 + RobotPredictor.maxTurn(lastEnemyVelocity)) {
            patternMatcher.scan(enemyVelocity, deltaH, velocitySignChange, deltaHSignChange, latVelocity, enemyVelocity * Math.cos(enemyHeading - absoluteBearing), latVelocitySignChange);
        }

        lastEnemyVelocity = enemyVelocity;
        lastEnemyHeading = enemyHeading;

        enemyPositionHistory.add((Point2D.Double) (enemyLocation));
        if (enemyPositionHistory.size() > RECORDED_POSITION_TICKS + 1) {
            enemyPositionHistory.remove(0);
        }
        Point2D.Double latestPoint = enemyLocation;
        Point2D.Double earliestPoint = (Point2D.Double) (enemyPositionHistory.get(0));
        double lastDTraveled = latestPoint.distance(earliestPoint);

        double wallAhead = maxEscapeAngle;
        double wallReverse = maxEscapeAngle;
        Point2D.Double enemyProjectedLocation;
        for (wallAhead = 0; wallAhead <= 1.5 * maxEscapeAngle; wallAhead += 0.005) {
            enemyProjectedLocation = KUtils.projectMotion(myLocation, absoluteBearing + (enemyOrbitDirection * wallAhead), enemyDistance);
            if (!battleField.contains(enemyProjectedLocation)) {
                break;
            }
        }
        for (wallReverse = 0; wallReverse <= 1.5 * maxEscapeAngle; wallReverse += 0.005) {
            enemyProjectedLocation = KUtils.projectMotion(myLocation, absoluteBearing - (enemyOrbitDirection * wallReverse), enemyDistance);
            if (!battleField.contains(enemyProjectedLocation)) {
                break;
            }
        }
        wallAhead /= maxEscapeAngle;
        wallReverse /= maxEscapeAngle;

        double approachAngle = Math.abs(Utils.normalRelativeAngle(enemyHeading - absoluteBearing + (enemyVelocity > 0 ? 0 : Math.PI)));

        robot.setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - robot.getGunHeadingRadians()));

        if (robot.getTime() < 3 / robot.getGunCoolingRate()) {
            waves.clear();
        }
        if (robot.getOthers() == 0) {
            waves.clear();
            return;
        }
        if (myEnergy - bulletPower < 0.01 && (!isTC || myEnergy <= 0)) {
            waves.clear();
            return;
        }
        if ((myEnergy < 5 || (isMelee && myEnergy < 20)) && (myEnergy > enemyEnergy) && (myEnergy - bulletPower < enemyEnergy) && (!isTC)) {
            waves.clear();
            return;
        }

        updateWaves();

        GunWave w = new GunWave();
        w.isReal = false;
        w.source = myLocation;
        w.absoluteBearing = KUtils.absoluteBearing(robotPredictor.getNextLocation(robot), nextEnemyLocation);
        w.maxEscapeAngle = maxEscapeAngle;
        w.speed = bulletSpeed;
        w.fireTime = gameTime;
        w.orbitDirection = enemyOrbitDirection;
        w.distance = enemyDistance;
        w.normalizedDistance = bulletImpactTime * NORMAL_BULLET_SPEED;
        w.latVelocity = latVelocity;
        w.accel = accel;
        w.vChangeTimer = vChangeTimer;
        w.lastDTraveled = lastDTraveled;
        w.wallAhead = wallAhead;
        w.wallReverse = wallReverse;

        w.enemyLocation = enemyLocation;
        w.enemyVelocity = enemyVelocity;
        w.enemyHeading = enemyHeading;
        w.deltaHSign = deltaHSign;
        w.velocitySign = velocitySign;

        waves.add(w);

        int ticksUntilShot = patternMatcher.ticksUntilShot = (int) (robot.getGunHeat() / robot.getGunCoolingRate());
        if (ticksUntilShot > 6) {
            setCurrentGun();
            return;
        }

        double firingAngle = currentGun.getFiringAngle(w);
        if (isAntiBulletShielding) {
            firingAngle += (Math.random() - 0.5) * KUtils.botWidthAngle(5.0, enemyDistance);
        }

        double gunTurn = Utils.normalRelativeAngle(firingAngle - robot.getGunHeadingRadians());
        robot.setTurnGunRightRadians(gunTurn);
        if (Math.abs(gunTurn) < KUtils.botWidthAngle(18.0, enemyDistance) && robot.getGunHeat() == 0.0) {
            Iterator i = virtualGuns.iterator();
            while (i.hasNext()) {
                GunSystem current = (GunSystem) (i.next());
                if (current == currentGun) {
                    w.virtualBulletAngles.put(current, new Double(firingAngle));
                } else {
                    w.virtualBulletAngles.put(current, new Double(current.getFiringAngle(w)));
                }
            }

            w.isReal = true;
            robot.setFire(bulletPower);
        }

        w.absoluteBearing = absoluteBearing;
    }

    public double findBulletPower() {
        if (isTC) {
            return Math.min(3, myEnergy);
        }

        double minimumKillPower = 0.001 + (enemyEnergy / 4);
        if (enemyEnergy >= 4.5) {
            minimumKillPower = 0.001 + ((enemyEnergy + 2) / 6);
        }

        double bulletPower = 1.999;
        if (!isMelee && (double) (hits) / (double) (shots) > 0.3 && robot.getRoundNum() > 3) {
            bulletPower = 2.499;
        }
        if (myEnergy < 25.0 && enemyEnergy > myEnergy) {
            bulletPower = 1.499;
        }
        if (myEnergy < 15.0) {
            bulletPower = 1.499;
        }
        if (myEnergy < 15.0 && enemyEnergy > myEnergy) {
            bulletPower = 0.999;
        }
        if (myEnergy < 4.0) {
            bulletPower = 0.499;
        }

        if (enemyDistance < FULL_POWER_THRESHOLD) {
            bulletPower = 3.0;
        }

        return Math.max(Math.min(Math.min(bulletPower, myEnergy), minimumKillPower), 0.1);
    }

    public void setCurrentGun() {
        GunSystem bestGun = mainGun;
        double bestGunHits = ((Integer) virtualGunHits.get(bestGun)).intValue();

        double nonMainMultiplier;
        if (robot.getRoundNum() < 2) {
            nonMainMultiplier = 0;
        } else if (robot.getRoundNum() < 5) {
            nonMainMultiplier = 0.8;
        } else if (robot.getRoundNum() < 15) {
            nonMainMultiplier = 0.9;
        } else {
            nonMainMultiplier = 1;
        }
        if (robot.getRoundNum() >= 5 && (double) bestGunHits / shots > 0.25) {
            nonMainMultiplier = 0.9;
        }

        Iterator i = virtualGuns.iterator();
        while (i.hasNext()) {
            GunSystem current = (GunSystem) (i.next());
            if (current == mainGun) {
                continue;
            }

            double currentHits = nonMainMultiplier * ((Integer) (virtualGunHits.get(current))).intValue();
            if (currentHits > bestGunHits) {
                bestGunHits = currentHits;
                bestGun = current;
            }
        }

        if (bestGun != currentGun) {
            System.out.println("Switching gun to " + bestGun.getName());
        }
        currentGun = bestGun;
    }

    public void updateWaves() {
        int n = 0;
        while (n < waves.size()) {
            GunWave w = (GunWave) (waves.get(n++));
            w.distance = enemyLocation.distance(w.source);
            w.setRadius(gameTime);

            double angle = KUtils.absoluteBearing(w.source, enemyLocation);
            if (!w.hasPassed && bulletHits(w, angle)) {
                if (w.isReal) {
                    shots++;
                }
                w.hasPassed = true;

                lastGF = w.getGF(enemyLocation);
                Iterator i = virtualGuns.iterator();
                while (i.hasNext()) {
                    ((GunSystem) i.next()).wavePassed(lastGF, w);
                }
            }

            if (w.radius - 50 > w.distance) {
                waves.remove(w);
                n--;
            }

            if (w.isReal) {
                Iterator i = virtualGuns.iterator();
                while (i.hasNext()) {
                    GunSystem g = (GunSystem) (i.next());
                    if (!w.virtualBulletAngles.containsKey(g)) {
                        continue;
                    }

                    double bulletAngle = ((Double) w.virtualBulletAngles.get(g)).doubleValue();

                    if (bulletHits(w, bulletAngle)) {
                        Integer oldHits = (Integer) virtualGunHits.get(g);
                        virtualGunHits.put(g, new Integer(oldHits.intValue() + 1));
                        w.virtualBulletAngles.remove(g);
                    }
                }
            }
        }
    }

    public boolean bulletHits(GunWave w, double angle) {
        Point2D.Double lastBulletLoc = KUtils.projectMotion(w.source, angle, w.radius - w.speed);
        Point2D.Double currentBulletLoc = KUtils.projectMotion(w.source, angle, w.radius);
        Point2D.Double nextBulletLoc = KUtils.projectMotion(w.source, angle, w.radius + w.speed);

        Line2D.Double lastFireLine = new Line2D.Double(lastBulletLoc, currentBulletLoc);
        Line2D.Double currentFireLine = new Line2D.Double(currentBulletLoc, nextBulletLoc);
        Rectangle2D.Double enemyRectangle = new Rectangle2D.Double(enemyLocation.x - 18.0, enemyLocation.y - 18.0, 36.0, 36.0);

        return enemyRectangle.intersectsLine(currentFireLine) || enemyRectangle.intersectsLine(lastFireLine);
    }

    public void onBulletHit(BulletHitEvent e) {
        damageDealt += Math.min(enemyEnergy, (4 * e.getBullet().getPower()) + Math.max(2 * (e.getBullet().getPower() - 1), 0));
        hits++;
    }

    public void printStats() {
        System.out.println("Main Gun Score: " + ((Integer) (virtualGunHits.get((GunSystem) virtualGuns.get(0)))).intValue());
        System.out.println("Anti Surfer Gun Score: " + ((Integer) (virtualGunHits.get((GunSystem) virtualGuns.get(1)))).intValue());
        System.out.println("Pattern Matching Gun Score: " + ((Integer) (virtualGunHits.get((GunSystem) virtualGuns.get(2)))).intValue());
        System.out.println("My Hit Rate: " + hits + "/" + shots + " = " + 100f * hits / shots + "%");
        if (isTC) {
            System.out.println("TC Score: " + (float) (damageDealt / (1 + robot.getRoundNum())));
        }
    }

    public void onPaint(java.awt.Graphics2D g) {
        if (robot.getOthers() == 0) {
            waves.clear();
        }
        if (gameTime != robot.getTime()) {
            gameTime = robot.getTime();
            updateWaves();
        }

        Iterator i = waves.iterator();
        while (i.hasNext()) {
            GunWave w = (GunWave) (i.next());
            if (w.isReal) {
                g.setColor(Color.gray);
                double r = w.radius;
                g.drawOval((int) Math.round(w.source.x - r),
                        (int) Math.round(w.source.y - r),
                        (int) Math.round(2 * r),
                        (int) Math.round(2 * r));

                r = w.radius + w.speed;
                g.drawOval((int) Math.round(w.source.x - r),
                        (int) Math.round(w.source.y - r),
                        (int) Math.round(2 * r),
                        (int) Math.round(2 * r));

                Point2D.Double projected = KUtils.projectMotion(w.source, w.absoluteBearing, r);
                g.drawLine((int) Math.round(w.source.x),
                        (int) Math.round(w.source.y),
                        (int) Math.round(projected.x),
                        (int) Math.round(projected.y));

                Iterator i2 = w.virtualBulletAngles.keySet().iterator();
                while (i2.hasNext()) {
                    GunSystem currentKey = (GunSystem) i2.next();
                    g.setColor(currentKey == virtualGuns.get(0) ? Color.blue : (currentKey == virtualGuns.get(1) ? Color.green : Color.red));

                    double angle = ((Double) w.virtualBulletAngles.get(currentKey)).doubleValue();
                    Point2D.Double currentBulletLoc = KUtils.projectMotion(w.source, angle, w.radius);
                    Point2D.Double nextBulletLoc = KUtils.projectMotion(w.source, angle, w.radius + w.speed);

                    g.drawLine((int) Math.round(currentBulletLoc.x),
                            (int) Math.round(currentBulletLoc.y),
                            (int) Math.round(nextBulletLoc.x),
                            (int) Math.round(nextBulletLoc.y));
                }
            }
        }

        g.setColor(Color.gray);
        if (enemyLocation != null) {
            g.drawRect((int) Math.round(enemyLocation.x - 18.0), (int) Math.round(enemyLocation.y - 18.0), 36, 36);
        }
    }
}