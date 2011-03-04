/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package voidious.gun;

import robocode.*;
import robocode.util.Utils;
import voidious.gfx.ColoredValueSet;
import voidious.gfx.RoboGraphic;
import voidious.gfx.RoboPainter;
import voidious.radar.DiamondEyes;
import voidious.utils.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class DiamondFist implements RoboPainter {
    protected static final boolean ENABLE_DEBUGGING_GRAPHICS = true;
    protected static final int CLUSTER_SIZE_MAIN = 30;
    protected static final int CLUSTER_SIZE_TRIPHAMMER = 250;
    protected static final int CLUSTER_SIZE_EVERYONE = 100;
    protected static final double ANTIGRAV_MIN_FORCE = 0.000001;
    protected static final double ANTIGRAV_MAX_FORCE = 0.0001;

    protected static final double LONGER_THAN_LONGEST_DISTANCE = 50000;
    protected static final double DEFAULT_ENERGY = 100;
    protected static final double BOT_WIDTH = 36;
    protected static final double BOT_HALF_WIDTH = 18;
    protected static final double NO_FIRING_ANGLE = -999;
    protected static final boolean WAVE_AIMING = true;
    protected static final boolean WAVE_COLLECTING = false;
    protected static final boolean IS_VISIT = true;
    protected static final boolean IS_BULLET_HIT = false;
    protected static final int INACTIVE_WAVE_OFFSET = -100;
    protected static final DiaWave NO_WAVE_FOUND = null;
    protected static final String NO_OPPONENT = "";
    protected static final String VIEW_1V1_MAIN = "main";
    protected static final String VIEW_1V1_ANTISURFER = "antisurf";
    protected static final String VIEW_MELEE = "melee";

    protected AdvancedRobot _robot;
    protected DiamondEyes _radar;
    protected boolean _tcMode;
    protected HashMap<String, EnemyDataGun> _enemies;
    protected ArrayList<DiaWave> _waves;
    protected DiaGun _mainGun;
    protected DiaGun _asGun;
    protected DiaGun _currentGun;
    protected VirtualGunsManager _virtualGuns;
    protected Point2D.Double _myLocation;
    public String _opponentName;
    protected double _bulletPower;
    protected int _enemiesAlive;
    protected int _enemiesTotal;
    protected long _lastBulletFiredTime;
    protected Rectangle2D.Double _fieldRect;
    protected double _battleFieldWidth;
    protected double _battleFieldHeight;
    protected Point2D.Double _centerField;
    protected ArrayList<Point2D.Double> _corners;

    protected boolean _startedDuel;
    protected double _damageGiven;
    protected Vector<RoboGraphic> _renderables;
    protected boolean _painting;
    protected boolean _robocodePainting;

    public DiamondFist(AdvancedRobot robot, DiamondEyes radar,
                       boolean isTc) {
        _robot = robot;
        _radar = radar;
        _tcMode = isTc;
        _enemies = new HashMap<String, EnemyDataGun>();
        _waves = new ArrayList<DiaWave>();
        _enemiesTotal = _robot.getOthers();
        _battleFieldWidth = _robot.getBattleFieldWidth();
        _battleFieldHeight = _robot.getBattleFieldHeight();
        _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18,
                _battleFieldWidth - 36, _battleFieldHeight - 36);
        _centerField = new Point2D.Double(_robot.getBattleFieldWidth() / 2,
                _robot.getBattleFieldHeight() / 2);
        _corners = new ArrayList<Point2D.Double>();

        _opponentName = NO_OPPONENT;
        _damageGiven = 0;
        _renderables = new Vector<RoboGraphic>();
        _painting = false;
        _robocodePainting = false;

        initGuns();
    }

    public void initGuns() {
        _virtualGuns = new VirtualGunsManager();

        if (_enemiesTotal > 1) {
            _mainGun = new MainGun(_enemies, _fieldRect, _renderables,
                    _enemiesTotal);
        } else {
            _mainGun =
                    new TripHammerKNNGun(_fieldRect, _renderables);
        }

        _virtualGuns.addGun(_mainGun);
        _currentGun = _mainGun;

        _asGun = new AntiSurferGun(_enemies, _fieldRect, _renderables,
                _enemiesTotal);
        _virtualGuns.addGun(_asGun);

        _corners.add(new Point2D.Double(0, 0));
        _corners.add(new Point2D.Double(0, _battleFieldHeight));
        _corners.add(new Point2D.Double(_battleFieldWidth, 0));
        _corners.add(new Point2D.Double(_battleFieldWidth, _battleFieldHeight));
    }

    public void initGunViews(EnemyDataGun edg) {
        // TODO: this should loop through all our guns and do this, then
        //       add the melee one, instead of being hard-coded.
        // TODO: the guns themselves should probably contain some of
        //       this code instead

        if (_enemiesTotal > 1) {
            DistanceFormula distanceMelee = new DistanceMelee(_enemiesTotal);
            DataView meleeView = new DataView(1, distanceMelee,
                    CLUSTER_SIZE_EVERYONE, DataView.BULLET_HITS_OFF,
                    DataView.VISITS_ON, DataView.VIRTUAL_ON, DataView.MELEE_ON,
                    DataView.ALWAYS_ON, DataView.UNLIMITED, DataView.NO_DECAY);
            edg.registerDataLogView(VIEW_MELEE, meleeView);

            DistanceFormula distanceMain = new DistanceMain(_enemiesTotal);
            DataView mainView = new DataView(1, distanceMain, CLUSTER_SIZE_MAIN,
                    DataView.BULLET_HITS_OFF, DataView.VISITS_ON,
                    DataView.VIRTUAL_ON, DataView.MELEE_ON, DataView.ALWAYS_ON,
                    DataView.UNLIMITED, DataView.NO_DECAY);
            edg.registerDataLogView(VIEW_1V1_MAIN, mainView);
        } else {
            ((TripHammerGun) _mainGun).setEnemyData(edg);
        }

        DistanceFormula distanceAs = new DistanceAntiSurfer(_enemiesTotal);
        DataView asView1 = new DataView(1, distanceAs, 4,
                DataView.BULLET_HITS_OFF, DataView.VISITS_ON,
                DataView.VIRTUAL_ON, DataView.MELEE_OFF, DataView.ALWAYS_ON, 200,
                DataView.NO_DECAY);
        DataView asView2 = new DataView(1, distanceAs, 4,
                DataView.BULLET_HITS_OFF, DataView.VISITS_ON,
                DataView.VIRTUAL_ON, DataView.MELEE_OFF, DataView.ALWAYS_ON, 1000,
                DataView.NO_DECAY);
        DataView asView3 = new DataView(1, distanceAs, 4,
                DataView.BULLET_HITS_OFF, DataView.VISITS_ON,
                DataView.VIRTUAL_ON, DataView.MELEE_OFF, DataView.ALWAYS_ON, 5000,
                DataView.NO_DECAY);
//        DataView asView4 = new DataView(-1, distanceAs, 4, 
//            DataView.BULLET_HITS_ON, DataView.VISITS_OFF, 
//            DataView.VIRTUAL_OFF, DataView.MELEE_OFF, DataView.ALWAYS_ON, 5000,
//            DataView.NO_DECAY);
        edg.registerDataLogView(VIEW_1V1_ANTISURFER + "1", asView1);
        edg.registerDataLogView(VIEW_1V1_ANTISURFER + "2", asView2);
        edg.registerDataLogView(VIEW_1V1_ANTISURFER + "3", asView3);
//        edg.registerDataLogView(VIEW_1V1_ANTISURFER + "4", asView4);

    }

    public void initRound(AdvancedRobot robot) {
        _robot = robot;
        Iterator<EnemyDataGun> edgIterator = _enemies.values().iterator();
        while (edgIterator.hasNext()) {
            EnemyDataGun enemyData = edgIterator.next();
            enemyData.energy = DEFAULT_ENERGY;
            enemyData.distance = LONGER_THAN_LONGEST_DISTANCE;
            enemyData.alive = true;
//            enemyData.clearDistancesSq();
            enemyData.pastLocations.clear();
        }
        _waves.clear();
        _myLocation = new Point2D.Double(_robot.getX(), _robot.getY());
        _bulletPower = 3;
        _enemiesAlive = _robot.getOthers();
        _lastBulletFiredTime = 0;
        _virtualGuns.clear();

        _startedDuel = false;
        _renderables.clear();
    }

    public void execute() {
        _myLocation = new Point2D.Double(_robot.getX(), _robot.getY());
        _enemiesAlive = _robot.getOthers();
        long currentTime = _robot.getTime();

        Iterator<EnemyDataGun> edgIterator = _enemies.values().iterator();
        while (edgIterator.hasNext()) {
            EnemyDataGun enemyData = edgIterator.next();
            if (enemyData.lastScanTime < currentTime) {
                enemyData.pastLocations.add(enemyData.location);
            }
        }

        updateBotDistancesSq();
        evalBulletPower();
        checkActiveWaves();
        if (is1v1()) {
            aimAndFire();
            if (!_startedDuel && !_opponentName.equals(NO_OPPONENT)) {
                _startedDuel = true;
                System.out.println("Current gun: " + _currentGun.getLabel() +
                        " (" + DiaUtils.round(_virtualGuns.getRating(
                        _currentGun, _opponentName) * 100, 2) +
                        ")");
            }
        } else {
            aimAndFireAtEveryone();
        }
        _myLocation = DiaUtils.nextLocation(_robot);
    }

    public void aimAndFire() {
        String closestBot = closestLivingBot();
        if (!closestBot.equals(NO_OPPONENT)) {
            EnemyDataGun enemyData = _enemies.get(closestBot);
            DiaWave fireWave = enemyData.lastWaveFired;
            Point2D.Double myNextLocation = DiaUtils.nextLocation(_robot);

            fireWave.setBulletPower(_bulletPower);

            // Leaves at least 2 ticks for aiming. (An extra one because the
            // difference could still get down to 2 in this setup, radar
            // turning one tick further away as one tick also goes by.)
            if (ticksUntilGunCool() - _radar.minTicksToScan(_opponentName) <= 3) {
                _radar.setRadarLock(_opponentName);
            } else {
                _radar.releaseRadarLock();
            }

            double firingAngle;
            // TODO: check if (energy == 0) could hit rounding errors
            if (enemyData.energy == 0 ||
                    _robot.getGunHeat() / _robot.getGunCoolingRate() > 3) {
                firingAngle = DiaUtils.absoluteBearing(
                        myNextLocation, fireWave.targetLocation);
                evalVirtualGuns();
            } else {
                firingAngle = _currentGun.aimWithWave(fireWave, paintStatus());
            }

            _robot.setTurnGunRightRadians(Utils.normalRelativeAngle(
                    firingAngle - _robot.getGunHeadingRadians()));

            if (Math.abs(_robot.getGunTurnRemainingRadians())
                    < DiaUtils.botWidthAimAngle(
                    myNextLocation.distance(fireWave.targetLocation)) ||
                    _enemiesAlive > 3) {
                Bullet realBullet = null;
                if (_tcMode) {
                    realBullet = _robot.setFireBullet(3);
                } else if (_robot.getEnergy() > _bulletPower) {
                    realBullet = _robot.setFireBullet(_bulletPower);
                }

                if (realBullet != null) {
                    _lastBulletFiredTime = _robot.getTime();
                    markFiringWaves();
                }
            }
        }
    }

    /*
        public void aimAndFireHot() {
            if (_targetName != NO_TARGET) {
                if (ticksUntilGunCool() - _radar.minTicksToScan(_targetName) <= 3) {
                    _radar.setRadarLock(_targetName);
                } else {
                    _radar.releaseRadarLock();
                }

                double bearingToTarget = DiaUtils.absoluteBearing(_myLocation,
                    _enemies.get(_targetName).location);
                _robot.setTurnGunRightRadians(Utils.normalRelativeAngle(
                    bearingToTarget - _robot.getGunHeadingRadians()));

                if ((Math.abs(_robot.getGunTurnRemainingRadians())
                    < DiaUtils.botWidthAimAngle(_enemies.get(_targetName).distance)
                    || _enemiesAlive > 3) && _robot.getEnergy() > 0.3) {

                    if (_robot.getEnergy() <= 3) {
                        _robot.setFire(0.3);
                    } else {
                        _robot.setFire(3);
                    }
                }
            }
        }
    */
    public void aimAndFireAtEveryone() {
        String closestBot = closestLivingBot();
        if (!closestBot.equals(NO_OPPONENT)) {
            double tolerance = DiaUtils.botWidthAimAngle(
                    _myLocation.distance(_enemies.get(closestBot).location));
            Point2D.Double myNextLocation = DiaUtils.nextLocation(_robot);

            int ticksUntilFire = (int) Math.ceil(DiaUtils.round(
                    _robot.getGunHeat() / _robot.getGunCoolingRate(), 2));

            if (ticksUntilFire % 2 == 0 || ticksUntilFire <= 4) {
                double firingAngle = aimAtEveryone(myNextLocation, _bulletPower);

                _robot.setTurnGunRightRadians(Utils.normalRelativeAngle(
                        firingAngle - _robot.getGunHeadingRadians()));

                if (Math.abs(_robot.getGunTurnRemainingRadians()) < tolerance) {
                    Bullet realBullet = null;
                    if (_tcMode) {
                        realBullet = _robot.setFireBullet(3);
                    } else if (_robot.getEnergy() > _bulletPower) {
                        realBullet = _robot.setFireBullet(_bulletPower);
                    }

                    if (realBullet != null) {
                        _lastBulletFiredTime = _robot.getTime();
                        markFiringWaves();
                    }
                }
            }
        }
    }

    public void evalBulletPower() {
        String closestBot = closestLivingBot();
        if (!closestBot.equals(NO_OPPONENT)) {
            EnemyDataGun enemyData = _enemies.get(closestBot);
            double myEnergy = _robot.getEnergy();

            if (_tcMode) {
                _bulletPower = Math.min(myEnergy, 3);
            } else if (is1v1()) {
                _bulletPower = 1.95;

//                double gunRating = (is1v1() ? 
//                    _virtualGuns.getRating(_currentGun, _opponentName) : 0);
//                int shotsFired = (is1v1() ?
//                    _virtualGuns.getShotsFired(_currentGun, _opponentName) : 0);

                if (enemyData.distance < 150) {
                    _bulletPower = 2.999;
                }

                if (myEnergy < 30 && enemyData.distance > 150) {
                    _bulletPower = Math.min(_bulletPower, 1.95 -
                            Math.sqrt((30 - myEnergy) / 8));
                }

                _bulletPower = Math.min(_bulletPower, enemyData.energy / 4);
                _bulletPower = Math.min(_bulletPower, myEnergy);
                _bulletPower = Math.max(_bulletPower, 0.1);
            } else {
                double avgEnemyEnergy = avgEnemyEnergy();

                _bulletPower = 2.999;

                if (_enemiesAlive <= 3) {
                    _bulletPower = 1.999;
                }

                if (_enemiesAlive <= 5 && enemyData.distance > 500) {
                    _bulletPower = 1.499;
                }

                if ((myEnergy < avgEnemyEnergy && _enemiesAlive <= 5 &&
                        enemyData.distance > 300) || enemyData.distance > 700) {

                    _bulletPower = 0.999;
                }

                if (myEnergy < 20 && myEnergy < avgEnemyEnergy) {
                    _bulletPower = Math.min(_bulletPower, 2 -
                            ((20 - myEnergy) / 11));
                }

                _bulletPower = Math.min(_bulletPower, myEnergy);
                _bulletPower = Math.max(_bulletPower, 0.1);
            }
        }
    }

    public void evalVirtualGuns() {
        DiaGun bestGun = _virtualGuns.bestGun(_opponentName);

        if (_currentGun != bestGun) {
            _currentGun = bestGun;
            System.out.println("Switching to " + _currentGun.getLabel() + " (" +
                    DiaUtils.round(
                            _virtualGuns.getRating(_currentGun, _opponentName) * 100, 2)
                    + ")");
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        String botName = e.getName();
        Point2D.Double enemyLocation = DiaUtils.translateToField(
                DiaUtils.project(_myLocation, Utils.normalAbsoluteAngle(
                        e.getBearingRadians() + _robot.getHeadingRadians()),
                        e.getDistance()), _battleFieldWidth, _battleFieldHeight);

        EnemyDataGun enemyData;
        if (_enemies.containsKey(botName)) {
            enemyData = _enemies.get(botName);
            enemyData.timeSinceDirectionChange +=
                    e.getTime() - enemyData.lastScanTime;
            enemyData.timeSinceVelocityChange +=
                    e.getTime() - enemyData.lastScanTime;

            if (Math.abs(e.getVelocity() -
                    enemyData.lastNonZeroVelocity) > 0.5) {
                enemyData.timeSinceVelocityChange = 0;
            }

            if (Math.abs(e.getVelocity()) > 0.5) {
                if (DiaUtils.nonZeroSign(e.getVelocity()) !=
                        DiaUtils.nonZeroSign(enemyData.lastNonZeroVelocity)) {
                    enemyData.timeSinceDirectionChange = 0;
                }
                enemyData.lastNonZeroVelocity = e.getVelocity();
            }

            enemyData.distance = e.getDistance();
            enemyData.energy = e.getEnergy();
            enemyData.location = enemyLocation;
            enemyData.lastScanTime = e.getTime();
            enemyData.previousVelocity = enemyData.velocity;
            enemyData.velocity = e.getVelocity();
            enemyData.heading = e.getHeadingRadians();
        } else {
            enemyData = new EnemyDataGun(e.getName(), e.getDistance(),
                    e.getEnergy(), enemyLocation, e.getTime(), e.getVelocity(),
                    e.getHeadingRadians());
            _enemies.put(botName, enemyData);
            initGunViews(enemyData);
        }
        enemyData.pastLocations.add(enemyLocation);

        fireNextTickWave(enemyLocation, botName, _bulletPower);

        if (is1v1()) {
            _opponentName = e.getName();
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        String botName = e.getName();

        try {
            _enemies.get(botName).alive = false;
        } catch (java.lang.NullPointerException npe) {
            System.out.println(
                    "WARNING (gun):  A bot died that I never knew existed!");
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        String botName = e.getName();

        try {
            _enemies.get(botName).damageTaken +=
                    Rules.getBulletDamage(e.getBullet().getPower());
        } catch (java.lang.NullPointerException npe) {
            System.out.println(
                    "WARNING (gun):  A bot shot me that I never knew existed!");
        }
    }

    public void onWin(WinEvent e) {
        roundOver();
    }

    public void onDeath(DeathEvent e) {
        roundOver();
    }

    public void onBulletHit(BulletHitEvent e) {
        _damageGiven += Math.min(
                Rules.getBulletDamage(e.getBullet().getPower()),
                _enemies.get(e.getName()).energy);

        String botName = e.getName();

        try {
            EnemyDataGun enemyData = _enemies.get(botName);
            enemyData.damageGiven +=
                    Rules.getBulletDamage(e.getBullet().getPower());

            long currentTime = _robot.getTime();
            Point2D.Double enemyLocation =
                    new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
            DiaWave hitWave = DiaUtils.findClosestWave(_waves, enemyLocation,
                    currentTime, DiaWave.ANY_WAVE, DiaWave.FIRING_WAVE, 50,
                    botName);
            if (hitWave != NO_WAVE_FOUND) {
                Point2D.Double hitVector =
                        hitWave.displacementVector(enemyLocation, currentTime);
                enemyData.saveDisplacementVector(hitWave, hitVector,
                        IS_BULLET_HIT);
            }
        } catch (java.lang.NullPointerException npe) {
            System.out.println(
                    "WARNING (gun):  I shot a bot that I never knew existed!");
        }
    }

    public void roundOver() {
        if (_tcMode) {
            System.out.println("TC score: " +
                    _damageGiven / (_robot.getRoundNum() + 1));
        }

        if (is1v1()) {
            _virtualGuns.printGunRatings(_opponentName);
        }
    }

    public void onPaint(Graphics2D g) {
        if (paintStatus()) {
            Iterator<RoboGraphic> i = _renderables.iterator();
            while (i.hasNext()) {
                RoboGraphic r = i.next();
                r.render(g);
            }
            _renderables.clear();
        }
    }

    public DiaWave fireNextTickWave(Point2D.Double targetLocation,
                                    String targetName, double bulletPower) {

        EnemyDataGun targetData = _enemies.get(targetName);
        Point2D.Double myNextLocation = DiaUtils.nextLocation(_robot);
        Point2D.Double enemyNextLocation = DiaUtils.translateToField(
                DiaUtils.nextLocation(targetLocation, targetData.velocity,
                        targetData.heading),
                _battleFieldWidth, _battleFieldHeight);
        Point2D.Double closestEnemyLocation = closestBotToEnemy(targetName);

        double accel = DiaUtils.limit(-Rules.DECELERATION,
                DiaUtils.accel(targetData.velocity, targetData.previousVelocity),
                Rules.ACCELERATION);
        double dl8t = targetLocation.distance(targetData.getPastLocation(
                Math.min(8, targetData.pastLocations.size() - 1)));
        double dl20t = targetLocation.distance(targetData.getPastLocation(
                Math.min(20, targetData.pastLocations.size() - 1)));
        double dl40t = targetLocation.distance(targetData.getPastLocation(
                Math.min(40, targetData.pastLocations.size() - 1)));
        double cornerDistance = cornerDistance(enemyNextLocation);
        double cornerBearing = cornerBearing(enemyNextLocation);

        Point2D.Double antiGravForce = antiGravForce(targetName);
/*
        if (paintStatus()) {
           _renderables.add(RoboGraphic.drawLine(targetData.location, DiaUtils.project(targetData.location, antiGravForce.x, 150 * (antiGravForce.y / ANTIGRAV_MAX_FORCE)), Color.white));
        }
        if (paintStatus()) {
            _renderables.add(RoboGraphic.drawLine(
                enemyNextLocation, orbitLocation, Color.green));
            _renderables.add(RoboGraphic.drawCircle(
                enemyNextLocation, 35, Color.green));
            _renderables.add(RoboGraphic.drawCircle(
                orbitLocation, 30, Color.gray));
            double angleToOrbit = DiaUtils.absoluteBearing(enemyNextLocation, 
                    orbitLocation);
            _renderables.addAll(Arrays.asList(
                RoboGraphic.drawArrowHead(
                    DiaUtils.project(enemyNextLocation, angleToOrbit,
                        enemyNextLocation.distance(orbitLocation) / 2 + 20),
                        30, angleToOrbit, Color.green)));
        }
*/
        DiaWave nextWave = new DiaWave(myNextLocation, enemyNextLocation,
                myNextLocation, _robot.getTime() + 1, bulletPower, targetName,
                targetData.heading, targetData.velocity, accel,
                DiaUtils.nonZeroSign(targetData.lastNonZeroVelocity),
                myNextLocation.distance(enemyNextLocation),
                closestEnemyLocation.distance(enemyNextLocation),
                targetData.timeSinceDirectionChange,
                targetData.timeSinceVelocityChange, 0, 0,
                dl8t, dl20t, dl40t, antiGravForce.x, antiGravForce.y,
                cornerDistance, cornerBearing, targetData.energy,
                _robot.getEnergy(), _enemiesAlive, _robot.getGunHeat(),
                _lastBulletFiredTime, _fieldRect, _battleFieldWidth,
                _battleFieldHeight);

        // A bit convoluted, but it's easier and faster to execute this way.
        nextWave.targetWallDistance = is1v1() ?
                nextWave.preciseEscapeAngle(DiaWave.POSITIVE_GUESSFACTOR) /
                        Math.asin(8.0 / nextWave.bulletSpeed) :
                Math.min(1.5, DiaUtils.directToWallDistance(enemyNextLocation,
                        targetData.distance, targetData.heading +
                                (targetData.lastNonZeroVelocity > 0 ? 0 : Math.PI),
                        bulletPower, _fieldRect));

        nextWave.targetRevWallDistance = is1v1() ?
                nextWave.preciseEscapeAngle(DiaWave.NEGATIVE_GUESSFACTOR) /
                        Math.asin(8.0 / nextWave.bulletSpeed) :
                Math.min(1.5, DiaUtils.directToWallDistance(enemyNextLocation,
                        targetData.distance, targetData.heading +
                                (targetData.lastNonZeroVelocity > 0 ? Math.PI : 0),
                        bulletPower, _fieldRect));

        targetData.lastWaveFired = nextWave;
        // TODO: rounding errors?
        if (_robot.getEnergy() != 0) {
            _waves.add(nextWave);
        }

        Iterator<EnemyDataGun> edgIterator = _enemies.values().iterator();
        while (edgIterator.hasNext()) {
            EnemyDataGun enemyData = edgIterator.next();

            if (enemyData.alive && !enemyData.botName.equals(targetName)) {
                Point2D.Double altNextLocation = DiaUtils.translateToField(
                        DiaUtils.nextLocation(enemyData.location,
                                enemyData.velocity, enemyData.heading),
                        _battleFieldWidth, _battleFieldHeight);
                DiaWave altWave = new DiaWave(altNextLocation, enemyNextLocation,
                        altNextLocation, _robot.getTime() + 1, bulletPower, targetName,
                        targetData.heading, targetData.velocity, accel,
                        DiaUtils.nonZeroSign(targetData.lastNonZeroVelocity),
                        altNextLocation.distance(enemyNextLocation),
                        closestEnemyLocation.distance(enemyNextLocation),
                        targetData.timeSinceDirectionChange,
                        targetData.timeSinceVelocityChange, 0, 0,
                        dl8t, dl20t, dl40t, antiGravForce.x, antiGravForce.y,
                        cornerDistance, cornerBearing, enemyData.energy,
                        _robot.getEnergy(), _enemiesAlive, _robot.getGunHeat(),
                        _lastBulletFiredTime, _fieldRect, _battleFieldWidth,
                        _battleFieldHeight);

                altWave.targetWallDistance =
                        Math.min(1.5, DiaUtils.directToWallDistance(enemyNextLocation,
                                altWave.sourceLocation.distance(targetData.location),
                                targetData.heading +
                                        (targetData.lastNonZeroVelocity > 0 ? 0 : Math.PI),
                                bulletPower, _fieldRect));

                altWave.targetRevWallDistance =
                        Math.min(1.5, DiaUtils.directToWallDistance(enemyNextLocation,
                                altWave.sourceLocation.distance(targetData.location),
                                targetData.heading +
                                        (targetData.lastNonZeroVelocity > 0 ? Math.PI : 0),
                                bulletPower, _fieldRect));

                altWave.altWave = true;
                // TODO: rounding errors?
                if (_robot.getEnergy() != 0) {
                    _waves.add(altWave);
                }
            }
        }


        return nextWave;
    }

    public void checkActiveWaves() {
        long currentTime = _robot.getTime();

        Iterator<DiaWave> wavesIterator = _waves.iterator();
        while (wavesIterator.hasNext()) {
            DiaWave w = wavesIterator.next();
            EnemyDataGun edg = _enemies.get(w.botName);

            if (w.fireTime == currentTime && !w.altWave) {
                w.gunHeat = _robot.getGunHeat();
                w.lastBulletFiredTime = _lastBulletFiredTime;
                boolean recalcWallDistance = false;
                if (w.bulletPower != _bulletPower) {
                    // If bullet power changes between segmentation tick
                    // and firing tick, update with correct power and
                    // related values (ie, wall distance).

                    w.setBulletPower(_bulletPower);

                    recalcWallDistance = true;
                }

                if (edg.lastScanTime == w.fireTime) {
                    // If we get two scans in a row, we can overwrite our
                    // estimates with real data.
                    w.targetLocation = edg.location;
                    w.sourceLocation = _myLocation;
                    w.absBearing = DiaUtils.absoluteBearing(
                            w.sourceLocation, w.targetLocation);

                    // Technically might recalc wall distance here, too,
                    // but lotsa CPU for negligible difference.
                }

                if (recalcWallDistance) {

                    // TODO: Lots of duplicated code, so ugly! Refactor.

                    w.clearCachedPreciseEscapeAngles();
                    _virtualGuns.clearWave(w);
                    w.targetWallDistance = is1v1() ?
                            w.preciseEscapeAngle(DiaWave.POSITIVE_GUESSFACTOR) /
                                    Math.asin(8.0 / w.bulletSpeed) :
                            Math.min(1.5, DiaUtils.directToWallDistance(
                                    w.targetLocation, w.targetDistance,
                                    w.effectiveHeading(), w.bulletPower, _fieldRect));

                    w.targetRevWallDistance = is1v1() ?
                            w.preciseEscapeAngle(DiaWave.NEGATIVE_GUESSFACTOR) /
                                    Math.asin(8.0 / w.bulletSpeed) :
                            Math.min(1.5, DiaUtils.directToWallDistance(
                                    w.targetLocation, w.targetDistance,
                                    w.effectiveHeading() + Math.PI, w.bulletPower,
                                    _fieldRect));
                }
            }

            if (!edg.alive) {
                wavesIterator.remove();
            } else if (!w.processedWaveBreak &&
                    w.wavePassedInterpolate(edg.location, edg.lastScanTime,
                            currentTime)) {

                Point2D.Double dispVector = w.displacementVector();
                edg.saveDisplacementVector(w, dispVector, IS_VISIT);
                w.processedWaveBreak = true;

                if (_enemiesTotal == 1) {
                    double[] wavePoint = ((TripHammerKNNGun) _mainGun)
                            .formula.dataPointFromWave(w);
                    ((TripHammerKNNGun) _mainGun).tree.insert(wavePoint);
                    double guessFactor = w.guessFactorPrecise(w.waveBreakLocation);
                    edg.guessFactors.put(wavePoint, guessFactor);
                }

                if (w.enemiesAlive == 1 && w.firingWave && !w.altWave) {
                    double hitAngle = DiaUtils.absoluteBearing(
                            w.sourceLocation, w.waveBreakLocation());
                    _virtualGuns.registerWaveBreak(w, hitAngle);
                }

                if (paintStatus()) {
                    double drawBearing = (w.orbitDirection *
                            DiaUtils.absoluteBearing(DiaWave.ORIGIN, dispVector)) +
                            w.effectiveHeading();
                    double drawDistance = dispVector.distance(DiaWave.ORIGIN) *
                            w.waveBreakBulletTicks();
                    _renderables.add(RoboGraphic.drawLine(
                            DiaUtils.project(w.targetLocation, w.targetHeading,
                                    25),
                            DiaUtils.project(w.targetLocation,
                                    w.targetHeading + Math.PI, 25),
                            Color.darkGray));
                    _renderables.add(RoboGraphic.drawLine(
                            DiaUtils.project(w.targetLocation,
                                    w.targetHeading + (Math.PI / 2),
                                    25),
                            DiaUtils.project(w.targetLocation,
                                    w.targetHeading - (Math.PI / 2), 25),
                            Color.darkGray));
                    _renderables.addAll(Arrays.asList(
                            RoboGraphic.drawArrowHead(DiaUtils.project(
                                    w.targetLocation, w.effectiveHeading(),
                                    25), 10, w.effectiveHeading(), Color.darkGray)));
//                    _renderables.addAll(Arrays.asList(
//                        RoboGraphic.drawArrowHead(DiaUtils.project(
//                            w.targetLocation, 
//                            w.effectiveHeading() + Math.PI,
//                            50), 12, w.effectiveHeading(), Color.gray)));
                    _renderables.add(RoboGraphic.drawLine(w.targetLocation,
                            DiaUtils.project(w.targetLocation, drawBearing,
                                    drawDistance),
                            Color.red));
//                    _renderables.addAll(Arrays.asList(
//                        RoboGraphic.drawArrowHead(
//                            DiaUtils.project(w.targetLocation, drawBearing, 
//                                drawDistance / 2 + 20), 30, drawBearing, 
//                            Color.red)));
                    _renderables.add(RoboGraphic.drawCircleFilled(
                            w.waveBreakLocation, Color.red, 4));
                    _renderables.add(RoboGraphic.drawPoint(w.targetLocation,
                            Color.red));
                    _renderables.add(RoboGraphic.drawText(
                            "" + (currentTime - w.fireTime),
                            w.targetLocation.x - 8,
                            w.targetLocation.y - 20, Color.white));
                }
            } else if (w.wavePassed(edg.location, currentTime,
                    INACTIVE_WAVE_OFFSET)) {
                wavesIterator.remove();
            }
        }
    }

    public void markFiringWaves() {
        long currentTime = _robot.getTime();
        Iterator<DiaWave> wavesIterator = _waves.iterator();
        while (wavesIterator.hasNext()) {
            DiaWave w = wavesIterator.next();

            if (w.fireTime == currentTime && !w.altWave) {
                w.firingWave = true;
                if (is1v1()) {
                    _virtualGuns.fireVirtualBullets(w);
                }
            }
        }
    }

    public double aimAtEveryone(Point2D.Double myNextLocation,
                                double bulletPower) {

        // TODO: parallel arrays is kinda ugly
        ArrayList<DiaWave> firingWaves = new ArrayList<DiaWave>();
        ArrayList<Double> firingAngles = new ArrayList<Double>();
        ArrayList<Double> firingDistances = new ArrayList<Double>();
        ArrayList<Double> bandwidths = new ArrayList<Double>();

        long currentTime = _robot.getTime();
        Iterator<EnemyDataGun> edgIterator = _enemies.values().iterator();
        while (edgIterator.hasNext()) {
            EnemyDataGun enemyData = edgIterator.next();
            if (enemyData.alive &&
                    enemyData.displacementVectors.size() >= 10 &&
                    enemyData.lastWaveFired != null) {

                DiaWave w = enemyData.lastWaveFired;
                w.setBulletPower(bulletPower);
                DataView view = enemyData.views.get(VIEW_MELEE);
                KdBucketTree scanTree = view.tree;
                int clusterSize = (int) Math.min(view.treeSize / 10,
                        CLUSTER_SIZE_EVERYONE / _robot.getOthers());
                double[] weights = view.formula.weights;
                double[] wavePoint =
                        view.formula.dataPointFromWave(w, WAVE_AIMING);
                double[][] nearestNeighbors = KdBucketTree.nearestNeighbors(
                        scanTree, wavePoint, clusterSize, weights);
                if (nearestNeighbors == null) {
                    continue;
                }

                int numScans = nearestNeighbors.length;

                for (int x = 0; x < numScans; x++) {
                    Point2D.Double dispVector =
                            enemyData.displacementVectors.get(nearestNeighbors[x]);
                    Point2D.Double projectedLocation = w.projectLocationBlind(
                            myNextLocation, dispVector, currentTime);
                    if (_fieldRect.contains(projectedLocation)) {
                        double thisDistance =
                                myNextLocation.distance(projectedLocation);
                        firingAngles.add(
                                DiaUtils.absoluteBearing(myNextLocation,
                                        projectedLocation));
                        firingWaves.add(w);
                        firingDistances.add(thisDistance);
                        bandwidths.add(DiaUtils.botWidthAimAngle(thisDistance));
                    }
                }
            }
        }

        double bestAngle = NO_FIRING_ANGLE;
        double bestDensity = Double.NEGATIVE_INFINITY;
        DiaWave bestWave = null;
        Double[] firingDistancesArray = firingDistances.toArray(new Double[0]);
        Double[] firingAnglesArray = firingAngles.toArray(new Double[0]);
        Double[] bandwidthsArray = bandwidths.toArray(new Double[0]);
        ColoredValueSet cvs = new ColoredValueSet();

        for (int x = 0; x < firingAnglesArray.length; x++) {
            double xFiringAngle = firingAnglesArray[x];
            double xDensity = 0;

            for (int y = 0; y < firingAnglesArray.length; y++) {
                double ux =
                        Utils.normalRelativeAngle(xFiringAngle - firingAnglesArray[y])
                                / bandwidthsArray[y];

                // Gaussian
                xDensity += Math.exp(-0.5 * ux * ux)
                        / firingDistancesArray[y];

                // Quartic
//                if (Math.abs(ux) <= 1) {
//                    xDensity += DiaUtils.square(1 - DiaUtils.square(ux))
//                        / firingDistancesArray[y];
//                }
            }

            if (xDensity > bestDensity) {
                bestAngle = xFiringAngle;
                bestDensity = xDensity;
                bestWave = firingWaves.get(x);
            }

            if (paintStatus()) {
                cvs.addValue(xDensity, xFiringAngle);
            }
        }

        if (firingAngles.isEmpty() || bestAngle == NO_FIRING_ANGLE) {
            return _enemies.get(closestLivingBot()).lastWaveFired.absBearing;
        }

        if (paintStatus()) {
            double bandwidth = DiaUtils.botWidthAimAngle(
                    myNextLocation.distance(
                            _enemies.get(closestLivingBot()).location));
            DiamondFist.paintGunAngles(_renderables, bestWave, cvs, bestAngle,
                    bandwidth);
        }

        return Utils.normalAbsoluteAngle(bestAngle);
    }

    protected long ticksUntilGunCool() {
        return Math.round(Math.ceil(
                _robot.getGunHeat() / _robot.getGunCoolingRate()));
    }

    protected void updateBotDistancesSq() {
        if (_enemies.size() <= 1) {
            return;
        }

        String[] botNames = new String[_enemies.size()];
        _enemies.keySet().toArray(botNames);

        for (int x = 0; x < botNames.length; x++) {
            EnemyDataGun edg1 = _enemies.get(botNames[x]);
            for (int y = x + 1; y < botNames.length; y++) {
                EnemyDataGun edg2 = _enemies.get(botNames[y]);
                if (edg1.alive && edg2.alive) {
                    double distanceSq = edg1.location.distanceSq(edg2.location);
                    edg1.setBotDistanceSq(botNames[y], distanceSq);
                    edg2.setBotDistanceSq(botNames[x], distanceSq);
                } else {
                    if (!edg1.alive) {
                        edg2.removeDistanceSq(botNames[x]);
                    }
                    if (!edg2.alive) {
                        edg1.removeDistanceSq(botNames[y]);
                    }
                }
            }
        }
    }

    protected Point2D.Double closestBotToEnemy(String enemyName) {
        Point2D.Double closestLocation = DiaUtils.nextLocation(_robot);

        if (_enemiesAlive > 1) {
            EnemyDataGun edg = _enemies.get(enemyName);
            String closestEnemy = edg.closestBot();
            if (closestEnemy != null &&
                    edg.getBotDistanceSq(closestEnemy) <
                            DiaUtils.square(_enemies.get(enemyName).distance)) {
                closestLocation = _enemies.get(closestEnemy).location;
            }
        }

        return closestLocation;
    }

    protected double cornerDistance(Point2D.Double p) {
        return Math.sqrt(
                DiaUtils.square(Math.min(p.x, _battleFieldWidth - p.x)) +
                        DiaUtils.square(Math.min(p.y, _battleFieldHeight - p.y)));
    }

    protected double cornerBearing(Point2D.Double p) {
        double distNearestCorner = Double.POSITIVE_INFINITY;
        Point2D.Double nearestCorner = null;

        Iterator<Point2D.Double> cornerIterator = _corners.iterator();
        while (cornerIterator.hasNext()) {
            Point2D.Double corner = cornerIterator.next();
            double thisDistance = corner.distanceSq(p);
            if (thisDistance < distNearestCorner) {
                distNearestCorner = thisDistance;
                nearestCorner = corner;
            }
        }

        return DiaUtils.absoluteBearing(p, nearestCorner);
    }

    protected Point2D.Double antiGravForce(String targetName) {
        EnemyDataGun targetData = _enemies.get(targetName);

        Point2D.Double forceVector =
                antiGravForce(_myLocation,
                        _robot.getEnergy() / Math.max(1, targetData.energy),
                        targetData.location);

        Iterator<EnemyDataGun> edgIterator = _enemies.values().iterator();
        while (edgIterator.hasNext()) {
            EnemyDataGun enemyData = edgIterator.next();
            if (enemyData.alive && !enemyData.botName.equals(targetName)) {
                Point2D.Double thisForce = antiGravForce(
                        enemyData.location,
                        enemyData.energy / Math.max(1, targetData.energy),
                        targetData.location);
                forceVector.x += thisForce.x;
                forceVector.y += thisForce.y;
            }
        }

        return new Point2D.Double(
                DiaUtils.absoluteBearing(DiaWave.ORIGIN, forceVector),
                DiaUtils.limit(ANTIGRAV_MIN_FORCE, DiaWave.ORIGIN.distance(forceVector), ANTIGRAV_MAX_FORCE));
    }

    public static Point2D.Double antiGravForce(Point2D.Double source,
                                               double energyFactor, Point2D.Double target) {

        return DiaUtils.project(DiaWave.ORIGIN,
                DiaUtils.absoluteBearing(source, target),
                DiaUtils.limit(0.5, energyFactor, 2) / source.distanceSq(target));
    }

    protected double avgEnemyEnergy() {
        Iterator<EnemyDataGun> edgIterator = _enemies.values().iterator();
        double totalEnergy = 0;
        while (edgIterator.hasNext()) {
            EnemyDataGun enemyData = edgIterator.next();
            if (enemyData.alive) {
                totalEnergy += enemyData.energy;
            }
        }

        return totalEnergy / _enemiesAlive;
    }

    protected String closestLivingBot() {
        String closestBot = NO_OPPONENT;
        double closestDistance = Double.POSITIVE_INFINITY;

        Iterator<EnemyDataGun> edgIterator = _enemies.values().iterator();
        while (edgIterator.hasNext()) {
            EnemyDataGun enemyData = edgIterator.next();
            if (enemyData.alive) {
                double thisDistance = _myLocation.distanceSq(enemyData.location);
                if (thisDistance < closestDistance) {
                    closestBot = enemyData.botName;
                    closestDistance = thisDistance;
                }
            }
        }

        return closestBot;
    }

    public boolean is1v1() {
        return (_enemiesAlive <= 1);
    }

    public void paintOn() {
        _painting = ENABLE_DEBUGGING_GRAPHICS;
    }

    public void paintOff() {
        _renderables.clear();
        _painting = false;
    }

    public void robocodePaintOn() {
        _robocodePainting = true;
    }

    public void robocodePaintOff() {
        _renderables.clear();
        _robocodePainting = false;
    }

    public String paintLabel() {
        return "Gun";
    }

    public boolean paintStatus() {
        return (_painting && _robocodePainting);
    }

    public static void paintGunAngles(Vector<RoboGraphic> renderables,
                                      DiaWave wave, ColoredValueSet cvs, double bestAngle, double bandwidth) {

        double arrowLength = Math.min(300,
                wave.sourceLocation.distance(wave.targetLocation) - 75);

        for (ColoredValueSet.ColoredValue cv : cvs.getColoredValues()) {
            Color c = cv.redColor();
            Point2D.Double angleHead = DiaUtils.project(wave.sourceLocation,
                    cv.firingAngle, arrowLength);

            renderables.add(RoboGraphic.drawLine(wave.sourceLocation,
                    angleHead, c));
            renderables.addAll(Arrays.asList(RoboGraphic.drawArrowHead(
                    angleHead, 10, cv.firingAngle, c)));

        }

        double bestArrowLength = Math.min(325,
                wave.sourceLocation.distance(wave.targetLocation) - 50);
        Point2D.Double angleHead = DiaUtils.project(wave.sourceLocation,
                bestAngle, bestArrowLength);
        Point2D.Double bestSource = DiaUtils.project(wave.sourceLocation,
                bestAngle, arrowLength);

        renderables.add(RoboGraphic.drawLine(bestSource,
                angleHead, Color.white));
        renderables.addAll(Arrays.asList(RoboGraphic.drawArrowHead(
                angleHead, 10, bestAngle, Color.white)));
        renderables.add(RoboGraphic.drawLine(
                DiaUtils.project(wave.sourceLocation, bestAngle -
                        bandwidth, bestArrowLength - 20),
                DiaUtils.project(wave.sourceLocation, bestAngle +
                        bandwidth, bestArrowLength - 20),
                Color.white));
    }
}
