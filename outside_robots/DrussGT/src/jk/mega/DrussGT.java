package jk.mega;

import ags.utils.KdTree;
import jk.mega.dGun.DrussGunDC;
import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.Comparable;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.StringIndexOutOfBoundsException;
import java.util.*;
import java.util.List;

public class DrussGT extends AdvancedRobot {
    static final int BINS = 101;
    static final int MIDDLE_BIN = (BINS - 1) / 2;
    static final boolean TC = false;


    static ArrayList statBuffers = new ArrayList();
    static ArrayList flattenerBuffers = new ArrayList();
    // static ArrayList<Scan> visitScans = new ArrayList<Scan>();
    // static ArrayList<Scan> hitScans = new ArrayList<Scan>();

    public Point2D.Double _myLocation = new Point2D.Double();     // our bot's location
    public Point2D.Double _enemyLocation = new Point2D.Double();  // enemy bot's location
    public Point2D.Double nextEnemyLocation;
    public int time_since_dirchange;
    public double direction = 1;

    public static ArrayList _distances;
    public static ArrayList<Double> _lateralVelocitys;
    public static ArrayList _advancingVelocitys;
    public static ArrayList _enemyWaves;
    public static ArrayList _surfDirections;
    public static ArrayList _surfAbsBearings;

    private static double BULLET_POWER = 1.9;

    private static double lateralDirection;

    // We must keep track of the enemy's energy level to detect EnergyDrop,
    // indicating a bullet is fired
    public double _oppEnergy = 100.0;

    // This is a rectangle that represents an 800x600 battle field,
    // used for a simple, iterative WallSmoothing method (by Kawigi).
    // If you're not familiar with WallSmoothing, the wall stick indicates
    // the amount of space we try to always have on either end of the tank
    // (extending straight out the front or back) before touching a wall.
    public static Rectangle2D.Double _fieldRect
            = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
    public ArrayList goToTargets;
    public Point2D.Double lastGoToPoint;
    public static double WALL_STICK = 160;
    public long lastScanTime = 0;
    public static double totalEnemyDamage;
    public static double weightedEnemyFirerate, weightedEnemyHitrate;
    public static double totalMyDamage;
    public boolean surfStatsChanged;
    public double enemyGunHeat;
    public double imaginaryGunHeat;
    public static double bestDistance = 400;
    public static boolean flattenerEnabled = false;
    public ScannedRobotEvent lastScan;

    static KdTree<Float> bulletPowerTree =
            new KdTree.SqrEuclid<Float>(3, new Integer(10000));

    long moveTime;
    long gunTime;

    EnemyWave mainWave;
    EnemyWave secondWave;
    EnemyWave thirdWave;

    boolean painting = false;
    ArrayList firstPointsPainting;
    ArrayList nextPointsPainting;


    // RaikoGun raikoGun = new RaikoGun(this);
    //  WaylanderGun waylanderGun = new WaylanderGun(this);
    DrussGunDC dgun = new DrussGunDC(this);

    static {
        FastTrig.init();
    }


    public void run() {
        if (getRoundNum() != 0) {
            System.out.println("Enemy damage: " + totalEnemyDamage);
            System.out.println("My damage:    " + totalMyDamage);
            System.out.println("Accumulated, weighted enemy hitrate % : " + (100 * weightedEnemyHitrate / weightedEnemyFirerate));
            System.out.println("Flattener enabled: " + flattenerEnabled);
        }

        if (!TC && getRoundNum() == 0) {

            //loadBufferManager.StatBuffers();
            statBuffers = BufferManager.getStatBuffers();
            flattenerBuffers = BufferManager.getFlattenerBuffers();


            //preloaded HOT hit
            BufferManager.SingleBuffer sb = new BufferManager.SingleBuffer();
            sb.bins = new float[7];
            BufferManager.StatBuffer stb = ((BufferManager.StatBuffer) statBuffers.get(0));
            stb.stats[0][0][0][0][0][0][0][0][0] = sb;
            sb.bins[1] = MIDDLE_BIN;
            sb.bins[0] = 1;
            sb.weight = stb._weight;
            sb.rollingDepth = stb.rollingDepth;

            _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
        }

        setColors(Color.YELLOW, Color.BLACK, Color.BLACK);
        lateralDirection = 1;

        _lateralVelocitys = new ArrayList();
        _advancingVelocitys = new ArrayList();
        _enemyWaves = new ArrayList();
        _surfDirections = new ArrayList();
        _surfAbsBearings = new ArrayList();
        _distances = new ArrayList();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);


        do {
            if (getRadarTurnRemaining() == 0 && getOthers() > 0) {
                if (getTime() > 9)
                    System.out.println("Lost radar lock");
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
            if (!TC)
                if (lastScanTime + 1 < getTime() || getOthers() == 0) {
                    time = super.getTime();
                    _myLocation = new Point2D.Double(getX(), getY());
                    updateWaves();
                    doSurfing();
                }


            time = super.getTime();
            execute();

        } while (true);
    }

    long time;

    public long getTime() {
        return time;

    }

    public void onScannedRobot(ScannedRobotEvent e) {
        long stime = -System.nanoTime();
        try {
            _myLocation = new Point2D.Double(getX(), getY());
            lastScan = e;
            lastScanTime = getTime();
            if (!TC) {
                if (_surfDirections.size() == 0)
                    enemyGunHeat = imaginaryGunHeat = getGunHeat();

                double lateralVelocity = getVelocity() * FastTrig.sin(e.getBearingRadians());
                double advancingVelocity = -getVelocity() * FastTrig.cos(e.getBearingRadians());
                double absBearing = e.getBearingRadians() + getHeadingRadians();
                if (lateralVelocity > 0)
                    lateralDirection = 1;
                else if (lateralVelocity < 0)
                    lateralDirection = -1;


                _surfDirections.add(0, new Integer((int) lateralDirection));
                _surfAbsBearings.add(0, new Double(absBearing + Math.PI));
                _lateralVelocitys.add(0, new Double(Math.abs(lateralVelocity)));
                _advancingVelocitys.add(0, new Integer((int) Math.round(advancingVelocity)));
                _distances.add(0, new Double(e.getDistance()));

                enemyGunHeat = Math.max(0.0, enemyGunHeat - getGunCoolingRate());
                imaginaryGunHeat = Math.max(enemyGunHeat, imaginaryGunHeat - getGunCoolingRate());

                nextEnemyLocation = project(project(_myLocation, absBearing, e.getDistance()), e.getHeadingRadians(), e.getVelocity());


                double bulletPower = _oppEnergy - e.getEnergy();
                addWave(bulletPower);
                addImaginaryWave();

                _oppEnergy = e.getEnergy();

                // update after EnemyWave detection, because that needs the previous
                // enemy location as the source of the wave
                _enemyLocation = project(_myLocation, absBearing, e.getDistance());

                updateWaves();

                if (weightedEnemyHitrate / weightedEnemyFirerate > 0.095 && getRoundNum() > 1) {
                    if (!flattenerEnabled)
                        System.out.println("Flattener Enabled");
                    flattenerEnabled = true;
                } else {
                    if (flattenerEnabled)
                        System.out.println("Flattener Disabled");
                    flattenerEnabled = false;
                }

                doSurfing();
            }
            moveTime = stime + System.nanoTime();
            // ANTI-MIRROR STUFF
            /*
            take 2 predicted positions
            from them iterate forward a wave until
              there is a collision with point mirrored around midpoint,
              predicting movement to these predicted positions
            use prediction towards point of lowest danger on subsequent waves
            point of collision is mirrorTarget
            latVel of collision point from
            */

            stime = -System.nanoTime();

            Point2D.Double finalMirrorPoint;
            double finalHeading;
            double finalVelocity;
            long finalTime;
            if (thirdWave != null && thirdWave.safestPoint != null && thirdWave.safestPoint.predictionStatus != null) {
                finalMirrorPoint = thirdWave.safestPoint.predictionStatus.endPoint;
                finalHeading = thirdWave.safestPoint.predictionStatus.finalHeading;
                finalVelocity = thirdWave.safestPoint.predictionStatus.finalVelocity;
                finalTime = thirdWave.safestPoint.predictionStatus.time - getTime();
            } else if (secondWave != null && secondWave.safestPoint != null && secondWave.safestPoint.predictionStatus != null) {
                finalMirrorPoint = secondWave.safestPoint.predictionStatus.endPoint;
                finalHeading = secondWave.safestPoint.predictionStatus.finalHeading;
                finalVelocity = secondWave.safestPoint.predictionStatus.finalVelocity;
                finalTime = secondWave.safestPoint.predictionStatus.time - getTime();
            } else if (mainWave != null && mainWave.safestPoint != null && mainWave.safestPoint.predictionStatus != null) {
                finalMirrorPoint = mainWave.safestPoint.predictionStatus.endPoint;
                finalHeading = mainWave.safestPoint.predictionStatus.finalHeading;
                finalVelocity = mainWave.safestPoint.predictionStatus.finalVelocity;
                finalTime = mainWave.safestPoint.predictionStatus.time - getTime();
            } else {
                finalMirrorPoint = _myLocation;
                finalHeading = getHeadingRadians();
                finalVelocity = getVelocity();
                finalTime = 0;
            }
            dgun.onScannedRobot(e, finalMirrorPoint, finalHeading, finalVelocity, (int) finalTime);
            gunTime = stime + System.nanoTime();

            // */
            double radarTurn = Utils.normalRelativeAngle(
                    e.getBearingRadians() + getHeadingRadians() - getRadarHeadingRadians());
            radarTurn += Math.signum(radarTurn) *
                    (Math.PI / 4 - Math.PI / 8 - Math.PI / 18) / 2.0;

            setTurnRadarRightRadians(radarTurn);

            // raikoGun.onScannedRobot(e);
            //  waylanderGun.onScannedRobot(e);


        } catch (Exception ex) {
            ex.printStackTrace();

            try {
                PrintStream out = new PrintStream(new RobocodeFileOutputStream(getDataFile(e.getName())));
                ex.printStackTrace(out);
                out.flush();
                out.close();
            } catch (IOException ioex) {
            }


        }
    }

    public void addImaginaryWave() {
        if (enemyGunHeat <= getGunCoolingRate() && imaginaryGunHeat <= getGunCoolingRate()) {

            List<KdTree.Entry<Float>> cl = bulletPowerTree.nearestNeighbor(
                    new double[]{getEnergy() / 200, _oppEnergy / 200, ((Double) _distances.get(0)).doubleValue() / 1200},
                    Math.min((int) Math.ceil(Math.sqrt(bulletPowerTree.size())), 100),
                    false);

            float[] bpBins = new float[99];

            Iterator<KdTree.Entry<Float>> it = cl.iterator();

            while (it.hasNext()) {
                KdTree.Entry<Float> p = it.next();
                double weight = 1 / (p.distance + 1e-15);

                int index = (int) Math.round(p.value * bpBins.length / 3);
                // System.out.println(p.value);

                for (int i = 0; i < bpBins.length; i++)
                    bpBins[i] += weight / (sqr(index - i) / 9 + 1);
            }

            int maxIndex = 66;
            for (int i = 0; i < bpBins.length; i++)
                if (bpBins[i] > bpBins[maxIndex])
                    maxIndex = i;

            double bulletPower = maxIndex * 3.0 / bpBins.length;

            imaginaryGunHeat = 1 + bulletPower / 5 - getGunCoolingRate();

            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime();
            ew.bulletVelocity = bulletVelocity(bulletPower);
            ew.distanceTraveled = -ew.bulletVelocity;
            ew.direction = ((Integer) _surfDirections.get(0)).intValue();
            ew.directAngle = ((Double) _surfAbsBearings.get(0)).doubleValue();
            ew.fireLocation = nextEnemyLocation; // next tick
            ew.imaginary = true;

            float lastLatVel = (float) _lateralVelocitys.get(0).doubleValue();
            float prevLatVel = (float) lastLatVel;
            try {
                prevLatVel = (float) _lateralVelocitys.get(1).doubleValue();
            } catch (Exception ex) {
            }

            float accel = lastLatVel - prevLatVel;

            float distance = (float) ((Double) _distances.get(0)).doubleValue();

            float advVel = (float) ((Integer) _advancingVelocitys.get(0)).intValue();

            float BFT = (float) (distance / ew.bulletVelocity);

            float tsdirchange = 0;
            for (int i = 1; i < _surfDirections.size() - 2; i++)
                if (((Integer) _surfDirections.get(i - 1)).intValue() == ((Integer) _surfDirections.get(i)).intValue())
                    tsdirchange++;
                else
                    break;


            float tsvchange = 0;
            for (int i = 1; i < _lateralVelocitys.size() - 2; i++)
                if (_lateralVelocitys.get(i - 1).doubleValue() <= _lateralVelocitys.get(i).doubleValue() + 0.4)
                    tsvchange++;
                else
                    break;


            float dl10 = 0;
            for (int i = 0; i < Math.min(10, _lateralVelocitys.size() - 2); i++)
                dl10 += (float) (_lateralVelocitys.get(i).doubleValue() * ((Integer) _surfDirections.get(i)).intValue());
            dl10 = Math.abs(dl10) * (10 / 8.0f);

            double MEA = maxEscapeAngle(ew.bulletVelocity);

            float forwardWall = (float) (wallDistance(_enemyLocation, distance, ew.directAngle, ew.direction) / MEA);
            float reverseWall = (float) (wallDistance(_enemyLocation, distance, ew.directAngle, -ew.direction) / MEA);

            tsdirchange /= BFT;
            tsvchange /= BFT;

            ew.indexes = BufferManager.getIndexes(
                    lastLatVel,
                    advVel,
                    BFT,
                    tsdirchange,
                    accel,
                    tsvchange,
                    dl10,
                    forwardWall,
                    reverseWall
            );

            ew.allStats = BufferManager.getStats(
                    statBuffers,
                    ew.indexes
            );
            if (flattenerEnabled)
                ew.flattenerStats = BufferManager.getStats(
                        flattenerBuffers,
                        ew.indexes
                );

            Scan s = new Scan();

            s.latVel = limit(0, lastLatVel / 8, 1);
            s.advVel = limit(0, advVel / 8, 1);
            s.dist = limit(0, BFT / (1200 / 11.0f), 1);
            s.forwardWall = limit(0, forwardWall, 1);
            s.reverseWall = limit(0, reverseWall, 1);
            s.lastVel = limit(0, prevLatVel / 8, 1);
            s.accel = (accel + 2) / 3;
            s.timeSinceDecel = limit(0, tsvchange / BFT, 1);
            s.timeSinceDirChange = limit(0, tsdirchange / BFT, 1);
            s.distLast20 = limit(0, dl10 / (100), 1);
            ew.scan = s;

            // if(secondWave == null)
            surfStatsChanged = true;
            _enemyWaves.add(ew);
        }
    }

    public void addWave(double bulletPower) {
        if (bulletPower < 3.01 && bulletPower > 0.099
                && _surfDirections.size() > 2
                ) {
            enemyGunHeat = 1 + bulletPower / 5 - getGunCoolingRate();
            imaginaryGunHeat = enemyGunHeat;


            bulletPowerTree.addPoint(
                    new double[]{getEnergy() / 200,
                            (_oppEnergy + bulletPower) / 200,
                            ((Double) _distances.get(2)).doubleValue() / 1200},
                    (float) bulletPower);


            EnemyWave imaginaryWave = null;
            for (int x = 0; x < _enemyWaves.size(); x++) {
                EnemyWave ew = (EnemyWave) _enemyWaves.get(x);
                if (ew.imaginary && ew.fireTime == getTime() - 2) {
                    imaginaryWave = ew;
                    _enemyWaves.remove(x);
                    x--;
                }
            }

            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime() - 2;
            ew.bulletVelocity = bulletVelocity(bulletPower);
            ew.distanceTraveled = ew.bulletVelocity;
            ew.direction = ((Integer) _surfDirections.get(2)).intValue();
            ew.directAngle = ((Double) _surfAbsBearings.get(2)).doubleValue();
            ew.fireLocation = (Point2D.Double) _enemyLocation.clone(); // last tick

            float lastLatVel = (float) _lateralVelocitys.get(2).doubleValue();
            float prevLatVel = (float) lastLatVel;
            try {
                prevLatVel = (float) _lateralVelocitys.get(3).doubleValue();
            } catch (Exception ex) {
            }

            float accel = lastLatVel - prevLatVel;

            float distance = (float) ((Double) _distances.get(2)).doubleValue();

            float advVel = (float) ((Integer) _advancingVelocitys.get(2)).intValue();

            float BFT = (float) (distance / ew.bulletVelocity);

            float tsdirchange = 0;
            for (int i = 3; i < _surfDirections.size(); i++)
                if (((Integer) _surfDirections.get(i - 1)).intValue() == ((Integer) _surfDirections.get(i)).intValue())
                    tsdirchange++;
                else
                    break;


            float tsvchange = 0;
            for (int i = 3; i < _lateralVelocitys.size(); i++)
                if (_lateralVelocitys.get(i - 1).doubleValue() <= _lateralVelocitys.get(i).doubleValue() + 0.4)
                    tsvchange++;
                else
                    break;


            float dl10 = 0;
            for (int i = 2; i < Math.min(10, _lateralVelocitys.size()); i++)
                dl10 += (float) (_lateralVelocitys.get(i).doubleValue() * ((Integer) _surfDirections.get(i)).intValue());
            dl10 = Math.abs(dl10) * (10 / 8.0f);

            double MEA = maxEscapeAngle(ew.bulletVelocity);

            float forwardWall = (float) (wallDistance(_enemyLocation, distance, ew.directAngle, ew.direction) / MEA);
            float reverseWall = (float) (wallDistance(_enemyLocation, distance, ew.directAngle, -ew.direction) / MEA);

            tsdirchange /= BFT;
            tsvchange /= BFT;

            ew.indexes = BufferManager.getIndexes(
                    lastLatVel,
                    advVel,
                    BFT,
                    tsdirchange,
                    accel,
                    tsvchange,
                    dl10,
                    forwardWall,
                    reverseWall
            );

            if (imaginaryWave != null
                    &&
                    Utils.isNear(ew.bulletVelocity, imaginaryWave.bulletVelocity)) {
                boolean same = true;
                for (int i = 0; i < ew.indexes.length && same; i++) {
                    int[] p = ew.indexes[i];
                    int[] q = imaginaryWave.indexes[i];
                    for (int j = 0; j < p.length && same; j++)
                        if (p[j] != q[j])
                            same = false;

                }


                if (same) {

                    ew.indexes = imaginaryWave.indexes;
                    ew.allStats = imaginaryWave.allStats;
                    ew.flattenerStats = imaginaryWave.flattenerStats;
                    // ew.bestBins = imaginaryWave.bestBins;
                    ew.scan = imaginaryWave.scan;
                }
            }
            if (ew.allStats == null) {

                ew.allStats = BufferManager.getStats(
                        statBuffers,
                        ew.indexes
                );
                if (flattenerEnabled)
                    ew.flattenerStats = BufferManager.getStats(
                            flattenerBuffers,
                            ew.indexes
                    );


                Scan s = new Scan();

                s.latVel = limit(0, lastLatVel / 8, 1);
                s.advVel = limit(0, advVel / 8, 1);
                s.dist = limit(0, BFT / (1200 / 11.0f), 1);
                s.forwardWall = limit(0, forwardWall, 1);
                s.reverseWall = limit(0, reverseWall, 1);
                s.lastVel = limit(0, prevLatVel / 8, 1);
                s.accel = (accel + 2) / 3;
                s.timeSinceDecel = limit(0, tsvchange / BFT, 1);
                s.timeSinceDirChange = limit(0, tsdirchange / BFT, 1);
                s.distLast20 = limit(0, dl10 / (100), 1);
                ew.scan = s;

            }
            // if(secondWave == null)
            surfStatsChanged = true;

            _enemyWaves.add(ew);
        }

    }

    public void endOfRound() {
        if (getRoundNum() + 1 == getNumRounds()) {
            System.out.println("Enemy damage: " + totalEnemyDamage);
            System.out.println("My damage:    " + totalMyDamage);
            System.out.println("Accumulated, weighted enemy hitrate % : " + (100 * weightedEnemyHitrate / weightedEnemyFirerate));
            statBuffers.clear();
            flattenerBuffers.clear();
            // visitScans.clear();
            System.gc();
        }
        System.out.println(Insulter.getInsult());
    }

    public void onHitRobot(HitRobotEvent e) {
        _oppEnergy -= 0.6;
    }


    public void onBulletHit(BulletHitEvent e) {
        double power = e.getBullet().getPower();
        double damage = 4 * power;
        if (power > 1)
            damage += 2 * (power - 1);

        if (enemyGunHeat < getGunCoolingRate() && getOthers() == 0) {

            double bulletPower = 2;
            if (_enemyWaves != null && _enemyWaves.size() > 0)
                bulletPower = (20 - ((EnemyWave) _enemyWaves.get(0)).bulletVelocity) / 3;
            bulletPower = Math.min(bulletPower, _oppEnergy);
            addWave(bulletPower);
            totalMyDamage += _oppEnergy - e.getEnergy();
        } else
            totalMyDamage += Math.min(_oppEnergy, damage);

        _oppEnergy -= Math.min(_oppEnergy, damage);

        dgun.onBulletHit(e);
    }

    public void onDeath(DeathEvent e) {
        Vector v = getAllEvents();
        Iterator i = v.iterator();
        while (i.hasNext()) {
            Object obj = i.next();
            if (obj instanceof HitByBulletEvent) {
                onHitByBullet((HitByBulletEvent) obj);
            }
        }
        endOfRound();
    }

    public void onWin(WinEvent e) {
        endOfRound();
    }

    public void onSkippedTurn(SkippedTurnEvent e) {
        System.out.println("SKIPPED TURN AT " + e.getTime());
        System.out.println("move time:" + moveTime);
        System.out.println("gun time:" + gunTime);
    }

    public void updateWaves() {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave) _enemyWaves.get(x);

            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            double myDistFromCenter = _myLocation.distance(ew.fireLocation);

            if (ew.imaginary && ew.distanceTraveled > ew.bulletVelocity) {
                _enemyWaves.remove(x);
                x--;
                continue;
            }

            if (ew.distanceTraveled > myDistFromCenter - ew.bulletVelocity
                    && !ew.flattenerLogged) {
                if (flattenerEnabled)
                    logFlattener(ew, _myLocation);
                ew.flattenerLogged = true;
                double botWidth = 2 * FastTrig.atan(25 / (ew.distanceTraveled - 18));
                double hitChance = botWidth / maxEscapeAngle(ew.bulletVelocity);
                weightedEnemyFirerate += 1 / hitChance;
                ew.bestBins = null;
            }


            if (ew.distanceTraveled >
                    myDistFromCenter + 50) {
                _enemyWaves.remove(x);
                x--;

            }
            // else if(Math.abs(ew.distanceTraveled - ew.bulletVelocity*2) < 0.001)
            //  && _myLocation.distance(_enemyLocation) < 300)
            // surfStatsChanged = true;
        }
    }

    public EnemyWave getClosestSurfableWave() {
        double closestDistance = Double.POSITIVE_INFINITY;
        EnemyWave surfWave = null;

        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave) _enemyWaves.get(x);
            double distance = _myLocation.distance(ew.fireLocation)
                    - ew.distanceTraveled;

            if (!ew.bulletGone && distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    // Given the EnemyWave that the bullet was on, and the point where we
    // were hit, calculate the index into our stat array for that factor.
    public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation)
                - ew.directAngle);
        double factor = Utils.normalRelativeAngle(offsetAngle)
                / maxEscapeAngle(ew.bulletVelocity) * ew.direction;

        return (int) Math.round(limit(0,
                (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
                BINS - 1));
    }

    // Given the EnemyWave that the bullet was on, and the point where we
    // were hit, update our stat array to reflect the danger in that area.
    public void logHit(EnemyWave ew, Point2D.Double targetLocation, boolean bulletHitBullet) {
        int index = getFactorIndex(ew, targetLocation);

        int min = Math.max(1, index - 20);
        int max = Math.min(BINS, index + 20);
        float[] morphProfile = new float[BINS];
        for (int i = 1; i < BINS; i++)
            morphProfile[i] = 1f / (sqr((index - i) / 3f) + 1f);
        // int newBins = 0;
        for (int i = 0, k = ew.allStats.size(); i < k; i++) {
            BufferManager.SingleBuffer sb = (BufferManager.SingleBuffer) ew.allStats.get(i);
            if (sb.bins == null) {
                sb.bins = new float[(int) Math.ceil(sb.rollingDepth) + 2];
                // newBins++;
            }
            for (int j = sb.bins.length - 1; j > 1; j--)
                sb.bins[j] = sb.bins[j - 1];
            sb.bins[1] = index;


            if (sb.bins[0] < 3)
                sb.bins[0] += 1;
        }


        for (int i = 0, k = _enemyWaves.size(); i < k; i++)
            ((EnemyWave) _enemyWaves.get(i)).bestBins = null;

        // if(newBins > 0){
        // System.out.print(newBins + " new movement buffer");
        // if(newBins > 1)
        // System.out.print("s");
        // System.out.println(" created at" + getTime());
        // }
        /*{
           double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation)
              - ew.directAngle);
           double factor = Utils.normalRelativeAngle(offsetAngle)
              / maxEscapeAngle(ew.bulletVelocity) * ew.direction;
           ew.scan.hitGF = (float)limit(-1,factor,1);
           hitScans.add(0,ew.scan);
        }*/

        surfStatsChanged = true;

    }

    public void logFlattener(EnemyWave ew, Point2D.Double targetLocation) {
        if (ew.flattenerStats == null)
            return;
        int index = getFactorIndex(ew, targetLocation);

        // int min = Math.max(1, index - 20);
        // int max = Math.min(BINS, index + 20);
        // float[] morphProfile = new float[BINS];
        // for(int i = 1; i< BINS; i++)
        // morphProfile[i] =  1f / (sqr((index - i)/3f) + 1f);
        //
        // int x = index;
        // int newBins = 0;
        for (int i = 0, k = ew.flattenerStats.size(); i < k; i++) {
            BufferManager.SingleBuffer sb = (BufferManager.SingleBuffer) ew.flattenerStats.get(i);

            if (sb.bins == null) {
                sb.bins = new float[(int) Math.ceil(sb.rollingDepth) + 2];
                // newBins++;
            }
            for (int j = sb.bins.length - 1; j > 1; j--)
                sb.bins[j] = sb.bins[j - 1];
            sb.bins[1] = index;

            if (sb.bins[0] < 3)
                sb.bins[0] += 1;

        }
        // if(newBins > 0)
        // System.out.println(newBins + " new flattener buffers created!");
        // if(flattenerEnabled){
        // for(int i = 0, k = _enemyWaves.size(); i < k; i++)
        // ((EnemyWave)_enemyWaves.get(i)).bestBins = null;
        //
        // surfStatsChanged = true;
        // }

        /*{double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation)
              - ew.directAngle);
           double factor = Utils.normalRelativeAngle(offsetAngle)
              / maxEscapeAngle(ew.bulletVelocity) * ew.direction;
           ew.scan.hitGF = (float)limit(-1,factor,1);

           visitScans.add(0,ew.scan);
           while(visitScans.size() > 1000)
              visitScans.remove(visitScans.size() - 1);
        }*/
    }

    // static double sqr(double d){
    // return d*d;
    // }
    static float sqr(float f) {
        return f * f;
    }

    // static double rollingAvg(double value, double newEntry, double depth, double weighting ) {
    // return (value * depth + newEntry * weighting)/(depth + weighting);
    // }
    // static float rollingAvg(float value, float newEntry, float depth, float weighting ) {
    // return (value * depth + newEntry * weighting)/(depth + weighting);
    // }
    //optimized version that doesn't have 'weighting' but takes a default as 1
    static float rollingAvg(float value, float newEntry, float depth) {
        return (value * depth + newEntry) / (depth + 1);
    }

    /**
     * Bell curve smoother... also know as gaussian smooth or normal distrabution
     * Credits go to Chase-san
     *
     * @param x Current Position
     * @param c Center (current index your adding)
     * @param w Width (number of binIndexes)
     * @return value of a bellcurve
     */
    // public static final double smoothingModifier = 5;
    // public static double bellcurve(int x, int c, int w) {
    // int diff = Math.abs(c - x);
    // double binsmooth = smoothingModifier/w;
    //
    // //I suppose technically you could also use Math.exp(-(binsmooth*binsmooth*diff*diff)/2.0);
    // return  Math.exp(-(binsmooth*binsmooth*diff*diff)/2.0);
    //    // return Math.pow(Math.E, -(binsmooth*binsmooth*diff*diff)/2.0);
    // }
    public void onBulletHitBullet(BulletHitBulletEvent e) {

        if (!_enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                    e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit the bullet
            hitWave = getCollisionWave(hitBulletLocation, e.getHitBullet().getPower());

            if (hitWave != null) {
                if (totalEnemyDamage > 0 || hitWave.distanceTraveled > hitWave.bulletVelocity * 2)
                    logHit(hitWave, hitBulletLocation, true);
                // We can remove this wave now, of course.
                hitWave.bulletGone = true;

            } else
                System.out.println("ERROR: DETECTED BULLET ON NONEXISTANT WAVE!");
        } else
            System.out.println("ERROR: DETECTED BULLET WITHOUT WAVES!");
        dgun.onBulletHitBullet(e);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        // If the _enemyWaves collection is empty, we must have missed the
        // detection of this wave somehow.
        if (!_enemyWaves.isEmpty()) {
            Bullet bullet = e.getBullet();
            Point2D.Double hitBulletLocation = new Point2D.Double(
                    e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit us.
            hitWave = getCollisionWave(_myLocation, e.getBullet().getPower());
            if (hitWave != null) {
                // hitBulletLocation =
                // project(hitWave.fireLocation, bullet.getHeading(), hitWave.distanceTraveled);

                logHit(hitWave, hitBulletLocation, false);

                // We can remove this wave now, of course.
                hitWave.bulletGone = true;
                if (_enemyLocation.distance(hitBulletLocation) > 200) {
                    double botWidth = 2 * FastTrig.atan(25 / (hitWave.distanceTraveled - 18));
                    double hitChance = botWidth / maxEscapeAngle(hitWave.bulletVelocity);
                    weightedEnemyHitrate += 1 / hitChance;
                }
            } else
                System.out.println("ERROR: DETECTED BULLET ON NONEXISTANT WAVE!");
        } else
            System.out.println("ERROR: DETECTED BULLET WITHOUT WAVES!");

        double power = e.getBullet().getPower();
        double damage = 4 * power;
        if (power > 1)
            damage += 2 * (power - 1);

        totalEnemyDamage += damage;

        _oppEnergy += power * 3;
    }

    EnemyWave getCollisionWave(Point2D.Double point, double bulletPower) {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave) _enemyWaves.get(x);
            double dist = ew.distanceTraveled - point.distance(ew.fireLocation);
            if (Math.abs(dist - 10) < 50
                    && Math.abs(bulletVelocity(bulletPower) - ew.bulletVelocity) < 0.01) {
                //  System.out.println("wave distance from bullet: " + dist);
                return ew;

            }
        }

        return null;
    }

    // CREDIT: mini sized predictor from Apollon, by rozu
    // http://robowiki.net?Apollon
    public ArrayList predictPositions(EnemyWave surfWave, double direction) {
        Point2D.Double predictedPosition = new Point2D.Double(getX(), getY());
        ArrayList positions = new ArrayList();

        double predictedVelocity = getVelocity();
        double predictedHeading = getHeadingRadians();
        double maxTurning, moveAngle, prefOffset, moveDir;
        Point2D.Double eLoc = _enemyLocation;

        int counter = 0; // number of ticks in the future
        boolean intercepted = false;

        do {
            //keeps a distance of 500
            //prefOffset = Math.PI/2;//(0.00154*surfWave.fireLocation.distance(predictedPosition) + 0.8);
            // double enemyDistance = _enemyLocation.distance(predictedPosition);

            // prefOffset = Math.PI/2;// - 1 + limit(350,enemyDistance, 800)/600;

            double absBearing = absoluteBearing(
                    eLoc = project(eLoc, lastScan.getHeadingRadians(), lastScan.getVelocity())
                    // surfWave.fireLocation
                    ,
                    predictedPosition
                    // _myLocation
            );
            prefOffset = Math.PI / 2 - 1 + limit(50, eLoc.distance(predictedPosition), 650) / 650;
            // }

            moveAngle =
                    wallSmoothing(predictedPosition, absBearing + (direction * prefOffset), direction)
                            - predictedHeading;
            moveDir = 1;

            if (FastTrig.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // maxTurning is built in like this, you can't turn more then this in one tick
            maxTurning = (Math.PI / 18) - (Math.PI / 240) * Math.abs(predictedVelocity);

            predictedHeading = Utils.normalRelativeAngle(predictedHeading
                    + limit(-maxTurning, moveAngle, maxTurning));

            // this one is nice ;). if predictedVelocity and moveDir have
            // different signs you want to brake down
            // otherwise you want to accelerate (look at the factor "2")
            // predictedVelocity += (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);

            double velAddition = (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);


            predictedVelocity = limit(-8, predictedVelocity + velAddition, 8);

            // calculate the new predicted position
            predictedPosition = project(predictedPosition, predictedHeading, predictedVelocity);

            PlaceTime pt = new PlaceTime();
            pt.place = predictedPosition;
            pt.time = (long) ((surfWave.fireLocation.distance(pt.place) - surfWave.distanceTraveled - surfWave.bulletVelocity) / surfWave.bulletVelocity) + getTime();

            positions.add(pt);

            counter++;

            if (predictedPosition.distance(surfWave.fireLocation) + surfWave.bulletVelocity * 3
                    <
                    surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)

                    ) {
                intercepted = true;

            }


        } while ((!intercepted || predictedPosition.distance(_myLocation) < 40) && counter < 500);

        return positions;
    }

    public void getBins(EnemyWave wave) {
        wave.bestBins = new float[BINS];

        for (int i = 0, k = wave.allStats.size(); i < k; i++) {
            BufferManager.SingleBuffer sb = (BufferManager.SingleBuffer) wave.allStats.get(i);
            if (sb.bins == null)
                continue;
            float multFactor = sb.bins[0] * sb.weight;
            if (multFactor != 0.0f) {
                float roll = 1 - 1 / (sb.rollingDepth + 1);
                for (int j = 1; j < sb.bins.length; j++) {
                    multFactor *= roll;
                    wave.bestBins[(int) sb.bins[j]] += multFactor;
                }
            }
        }
        heightNormalize(wave.bestBins);

        /*
                       float[] dcBins = new float[BINS];
                       int temp = 0;
                       {
                          float[] hitIndexes = new float[BINS];
                    m      for(int i = 0, k = Math.min(hitScans.size(),1000); i < k; i++){
                             Scan s;
                             float weight = (k - i)/(surfWave.scan.distance(s = (Scan)hitScans.get(i)));
                             int centerHit = (int)Math.round(MIDDLE_BIN*s.hitGF) + MIDDLE_BIN;
                             hitIndexes[centerHit] += weight;
                          }
                          for(int j = 0; j < BINS; j++)
                             for(int i = Math.max(0,j - 20), k = Math.min(BINS,j + 20); i < k; i++)
                                dcBins[i] += hitIndexes[j] / (((temp = i - j)*temp)/9f + 1);
                       }
                       normalize(dcBins);
                        // */

        if (flattenerEnabled && wave.flattenerStats != null) {

            float[] flattenerBins = new float[BINS];
            for (int i = 0, k = wave.flattenerStats.size(); i < k; i++) {
                BufferManager.SingleBuffer sb = (BufferManager.SingleBuffer) wave.flattenerStats.get(i);
                if (sb.bins == null)
                    continue;
                float multFactor = sb.bins[0] * sb.weight;
                if (multFactor != 0.0f) {
                    float roll = 1 - 1 / (sb.rollingDepth + 1);
                    for (int j = 1; j < sb.bins.length; j++) {
                        multFactor *= roll;
                        flattenerBins[(int) sb.bins[j]] += multFactor;
                    }
                }
            }
            heightNormalize(flattenerBins);

            /*
        float[] dcFlattenerBins = new float[BINS];
        {
           float[] hitIndexes = new float[BINS];


           for(int i = 0, k = visitScans.size(); i < k; i++){
              Scan s;
              float weight = (k - i)/(surfWave.scan.testDistance(s = (Scan)visitScans.get(i)));
              int centerHit = (int)Math.round(MIDDLE_BIN*s.hitGF) + MIDDLE_BIN;
              hitIndexes[centerHit] += weight;
           }

           for(int j = 0; j < BINS; j++)
              for(int i = Math.max(0,j - 20), k = Math.min(BINS,j + 20); i < k; i++)
                 dcFlattenerBins[i] += hitIndexes[j] / ((temp = i - j)*temp/9f + 1);

        }
        normalize(dcFlattenerBins);
        for(int i = 0; i < BINS; i++)
           surfWave.bestBins[i] = (surfWave.bestBins[i] + dcBins[i] + flattenerBins[i] + dcFlattenerBins[i])/4;
           // */
            for (int i = 1; i < BINS; i++)
                wave.bestBins[i] = (wave.bestBins[i] + flattenerBins[i]) * 0.5f;

        }
        //Smooth!
        float[] profile = new float[BINS * 2];
        for (int i = 1; i < BINS * 2; i++)
            profile[i] = 1f / (sqr((BINS - i) / 3f) + 1f);

        final int width = BINS / 2;
        float[] smoothBins = new float[BINS];
        for (int i = 1; i < BINS; i++)
            if (wave.bestBins[i] != 0.0)
                for (int j = 1, k = BINS; j < k; j++)
                    smoothBins[j] += wave.bestBins[i] * profile[j - i + BINS];
        wave.bestBins = smoothBins;

        // else
        // for(int i = 0; i < BINS; i++)
        // surfWave.bestBins[i] = (surfWave.bestBins[i] + dcBins[i])/2;
    }

    public PlaceTime getBestPoint(EnemyWave surfWave, EnemyWave nextWave, EnemyWave nnWave) {

        if (surfWave.bestBins == null) {
            getBins(surfWave);
            surfStatsChanged = true;
        }
        if (nextWave != null && nextWave.bestBins == null) {
            getBins(nextWave);
            surfStatsChanged = true;
        }
        if (nnWave != null && nnWave.bestBins == null) {
            getBins(nnWave);
        }

        if (nextWave != null && (nextWave.possPoints == null || surfStatsChanged)) {
            nextWave.possPoints = predictPositions(nextWave, 1);

            ArrayList reverse = predictPositions(nextWave, -1);
            nextWave.possPoints.ensureCapacity(reverse.size() + nextWave.possPoints.size());
            for (int i = 0; i < reverse.size(); i++)
                nextWave.possPoints.add(0, reverse.get(i));


            for (int i = 0; i < nextWave.possPoints.size(); i++) {
                PlaceTime pt = (PlaceTime) (nextWave.possPoints.get(i));
                pt.danger = getDanger(nextWave, pt);
            }

            nextWave.weight = getWaveWeight(nextWave);
            surfStatsChanged = true;
        }

        if (nnWave != null && (nnWave.possPoints == null || surfStatsChanged)) {
            nnWave.possPoints = predictPositions(nnWave, 1);

            ArrayList reverse = predictPositions(nnWave, -1);
            nnWave.possPoints.ensureCapacity(reverse.size() + nnWave.possPoints.size());
            for (int i = 0; i < reverse.size(); i++)
                nnWave.possPoints.add(0, reverse.get(i));

        }


        if (surfWave.safePoints == null || surfWave.safestPoint == null || surfStatsChanged) {


            surfWave.weight = getWaveWeight(surfWave);

            double vel = getVelocity();
            if (surfWave.safePoints == null || surfStatsChanged) {
                surfWave.safePoints = predictPositions(surfWave, lateralDirection);
                ArrayList reversePoints = predictPositions(surfWave, -lateralDirection);
                surfWave.safePoints.ensureCapacity(reversePoints.size() + surfWave.safePoints.size());
                for (int i = 0; i < reversePoints.size(); i++)
                    surfWave.safePoints.add(0, reversePoints.get(i));

            }


            PredictionStatus now = new PredictionStatus();
            now.finalHeading = getHeadingRadians();
            now.finalVelocity = getVelocity();
            now.distanceRemaining = getDistanceRemaining();
            now.time = getTime();
            now.endPoint = _myLocation;
            if (now.distanceRemaining < 0) {
                now.finalVelocity = -now.finalVelocity;
                now.distanceRemaining = -now.distanceRemaining;
                now.finalHeading = Utils.normalAbsoluteAngle(now.finalHeading + Math.PI);
            }

            ArrayList points = getPredictions(surfWave, surfWave.safePoints, now);

            ArrayList bestNextPoints = null;

            float minDanger = Float.POSITIVE_INFINITY;
            for (int i = 0, k = points.size(); i < k; i++) {
                PlaceTime pt = (PlaceTime) (points.get(i));
                pt.danger = getDanger(surfWave, pt);

                if (pt.danger < minDanger) {
                    surfWave.safestPoint = pt;
                    minDanger = pt.danger;
                }
            }

            if (nextWave != null) {
                minDanger = Float.POSITIVE_INFINITY;

                Collections.sort(points);//sorts according to danger

                for (int i = 0, k = points.size(); i < k; i++) {
                    PlaceTime pt = (PlaceTime) (points.get(i));
                    if (pt.danger > minDanger)
                        break;//sorted so all after here also to big to be possible
                    if (pt.predictionStatus == null)
                        pt.predictionStatus = futureStatus(
                                _myLocation,
                                pt.place,
                                now.finalVelocity,
                                now.finalHeading,
                                now.time,
                                surfWave);
                    //  (long)((surfWave.fireLocation.distance(pt.place) - surfWave.distanceTraveled - surfWave.bulletVelocity)/surfWave.bulletVelocity) + getTime();
                    float minSecondDanger = Float.POSITIVE_INFINITY;
                    ArrayList nextPoints = getPredictions(nextWave, nextWave.possPoints, pt.predictionStatus);
                    PlaceTime safePt = null;
                    for (int j = 0, l = nextPoints.size(); j < l; j++) {
                        PlaceTime nextPt = (PlaceTime) nextPoints.get(j);
                        if (nextPt.predictionStatus == null) {
                            if (nextPt.danger < minSecondDanger) {
                                minSecondDanger = nextPt.danger;
                                safePt = nextPt;
                            }
                        } else {
                            float d = getDanger(nextWave, nextPt);
                            if (d < minSecondDanger) {
                                minSecondDanger = d;
                                safePt = nextPt;
                            }
                        }
                    }
                    if (minSecondDanger != Float.POSITIVE_INFINITY)
                        pt.danger += minSecondDanger;


                    if (pt.danger < minDanger) {
                        surfWave.safestPoint = pt;
                        nextWave.safestPoint = safePt;
                        minDanger = pt.danger;
                        bestNextPoints = nextPoints;
                    }
                }
                if (nextWave != null && nextWave.safestPoint != null && nextWave.safestPoint.predictionStatus == null)
                    nextWave.safestPoint.predictionStatus = futureStatus(
                            surfWave.safestPoint.place,
                            nextWave.safestPoint.place,
                            surfWave.safestPoint.predictionStatus.finalVelocity,
                            surfWave.safestPoint.predictionStatus.finalHeading,
                            surfWave.safestPoint.time,
                            nextWave);


                if (nnWave != null
                        && nnWave.safestPoint != null
                        && nnWave.safestPoint.predictionStatus != null
                        && nextWave != null
                        && nextWave.safestPoint != null
                        && nextWave.safestPoint.predictionStatus != null
                        && nnWave.possPoints != null
                        && nnWave.possPoints.size() > 0) {

                    ArrayList nextPoints = getPredictions(
                            nnWave,
                            nnWave.possPoints,
                            nextWave.safestPoint.predictionStatus);

                    PlaceTime safePt = null;
                    float minnnDanger = Float.POSITIVE_INFINITY;
                    for (int j = 0, l = nextPoints.size(); j < l; j++) {
                        PlaceTime nextPt = (PlaceTime) nextPoints.get(j);

                        float d = getDanger(nnWave, nextPt);
                        if (d < minnnDanger) {
                            minnnDanger = d;
                            safePt = nextPt;
                        }
                    }
                    nnWave.safestPoint = safePt;
                    if (nnWave.safestPoint != null && nnWave.safestPoint.predictionStatus == null)
                        nnWave.safestPoint.predictionStatus = futureStatus(
                                nextWave.safestPoint.place,
                                nnWave.safestPoint.place,
                                nextWave.safestPoint.predictionStatus.finalVelocity,
                                nextWave.safestPoint.predictionStatus.finalHeading,
                                nextWave.safestPoint.time,
                                nnWave);
                }

            }

            surfStatsChanged = false;

            if (painting) {
                firstPointsPainting = points;
                nextPointsPainting = bestNextPoints;
            }

        }

        return surfWave.safestPoint;

    }

    public float getWaveWeight(EnemyWave wave) {
        // double tta = (wave.fireLocation.distance(_myLocation) - wave.distanceTraveled)/wave.bulletVelocity;

        // double relevance = Math.pow(0.88,tta/2);
        // double relevance = tta*tta - 200*tta + 10000;
        // double relevance = Math.pow(0.96,tta);
        double bp = (20 - wave.bulletVelocity) / 3;


        return (float) ((bp * 4 + Math.max(0, bp - 1) * 2));

    }

    public float getDanger(EnemyWave wave, PlaceTime pt) {

        if (!_fieldRect.contains(pt.place))
            return Float.POSITIVE_INFINITY;


        Point2D.Double startPlace;


        if (pt.predictionStatus != null) {
            // if(pt.predictionStatus.debug)
            // return Float.POSITIVE_INFINITY;
            // int waveCoverTime = (int)Math.ceil(36/wave.bulletVelocity);
            // double tempVel = pt.predictionStatus.finalVelocity;
            // int topSpeedTime = (int)Math.floor((pt.predictionStatus.distanceRemaining - 20)/tempVel);
            // double additionBotWidth = Math.min(topSpeedTime, waveCoverTime)*tempVel;
            // waveCoverTime -= topSpeedTime;

            // additionBotWidth += Math.min((2*0.5)*waveCoverTime*(waveCoverTime+1),
            // (2*0.5*0.5)*tempVel*(0.5*tempVel+1));

            // while(waveCoverTime-- > 0 && tempVel != 0.0){
            // additionBotWidth += tempVel;
            // if(pt.predictionStatus.distanceRemaining - additionBotWidth <= decelDistance(tempVel))
            // tempVel = Math.max(0,tempVel - 2);
            // }

            // startPlace = project(pt.predictionStatus.endPoint, pt.predictionStatus.finalHeading, additionBotWidth*0.5);
            // pt.predictionStatus.time = (long)((wave.fireLocation.distance(pt.predictionStatus.endPoint) - wave.distanceTraveled - wave.bulletVelocity)/wave.bulletVelocity) + getTime();
            startPlace = pt.predictionStatus.endPoint;
        } else
            startPlace = pt.place;
        // double angle = absoluteBearing(wave.fireLocation, startPlace);
        // botWidthPixels *= Math.max(Math.abs(Math.sin(angle + Math.PI/4)),Math.abs(Math.cos(angle + Math.PI/4)));
        //if(pt.predictionStatus != null)
        //  botWidthPixels += additionBotWidth*Math.abs(Math.sin(angle - pt.predictionStatus.finalHeading));
        int index = getFactorIndex(wave, startPlace);
        //the maximum possible width - sqrt(40*40 + 40*40) = 25 - diagonal
        // double angle = surfWave.directAngle + (index - MIDDLE_BIN)*maxEscapeAngle(surfWave.bulletVelocity)/MIDDLE_BIN;
        double botWidthAtEnd = 40 / (wave.fireLocation.distance(startPlace) - 34);

        double inv_binWidth = MIDDLE_BIN / maxEscapeAngle(wave.bulletVelocity);
        int botBinWidthAtEnd = (int) Math.round(botWidthAtEnd * inv_binWidth);


        float thisDanger = getAverageDanger(wave.bestBins, index, botBinWidthAtEnd);
        thisDanger *= botWidthAtEnd * botWidthAtEnd;
        double waveCenterDistHere = wave.fireLocation.distance(startPlace);

        thisDanger /= Math.cbrt(Math.min(_enemyLocation.distance(startPlace) - 34, waveCenterDistHere));

        float tta = (float) ((wave.fireLocation.distance(startPlace) - wave.distanceTraveled) / wave.bulletVelocity);

        // double relevance = Math.pow(0.88,tta/2);
        float relevance = tta * tta - 200 * tta + 10000;
        // double relevance = Math.pow(0.96,tta);

        return thisDanger * relevance * wave.weight;

    }

    public ArrayList getPredictions(EnemyWave wave, ArrayList points, PredictionStatus start) {
        int max = points.size() + 1;
        int min = -2;
        long waveHitTime = (long) ((wave.fireLocation.distance(start.endPoint) - wave.distanceTraveled - wave.bulletVelocity) / wave.bulletVelocity) + start.time;
        ArrayList likelyPoints = new ArrayList(max - min + 1);

        while ((max -= 2) > 0) {
            PlaceTime guessPT = (PlaceTime) points.get(max);
            PredictionStatus futureStatus = futureStatus(
                    start.endPoint,
                    guessPT.place,
                    start.finalVelocity,
                    start.finalHeading,
                    start.time, wave);
            if (futureStatus.distanceRemaining < 20 + 8 + 8 + 8) {
                guessPT = clone(guessPT);
                guessPT.time = futureStatus.time;
                guessPT.predictionStatus = futureStatus;
                likelyPoints.add(guessPT);
                break;
            }
        }

        while ((min += 2) < max) {
            PlaceTime guessPT = (PlaceTime) points.get(min);
            PredictionStatus futureStatus = futureStatus(
                    start.endPoint,
                    guessPT.place,
                    start.finalVelocity,
                    start.finalHeading,
                    start.time, wave);
            if (futureStatus.distanceRemaining < 20 + 8 + 8 + 8) {
                guessPT = clone(guessPT);
                guessPT.time = futureStatus.time;
                guessPT.predictionStatus = futureStatus;
                likelyPoints.add(guessPT);
                break;
            }
        }


        int maxStop = max - 1;
        for (; maxStop > min; maxStop--) {
            PlaceTime guessPT = (PlaceTime) points.get(maxStop);
            PredictionStatus futureStatus = futureStatus(
                    start.endPoint,
                    guessPT.place,
                    start.finalVelocity,
                    start.finalHeading,
                    start.time, wave);

            guessPT = clone(guessPT);
            guessPT.time = futureStatus.time;
            guessPT.predictionStatus = futureStatus;
            // if(guessPT.time != futureStatus.time)
            // futureStatus.debug = true;
            //  System.out.println("guessPt:" + guessPT.time + "  future:" + futureStatus.time);
            likelyPoints.add(guessPT);

            if (guessPT.predictionStatus.finalVelocity == 0.0
                    && guessPT.predictionStatus.distanceRemaining == 0.0)
                break;
        }

        int minStop = min + 1;
        for (; minStop < maxStop; minStop++) {
            PlaceTime guessPT = (PlaceTime) points.get(minStop);
            PredictionStatus futureStatus = futureStatus(
                    start.endPoint,
                    guessPT.place,
                    start.finalVelocity,
                    start.finalHeading,
                    start.time, wave);

            guessPT = clone(guessPT);
            guessPT.time = futureStatus.time;
            guessPT.predictionStatus = futureStatus;
            likelyPoints.add(guessPT);

            if (guessPT.predictionStatus.finalVelocity == 0.0
                    && guessPT.predictionStatus.distanceRemaining == 0.0)
                break;
        }

        for (int i = minStop; i <= maxStop; i++) {
            PlaceTime guessPT = ((PlaceTime) points.get(i));
            guessPT.predictionStatus = null;
            likelyPoints.add(clone(guessPT));
        }

        return likelyPoints;

    }

    static PlaceTime clone(PlaceTime pt) {
        PlaceTime p = new PlaceTime();
        p.predictionStatus = pt.predictionStatus;
        p.place = pt.place;
        p.time = pt.time;
        p.danger = pt.danger;
        return p;

    }

    static class PredictionStatus {
        double finalHeading, finalVelocity, distanceRemaining;
        long time;
        Point2D.Double endPoint;
        boolean debug;
    }

    //3 optimized methods from the new robocode engine
    private static double getNewVelocity(double velocity, double distance) {
        final double goalVel = Math.min(getMaxVelocity(distance), 8);

        if (velocity >= 0)
            return limit(velocity - 2,
                    goalVel, velocity + 1);

        return limit(velocity - 1,
                goalVel, velocity + maxDecel(-velocity));
    }

    final static double getMaxVelocity(double distance) {
        final double decelTime = Math.max(1, Math.ceil(
                //sum of 0... decelTime, solving for decelTime
                //using quadratic formula, then simplified a lot
                Math.sqrt(distance + 1) - 0.5));

        final double decelDist = (decelTime) * (decelTime - 1);
        // sum of 0..(decelTime-1)
        // * Rules.DECELERATION*0.5;

        return ((decelTime - 1) * 2) + ((distance - decelDist) / decelTime);
    }

    private static final double maxDecel(double speed) {
        return limit(1, speed * 0.5 + 1, 2);
    }

    public static PredictionStatus futureStatus(Point2D.Double fromLocation, Point2D.Double toLocation, double initialVelocity, double initialHeading, long currentTime, EnemyWave wave) {

        double wantedHeading = absoluteBearing(fromLocation, toLocation);
        double velocity = initialVelocity;
        double distanceRemaining = fromLocation.distance(toLocation);
        ;
        long time = currentTime;
        double theta = Utils.normalRelativeAngle(wantedHeading - initialHeading);
        double offsetSign = Math.signum(theta);
        theta = Math.abs(theta);

        PredictionStatus status = new PredictionStatus();
        status.finalHeading = initialHeading;

        if (theta > Math.PI / 2) {
            theta = Math.PI - theta;
            velocity = -velocity;
            offsetSign = -offsetSign;
            status.finalHeading = Utils.normalAbsoluteAngle(initialHeading + Math.PI);
        }

        double waveCenterDist = wave.fireLocation.distance(fromLocation);
        double waveBearing = absoluteBearing(fromLocation, wave.fireLocation);
        double waveOffset = Utils.normalAbsoluteAngle(waveBearing - status.finalHeading);
        double waveOffsetSign = 1;
        if (waveOffset > Math.PI) {
            waveOffset = 2 * Math.PI - waveOffset;
            waveOffsetSign = -1;
        }

        do {
            double deltaHeading = (theta - (theta = Math.max(0, theta - (Math.PI / 18) + (Math.PI / 240) * Math.abs(velocity)))) * offsetSign;
            status.finalHeading += deltaHeading;
            waveOffset -= deltaHeading * waveOffsetSign;
            if (waveOffset > Math.PI) {
                waveOffset = 2 * Math.PI - waveOffset;
                waveOffsetSign = -waveOffsetSign;
            }
            // velocity = getNewVelocity(velocity, distanceRemaining);

            // 	/*
            if (velocity >= 0 && distanceRemaining >= decelDistance(velocity))
                velocity = Math.min(velocity + 1, 8);
            else
                velocity = limit(-1.9999999999, Math.abs(velocity) - Math.min(Math.max(Math.abs(velocity), distanceRemaining), 2), 6) * (velocity < 0 ? -1 : 1);
            //    */

            if (theta == 0)
                distanceRemaining -= velocity;
            else { //rule of cosines
                double oldDistRemSq = distanceRemaining * distanceRemaining;
                double distRemSq = velocity * velocity + oldDistRemSq - 2 * velocity * distanceRemaining * FastTrig.cos(theta);
                if (distRemSq <= 0.1)
                    distanceRemaining = 0;
                else {
                    distanceRemaining = Math.sqrt(distRemSq);

                    double acosVal = (velocity * velocity + distRemSq - oldDistRemSq) / (2 * velocity * distanceRemaining);
                    if (acosVal < -1)
                        acosVal = -1;
                    else if (acosVal > 1)
                        acosVal = 1;
                    theta = Math.PI - FastTrig.acos(acosVal);
                    if (theta > Math.PI / 2) {//in case of overshoot
                        theta = Math.PI - theta;
                        velocity = -velocity;
                        offsetSign = -offsetSign;
                        status.finalHeading += Math.PI;
                        waveOffset = Math.PI - waveOffset;
                        waveOffsetSign = -waveOffsetSign;

                    }
                }
            }

            if (velocity > 0.01 || velocity < -0.01) {

                double newWaveDSq = (velocity * velocity + waveCenterDist * waveCenterDist -
                        2 * velocity * waveCenterDist * FastTrig.cos(waveOffset));
                double newWaveD = Math.sqrt(newWaveDSq);


                double acosVal = (waveCenterDist * waveCenterDist
                        - velocity * velocity - newWaveDSq) / (2 * velocity * newWaveD);

                if (acosVal < -1)
                    acosVal = -1;
                else if (acosVal > 1)
                    acosVal = 1;


                double newWaveOffset = FastTrig.acos(acosVal);

                double alpha = newWaveOffset - waveOffset;

                waveBearing += alpha * waveOffsetSign;


                waveOffset = newWaveOffset;
                waveCenterDist = newWaveD;
            }


            time++;

        } while (wave.bulletVelocity * (time + 1 - wave.fireTime) < waveCenterDist
                && !(distanceRemaining == 0.0 && velocity == 0));

        // while(wave.bulletVelocity*(time + 2 - wave.fireTime) < waveCenterDist)
        // time++;
        //if(distanceRemaining == 0.0 && velocity == 0)
        time = (long) (waveCenterDist / wave.bulletVelocity) + wave.fireTime - 1;


        status.distanceRemaining = Math.abs(distanceRemaining);
        status.finalVelocity = velocity;
        status.finalHeading = Utils.normalAbsoluteAngle(status.finalHeading);
        // Point2D.Double endPoint = project(toLocation,status.finalHeading - theta*offsetSign,-distanceRemaining);
        status.time = time;

        status.endPoint = project(wave.fireLocation, waveBearing, -waveCenterDist);
        // double realCDist = wave.fireLocation.distance(status.endPoint);
        // System.out.println("realCDist:" + realCDist + "  waveCenterDist:" + waveCenterDist);
        // if(endPoint.distanceSq(status.endPoint) > 1)
        // status.debug = true;

        return status;
    }

    public static void heightNormalize(float[] bins) {
        float max = 0;
        for (int i = 1; i < bins.length; i++)
            if (bins[i] > max)
                max = bins[i];
        max = 1 / max;
        if (max != 0)
            for (int i = 1; i < bins.length; i++)
                bins[i] *= max;

    }

    public static void areaNormalize(float[] bins) {
        float total = 0;
        for (int i = 1; i < bins.length; i++)
            total += bins[i];
        total = 1 / total;
        if (total != 0) {
            for (int i = 1; i < bins.length; i++)
                bins[i] *= total;
        }
    }

    public float getAverageDanger(float[] bins, int index, int botBinWidth) {
        botBinWidth = (int) limit(2, botBinWidth, BINS - 1);
        float totalDanger = 0;

        int minIndex = Math.max(1, index - botBinWidth / 2);
        int maxIndex = Math.min(BINS - 1, index + botBinWidth / 2) + 1;
        for (int i = minIndex; i < maxIndex; i++)
            totalDanger += bins[i];

        return totalDanger / (maxIndex - minIndex);

    }

    public void doSurfing() {
        mainWave = getClosestSurfableWave();
        boolean surf = false;
        if (mainWave != null) {
            _enemyWaves.remove(mainWave);
            secondWave = getClosestSurfableWave();

            if (secondWave != null) {
                _enemyWaves.remove(secondWave);
                thirdWave = getClosestSurfableWave();
                _enemyWaves.add(secondWave);
            }

            _enemyWaves.add(mainWave);
            PlaceTime bestPoint = getBestPoint(mainWave, secondWave, thirdWave);
            if (bestPoint != null && bestPoint.place != null) {
                surf = true;
                goTo(bestPoint, mainWave);
                direction = -lateralDirection;
            } else {
                mainWave = secondWave = null;
            }
        } else
            secondWave = null;
        if (!surf) {
            double distance = _enemyLocation.distanceSq(_myLocation);
            double absBearing = absoluteBearing(_myLocation, _enemyLocation);
            double headingRadians = getHeadingRadians();
            double stick = limit(121, distance, 160);
            double goAngle, revGoAngle, revOffset;
            double offset = revOffset = Math.max(Math.PI / 3 + 0.021, Math.PI / 2 + 1 - limit(0.2, distance / (400 * 400), 1.2));
            int count = 0;
            Point2D.Double endPoint, revEndPoint;

            while (!_fieldRect.
                    contains(endPoint = project(_myLocation, goAngle = absBearing + direction * (offset -= 0.02), stick))
                    && count++ < 50) ;

            count = 0;

            while (!_fieldRect.
                    contains(revEndPoint = project(_myLocation, revGoAngle = absBearing - direction * (revOffset -= 0.02), stick))
                    && count++ < 50) ;

            if (offset < revOffset) {
                direction = -direction;
                goAngle = revGoAngle;
            }


            setAhead(50 * FastTrig.cos(goAngle -= headingRadians));
            setTurnRightRadians(FastTrig.tan(goAngle));

        } else {

        }
    }

    private void goTo(PlaceTime pt, EnemyWave surfWave) {
        Point2D.Double place = pt.place;
        lastGoToPoint = place;
        double distance = _myLocation.distance(place);
        double dir = 1;
        double angle = Utils.normalRelativeAngle(absoluteBearing(_myLocation, place) - getHeadingRadians());
        if (Math.abs(angle) > Math.PI / 2) {
            dir = -1;
            if (angle > 0) {
                angle -= Math.PI;
            } else {
                angle += Math.PI;
            }
        }
        if (-1 < distance && distance < 1)
            angle = 0;
        if (flattenerEnabled && pt.predictionStatus != null && pt.predictionStatus.distanceRemaining == 0.0) {
            double myVel = getVelocity();
            double heading = getHeadingRadians();
            if (myVel < 0) {
                myVel = -myVel;
                heading += Math.PI;
            }
            double maxTurn = Math.PI / 18 - (Math.PI / 240) * myVel;
            heading += limit(-maxTurn, angle, maxTurn);

            double nextVel = limit(0, myVel - 2, 6);

            Point2D.Double nextLocation = project(_myLocation, heading, nextVel);
            PredictionStatus stillOption =
                    futureStatus(nextLocation, pt.place, nextVel, heading, getTime() + 1, surfWave);
            if (stillOption.distanceRemaining == 0.0) {
                distance = 0;
                //angle = 0;
            }
        }

        setTurnRightRadians(angle);
        setAhead(distance * dir);

    }

    static double decelDistance(double vel) {

        int intVel = (int) Math.ceil(vel);
        switch (intVel) {
            case 8:
                return 6 + 4 + 2;
            case 7:
                return 5 + 3 + 1;
            case 6:
                return 4 + 2;
            case 5:
                return 3 + 1;
            case 4:
                return 2;
            case 3:
                return 1;
            case 2:
                // return 2;
            case 1:
                // return 1;
            case 0:
                return 0;

            default:
                return 6 + 4 + 2;


        }

        // double dist = 0;
        // while(vel > 0){
        // vel = limit(0, vel - 2, 8);
        // dist += vel;
        // }
        // return dist;
    }


    private double absoluteBearing(Point2D source, Point2D target) {
        return FastTrig.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    // This can be defined as an inner class if you want.
    static class EnemyWave {
        Point2D.Double fireLocation;
        long fireTime;
        double bulletVelocity, directAngle, distanceTraveled;
        int direction;
        ArrayList allStats;
        ArrayList flattenerStats;
        boolean flattenerLogged = false;
        float[] bestBins;
        ArrayList safePoints;
        PlaceTime safestPoint;

        int[][] indexes;

        boolean bulletGone = false;
        boolean imaginary = false;

        float weight = 0;

        Scan scan;

        //used for second-wave surfing
        ArrayList possPoints;
        PlaceTime possSafePT;

        public EnemyWave() {
        }
    }

    static class PlaceTime implements Comparable {
        Point2D.Double place;
        long time;

        PredictionStatus predictionStatus;

        //speed optimizations - don't try this at home, kids!
        float danger;

        public int compareTo(Object o) {
            return (int) Math.signum(danger - ((PlaceTime) o).danger);
        }
    }


    //non-iterative wallsmoothing by Simonton - to save your CPUs
    public static final double HALF_PI = Math.PI / 2;
    public static final double WALKING_STICK = 160;
    public static final double WALL_MARGIN = 19;
    public static final double S = WALL_MARGIN;
    public static final double W = WALL_MARGIN;
    public static final double N = 600 - WALL_MARGIN;
    public static final double E = 800 - WALL_MARGIN;

    // angle = the angle you'd like to go if there weren't any walls
    // oDir  =  1 if you are currently orbiting the enemy clockwise
    //         -1 if you are currently orbiting the enemy counter-clockwise
    // returns the angle you should travel to avoid walls
    double wallSmoothing(Point2D.Double botLocation, double angle, double oDir) {
        // if(!_fieldRect.contains(project(botLocation,angle + Math.PI*(oDir + 1),WALKING_STICK))){
        angle = smoothWest(N - botLocation.y, angle - HALF_PI, oDir) + HALF_PI;
        angle = smoothWest(E - botLocation.x, angle + Math.PI, oDir) - Math.PI;
        angle = smoothWest(botLocation.y - S, angle + HALF_PI, oDir) - HALF_PI;
        angle = smoothWest(botLocation.x - W, angle, oDir);

        // for bots that could calculate an angle that is pointing pretty far
        // into a corner, these three lines may be necessary when travelling
        // counter-clockwise (since the smoothing above may have moved the
        // walking stick into another wall)
        angle = smoothWest(botLocation.y - S, angle + HALF_PI, oDir) - HALF_PI;
        angle = smoothWest(E - botLocation.x, angle + Math.PI, oDir) - Math.PI;
        angle = smoothWest(N - botLocation.y, angle - HALF_PI, oDir) + HALF_PI;
        // }
        return angle;
    }

    // smooths agains the west wall
    static double smoothWest(double dist, double angle, double oDir) {
        if (dist < -WALKING_STICK * FastTrig.sin(angle)) {
            return FastTrig.acos(oDir * dist / WALKING_STICK) - oDir * HALF_PI;
        }
        return angle;
    }

    //CREDIT: MORE STUFF BY SIMONTON =)

    // eDist  = the distance from you to the enemy
    // eAngle = the absolute angle from you to the enemy
    // oDir   =  1 for the clockwise orbit distance
    //          -1 for the counter-clockwise orbit distance
    // returns: the positive orbital distance (in radians) the enemy can travel
    //          before hitting a wall (possibly infinity).
    double wallDistance(Point2D.Double sourceLocation, double eDist, double eAngle, int oDir) {
        return Math.min(Math.min(Math.min(
                distanceWest(N - sourceLocation.getY(), eDist, eAngle - HALF_PI, oDir),
                distanceWest(E - sourceLocation.getX(), eDist, eAngle + Math.PI, oDir)),
                distanceWest(sourceLocation.getY() - S, eDist, eAngle + HALF_PI, oDir)),
                distanceWest(sourceLocation.getX() - W, eDist, eAngle, oDir));
    }

    double distanceWest(double toWall, double eDist, double eAngle, int oDir) {
        if (eDist <= toWall) {
            return Double.POSITIVE_INFINITY;
        }
        double wallAngle = FastTrig.acos(-oDir * toWall / eDist) + oDir * HALF_PI;
        return Utils.normalAbsoluteAngle(oDir * (wallAngle - eAngle));
    }


    // CREDIT: from CassiusClay, by PEZ
    //   - returns point length away from sourceLocation, at angle
    // robowiki.net?CassiusClay
    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + FastTrig.sin(angle) * length,
                sourceLocation.y + FastTrig.cos(angle) * length);
    }

    // got this from RaikoMicro, by Jamougha, but I think it's used by many authors
    //  - returns the absolute angle (in radians) from source to target points
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return FastTrig.atan2(target.x - source.x, target.y - source.y);
    }

    public static double limit(double min, double value, double max) {
        if (value > max)
            return max;
        if (value < min)
            return min;

        return value;
    }

    public static float limit(float min, float value, float max) {
        if (value > max)
            return max;
        if (value < min)
            return min;

        return value;
    }

    public static double bulletVelocity(double power) {
        return (20D - (3D * power));
    }

    public static double maxEscapeAngle(double velocity) {
        return FastTrig.asin(8.0 / velocity);
    }

    public void onPaint(java.awt.Graphics2D g) {
        painting = true;
        g.setColor(Color.red);

        for (int i = 0; i < _enemyWaves.size(); i++) {
            g.setColor(Color.red);
            EnemyWave w = (EnemyWave) (_enemyWaves.get(i));
            int radius = (int) (w.distanceTraveled);
            Point2D.Double center = w.fireLocation;
            if (radius - 40 < center.distance(_myLocation)) {
                // g.drawOval((int)(center.x - radius ), (int)(center.y - radius), radius*2, radius*2);
                if (w.bestBins != null) {
                    double MEA = maxEscapeAngle(w.bulletVelocity);
                    for (int j = 0; j < BINS; j++) {

                        double thisDanger = w.bestBins[j];
                        g.setColor(Color.blue);
                        if (thisDanger > 0.1)
                            g.setColor(Color.green);
                        if (thisDanger > 0.3)
                            g.setColor(Color.yellow);
                        if (thisDanger > 0.6)
                            g.setColor(Color.orange);
                        if (thisDanger > 0.9)
                            g.setColor(Color.red);
                        Point2D.Double p1 = project(center, w.directAngle + w.direction * (0.5 + j - MIDDLE_BIN) / (double) MIDDLE_BIN * MEA, radius);
                        Point2D.Double p2 = project(center, w.directAngle + w.direction * (j - 0.5 - MIDDLE_BIN) / (double) MIDDLE_BIN * MEA, radius);
                        g.drawLine((int) (p1.x), (int) (p1.y), (int) (p2.x), (int) (p2.y));


                    }
                }
                if (w.imaginary) {
                    g.setColor(Color.white);
                    g.drawString("imaginary wave in air", 100, 35);
                    g.drawString("velocity: " + w.bulletVelocity, 100, 25);
                    g.drawString("traveled distance: " + w.distanceTraveled, 100, 15);
                }


            }
        }
        {
            g.setColor(Color.white);
            g.drawString("enemy gunheat: " + enemyGunHeat, 300, 15);
            g.drawString("imaginary enemy gunheat" + imaginaryGunHeat, 300, 5);
            g.drawRect((int) _myLocation.x - 18, (int) _myLocation.y - 18, 36, 36);
        }

        if (firstPointsPainting != null) {

            for (int i = 0; i < firstPointsPainting.size(); i++) {
                g.setColor(Color.green);
                PlaceTime pt = ((PlaceTime) firstPointsPainting.get(i));
                Point2D.Double goToTarget = pt.place;
                if (pt.predictionStatus != null) {
                    goToTarget = pt.predictionStatus.endPoint;
                    if (pt.predictionStatus.debug)
                        g.setColor(Color.red);
                }

                g.drawOval((int) goToTarget.x - 2, (int) goToTarget.y - 2, 4, 4);
            }
        }
        if (lastGoToPoint != null) {
            g.setColor(Color.orange);
            g.drawOval((int) lastGoToPoint.x - 3, (int) lastGoToPoint.y - 3, 6, 6);
            g.drawOval((int) lastGoToPoint.x - 4, (int) lastGoToPoint.y - 4, 8, 8);
        }
        if (secondWave != null && secondWave.possSafePT != null) {
            g.setColor(Color.white);
            g.drawOval((int) secondWave.possSafePT.place.x - 3, (int) secondWave.possSafePT.place.y - 3, 6, 6);
        }
        if (nextPointsPainting != null) {
            g.setColor(Color.pink);
            for (int i = 0; i < nextPointsPainting.size(); i++) {
                PlaceTime pt = ((PlaceTime) nextPointsPainting.get(i));
                Point2D.Double goToTarget;
                if (pt.predictionStatus == null)
                    goToTarget = pt.place;
                else
                    goToTarget = pt.predictionStatus.endPoint;
                g.drawOval((int) goToTarget.x - 2, (int) goToTarget.y - 2, 4, 4);
            }
        }
        //          if(mainWave != null && mainWave.possPoints != null){
        //             g.setColor(Color.red);
        //             for(int i = 0; i < mainWave.possPoints.size(); i++){
        //                Point2D.Double goToTarget = ((PlaceTime)mainWave.possPoints.get(i)).place;
        //                g.drawOval((int)goToTarget.x - 2, (int)goToTarget.y - 2, 4,4);
        //             }
        //          }
        //          if(mainWave != null && mainWave.possSafePT != null){
        //             g.setColor(Color.magenta);
        //             g.drawOval((int)mainWave.possSafePT.place.x - 3, (int)mainWave.possSafePT.place.y - 3, 6,6);
        //
        //          }
        // g.setColor(Color.white);
        dgun.onPaint(g);
    }
}

class Scan implements Comparable {
    static float
            latVelWeight = 16,
            advVelWeight = 2,
            distWeight = 8,
            forwardWallWeight = 8,
            reverseWallWeight = 1,
            lastVelWeight = 4,
            accelWeight = 8,
            timeSinceDecelWeight = 2,
            timeSinceDirChangeWeight = 2,
            distLast20Weight = 2,
            timeWeight = 0;


    float latVel,
            advVel,
            dist,
            accel,
            forwardWall,
            reverseWall,
            lastVel,
            timeSinceDecel,
            timeSinceDirChange,
            distLast20;

    float hitGF;

    float weight;

    float distanceToCenter;

    float testDistance(Scan s) {
        return pow2(latVel - s.latVel)
                //  + advVelWeight*pow4(advVel - s.advVel)
                + pow2(dist - s.dist)
                + pow2(forwardWall - s.forwardWall)
                //  + reverseWallWeight*pow4(reverseWall - s.reverseWall)
                // + lastVelWeight*pow4(lastVel - s.lastVel)
                + pow2(accel - s.accel)
                + pow2(timeSinceDecel - s.timeSinceDecel)
                //+ pow4(timeSinceDirChange - s.timeSinceDirChange)
                + pow2(distLast20 - s.distLast20)
                ;
    }

    float distance(Scan s) {
        return latVelWeight * pow4(latVel - s.latVel)
                + advVelWeight * pow4(advVel - s.advVel)
                + distWeight * pow4(dist - s.dist)
                + forwardWallWeight * pow4(forwardWall - s.forwardWall)
                + reverseWallWeight * pow4(reverseWall - s.reverseWall)
                // + lastVelWeight*pow4(lastVel - s.lastVel)
                + accelWeight * pow4(accel - s.accel)
                + timeSinceDecelWeight * pow4(timeSinceDecel - s.timeSinceDecel)
                + timeSinceDirChangeWeight * pow4(timeSinceDirChange - s.timeSinceDirChange)
                + distLast20Weight * pow4(distLast20 - s.distLast20)
                ;
    }

    float flatDistance(Scan s) {
        return pow4(latVel - s.latVel)
                + pow4(advVel - s.advVel)
                + pow4(dist - s.dist)
                + pow4(forwardWall - s.forwardWall)
                + pow4(reverseWall - s.reverseWall)
                //  +pow4(lastVel - s.lastVel)
                + pow4(accel - s.accel)
                + pow4(timeSinceDecel - s.timeSinceDecel)
                + pow4(timeSinceDirChange - s.timeSinceDirChange)
                + pow4(distLast20 - s.distLast20)
                ;


    }

    public int compareTo(Object s) {
        if (distanceToCenter < ((Scan) s).distanceToCenter)
            return 1;
        return -1;
    }

    public static float pow4(float d) {
        return d * d * d * d;
    }

    public static float pow2(float d) {
        return d * d;
    }
}


class WaylanderGun {
    final static double angleScale = 24;
    final static double velocityScale = 1;
    static double lastEnemyHeading;

    static boolean firstScan;
    static StringBuilder data = new StringBuilder();
    AdvancedRobot bot;

    //DEBUG
    // Vector points = new Vector();
    public WaylanderGun(AdvancedRobot bot) {
        this.bot = bot;
        firstScan = true;
        try {
            data.delete(60000, 80000);
        } catch (StringIndexOutOfBoundsException e) {
        }
    }


    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {

        double headingRadians;
        double eDistance;
        double eHeadingRadians = e.getHeadingRadians();
        double absbearing = e.getBearingRadians() + (headingRadians = bot.getHeadingRadians());

        boolean rammer = (eDistance = e.getDistance()) < 100
                || bot.getTime() < 20;
        // 	|| Math.cos(absbearing - eHeadingRadians)*e.getVelocity() < -5 ;


        Rectangle2D.Double field = new Rectangle2D.Double(17, 17, 766, 566);


        if (!firstScan)
            data.insert(0, (char) ((eHeadingRadians - lastEnemyHeading) * angleScale))
                    .insert(0, (char) (e.getVelocity() * velocityScale));


        int keyLength = Math.min(data.length(), Math.min(Math.max(2, (int) bot.getTime() * 2 - 8), 256));

        int index = -1;
        do {

            index = data.indexOf(data.substring(0, keyLength), (int) eDistance / 11)
                    / 2;//sorts out even/odd numbers

        } while (index <= 0 && (keyLength /= 2) > 1);


        double bulletPower = rammer ? 3 : Math.min(2, Math.min(bot.getEnergy() / 16, e.getEnergy() / 2));


        double eX = eDistance * FastTrig.sin(absbearing);
        double eY = eDistance * FastTrig.cos(absbearing);

        double db = 0;
        double ww = eHeadingRadians;
        double speed = e.getVelocity();
        double w = eHeadingRadians - lastEnemyHeading;
        do {
            // db+=(20-3*bulletPower); 
            if (index > 1) {
                speed = (short) data.charAt(index * 2);
                w = ((short) data.charAt(index-- * 2 - 1)) / angleScale;
            }
            // eX+= (speed*Math.sin(ww));
            // eY+= (speed*Math.cos(ww));
        }
        while ((db += (20 - 3 * bulletPower)) < Point2D.distance(0, 0, eX += (speed * FastTrig.sin(ww += w)), eY += (speed * FastTrig.cos(ww)))
                && field.contains(eX + bot.getX(), eY + bot.getY()));

        //DEBUG
        // if(getGunHeat() <= 0.1)
        // points.add(new Point2D.Double(eX + getX(), eY + getY()));


        bot.setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(eX, eY) - bot.getGunHeadingRadians()));
        bot.setFire(bulletPower);


        bot.setTurnRadarRightRadians(Math.sin(absbearing - bot.getRadarHeadingRadians()) * 2);

        lastEnemyHeading = eHeadingRadians;
        firstScan = false;

    }

    //DEBUG ONLY
    /*
        public void onPaint(java.awt.Graphics2D g) {
          g.setColor(Color.red);
          int firstPoint = points.size() - 200;
          for(int i = 0; i < firstPoint; i++)
             points.remove(i);


          for(int i = 0; i < points.size(); i++)
             g.drawOval((int)(((Point2D.Double)(points.get(i))).x),(int)(((Point2D.Double)(points.get(i))).y),
                2,2);
       }
    */

}


class RaikoGun {

    //	private static final double BEST_DISTANCE = 525;
    //	private static boolean flat = true;
    private static double bearingDirection = 1, lastLatVel, lastVelocity, /*lastReverseTime, circleDir = 1, enemyFirePower,*/
            enemyEnergy, enemyDistance, lastVChangeTime, enemyLatVel, enemyVelocity/*, enemyFireTime, numBadHits*/;
    private static Point2D.Double enemyLocation;
    private static final int GF_ZERO = 15;
    private static final int GF_ONE = 30;
    private static String enemyName;
    private static int[][][][][][] guessFactors = new int[3][5][3][3][8][GF_ONE + 1];
    //	private static double numWins;

    private AdvancedRobot bot;

    public RaikoGun(AdvancedRobot bot) {
        this.bot = bot;
    }

    public void run() {
        //        setColors(Color.red, Color.white, Color.white);
        // bot.setAdjustGunForRobotTurn(true);
        // bot.setAdjustRadarForGunTurn(true);
    }

    public void onScannedRobot(ScannedRobotEvent e) {


        /*-------- setup data -----*/
        if (enemyName == null) {

            enemyName = e.getName();
            //			restoreData();
        }
        Point2D.Double robotLocation = new Point2D.Double(bot.getX(), bot.getY());
        double theta;
        double enemyAbsoluteBearing = bot.getHeadingRadians() + e.getBearingRadians();
        enemyDistance = e.getDistance();
        enemyLocation = projectMotion(robotLocation, enemyAbsoluteBearing, enemyDistance);

        //        if ((enemyEnergy -= e.getEnergy()) >= 0.1 && enemyEnergy <= 3.0) {
        //            enemyFirePower = enemyEnergy;
        //			enemyFireTime = bot.getTime();
        //		}

        enemyEnergy = e.getEnergy();

        Rectangle2D.Double BF = new Rectangle2D.Double(18, 18, 764, 564);

        //		/* ---- Movement ---- */
        //
        //		Point2D.Double newDestination;
        //
        //		double distDelta = 0.02 + Math.PI/2 + (enemyDistance > BEST_DISTANCE  ? -.1 : .5);
        //
        //		while (!BF.contains(newDestination = projectMotion(robotLocation, enemyAbsoluteBearing + circleDir*(distDelta-=0.02), 170)));
        //
        //		theta = 0.5952*(20D - 3D*enemyFirePower)/enemyDistance;
        //		if ( (flat && Math.random() > Math.pow(theta, theta)) || distDelta < Math.PI/5 || (distDelta < Math.PI/3.5 && enemyDistance < 400) ){
        //			circleDir = -circleDir;
        //			lastReverseTime = getTime();
        //		}
        //
        //		theta = absoluteBearing(robotLocation, newDestination) - getHeadingRadians();
        //		setAhead(Math.cos(theta)*100);
        //		setTurnRightRadians(Math.tan(theta));
        //

        /* ------------- Fire control ------- */

        /*
            To explain the below; if the enemy's absolute acceleration is
            zero then we segment on time since last velocity change, lateral
            acceleration and lateral velocity.
            If their absolute acceleration is non zero then we segment on absolute
            acceleration and absolute velocity.
            Regardless we segment on walls (near/far approach to walls) and distance.
            I'm trying to have my cake and eat it, basically. :-)
        */
        MicroWave w = new MicroWave();

        lastLatVel = enemyLatVel;
        lastVelocity = enemyVelocity;
        enemyLatVel = (enemyVelocity = e.getVelocity()) * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing);

        int distanceIndex = (int) enemyDistance / 140;

        double bulletPower = distanceIndex == 0 ? 3 : 2;
        theta = Math.min(bot.getEnergy() / 4, Math.min(enemyEnergy / 4, bulletPower));
        if (theta == bulletPower)
            bot.addCustomEvent(w);
        bulletPower = theta;
        w.bulletVelocity = 20D - 3D * bulletPower;

        int accelIndex = (int) Math.round(Math.abs(enemyLatVel) - Math.abs(lastLatVel));

        if (enemyLatVel != 0)
            bearingDirection = enemyLatVel > 0 ? 1 : -1;
        w.bearingDirection = bearingDirection * Math.asin(8D / w.bulletVelocity) / GF_ZERO;

        double moveTime = w.bulletVelocity * lastVChangeTime++ / enemyDistance;
        int bestGF = moveTime < .1 ? 1 : moveTime < .3 ? 2 : moveTime < 1 ? 3 : 4;

        int vIndex = (int) Math.abs(enemyLatVel / 3);

        if (Math.abs(Math.abs(enemyVelocity) - Math.abs(lastVelocity)) > .6) {
            lastVChangeTime = 0;
            bestGF = 0;

            accelIndex = (int) Math.round(Math.abs(enemyVelocity) - Math.abs(lastVelocity));
            vIndex = (int) Math.abs(enemyVelocity / 3);
        }

        if (accelIndex != 0)
            accelIndex = accelIndex > 0 ? 1 : 2;

        w.firePosition = robotLocation;
        w.enemyAbsBearing = enemyAbsoluteBearing;
        //now using PEZ' near-wall segment
        w.waveGuessFactors = guessFactors[accelIndex][bestGF][vIndex][BF.contains(projectMotion(robotLocation, enemyAbsoluteBearing + w.bearingDirection * GF_ZERO, enemyDistance)) ? 0 : BF.contains(projectMotion(robotLocation, enemyAbsoluteBearing + .5 * w.bearingDirection * GF_ZERO, enemyDistance)) ? 1 : 2][distanceIndex];


        bestGF = GF_ZERO;

        for (int gf = GF_ONE; gf >= 0 && enemyEnergy > 0; gf--)
            if (w.waveGuessFactors[gf] > w.waveGuessFactors[bestGF])
                bestGF = gf;

        bot.setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - bot.getGunHeadingRadians() + w.bearingDirection * (bestGF - GF_ZERO)));


        if (bot.getEnergy() > 1 || distanceIndex == 0)
            bot.setFire(bulletPower);

        bot.setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - bot.getRadarHeadingRadians()) * 2);

    }
    //    public void onHitByBullet(HitByBulletEvent e) {
    //		/*
    //		The infamous Axe-hack
    //	 	see: http://robowiki.net/?Musashi
    //		*/
    //		if ((double)(bot.getTime() - lastReverseTime) > enemyDistance/e.getVelocity() && enemyDistance > 200 && !flat)
    //	    	flat = (++numBadHits/(bot.getRoundNum()+1) > 1.1);
    //    }


    private static Point2D.Double projectMotion(Point2D.Double loc, double heading, double distance) {

        return new Point2D.Double(loc.x + distance * Math.sin(heading), loc.y + distance * Math.cos(heading));
    }

    private static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }


    //	public void onWin(WinEvent e){
    //		numWins++;
    //		saveData();
    //	}
    //	public void onDeath(DeathEvent e){
    //		saveData();
    //	}

    //	//Stole Kawigi's smaller save/load methods
    //	private void restoreData(){
    //		try
    //		{
    //			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(bot.getDataFile(enemyName))));
    //			guessFactors = (int[][][][][][])in.readObject();
    //			in.close();
    //		} catch (Exception ex){flat = false;}
    //	}

    //	private void saveData()
    //	{
    //		if (flat && numWins/(getRoundNum()+1) < .7 && getNumRounds() == getRoundNum()+1)
    //		try{
    //			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new RobocodeFileOutputStream(getDataFile(enemyName))));
    //			out.writeObject(guessFactors);
    //			out.close();
    //		}
    //		catch (IOException ex){}
    //	}


    class MicroWave extends Condition {

        Point2D.Double firePosition;
        int[] waveGuessFactors;
        double enemyAbsBearing, distance, bearingDirection, bulletVelocity;

        public boolean test() {

            if ((RaikoGun.enemyLocation).distance(firePosition) <= (distance += bulletVelocity) + bulletVelocity) {
                try {
                    waveGuessFactors[(int) Math.round((Utils.normalRelativeAngle(absoluteBearing(firePosition, RaikoGun.enemyLocation) - enemyAbsBearing)) / bearingDirection + GF_ZERO)]++;
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                bot.removeCustomEvent(this);
            }
            return false;
        }
    }


}