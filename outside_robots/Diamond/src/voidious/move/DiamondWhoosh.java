package voidious.move;

import robocode.*;
import robocode.util.Utils;
import voidious.gfx.RoboGraphic;
import voidious.gfx.RoboPainter;
import voidious.utils.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * Copyright (c) 2009-2010 - Voidious
 * <p/>
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * <p/>
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software.
 * <p/>
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * <p/>
 * 3. This notice may not be removed or altered from any source
 * distribution.
 */

//1v1 TODO: adjust for wall hits in enemy wave detection
//1v1 TODO: rammer detection

public class DiamondWhoosh implements RoboPainter {
    protected static final boolean ENABLE_DEBUGGING_GRAPHICS = true;
    protected static final double CURRENT_DESTINATION_BIAS = 0.8;
    protected static final int RECENT_LOCATIONS_TO_STORE = 50;
    protected static final double DIRECTION_CHANGE_THRESHOLD = Math.PI / 2;
    protected static final long NUM_SLICES_BOT = 100;
    protected static final double DEFAULT_ENERGY = 100;
    protected static final double LONGER_THAN_LONGEST_DISTANCE = 50000;
    protected static final double MAX_ATTACK_ANGLE = Math.PI * .35;
    protected static final int WAVES_TO_SURF = 2;
    protected static final double WALL_STICK = 160;
    protected static final double FEARFUL_DISTANCING_EXPONENT = 4;
    protected static final double NORMAL_DISTANCING_EXPONENT = 2;
    protected static final double SURF_ORIENTATION_VELOCITY_THRESHOLD = 0.1;
    protected static final double DEFAULT_ATTACK_ANGLE = -1.047;

    protected static final double HALF_PI = Math.PI / 2;
    protected static final double BOT_WIDTH = 36;
    protected static final double BOT_HALF_WIDTH = 18;
    protected static final double MAX_WAVE_INTERCEPT_OFFSET =
            BOT_HALF_WIDTH / Math.cos(Math.PI / 4);
    protected static final double TYPICAL_DISTANCE = 465;
    protected static final double TYPICAL_ESCAPE_RANGE = 0.98;
    protected static final int CLOCKWISE_OPTION = 1;
    protected static final int STOP_OPTION = 0;
    protected static final int COUNTERCLOCKWISE_OPTION = -1;
    protected static final int NO_SURFABLE_WAVES = 0;
    protected static final int FIRST_WAVE = 0;
    protected static final DiaWave NO_WAVE_FOUND = null;
    protected static final boolean OBSERVE_WALL_HITS = false;

    protected AdvancedRobot _robot;
    protected HashMap<String, EnemyDataMove> _enemies;
    protected Destination _currentDestination;
    protected double _currentHeading;
    protected double _previousHeading;
    protected long _timeSinceReverseDirection;
    protected long _timeSinceVelocityChange;
    protected int _minBotsCloser;

    protected Point2D.Double _myLocation;
    protected LinkedList<OldLocation> _recentLocations;
    protected double _myEnergy;
    protected long _currentTime;
    protected int _roundNum;
    protected int _enemiesAlive;
    protected int _enemiesTotal;

    protected double _desiredDistance;
    protected double _fearDistance;
    protected double _smoothAwayDistance;
    protected double _wallStick = WALL_STICK;
    protected int _lastMovementChoice = CLOCKWISE_OPTION;
    protected ArrayList<MovementChoice> _movementOptions;
    protected MovementChoice _optionCounterClockwise;
    protected MovementChoice _optionStop;
    protected MovementChoice _optionClockwise;
    protected DistanceController _currentDistancer;
    protected String _opponentName;
    protected DiaWave _lastWaveSurfed;
    protected double _lastVelocity;
    protected double _previousVelocity;
    protected int _flattenerToggleTimer;
    protected double _lastNonZeroVelocity;
    protected LinkedList<DiaWave> _enemyWaves;
    protected LinkedList<DiaWave> _virtualWaves;
    protected ArrayList<LinkedList<DiaWave>> _waveLists;
    protected LinkedList<DiaWave> _potentialWaves;
    protected DiaWave _imaginaryWave;
    protected int _imaginaryWaveIndex;
    //    protected GunAnalyzer _analyzer;
    protected LinkedList<Point2D.Double> _pastLocations;
    protected java.awt.geom.Rectangle2D.Double _fieldRect;
    protected double _battleFieldWidth;
    protected double _battleFieldHeight;

    protected Vector<RoboGraphic> _renderables;
    protected boolean _painting;
    protected boolean _robocodePainting;
    protected String _className = "DiamondWhoosh";

    public DiamondWhoosh(AdvancedRobot robot) {
        _robot = robot;
        _enemies = new HashMap<String, EnemyDataMove>();
        _battleFieldWidth = _robot.getBattleFieldWidth();
        _battleFieldHeight = _robot.getBattleFieldHeight();
        _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18,
                _battleFieldWidth - 36, _battleFieldHeight - 36);
        _recentLocations = new LinkedList<OldLocation>();
        _enemiesTotal = _robot.getOthers();
        _enemyWaves = new LinkedList<DiaWave>();
        _virtualWaves = new LinkedList<DiaWave>();
        _potentialWaves = new LinkedList<DiaWave>();
        _waveLists = new ArrayList<LinkedList<DiaWave>>();
        _waveLists.add(_enemyWaves);
        _waveLists.add(_virtualWaves);
        _imaginaryWave = NO_WAVE_FOUND;
//        _analyzer = new GunAnalyzer();
        _pastLocations = new LinkedList<Point2D.Double>();
        _previousVelocity = _robot.getVelocity();

        _currentDistancer = new BasicDistancing();
        _opponentName = "";
        initializeMovementOptions();

        _renderables = new Vector<RoboGraphic>();
        _painting = false;
        _robocodePainting = false;
    }

    public void initRound(AdvancedRobot robot) {
        _robot = robot;
        Iterator<EnemyDataMove> edmIterator = _enemies.values().iterator();
        while (edmIterator.hasNext()) {
            EnemyDataMove enemyData = edmIterator.next();
            enemyData.energy = DEFAULT_ENERGY;
            enemyData.distance = LONGER_THAN_LONGEST_DISTANCE;
            enemyData.alive = true;
            enemyData.clearDistancesSq();
            enemyData.lastTimeHit = Long.MIN_VALUE;
            enemyData.lastTimeClosest = Long.MIN_VALUE;
            enemyData.pastLocations.clear();
            enemyData.raw1v1ShotsFiredThisRound = 0;
            enemyData.raw1v1ShotsHitThisRound = 0;
            enemyData.weighted1v1ShotsHitThisRound = 0;
            enemyData.clearNeighborCache();
            enemyData.lastBulletPower = 0;
        }
        _myLocation = new Point2D.Double(_robot.getX(), _robot.getY());
        _previousVelocity = _robot.getVelocity();
        _currentTime = 0;
        _roundNum = _robot.getRoundNum() + 1;
        _currentDestination =
                new Destination(_myLocation, Double.POSITIVE_INFINITY, 0);
        _timeSinceReverseDirection = 0;
        _timeSinceVelocityChange = 0;
        _currentHeading = _previousHeading = _robot.getHeadingRadians();
        _enemiesAlive = _robot.getOthers();
        _lastNonZeroVelocity = 0;
        _minBotsCloser = 0;

        _recentLocations.clear();
        _enemyWaves.clear();
        _virtualWaves.clear();
        _potentialWaves.clear();
        _imaginaryWave = NO_WAVE_FOUND;
        _pastLocations.clear();

        _renderables.clear();
    }

    public void execute() {
        DiaUtils.log(_className, "execute", "", true);
        _myLocation = new Point2D.Double(_robot.getX(), _robot.getY());
        _myEnergy = _robot.getEnergy();
        _currentTime = _robot.getTime();
        _previousHeading = _currentHeading;
        _enemiesAlive = _robot.getOthers();
        if (Math.abs(_robot.getVelocity()) >
                SURF_ORIENTATION_VELOCITY_THRESHOLD) {
            _lastNonZeroVelocity = _robot.getVelocity();
        }
        _currentHeading = Utils.normalAbsoluteAngle(_robot.getHeadingRadians() +
                (_lastNonZeroVelocity < 0 ? Math.PI : 0));
        Iterator<EnemyDataMove> edmIterator = _enemies.values().iterator();
        while (edmIterator.hasNext()) {
            EnemyDataMove enemyData = edmIterator.next();
            if (enemyData.alive) {
                if (enemyData.lastScanTime < _currentTime) {
                    enemyData.pastLocations.add(enemyData.location);
                }
                enemyData.timeAliveTogether++;
                enemyData.totalDistance += enemyData.distance;
            }
        }
        updateBotDistances();
//        updateAvoidBeingTargeted();
//        updateStayPerpendicular();
        updateDamageFactors();

        move();
        _myLocation = DiaUtils.nextLocation(_robot);
        _pastLocations.addFirst(_myLocation);
        _currentTime = _currentTime + 1;
        _previousVelocity = _robot.getVelocity();
        DiaUtils.log(_className, "execute", "", false);
    }

    protected void move() {
        if (is1v1()) {
            checkActiveEnemyWaves();
            move1v1();
        } else {
            updateTimeSinceTimers();
            moveFfa();
        }
    }

    public void move1v1() {
        evaluateDistancing();
        evaluateFlattener();

        surf();
    }

    public void moveFfa() {
        if (_currentTime % 5 == 0) {
            for (int x = 0; x < 5; x++) {
                _recentLocations.addFirst(
                        new OldLocation(DiaUtils.project(_myLocation,
                                Math.random() * Math.PI * 2,
                                5 + Math.random() * Math.random() * 200),
                                _currentTime));
            }
            while (_recentLocations.size() > RECENT_LOCATIONS_TO_STORE) {
                _recentLocations.removeLast();
            }
        }

        if (_enemies.size() == 0) {
            return;
        }

        ArrayList<Destination> possibleDestinations =
                new ArrayList<Destination>();

        possibleDestinations.addAll(generatePointsAroundBot());
//        possibleDestinations.addAll(generatePointsNearPreviousDestination());

        if (_myLocation.distance(_currentDestination.location) <=
                _myLocation.distance(_enemies.get(closestBot()).location)) {
            _currentDestination.goAngle = DiaUtils.absoluteBearing(_myLocation,
                    _currentDestination.location);
            _currentDestination.risk = CURRENT_DESTINATION_BIAS *
                    evalDestinationRisk(_currentDestination.location,
                            _currentDestination.goAngle);
            possibleDestinations.add(_currentDestination);
        }

        Destination nextDestination;
        do {
            nextDestination = safestDestination(possibleDestinations);
            possibleDestinations.remove(nextDestination);
        } while (wouldHitWall(nextDestination));

        double goAngle =
                DiaUtils.absoluteBearing(_myLocation, nextDestination.location);
        DiaUtils.setBackAsFront(_robot, goAngle);

        _currentDestination = nextDestination;

        if (paintStatus()) {
            possibleDestinations.add(_currentDestination);
            drawRisks(possibleDestinations);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        DiaUtils.log(_className, "onScannedRobot", "", true);

        String botName = e.getName();
        _opponentName = botName;
        double absBearing = Utils.normalAbsoluteAngle(
                e.getBearingRadians() + _robot.getHeadingRadians());
        Point2D.Double enemyLocation = DiaUtils.project(_myLocation,
                absBearing, e.getDistance());

        EnemyDataMove enemyData;
        double previousEnergy;
        if (_enemies.containsKey(botName)) {
            enemyData = _enemies.get(botName);
            previousEnergy = enemyData.energy;
            enemyData.energy = e.getEnergy();
            enemyData.distance = e.getDistance();
            enemyData.location = enemyLocation;
            enemyData.heading = e.getHeadingRadians();
            enemyData.velocity = e.getVelocity();
            enemyData.absBearing = absBearing;
            enemyData.lastScanTime = _currentTime;
        } else {
            previousEnergy = e.getEnergy();
            enemyData = new EnemyDataMove(botName, e.getDistance(),
                    e.getEnergy(), enemyLocation, e.getHeadingRadians(), absBearing,
                    _currentTime);
            _enemies.put(botName, enemyData);
        }
        enemyData.pastLocations.add(enemyLocation);

        if (is1v1()) {
            updateTimeSinceTimers();

            double currentEnergy = e.getEnergy();
            boolean realShotFired = false;
            double bulletPower = previousEnergy - currentEnergy;
            if (bulletPower > 0.09 && bulletPower < 3.01) {
                realShotFired = true;
                enemyData.raw1v1ShotsFired++;
                enemyData.raw1v1ShotsFiredThisRound++;
            }
            fireEnemyWave(realShotFired, botName, bulletPower);
        }

        DiaUtils.log(_className, "onScannedRobot", "", false);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        String botName = e.getName();

        try {
            _enemies.get(botName).alive = false;
        } catch (java.lang.NullPointerException npe) {
            System.out.println(
                    "WARNING (move): A bot died that I never knew existed!");
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        String botName = e.getName();

        try {
            EnemyDataMove enemyData = _enemies.get(botName);
            enemyData.lastTimeHit = _robot.getTime();
            enemyData.damageTaken +=
                    Rules.getBulletDamage(e.getBullet().getPower());
            enemyData.totalBulletPower += e.getBullet().getPower();
            enemyData.totalTimesHit++;
            enemyData.energy += Rules.getBulletHitBonus(e.getBullet().getPower());
            DiaWave hitWave = processBullet(e.getBullet());
            if (hitWave != NO_WAVE_FOUND) {
                double thisHit = (hitWave.targetDistance
                        / TYPICAL_DISTANCE)
                        * (hitWave.escapeAngleRange() / TYPICAL_ESCAPE_RANGE);
                enemyData.weighted1v1ShotsHit += thisHit;
                enemyData.weighted1v1ShotsHitThisRound += thisHit;
                enemyData.raw1v1ShotsHit++;
                enemyData.raw1v1ShotsHitThisRound++;
            }
        } catch (java.lang.NullPointerException npe) {
            System.out.println(
                    "WARNING (move): A bot shot me that I never knew existed!");
        }

        if (is1v1()) {
            duelOpponent().clearNeighborCache();
        }
    }

    public void onBulletHit(BulletHitEvent e) {
        String botName = e.getName();

        try {
            EnemyDataMove enemyData = _enemies.get(botName);
            enemyData.energy -=
                    Rules.getBulletDamage(e.getBullet().getPower());
            enemyData.damageGiven +=
                    Rules.getBulletDamage(e.getBullet().getPower());
        } catch (java.lang.NullPointerException npe) {
            System.out.println(
                    "WARNING (move): One of my bullets hit a bot that I never " +
                            "knew existed!");
        }

    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        String botName = e.getHitBullet().getName();

        try {
            EnemyDataMove enemyData = _enemies.get(botName);
            DiaWave hitWave = processBullet(e.getHitBullet());
            if (hitWave != NO_WAVE_FOUND) {
                enemyData.raw1v1ShotsFired--;
                enemyData.raw1v1ShotsFiredThisRound--;
            }
        } catch (java.lang.NullPointerException npe) {
            System.out.println(
                    "WARNING (move): One of my bullets hit a bullet from a bot " +
                            "that I never knew existed!");
        }

        if (is1v1()) {
            duelOpponent().clearNeighborCache();
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

    public void onWin(WinEvent e) {
        roundOver();
    }

    public void onDeath(DeathEvent e) {
        roundOver();
    }

    protected void roundOver() {
        if (is1v1()) {
            duelOpponent().lastRoundNormalized1v1HitPercentage =
                    normalizedEnemyHitPercentageThisRound();
            System.out.println("Enemy normalized hit %: " + DiaUtils.round(normalizedEnemyHitPercentage(), 2));
            System.out.println("Flattener enabled: " + duelOpponent().flattenerEnabled);
        }
    }

    protected boolean wouldHitWall(Destination d) {
        long ticksAhead = 5;

        double heading = _robot.getHeadingRadians();
        double velocity = _robot.getVelocity();

        RobotState state = new RobotState(_myLocation, heading, velocity);
        boolean hitWall = false;

        for (int x = 0; x < ticksAhead && !hitWall; x++) {
            state = DiaUtils.nextLocation(state.location, state.velocity, 8.0,
                    state.heading, DiaUtils.absoluteBearing(state.location,
                    d.location),
                    _currentTime + x, false, true, _battleFieldWidth,
                    _battleFieldHeight);
            if (!_fieldRect.contains(state.location)) {
                hitWall = true;
            }
        }

        return hitWall;
    }

    protected ArrayList<Destination> generatePointsAroundBot() {
        ArrayList<Destination> destinations = new ArrayList<Destination>();

        double movementStick = Math.min(100 + Math.random() * 100,
                distanceToClosestBot());

        double sliceSize = (2 * Math.PI) / NUM_SLICES_BOT;
        for (int x = 0; x < NUM_SLICES_BOT; x++) {
            double angle = x * sliceSize;
            Point2D.Double dest = DiaUtils.project(_myLocation, angle,
                    movementStick);
            dest.x = DiaUtils.limit(BOT_HALF_WIDTH, dest.x,
                    _battleFieldWidth - BOT_HALF_WIDTH);
            dest.y = DiaUtils.limit(BOT_HALF_WIDTH, dest.y,
                    _battleFieldHeight - BOT_HALF_WIDTH);
            destinations.add(new Destination(dest,
                    evalDestinationRisk(dest, angle), angle));
        }

        return destinations;
    }

    /*
        protected ArrayList<Destination> generatePointsNearPreviousDestination() {
            ArrayList<Destination> destinations = new ArrayList<Destination>();

            double maxDistance = distanceToClosestBot();

            double sliceSize = (2 * Math.PI) / NUM_SLICES_PREVIOUS_DESTINATION;
            for (int x = 0; x < NUM_SLICES_PREVIOUS_DESTINATION; x++) {
                double angle = x * sliceSize;
                Point2D.Double dest = DiaUtils.project(_currentDestination.location,
                    angle, 10 + Math.random() * 40);
                if (_myLocation.distance(dest) <= maxDistance) {
                    if (_fieldRect.contains(dest)) {
                        double angleFromBot =
                            DiaUtils.absoluteBearing(_myLocation, dest);
                        destinations.add(new Destination(dest,
                            evalDestinationRisk(dest, angleFromBot), angleFromBot));
                    }
                }
            }

            return destinations;
        }
    */
    protected double evalDestinationRisk(Point2D.Double destination,
                                         double goAngle) {

        Iterator<EnemyDataMove> edmIterator = _enemies.values().iterator();
        double risk = 0;
        while (edmIterator.hasNext()) {
            EnemyDataMove enemyData = edmIterator.next();
            if (enemyData.alive) {
                double botRisk = 0;
                double distanceSq = destination.distanceSq(enemyData.location);
                botRisk = DiaUtils.limit(0.25, enemyData.energy / _myEnergy, 4)
                        * (1 + Math.abs(
                        Math.cos(enemyData.absBearing - goAngle)))
                        * enemyData.damageFactor
//                        * (20 + Math.sqrt(cornerDistance(destination)))
//                        * (_currentTime - enemyData.lastTimeHit < 100 ? 2 : 1)
//                        * (100 + enemyData.damageTaken) // could cache this
                        / (distanceSq
                        * (enemyData.botsCloser(distanceSq * .8) + 1)
//                            * (100 + enemyData.damageGiven) // could cache this
//                            * (1 + enemyData.sumAbsBearingRisk(destination))
                );
/*
                for (int x = 10; 
                     x < Math.min(101, enemyData.pastLocations.size()); 
                     x += 10) {
                    botRisk *= 1 + (250 / enemyData.getPastLocation(x)
                                .distanceSq(destination));
                }
*/
                risk += botRisk;
            }
        }

        double randomRisk = 0;
        Iterator<OldLocation> rlIterator = _recentLocations.iterator();
        while (rlIterator.hasNext()) {
            OldLocation ol = rlIterator.next();
            randomRisk += 30.0 / ol.location.distanceSq(destination);
        }

        risk *= 1 + randomRisk;

        return risk;
    }

    protected Destination safestDestination(
            ArrayList<Destination> possibleDestinations) {

        double lowestRisk = Double.POSITIVE_INFINITY;
        Destination safest = null;

        Iterator<Destination> destIterator = possibleDestinations.iterator();
        while (destIterator.hasNext()) {
            Destination dest = destIterator.next();
            if (dest.risk < lowestRisk) {
                lowestRisk = dest.risk;
                safest = dest;
            }
        }

        if (safest == null) {
            System.out.println("WARNING: No safe destinations found, there " +
                    "must be a bug in the risk evaluation.");
            safest = _currentDestination;
        }

        return safest;
    }

    /*
        protected double cornerDistance(Point2D.Double p) {
            return Math.sqrt(
                DiaUtils.square(Math.min(p.x, _battleFieldWidth  - p.x)) +
                DiaUtils.square(Math.min(p.y, _battleFieldHeight - p.y)));
         }

        protected double nearestWallDistance(Point2D.Double p) {
            return Math.min(p.y, Math.min(_battleFieldHeight - p.y,
                Math.min(p.x, _battleFieldWidth  - p.x)));
        }
    */
    protected double bulletTicksFromClosestBot() {
        if (_enemies.isEmpty()) {
            return 10;
        }

        return bulletTicksFromBot(closestBot());
    }

    protected double bulletTicksFromBot(String botName) {
        EnemyDataMove ms = _enemies.get(botName);
        double bulletTicks =
                Math.ceil((ms.distance - BOT_HALF_WIDTH)
                        / Rules.getBulletSpeed(ms.avgBulletPower()));

        return bulletTicks;
    }

    protected String closestBot() {
        double closestDistance = Double.POSITIVE_INFINITY;
        String closestBot = null;

        Iterator<String> enemyDataKeys = _enemies.keySet().iterator();
        while (enemyDataKeys.hasNext()) {
            String botName = enemyDataKeys.next();
            EnemyDataMove enemyData = _enemies.get(botName);
            if (enemyData.alive && enemyData.distance < closestDistance) {
                closestDistance = enemyData.distance;
                closestBot = botName;
            }
        }

        return closestBot;
    }

    protected double distanceToClosestBot() {
        double closestDistance = Double.POSITIVE_INFINITY;

        Iterator<String> enemyDataKeys = _enemies.keySet().iterator();
        while (enemyDataKeys.hasNext()) {
            String botName = enemyDataKeys.next();
            EnemyDataMove enemyData = _enemies.get(botName);
            if (enemyData.alive && enemyData.distance < closestDistance) {
                closestDistance = enemyData.distance;
            }
        }

        return closestDistance;
    }

    protected void updateBotDistances() {
        if (_enemies.size() <= 1) {
            return;
        }

        String[] botNames = new String[_enemies.size()];
        _enemies.keySet().toArray(botNames);

        for (int x = 0; x < botNames.length; x++) {
            EnemyDataMove edm1 = _enemies.get(botNames[x]);
            for (int y = x + 1; y < botNames.length; y++) {
                EnemyDataMove edm2 = _enemies.get(botNames[y]);
                if (edm1.alive && edm2.alive) {
                    double distanceSq = edm1.location.distanceSq(edm2.location);
                    edm1.setBotDistanceSq(botNames[y], distanceSq);
                    edm2.setBotDistanceSq(botNames[x], distanceSq);
                } else {
                    if (!edm1.alive) {
                        edm2.removeDistanceSq(botNames[x]);
                    }
                    if (!edm2.alive) {
                        edm1.removeDistanceSq(botNames[y]);
                    }
                }
            }
            edm1.distance = _myLocation.distance(edm1.location);
        }
    }

    public void updateDamageFactors() {
        if (_enemies.size() <= 1) {
            return;
        }

        Iterator<EnemyDataMove> edmIterator = _enemies.values().iterator();
        while (edmIterator.hasNext()) {
            EnemyDataMove enemyData = edmIterator.next();
            enemyData.damageFactor =
                    ((enemyData.damageTaken + 10) / (enemyData.damageGiven + 10))
                            * enemyData.totalDistance
                            / DiaUtils.square(enemyData.timeAliveTogether);
        }
    }

    /*
        protected void updateAvoidBeingTargeted() {
            Iterator<EnemyDataMove> edmIterator = _enemies.values().iterator();
            _minBotsCloser = _enemiesTotal;
            while (edmIterator.hasNext()) {
                EnemyDataMove enemyData = edmIterator.next();
                double distanceSq =
                    _myLocation.distanceSq(enemyData.location);
                if (enemyData.damageTaken * 1.5 >= enemyData.damageGiven) {
                    enemyData.avoidBeingTargeted = true;
                } else {
                    enemyData.avoidBeingTargeted = false;
                }
                int botsCloser = enemyData.botsCloser(distanceSq * .8);
                if (botsCloser < _minBotsCloser) {
                    _minBotsCloser = botsCloser;
                }
            }
        }

        protected void updateStayPerpendicular() {
            Iterator<EnemyDataMove> edmIterator = _enemies.values().iterator();
            while (edmIterator.hasNext()) {
                EnemyDataMove enemyData = edmIterator.next();
                double distanceSq =
                    _myLocation.distanceSq(enemyData.location);
                int botsCloser = enemyData.botsCloser(distanceSq);
                if (botsCloser == 0) {
                    enemyData.lastTimeClosest = _currentTime;
                }
                if (_currentTime - enemyData.lastTimeHit < 75 ||
                    _currentTime - enemyData.lastTimeClosest < 100 ||
                    botsCloser <= 1) {
                    enemyData.stayPerpendicular = true;
                } else {
                    enemyData.stayPerpendicular = false;
                }
            }
        }
    */
    public void surf() {
        if (_opponentName.equals("")) {
            return;
        }

        RobotState currentState = new RobotState(_myLocation,
                _robot.getHeadingRadians(), _robot.getVelocity(), _robot.getTime());
        boolean goingClockwise = (_lastMovementChoice == CLOCKWISE_OPTION);

        Collections.sort(_movementOptions);

        double bestSurfDanger = Double.POSITIVE_INFINITY;
        for (int x = 0; x < _movementOptions.size(); x++) {
            MovementChoice mc = _movementOptions.get(x);
            double thisDanger = checkDanger(currentState,
                    mc.getMovementOption(), goingClockwise, FIRST_WAVE,
                    WAVES_TO_SURF, bestSurfDanger);
            mc.lastDanger = thisDanger;

            bestSurfDanger = Math.min(bestSurfDanger, thisDanger);
        }

        double orbitCounterClockwiseDanger = _optionCounterClockwise.lastDanger;
        double stopDanger = _optionStop.lastDanger;
        double orbitClockwiseDanger = _optionClockwise.lastDanger;

        int goOrientation = _lastMovementChoice;

        DiaWave orbitWave = findSurfableWave(FIRST_WAVE);
        if (orbitWave != _lastWaveSurfed) {
            duelOpponent().clearNeighborCache();
            _lastWaveSurfed = orbitWave;
        }

        double orbitAbsBearing, distanceToClosestWaveSource;
        Point2D.Double orbitPoint;

        if (orbitWave == null) {
            orbitPoint = _enemies.get(_opponentName).location;
        } else {
            orbitPoint = orbitWave.sourceLocation;
        }
        distanceToClosestWaveSource = _myLocation.distance(orbitPoint);
        orbitAbsBearing = DiaUtils.absoluteBearing(orbitPoint, _myLocation);

        double goAngle;
        if (stopDanger == NO_SURFABLE_WAVES) {
            _robot.setMaxVelocity(8);

            double goAngleCcw = orbitAbsBearing +
                    (COUNTERCLOCKWISE_OPTION * ((Math.PI / 2) +
                            DEFAULT_ATTACK_ANGLE));
            goAngleCcw = wallSmoothing(_myLocation, goAngleCcw,
                    COUNTERCLOCKWISE_OPTION, distanceToClosestWaveSource);

            double goAngleCw = orbitAbsBearing +
                    (CLOCKWISE_OPTION * ((Math.PI / 2) + DEFAULT_ATTACK_ANGLE));
            goAngleCw = wallSmoothing(_myLocation, goAngleCw,
                    CLOCKWISE_OPTION, distanceToClosestWaveSource);

            if (Math.abs(Utils.normalRelativeAngle(goAngleCw - orbitAbsBearing))
                    < Math.abs(Utils.normalRelativeAngle(goAngleCcw - orbitAbsBearing))) {
                goOrientation = CLOCKWISE_OPTION;
                goAngle = goAngleCw;
            } else {
                goOrientation = COUNTERCLOCKWISE_OPTION;
                goAngle = goAngleCcw;
            }
        } else {
            _robot.setMaxVelocity(8);
            double attackAngle =
                    _currentDistancer.attackAngle(distanceToClosestWaveSource,
                            _desiredDistance);

            if (stopDanger <= orbitCounterClockwiseDanger &&
                    stopDanger <= orbitClockwiseDanger) {

                _robot.setMaxVelocity(0);
            } else {
                if (orbitClockwiseDanger < orbitCounterClockwiseDanger) {
                    goOrientation = CLOCKWISE_OPTION;
                } else {
                    goOrientation = COUNTERCLOCKWISE_OPTION;
                }
            }

            goAngle = orbitAbsBearing +
                    (goOrientation * ((Math.PI / 2) + attackAngle));
            goAngle = wallSmoothing(_myLocation, goAngle, goOrientation,
                    distanceToClosestWaveSource);

            if (paintStatus()) {
                drawRawWaves();
                for (int x = 0; x < WAVES_TO_SURF; x++) {
                    drawWaveDangers(x);
                }
            }
        }


        DiaUtils.setBackAsFront(_robot, goAngle);

        _lastMovementChoice = goOrientation;

    }

    public double checkDanger(RobotState startState, int movementOption,
                              boolean previouslyMovingClockwise, int surfableWaveIndex,
                              int numWavesToSurf, double cutoffDanger) {

        DiaUtils.log(_className, "checkDanger",
                "movementOption=" + movementOption + ", " +
                        "previouslyMovingClockwise=" + previouslyMovingClockwise + ", " +
                        "surfableWaveIndex=" + surfableWaveIndex + ", " +
                        "numWavesToSurf=" + numWavesToSurf + ", " +
                        "cutoffDanger=" + cutoffDanger,
                true);

        boolean predictClockwise;
        if (movementOption == CLOCKWISE_OPTION) {
            predictClockwise = true;
        } else if (movementOption == COUNTERCLOCKWISE_OPTION) {
            predictClockwise = false;
        } else {
            predictClockwise = previouslyMovingClockwise;
        }

        DiaWave surfWave = findSurfableWave(surfableWaveIndex);

        if (surfWave == null) {
            DiaUtils.log(_className, "checkDanger",
                    Double.toString(NO_SURFABLE_WAVES), false);

            return NO_SURFABLE_WAVES;
        }

        double maxHitInterceptOffset =
                surfWave.bulletSpeed + MAX_WAVE_INTERCEPT_OFFSET;
        double wavePassedInterceptOffset = surfWave.bulletSpeed;
        RobotState predictedState = startState;
        RobotState passedState = startState;

        boolean wavePassed = false;
        boolean waveHit = false;

        ArrayList<RobotState> dangerStates = new ArrayList<RobotState>();

        double maxVelocity = (movementOption == STOP_OPTION) ? 0 : 8;

        do {
            double orbitAbsBearing = DiaUtils.absoluteBearing(
                    surfWave.sourceLocation, predictedState.location);
            double orbitDistance =
                    surfWave.sourceLocation.distance(predictedState.location);
            double attackAngle = _currentDistancer.attackAngle(orbitDistance,
                    _desiredDistance);
            boolean clockwiseSmoothing = predictClockwise;

            if (orbitDistance < _smoothAwayDistance) {
                clockwiseSmoothing = !clockwiseSmoothing;
            }

            predictedState =
                    DiaUtils.nextPerpendicularWallSmoothedLocation(
                            predictedState.location, orbitAbsBearing,
                            predictedState.velocity, maxVelocity,
                            predictedState.heading, attackAngle, clockwiseSmoothing,
                            predictedState.time, _fieldRect, _battleFieldWidth,
                            _battleFieldHeight, _wallStick, OBSERVE_WALL_HITS);

            if (!waveHit &&
                    surfWave.wavePassed(predictedState.location,
                            predictedState.time, maxHitInterceptOffset)) {

                double waveHitInterceptOffset = surfWave.bulletSpeed +
                        DiaUtils.preciseFrontBumperOffset(surfWave.sourceLocation,
                                predictedState.location);

                if (surfWave.wavePassed(predictedState.location,
                        predictedState.time, waveHitInterceptOffset)) {

                    RobotState dangerState = predictedState;
                    boolean waveGone = false;
                    double dangerMaxVelocity = 0;
                    double minGoneInterceptOffset = -BOT_HALF_WIDTH;

                    do {
                        dangerStates.add(dangerState);

                        double dangerOrbitAbsBearing = DiaUtils.absoluteBearing(
                                surfWave.sourceLocation, dangerState.location);
                        double dangerOrbitDistance = surfWave.sourceLocation
                                .distance(dangerState.location);
                        double dangerAttackAngle =
                                _currentDistancer.attackAngle(orbitDistance,
                                        _desiredDistance);
                        boolean dangerClockwiseSmoothing = predictClockwise;

                        if (dangerOrbitDistance < _smoothAwayDistance) {
                            dangerClockwiseSmoothing = !dangerClockwiseSmoothing;
                        }

                        dangerState =
                                DiaUtils.nextPerpendicularWallSmoothedLocation(
                                        dangerState.location, dangerOrbitAbsBearing,
                                        dangerState.velocity, dangerMaxVelocity,
                                        dangerState.heading, dangerAttackAngle,
                                        dangerClockwiseSmoothing, dangerState.time,
                                        _fieldRect, _battleFieldWidth,
                                        _battleFieldHeight, _wallStick,
                                        OBSERVE_WALL_HITS);

                        if (!waveGone &&
                                surfWave.wavePassed(dangerState.location,
                                        dangerState.time, minGoneInterceptOffset)) {

                            if (surfWave.waveGone(dangerState.location,
                                    dangerState.time)) {

                                waveGone = true;
                            }
                        }
                    } while (!waveGone);

                    waveHit = true;
                }
            }

            if (!wavePassed &&
                    surfWave.wavePassed(predictedState.location,
                            predictedState.time, wavePassedInterceptOffset)) {

                passedState = predictedState;
                wavePassed = true;
            }
        } while (!wavePassed);

//        double danger = getDangerScore(surfWave, dangerState.location,
//              surfableWaveIndex);

        double[] angleAndBandwidth = DiaUtils.preciseBotWidth(surfWave,
                dangerStates);
        double hitFactor = surfWave.guessFactor(angleAndBandwidth[0]);
        double bandwidth =
                angleAndBandwidth[1] / Math.asin(8.0 / surfWave.bulletSpeed);
        double danger =
                getDangerScore(surfWave, hitFactor, bandwidth, surfableWaveIndex);

        danger *= Rules.getBulletDamage(surfWave.bulletPower);

        double currentDistanceToWaveSource =
                _myLocation.distance(surfWave.sourceLocation);
        double currentDistanceToWave =
                currentDistanceToWaveSource -
                        surfWave.distanceTraveled(_robot.getTime());
        double timeToImpact = currentDistanceToWave / surfWave.bulletSpeed;

//        if (duelOpponent().flattenerEnabled) {
//            danger /= DiaUtils.square(timeToImpact);          
//        } else {
        danger /= timeToImpact;
//        }

        double firstWaveMultiplier = 1;
        if (surfableWaveIndex == FIRST_WAVE) {
            double predictedDistanceToWaveSource =
                    surfWave.sourceLocation.distance(passedState.location);
            double predictedDistanceToEnemy =
                    _enemies.get(_opponentName).location
                            .distance(passedState.location);

            double shorterDistance = Math.min(predictedDistanceToWaveSource,
                    predictedDistanceToEnemy);
            double distancingDangerBase =
                    currentDistanceToWaveSource / shorterDistance;
            double distancingDangerExponent = shorterDistance > _fearDistance ?
                    NORMAL_DISTANCING_EXPONENT : FEARFUL_DISTANCING_EXPONENT;

            danger *= firstWaveMultiplier = Math.pow(distancingDangerBase,
                    distancingDangerExponent);
            ;
        }

        if (surfableWaveIndex + 1 < numWavesToSurf && danger < cutoffDanger) {
            double nextCounterClockwiseDanger = checkDanger(passedState,
                    COUNTERCLOCKWISE_OPTION, predictClockwise,
                    surfableWaveIndex + 1, numWavesToSurf, cutoffDanger);
            double nextStopDanger = checkDanger(passedState,
                    STOP_OPTION, predictClockwise, surfableWaveIndex + 1,
                    numWavesToSurf, cutoffDanger);
            double nextClockwiseDanger = checkDanger(passedState,
                    CLOCKWISE_OPTION, predictClockwise,
                    surfableWaveIndex + 1, numWavesToSurf, cutoffDanger);

            danger += firstWaveMultiplier * Math.min(nextCounterClockwiseDanger,
                    Math.min(nextStopDanger, nextClockwiseDanger));
        }

        DiaUtils.log(_className, "checkDanger", Double.toString(danger), false);

        return danger;
    }

    public double getDangerScore(DiaWave w, Point2D.Double predictedLocation,
                                 int surfableWaveIndex) {

        double checkFactor = w.guessFactor(predictedLocation);
        double bandwidth = DiaUtils.botWidthAimAngle(
                predictedLocation.distance(w.sourceLocation))
                / Math.asin(8.0 / w.bulletSpeed);

        return getDangerScore(w, checkFactor, bandwidth,
                surfableWaveIndex);
    }

    public double getDangerScore(DiaWave w, double checkFactor,
                                 double bandwidth, int surfableWaveIndex) {

        EnemyDataMove enemyData = _enemies.get(w.botName);

        if (enemyData.guessFactors.size() == 0) {
            return headOnDanger(checkFactor);
        }

        double totalDanger = 0;
        Iterator<DataView> viewsIterator = enemyData.views.iterator();
        double totalScanWeight = 0;
        while (viewsIterator.hasNext()) {
            DataView view = viewsIterator.next();
            if (view.enabled(normalizedEnemyHitPercentage(),
                    duelOpponent().flattenerEnabled) && view.treeSize > 0) {

                double[] wavePoint = view.formula.dataPointFromWave(w);
                double[][] nearestNeighbors;

                if (view.cachedNeighbors.size() > surfableWaveIndex) {
                    nearestNeighbors = view.cachedNeighbors.get(surfableWaveIndex);
                } else {
                    int thisClusterSize =
                            Math.min((int) Math.ceil(view.treeSize / 5.0),
                                    view.clusterSize);
                    nearestNeighbors = KdBucketTree.nearestNeighbors(
                            view.tree, wavePoint, thisClusterSize,
                            view.formula.weights);
                    view.cachedNeighbors.add(nearestNeighbors);
                }

                int numScans = nearestNeighbors.length;
                if (view.decayRate != DataView.NO_DECAY) {
                    TimestampedGuessFactor[] sortedGfs = new TimestampedGuessFactor[numScans];
                    for (int x = 0; x < numScans; x++) {
                        sortedGfs[x] = enemyData.guessFactors.get(nearestNeighbors[x]);
                    }
                    Arrays.sort(sortedGfs);
                    for (int x = 0; x < numScans; x++) {
                        if (x == numScans - 1) {
                            sortedGfs[x].weight = 1;
                        } else {
                            sortedGfs[x].weight = 1 /
                                    DiaUtils.power(view.decayRate, numScans - x);
                        }
                    }
                }

                double density = 0;
                double viewScanWeight = 0;
                for (int x = 0; x < numScans; x++) {
                    TimestampedGuessFactor tsgf =
                            enemyData.guessFactors.get(nearestNeighbors[x]);
                    double xGuessFactor = tsgf.guessFactor;

                    double ux = (xGuessFactor - checkFactor) / bandwidth;
                    double scanWeight = tsgf.weight / KdBucketTree.distance(
                            nearestNeighbors[x], wavePoint, view.formula.weights);

                    // Gaussian
                    density += Math.exp(-0.5 * ux * ux) * scanWeight;

                    viewScanWeight += scanWeight;
                }

                totalScanWeight += viewScanWeight * view.weight;
                totalDanger += view.weight * density;
            }
        }

        return totalDanger / totalScanWeight;
    }

    public void initializeMovementOptions() {
        _movementOptions = new ArrayList<MovementChoice>();

        _movementOptions.add(_optionCounterClockwise =
                new MovementCounterClockwise());
        _movementOptions.add(_optionStop = new MovementStop());
        _movementOptions.add(_optionClockwise = new MovementClockwise());

    }

    public void fireEnemyWave(boolean detectedEnemyWave, String botName,
                              double bulletPower) {

        double currentVelocity = _robot.getVelocity();
        EnemyDataMove enemyData = _enemies.get(botName);
        int velocitySign = DiaUtils.nonZeroSign(
                Math.abs(currentVelocity) > SURF_ORIENTATION_VELOCITY_THRESHOLD ?
                        currentVelocity : _lastNonZeroVelocity);
        double accel = DiaUtils.limit(-Rules.DECELERATION,
                DiaUtils.accel(currentVelocity, _previousVelocity),
                Rules.ACCELERATION);
        double guessedPower = guessBulletPower(enemyData.distance,
                enemyData.energy, _robot.getEnergy());

        DiaWave futureWave = new DiaWave(enemyData.location, _myLocation,
                enemyData.location, _robot.getTime() + 1, guessedPower, botName,
                _robot.getHeadingRadians(), currentVelocity, accel, velocitySign,
                enemyData.distance, 0, _timeSinceReverseDirection,
                _timeSinceVelocityChange, 0, 0,
                _myLocation.distance(
                        _pastLocations.get(Math.min(8, _pastLocations.size() - 1))),
                _myLocation.distance(
                        _pastLocations.get(Math.min(20, _pastLocations.size() - 1))),
                _myLocation.distance(
                        _pastLocations.get(Math.min(40, _pastLocations.size() - 1))),
                0, 0, 0, 0, _robot.getEnergy(), enemyData.energy, 1, 0, 0,
                _fieldRect, _battleFieldWidth, _battleFieldHeight);
        _potentialWaves.addFirst(futureWave);

        _imaginaryWaveIndex = -1;
        for (int x = 0; x < WAVES_TO_SURF; x++) {
            if (findSurfableWave(x) == NO_WAVE_FOUND) {
                _imaginaryWaveIndex = x;
                break;
            }
        }

        double enemyGunHeat = enemyData.getGunHeat(_currentTime);
        if (_imaginaryWaveIndex >= 0 && enemyGunHeat < 0.1000001) {
            enemyData.clearNeighborCache();
            Point2D.Double aimedFromLocation = null;

            if (enemyGunHeat < 0.0000001 && _potentialWaves.size() >= 2) {
                _imaginaryWave = _potentialWaves.get(1);
                aimedFromLocation = enemyData.getPastLocation(1);
            } else {
                _imaginaryWave = futureWave;
                aimedFromLocation = enemyData.location;
                Point2D.Double sourceLocation = DiaUtils.nextLocation(
                        enemyData.location, enemyData.velocity, enemyData.heading);

                if (sourceLocation.distance(_myLocation) < BOT_WIDTH) {
                    sourceLocation = enemyData.location;
                }

                _imaginaryWave.sourceLocation = sourceLocation;
            }

            _imaginaryWave.targetWallDistance =
                    Math.min(1.5, DiaUtils.orbitalWallDistance(
                            aimedFromLocation, _imaginaryWave.targetLocation,
                            bulletPower, _imaginaryWave.orbitDirection, _fieldRect));
            _imaginaryWave.targetRevWallDistance =
                    Math.min(1.5, DiaUtils.orbitalWallDistance(
                            aimedFromLocation, _imaginaryWave.targetLocation,
                            bulletPower, -_imaginaryWave.orbitDirection, _fieldRect));
        }

        if (_potentialWaves.size() >= 3) {
            DiaWave enemyWave = _potentialWaves.get(2);
            Point2D.Double aimedFromLocation =
                    enemyData.getPastLocation(2);
            enemyWave.sourceLocation = enemyData.getPastLocation(1);
            enemyWave.targetLocation = _pastLocations.get(2);
            enemyWave.absBearing =
                    DiaUtils.absoluteBearing(aimedFromLocation,
                            enemyWave.targetLocation);
            enemyWave.setBulletPower(bulletPower);
            enemyWave.targetWallDistance =
                    Math.min(1.5, DiaUtils.orbitalWallDistance(
                            aimedFromLocation, enemyWave.targetLocation,
                            bulletPower, enemyWave.orbitDirection, _fieldRect));
            enemyWave.targetRevWallDistance =
                    Math.min(1.5, DiaUtils.orbitalWallDistance(
                            aimedFromLocation, enemyWave.targetLocation,
                            bulletPower, -enemyWave.orbitDirection, _fieldRect));

            if (detectedEnemyWave) {
                if (_imaginaryWave != NO_WAVE_FOUND) {
                    enemyData.clearNeighborCache();
                }
                _imaginaryWave = NO_WAVE_FOUND;

                enemyWave.firingWave = true;
                enemyData.lastBulletPower = bulletPower;
                enemyData.lastBulletFireTime = enemyWave.fireTime;
                _enemyWaves.addLast(enemyWave);

                double[] dataPoint = bulletPowerDataPoint(enemyWave.targetDistance,
                        enemyWave.sourceEnergy, enemyWave.targetEnergy);
                enemyData.powerTree.insert(dataPoint);
                enemyData.bulletPowers.put(dataPoint, bulletPower);
            } else {
                _virtualWaves.addLast(enemyWave);
            }
        }
    }

    public void checkActiveEnemyWaves() {
        int currentRound = _robot.getRoundNum();
        long currentTime = _robot.getTime();

        for (LinkedList<DiaWave> waves : _waveLists) {
            Iterator<DiaWave> wavesIterator = waves.iterator();
            while (wavesIterator.hasNext()) {
                DiaWave w = wavesIterator.next();
                if (!w.processedWaveBreak &&
                        w.wavePassed(_myLocation, currentTime, 0)) {
                    double visitGuessFactor = w.guessFactor(_myLocation);
                    EnemyDataMove enemyData = _enemies.get(w.botName);

                    if (w.firingWave) {
                        Iterator<DataView> viewsIterator = enemyData.views.iterator();
                        while (viewsIterator.hasNext()) {
                            DataView view = viewsIterator.next();
                            if (view.logVisits) {
                                double[] wavePoint = view.logWave(w);
                                enemyData.guessFactors.put(wavePoint,
                                        new TimestampedGuessFactor(currentRound, currentTime,
                                                visitGuessFactor));
                            }
                        }
//                        _analyzer.registerVisit(w, visitGuessFactor);
                    }
                    w.processedWaveBreak = true;

//                    _analyzer.logGuessFactor(w, visitGuessFactor);
                }

                if (w.processedWaveBreak &&
                        w.waveGone(_myLocation, currentTime)) {
                    wavesIterator.remove();
                }
            }
        }
    }

    public DiaWave processBullet(Bullet bullet) {
        long currentTime = _robot.getTime();
        int currentRound = _robot.getRoundNum();

        Point2D.Double bulletLocation =
                new Point2D.Double(bullet.getX(), bullet.getY());
        String botName = bullet.getName();

        int tightMatchDistanceThreshold = 50;
        DiaWave hitWave = DiaUtils.findClosestWave(_enemyWaves,
                bulletLocation, _robot.getTime(), DiaWave.ANY_WAVE,
                tightMatchDistanceThreshold, bullet.getPower());

        if (hitWave == null) {
            return NO_WAVE_FOUND;
        }

        if (!botName.equals(hitWave.botName)) {
            return NO_WAVE_FOUND;
        }

        double hitGuessFactor = hitWave.guessFactor(bulletLocation);
        EnemyDataMove enemyData = _enemies.get(botName);
        Iterator<DataView> viewsIterator = enemyData.views.iterator();
        while (viewsIterator.hasNext()) {
            DataView view = viewsIterator.next();
            if (view.logBulletHits) {
                double[] wavePoint = view.logWave(hitWave);
                enemyData.guessFactors.put(wavePoint,
                        new TimestampedGuessFactor(currentRound, currentTime,
                                hitGuessFactor));
            }
        }
//        _analyzer.registerBulletHit(hitWave, hitGuessFactor);

        if (paintStatus()) {
            _renderables.add(RoboGraphic.drawLine(
                    hitWave.sourceLocation,
                    DiaUtils.project(hitWave.sourceLocation, hitWave.absBearing,
                            hitWave.distanceTraveled(currentTime)),
                    Color.yellow));
            if (Math.abs(hitGuessFactor) > 0.01) {
                _renderables.add(RoboGraphic.drawLine(
                        hitWave.sourceLocation, bulletLocation,
                        (hitGuessFactor > 0 ? Color.blue : Color.red)));
            }

        }

        hitWave.processedBulletHit = true;

        return hitWave;
    }

    public DiaWave findSurfableWave(int waveIndex) {
        DiaUtils.log(_className, "findSurfableWave", "waveIndex=" + waveIndex, true);

        int searchWaveIndex = 0;
        long currentTime = _robot.getTime();

        DiaWave returnWave = null;
        Iterator<DiaWave> wavesIterator = _enemyWaves.iterator();
        while (wavesIterator.hasNext()) {
            DiaWave w = wavesIterator.next();
            double distanceToWaveSource =
                    _myLocation.distance(w.sourceLocation);
            double distanceToWave = distanceToWaveSource -
                    w.distanceTraveled(currentTime);

            if (!w.processedBulletHit && distanceToWave > w.bulletSpeed) {
                if (searchWaveIndex == waveIndex) {
                    returnWave = w;
                    break;
                } else {
                    searchWaveIndex++;
                }
            }
        }

        if (returnWave == null) {
            if (_imaginaryWave != NO_WAVE_FOUND && waveIndex == _imaginaryWaveIndex) {
                returnWave = _imaginaryWave;
            } else {
                returnWave = NO_WAVE_FOUND;
            }
        }

        DiaUtils.log(_className, "findSurfableWave",
                (returnWave == null ? "null" : returnWave.toString()), false);

        return returnWave;
    }

    public void evaluateDistancing() {
        _desiredDistance = 650;
        _fearDistance = 200;
        _smoothAwayDistance = 75;
    }

    public void evaluateFlattener() {
        EnemyDataMove enemyData = duelOpponent();
        if (enemyData == null) {
            return;
        }

        double normalizedHitPercentageThreshold =
                calculateFlattenerThreshold();
        double thresholdMargin = (enemyData.flattenerEnabled ? -1.0 : 0.0);

        if (normalizedEnemyHitPercentage() >
                normalizedHitPercentageThreshold + thresholdMargin) {
            setFlattener(true);
        } else {
            setFlattener(false);
        }
    }

    public double calculateFlattenerThreshold() {
        double normalizedHitPercentageThreshold;

        normalizedHitPercentageThreshold = 200;
        ;

        EnemyDataMove enemyData = duelOpponent();
        if (_robot.getRoundNum() >= 20 &&
                enemyData.raw1v1ShotsFired > 500) {
            normalizedHitPercentageThreshold = 10.0;
        } else if (_robot.getRoundNum() >= 10 &&
                enemyData.raw1v1ShotsFired > 300) {
            normalizedHitPercentageThreshold = 10.7;
        } else if (_robot.getRoundNum() >= 5 &&
                enemyData.raw1v1ShotsFired > 150) {
            if (normalizedEnemyHitPercentageLastRound() >= 12) {
                normalizedHitPercentageThreshold = 12;
            } else {
                normalizedHitPercentageThreshold = 15;
            }
        }

        return normalizedHitPercentageThreshold;
    }

    public double normalizedEnemyHitPercentage() {
        EnemyDataMove enemyData = duelOpponent();

        return ((enemyData == null ||
                enemyData.raw1v1ShotsFired == 0) ? 0 :
                (((double) enemyData.weighted1v1ShotsHit /
                        enemyData.raw1v1ShotsFired) * 100.0));
    }

    public double normalizedEnemyHitPercentageThisRound() {
        EnemyDataMove enemyData = duelOpponent();

        return ((enemyData == null ||
                enemyData.raw1v1ShotsFiredThisRound == 0) ? 0 :
                (((double) enemyData.weighted1v1ShotsHitThisRound /
                        enemyData.raw1v1ShotsFiredThisRound) * 100.0));
    }

    public double rawEnemyHitPercentage() {
        EnemyDataMove enemyData = duelOpponent();

        return ((enemyData == null ||
                enemyData.raw1v1ShotsFired == 0) ? 0 :
                (((double) enemyData.raw1v1ShotsHit /
                        enemyData.raw1v1ShotsFired) * 100.0));
    }

    public double normalizedEnemyHitPercentageLastRound() {
        EnemyDataMove enemyData = duelOpponent();

        return enemyData == null ? 0 :
                enemyData.lastRoundNormalized1v1HitPercentage;
    }

    public void setFlattener(boolean enableFlattener) {
        boolean oldFlattenerSetting = duelOpponent().flattenerEnabled;

        int preventFlattenerThrashing = 100;
        if (_flattenerToggleTimer++ > preventFlattenerThrashing &&
                oldFlattenerSetting != enableFlattener) {
            _flattenerToggleTimer = 0;

            if (enableFlattener) {
                enableFlattener();
            } else {
                disableFlattener();
            }

            duelOpponent().clearNeighborCache();
        }
    }

    public void enableFlattener() {
        duelOpponent().flattenerEnabled = true;

        System.out.println("Curve Flattening enabled.");
    }

    public void disableFlattener() {
        duelOpponent().flattenerEnabled = false;

        System.out.println("Curve Flattening disabled.");
    }

    public double wallSmoothing(Point2D.Double startLocation,
                                double goAngleRadians, int orientation, double currentDistance) {

        if (currentDistance < _smoothAwayDistance) {
            orientation *= -1;
        }

        double smoothedAngle = DiaUtils.wallSmoothing(
                _fieldRect, _battleFieldWidth, _battleFieldHeight, startLocation,
                goAngleRadians, orientation, _wallStick);

        return smoothedAngle;
    }

    // In FFA, fine to do this from execute().
    // In 1v1, want to do it before firing waves, so we do
    // it from onScannedRobot.
    public void updateTimeSinceTimers() {
        if (Math.abs(Utils.normalRelativeAngle(_currentHeading -
                _previousHeading)) > DIRECTION_CHANGE_THRESHOLD) {
            _timeSinceReverseDirection = 0;
        } else {
            _timeSinceReverseDirection++;
        }

        double newVelocity = _robot.getVelocity();
        if (Math.abs(newVelocity - _lastVelocity) > 0.5) {
            _timeSinceVelocityChange = 0;
        } else {
            _timeSinceVelocityChange++;
        }
        _lastVelocity = newVelocity;
    }

    public void drawRisks(ArrayList<Destination> destinations) {
        double lowestRisk = Double.POSITIVE_INFINITY;
        double highestRisk = Double.NEGATIVE_INFINITY;

        Iterator<Destination> destIterator = destinations.iterator();
        double[] risks = new double[destinations.size()];
        int x = 0;
        while (destIterator.hasNext()) {
            Destination d = destIterator.next();
            risks[x++] = d.risk;
            if (d.risk < lowestRisk) {
                lowestRisk = d.risk;
            }

            if (d.risk > highestRisk) {
                highestRisk = d.risk;
            }
        }


        double avg = DiaUtils.average(risks);
        double stDev = DiaUtils.standardDeviation(risks);

        destIterator = destinations.iterator();

        while (destIterator.hasNext()) {
            Destination d = destIterator.next();

            _renderables.add(RoboGraphic.drawCircleFilled(d.location,
                    riskColor(d.risk - lowestRisk, avg - lowestRisk,
                            stDev, true, 1), 2));
        }
    }

    public void drawRawWaves() {
        long currentTime = _robot.getTime();
        Iterator<DiaWave> wavesIterator = _enemyWaves.iterator();
        while (wavesIterator.hasNext()) {
            DiaWave w = wavesIterator.next();
            _renderables.add(RoboGraphic.drawCircle(w.sourceLocation,
                    (currentTime - w.fireTime + 1) * w.bulletSpeed, Color.darkGray));
            _renderables.add(RoboGraphic.drawLine(w.sourceLocation,
                    DiaUtils.project(w.sourceLocation, w.absBearing,
                            w.distanceTraveled(currentTime + 1)), Color.darkGray));
            _renderables.add(RoboGraphic.drawCircle(
                    DiaUtils.project(w.sourceLocation, w.absBearing,
                            w.distanceTraveled(currentTime + 1)),
                    5, Color.darkGray));
            _renderables.add(RoboGraphic.drawCircleFilled(w.sourceLocation,
                    Color.darkGray, 4));
        }
    }

    public void drawWaveDangers(int surfableWaveIndex) {
        DiaWave surfWave = findSurfableWave(surfableWaveIndex);
        if (surfWave == null) {
            return;
        }

        double maxEscapeAngle = Math.asin(8.0 / surfWave.bulletSpeed);

        int numBins = 51;
        double halfBins = (double) (numBins - 1) / 2;

        double[] gfDangers = new double[numBins];
        double minDanger = Double.POSITIVE_INFINITY;

        for (int x = 0; x <= numBins - 1; x++) {
            double gf = (x - halfBins) / halfBins;
            double bearingOffset = surfWave.orbitDirection *
                    (gf * maxEscapeAngle);
            Point2D.Double dangerLocation = DiaUtils.project(
                    surfWave.sourceLocation, surfWave.absBearing + bearingOffset,
                    surfWave.distanceTraveled(_currentTime));
            gfDangers[x] =
                    getDangerScore(surfWave, dangerLocation, surfableWaveIndex);

            if (gfDangers[x] < minDanger) {
                minDanger = gfDangers[x];
            }
        }

        double avg = DiaUtils.average(gfDangers);
        double stDev = DiaUtils.standardDeviation(gfDangers);

        for (int x = 0; x <= numBins - 1; x++) {
            double gf = (x - halfBins) / halfBins;
            double bearingOffset = surfWave.orbitDirection *
                    (gf * maxEscapeAngle);
            Point2D.Double drawLocation = DiaUtils.project(
                    surfWave.sourceLocation, surfWave.absBearing + bearingOffset,
                    surfWave.distanceTraveled(_currentTime) - 12);

            _renderables.add(RoboGraphic.drawCircleFilled(drawLocation,
                    riskColor(gfDangers[x] - minDanger, avg - minDanger,
                            stDev, false, 1), 2));
        }
    }

    public static Color riskColor(double risk, double avg,
                                  double stDev, boolean safestYellow, double maxStDev) {

        if (risk < .0000001 && safestYellow) {
            return Color.yellow;
        }

        return new Color(
                (int) DiaUtils.limit(0, 255 * (risk - (avg - maxStDev * stDev))
                        / (2 * maxStDev * stDev), 255),
                0,
                (int) DiaUtils.limit(0, 255 * ((avg + maxStDev * stDev) - risk)
                        / (2 * maxStDev * stDev), 255));
    }

    public boolean is1v1() {
        return (_enemiesAlive <= 1);
    }

    public EnemyDataMove duelOpponent() {
        return _enemies.get(_opponentName);
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
        return "Movement";
    }

    public boolean paintStatus() {
        return (_painting && _robocodePainting);
    }

    public static double headOnDanger(double checkFactor) {
        return 1 - DiaUtils.square(checkFactor);
    }

    static double[] bulletPowerWeights = new double[]{3, 5, 1};

    public double[] bulletPowerDataPoint(double distance, double enemyEnergy,
                                         double myEnergy) {

        return new double[]{
                Math.min(distance, 800) / 800,
                Math.min(enemyEnergy, 125) / 125,
                Math.min(myEnergy, 125) / 125
        };
    }

    public double guessBulletPower(double distance, double enemyEnergy,
                                   double myEnergy) {

        int numBullets = duelOpponent().bulletPowers.size();
        if (numBullets == 0) {
            return 1.9;
        } else {
            double[] searchPoint = bulletPowerDataPoint(distance, enemyEnergy, myEnergy);
            double[][] bulletPowers = KdBucketTree.nearestNeighbors(
                    duelOpponent().powerTree, searchPoint,
                    (int) Math.min(20, Math.ceil(numBullets / 3.0)),
                    bulletPowerWeights);

            double powerTotal = 0;
            for (int x = 0; x < bulletPowers.length; x++) {
                powerTotal += duelOpponent().bulletPowers.get(bulletPowers[x]);
            }

            return DiaUtils.round(powerTotal / bulletPowers.length, 6);
        }
    }
}

interface DistanceController {
    public double attackAngle(double currentDistance, double desiredDistance);
}

class BasicDistancing implements DistanceController {
    public double attackAngle(double currentDistance, double desiredDistance) {

        double distanceOffset = currentDistance - desiredDistance;
        double attackAngle = DiaUtils.limit(-DiamondWhoosh.MAX_ATTACK_ANGLE,
                (distanceOffset / desiredDistance) * 1.25,
                DiamondWhoosh.MAX_ATTACK_ANGLE);

        return attackAngle;
    }
}

abstract class MovementChoice implements java.lang.Comparable<MovementChoice> {
    public double lastDanger = Double.POSITIVE_INFINITY;

    abstract public int getMovementOption();

    public int compareTo(MovementChoice m2) {
        if (lastDanger < m2.lastDanger) {
            return -1;
        } else if (lastDanger > m2.lastDanger) {
            return 1;
        } else {
            return 0;
        }
    }
}

class MovementCounterClockwise extends MovementChoice {
    public int getMovementOption() {
        return DiamondWhoosh.COUNTERCLOCKWISE_OPTION;
    }
}

class MovementStop extends MovementChoice {
    public int getMovementOption() {
        return DiamondWhoosh.STOP_OPTION;
    }
}

class MovementClockwise extends MovementChoice {
    public int getMovementOption() {
        return DiamondWhoosh.CLOCKWISE_OPTION;
    }
}
