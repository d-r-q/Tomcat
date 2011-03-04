package kc.serpent.movement;

import kc.serpent.gun.*;
import kc.serpent.utils.*;
import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

public class MovementBase {
    public static boolean isMC = false;
    public static boolean isMelee = false;
    AdvancedRobot robot;

    public MovementBase(AdvancedRobot robot) {
        this.robot = robot;
    }

    static final double FLEE_THRESHOLD = 133.0;
    static final double ANTI_RAM_SMOOTH_MARGIN = 25.0;
    static final int RECORDED_POSITION_TICKS = 10;

    static final int ANTI_HOT = 0, ANTI_SIMPLE = 1, ANTI_NORMAL = 2, ANTI_ADVANCED = 3;
    static final int NORMAL_MODE = 0, WITHDRAW_MODE = 1, FLEE_MODE = 2;

    static double NORMAL_BULLET_SPEED;
    static double MAX_DISTANCE;

    Point2D.Double myLocation;
    Point2D.Double enemyLocation;
    static Point2D.Double center;
    static double battleFieldWidth;
    static double battleFieldHeight;
    static Rectangle2D battleField;
    static Rectangle2D antiRamSmoothingField;
    WallSmoother smoother = new WallSmoother();

    long scanTicks;
    long gameTime;
    int myOrbitDirection;
    double absoluteBearing;
    double myEnergy;
    double myHeading;
    double myVelocity;
    double lastVelocity;
    double enemyEnergy;
    double enemyVelocity;
    double enemyHeading;
    double enemyDistance;
    double enemyGunHeat;
    long lastVChangeTime;
    ArrayList myPositionHistory = new ArrayList();

    int lastOrbitDirection;
    double lastAbsoluteBearing;
    double normalizedDistance;
    double latVelocity;
    double accel;
    double vChangeTimer;
    double accelTimer;
    double deccelTimer;
    double lastDTraveled;
    double wallAhead;
    double wallReverse;

    double totalEscapeAngle;
    double totalDistanceFactor;
    double damageRecieved;
    double enemyHitRate;
    double lastRoundEnemyHitRate;
    int enemyShots;
    int roundEnemyShots;
    int bulletCollisions;
    int roundBulletCollisions;
    int enemyHits;
    int nonHeadOnHits;
    int movementMode;
    int antiRamMode;
    int currentDirection;

    Move[][] move = new Move[3][3];

    long lastEnemyFireTime;
    double lastEnemyBulletPower;

    boolean surfableWaveExists;

    MovementWave virtualWave;
    MovementWave nearestSurfableWave;
    MovementWave secondarySurfableWave;
    ArrayList surfableWaves = new ArrayList();
    ArrayList dangerousWaves = new ArrayList();
    ArrayList enemyWaves = new ArrayList();

    MovementSystem movement = new SHLMovement();

    public void init() {
        move[0][0] = new Move(-1, -1);
        move[0][1] = new Move(-1, 0);
        move[0][2] = new Move(-1, 1);
        move[1][0] = new Move(0, -1);
        move[1][1] = new Move(0, 0);
        move[1][2] = new Move(0, 1);
        move[2][0] = new Move(1, -1);
        move[2][1] = new Move(1, 0);
        move[2][2] = new Move(1, 1);

        battleFieldWidth = robot.getBattleFieldWidth();
        battleFieldHeight = robot.getBattleFieldHeight();
        center = new Point2D.Double(battleFieldWidth / 2, battleFieldHeight / 2);
        battleField = KUtils.makeField(battleFieldWidth, battleFieldHeight, 18.0);
        antiRamSmoothingField = KUtils.makeField(battleFieldWidth, battleFieldHeight, ANTI_RAM_SMOOTH_MARGIN);

        MAX_DISTANCE = Math.max(battleFieldWidth, battleFieldHeight);
        NORMAL_BULLET_SPEED = isMC ? 11 : KUtils.bulletSpeed(2.0);

        smoother.init(battleFieldWidth, battleFieldHeight);
        movement.init(this);
    }

    public void reset() {
        currentDirection = 1;
        nonHeadOnHits = 0;
        roundEnemyShots = 0;
        roundBulletCollisions = 0;
        lastEnemyFireTime = -1;
        lastEnemyBulletPower = 3;
        lastRoundEnemyHitRate = enemyHitRate;

        gameTime = -1;
        myOrbitDirection = 1;

        myEnergy = 100;
        enemyEnergy = 100;
        enemyVelocity = 0;
        enemyGunHeat = Double.POSITIVE_INFINITY;
        lastVelocity = 0;
        lastVChangeTime = 0;
        scanTicks = 0;
        myPositionHistory.clear();

        surfableWaveExists = false;
        nearestSurfableWave = secondarySurfableWave = virtualWave = null;
        surfableWaves.clear();
        dangerousWaves.clear();
        enemyWaves.clear();

        movement.reset();

        setMovementMode();
        System.out.println("Using " + getMovementModeName() + " movement");
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (gameTime != robot.getTime()) {
            updateCurrentState();
        }
        if (robot.getOthers() > 0) {
            enemyLocation = KUtils.projectMotion(myLocation, myHeading + e.getBearingRadians(), e.getDistance());
        }
        enemyDistance = myLocation.distance(enemyLocation);
        absoluteBearing = KUtils.absoluteBearing(myLocation, enemyLocation);
        if (Math.sin(myHeading - absoluteBearing) * myVelocity != 0) {
            myOrbitDirection = -KUtils.sign(Math.sin(myHeading - absoluteBearing) * myVelocity);
        }

        double wallDamage = 0;
        if (Math.abs(e.getVelocity()) == 0 && Math.abs(enemyVelocity) > 2.0) {
            wallDamage = Math.max(0, (Math.abs(enemyVelocity) / 2) - 1);
        }
        enemyHeading = e.getHeadingRadians();
        enemyVelocity = e.getVelocity();
        double energyDifference = enemyEnergy - e.getEnergy() - wallDamage;
        enemyEnergy = e.getEnergy();
        if (energyDifference > 0.09999 && energyDifference < 3.0001 && scanTicks > 2 && (enemyGunHeat <= -0.1 || enemyGunHeat > 1000)) {
            clearVirtualWave();

            lastEnemyFireTime = gameTime - 1;
            lastEnemyBulletPower = energyDifference;
            enemyGunHeat = 1 + (energyDifference / 5);

            if (lastEnemyBulletPower >= 1.0) {
                GunBase.isAntiBulletShielding = false;
            }
        }

        enemyGunHeat -= robot.getGunCoolingRate();

        if (scanTicks > 1) {
            MovementWave w = new MovementWave();
            w.isReal = false;
            w.source = enemyLocation;
            w.absoluteBearing = lastAbsoluteBearing + Math.PI;
            w.power = Math.min(enemyEnergy, lastEnemyBulletPower);
            w.speed = KUtils.bulletSpeed(w.power);
            w.maxEscapeAngle = KUtils.maxEscapeAngle(w.speed);
            w.fireTime = gameTime;
            w.orbitDirection = lastOrbitDirection;

            w.normalizedDistance = normalizedDistance;
            w.latVelocity = latVelocity;
            w.accel = accel;
            w.vChangeTimer = vChangeTimer;
            w.lastDTraveled = lastDTraveled;
            w.wallAhead = wallAhead;
            w.wallReverse = wallReverse;

            w.normalizedDistance /= w.speed;
            w.vChangeTimer /= w.speed;
            w.wallAhead /= w.maxEscapeAngle;
            w.wallReverse /= w.maxEscapeAngle;

            if (enemyGunHeat <= 0) {
                w.isReal = true;
                virtualWave = w;
                movement.setWaveFeatures(w);
                surfableWaves.add(w);
                enemyGunHeat = Double.POSITIVE_INFINITY;
            }

            enemyWaves.add(w);
            updateWaves();
        }

        if ((Math.abs(Utils.normalRelativeAngle(absoluteBearing - e.getHeadingRadians())) < Math.PI / 6 && enemyVelocity < -4)
                || (Math.abs(Utils.normalRelativeAngle(absoluteBearing - e.getHeadingRadians() - Math.PI)) < Math.PI / 6 && enemyVelocity > 4)
                || (enemyDistance < 0.0 && enemyEnergy > 0)) {
            if (enemyDistance < FLEE_THRESHOLD) {
                antiRamMode = FLEE_MODE;
            } else {
                antiRamMode = WITHDRAW_MODE;
            }
        } else {
            antiRamMode = NORMAL_MODE;
        }

        if (surfableWaveExists && antiRamMode != FLEE_MODE) {
            surf();
        } else {
            position();
        }

        int roundsLeft = robot.getNumRounds() - robot.getRoundNum() - 1;
        if (!isMelee
                && surfableWaveExists
                && !isMC
                && enemyHits == 0
                && ((nearestSurfableWave.power <= 0.3 && roundsLeft < 6)
                || (roundsLeft < 3 && nearestSurfableWave.power <= 3 - roundsLeft))) {
            robot.setMaxVelocity(0);
        }

        lastAbsoluteBearing = absoluteBearing;
        accel = Math.abs(myVelocity - lastVelocity) * (Math.abs(myVelocity) < Math.abs(lastVelocity) ? -1 : 1);

        normalizedDistance = enemyDistance * NORMAL_BULLET_SPEED;
        lastVelocity = myVelocity;
        latVelocity = Math.sin(myHeading - absoluteBearing) * myVelocity;
        if (Math.abs(accel) > 0.01) {
            lastVChangeTime = gameTime;
        }
        vChangeTimer = (double) (gameTime - lastVChangeTime);

        lastOrbitDirection = myOrbitDirection;

        myPositionHistory.add((Point2D.Double) (myLocation));
        if (myPositionHistory.size() > RECORDED_POSITION_TICKS + 1) {
            myPositionHistory.remove(0);
        }
        Point2D.Double latestPoint = myLocation;
        Point2D.Double earliestPoint = (Point2D.Double) (myPositionHistory.get(0));
        lastDTraveled = latestPoint.distance(earliestPoint);

        wallAhead = 1.5;
        wallReverse = 1.5;
        for (wallAhead = 0; wallAhead <= 1.5; wallAhead += 0.005) {
            Point2D.Double projectedLocation = KUtils.projectMotion(enemyLocation, absoluteBearing + Math.PI + (myOrbitDirection * wallAhead), enemyDistance);
            if (!battleField.contains(projectedLocation)) {
                break;
            }
        }
        for (wallReverse = 0; wallReverse <= 1.5; wallReverse += 0.005) {
            Point2D.Double projectedLocation = KUtils.projectMotion(enemyLocation, absoluteBearing + Math.PI - (myOrbitDirection * wallReverse), enemyDistance);
            if (!battleField.contains(projectedLocation)) {
                break;
            }
        }

        scanTicks++;
    }

    public void position() {
        robot.setMaxVelocity(8);

        int bestDirection = 0;
        double bestTurn = 0;
        double bestValue = Double.NEGATIVE_INFINITY;
        double centerDistance = myLocation.distance(center);

        Point2D.Double enemyTarget = KUtils.projectMotion(enemyLocation, enemyHeading, enemyDistance * KUtils.sign(enemyVelocity));
        double enemyTargetDistance = myLocation.distance(enemyTarget);

        for (double testHeading = 0; testHeading < 2 * Math.PI; testHeading += (2 * Math.PI) / 500) {
            double turn = Utils.normalRelativeAngle(testHeading - myHeading);
            int direction = 1;
            if (turn < -Math.PI / 2) {
                turn += Math.PI;
                direction = -1;
            }
            if (turn > Math.PI / 2) {
                turn -= Math.PI;
                direction = -1;
            }

            Point2D.Double testLocation = KUtils.projectMotion(myLocation, testHeading, 10);

            double value = Math.abs(Math.sin(Utils.normalRelativeAngle(absoluteBearing - testHeading)));
            if (enemyEnergy < 10 && myEnergy > 20 && (enemyGunHeat < -0.5 || enemyGunHeat > 10000)) {
                value -= (testLocation.distance(enemyLocation) - enemyDistance) * enemyDistance / 10000;
            } else {
                value += ((testLocation.distance(enemyLocation) - enemyDistance) / enemyDistance) * (gameTime < 50 ? 70 : 30);
            }
            value -= centerDistance * (testLocation.distance(center) - centerDistance) / 100000;

            if (antiRamMode == FLEE_MODE) {
                testLocation = KUtils.projectMotion(myLocation, testHeading, enemyDistance / 15);
                value = (testLocation.distance(enemyLocation) - enemyDistance) * 20;
                value += (testLocation.distance(enemyTarget) - enemyTargetDistance) * 10;
                value -= (testLocation.distance(center) - centerDistance) * 3;
            }


            if (!antiRamSmoothingField.contains(KUtils.projectMotion(myLocation, testHeading, antiRamMode == FLEE_MODE ? 120 : 170))) {
                value -= 1000000000;
            }

            if (value > bestValue) {
                bestValue = value;
                bestTurn = turn;
                bestDirection = direction;
            }
        }

        currentDirection = bestDirection;
        robot.setTurnRightRadians(antiRamMode == FLEE_MODE ? KUtils.sign(bestTurn) : bestTurn);
        robot.setAhead(Double.POSITIVE_INFINITY * bestDirection);
    }

    public void surf() {
        robot.setMaxVelocity(8);

        for (int i = 0; i < 3; i++) {
            move[i][0].init(myLocation, myHeading, myVelocity);
            move[i][0].setLocation(true);
            move[i][0].riskMod *= Math.pow(enemyDistance / move[i][0].location.distance(enemyLocation), KUtils.quadratic(enemyDistance, 5.4700855e-6, -0.00936752, 6));
        }

        if (!(move[0][0].hitWall && move[2][0].hitWall)) {
            if (move[0][0].hitWall) {
                move[0][0].riskMod *= 5;
            }
            if (move[2][0].hitWall) {
                move[2][0].riskMod *= 5;
            }
        }

        if (move[1][0].hitWall) {
            move[1][0].riskMod *= 5;
        }

        if (antiRamMode == WITHDRAW_MODE) {
            move[1][0].riskMod *= 1000;
        }

        if (secondarySurfableWave != null) {
            for (int i = 0; i < 3; i++) {
                move[i][1].init(move[i][0]);
                move[i][2].init(move[i][0]);
            }
            for (int i = 0; i < 3; i++) {
                for (int ii = 0; ii < 3; ii++) {
                    move[i][ii].setLocation(false);
                    move[i][ii].risk *= move[i][ii].riskMod;
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                move[i][1].risk = Double.POSITIVE_INFINITY;
                move[i][2].risk = Double.POSITIVE_INFINITY;
                move[i][0].risk *= move[i][0].riskMod;
            }
        }

        Move bestMove = move[0][0];
        for (int i = 0; i < 3; i++) {
            for (int ii = 0; ii < 3; ii++) {
                if (move[i][ii].risk < bestMove.risk) {
                    bestMove = move[i][ii];
                }
            }
        }

        double maxVelocity = bestMove.firstDirection == 0 ? 0 : 8;
        currentDirection = bestMove.firstDirection == 0 ? currentDirection : bestMove.firstDirection;
        robot.setMaxVelocity(maxVelocity);
        robot.setAhead(Double.POSITIVE_INFINITY * currentDirection);

        double targetHeading = optimumHeading(myLocation, myHeading, RobotPredictor.nextVelocity(myVelocity, currentDirection, maxVelocity), myVelocity, absoluteBearing, currentDirection);
        robot.setTurnRightRadians(Utils.normalRelativeAngle(targetHeading - myHeading));
    }

    public class Move {
        Point2D.Double location;
        double heading;
        double velocity;

        int firstDirection;
        int secondDirection;
        int time;
        double risk;
        double riskMod;
        boolean hitWall;

        double[] window;

        public Move(int firstDirection, int secondDirection) {
            this.firstDirection = firstDirection;
            this.secondDirection = secondDirection;
        }

        public void init(Point2D.Double location, double heading, double velocity) {
            this.location = new Point2D.Double(location.getX(), location.getY());
            this.heading = heading;
            this.velocity = velocity;

            risk = 0;
            time = 0;
            riskMod = 1;
            hitWall = false;
            window = new double[2];
        }

        public void init(Move m) {
            this.location = new Point2D.Double(m.location.getX(), m.location.getY());
            this.heading = m.heading;
            this.velocity = m.velocity;
            this.time = m.time;
            this.risk = m.risk;
            this.hitWall = m.hitWall;
            this.riskMod = m.riskMod;
            window = new double[2];
        }

        public void setLocation(boolean surfingFirst) {
            int testDirection = surfingFirst ? firstDirection : secondDirection;

            MovementWave w = surfingFirst ? nearestSurfableWave : secondarySurfableWave;
            if (w == null) {
                w = (MovementWave) enemyWaves.get(enemyWaves.size() - 1);
            }

            int direction = testDirection == 0 ? currentDirection : testDirection;
            double maxVelocity = testDirection == 0 ? 0 : 8;

            boolean precisionSurf = (surfingFirst || secondarySurfableWave.impactTime - nearestSurfableWave.impactTime < 8);
            boolean baseRiskSet = false;

            while (true) {
                double waveBearing = KUtils.absoluteBearing(w.source, location);
                double absoluteBearing = KUtils.absoluteBearing(location, enemyLocation);

                time++;
                double waveRadius = w.radius + (time * w.speed);

                if (waveRadius >= PreciseUtils.minWaveDistance(waveBearing, location, w)) {
                    if (precisionSurf) {
                        if (window[0] == 0) {
                            window[0] = waveBearing;
                        }
                        if (window[1] == 0) {
                            window[1] = waveBearing;
                        }

                        double[] currentWindow = PreciseUtils.getInterceptRange(location, waveBearing, waveRadius, w);
                        if (currentWindow[0] != 0 && currentWindow[0] < Utils.normalRelativeAngle(window[0] - waveBearing)) {
                            window[0] = currentWindow[0] + waveBearing;
                        }
                        if (currentWindow[1] != 0 && currentWindow[1] > Utils.normalRelativeAngle(window[1] - waveBearing)) {
                            window[1] = currentWindow[1] + waveBearing;
                        }
                    }

                    if (!baseRiskSet) {
                        baseRiskSet = true;

                        if (surfingFirst) {
                            if (time != 1) {
                                setRisk(false, true, false, false);//imprecise on first wave
                            }
                        } else {
                            setRisk(false, false, true, true);//imprecise on all other waves
                        }

                        if (!precisionSurf) {
                            break;
                        }
                    }
                }

                if (precisionSurf && waveRadius - w.speed > PreciseUtils.maxWaveDistance(waveBearing, location, w) && time != 1) {
                    if (surfingFirst) {
                        setRisk(true, true, false, false);//precise on first wave
                    } else {
                        setRisk(true, false, true, false);//precise on second wave
                    }
                    break;
                }

                double nextVelocity = RobotPredictor.nextVelocity(velocity, direction, maxVelocity);
                double targetHeading = optimumHeading(location, heading, nextVelocity, velocity, absoluteBearing, direction);
                double targetTurn = Utils.normalRelativeAngle(targetHeading - heading);

                heading += RobotPredictor.turnIncrement(targetTurn, velocity);
                velocity = nextVelocity;
                location = KUtils.projectMotion(location, heading, velocity);

                if (!battleField.contains(location) && surfingFirst) {
                    hitWall = true;
                    velocity = 0;
                    location.x = KUtils.minMax(location.x, 18.0, battleFieldWidth - 18.0);
                    location.y = KUtils.minMax(location.y, 18.0, battleFieldHeight - 18.0);
                }
            }
        }

        public void setRisk(boolean isPrecise, boolean surfFirst, boolean surfSecond, boolean surfOthers) {
            Iterator i = surfableWaves.iterator();
            while (i.hasNext()) {
                MovementWave w = (MovementWave) (i.next());

                if (!surfFirst && w == nearestSurfableWave) {
                    continue;
                }
                if (!surfSecond && w == secondarySurfableWave) {
                    continue;
                }
                if (!surfOthers && w != nearestSurfableWave && w != secondarySurfableWave) {
                    return;
                }

                double GF = w.getGF(location);

                if (!isPrecise) {
                    if (movementMode == ANTI_HOT) {
                        risk += KUtils.sixteenth(2 - Math.abs(GF)) * w.weight;
                    } else {
                        risk += movement.getRisk(GF, movementMode, w) * w.weight;
                    }
                } else {
                    for (int n = 0; n < 2; n++) {
                        window[n] = Utils.normalRelativeAngle(window[n] - w.absoluteBearing) / w.maxEscapeAngle;
                    }

                    if (w.orbitDirection == -1) {
                        double temp = -window[0];
                        window[0] = -window[1];
                        window[1] = temp;
                    }

                    if (movementMode == ANTI_HOT) {
                        if (KUtils.inBounds(0.0, 0.02, window)) {
                            risk += 262144.0 * w.weight;
                        }
                    } else {
                        risk += movement.getRisk(window, movementMode, w) * w.weight;
                    }
                }
            }
        }
    }

    double optimumHeading(Point2D.Double location, double heading, double nextVelocity, double velocity, double absoluteBearing, int direction) {
        int orbitDirection;
        double headingFactor = Math.sin(heading - absoluteBearing);
        if (nextVelocity != 0) {
            orbitDirection = -KUtils.sign(headingFactor * nextVelocity);
            direction = KUtils.sign(nextVelocity);
        } else {
            orbitDirection = -KUtils.sign(headingFactor * direction);
        }

        double distance = location.distance(enemyLocation);
        double evasion = 0;

        if (movementMode == ANTI_HOT) {
            evasion = KUtils.quartic(distance, -1.77739e-12, 3.49197e-9, -2.96805e-6, 0.0016218, -0.398912);
        } else if (movementMode == ANTI_SIMPLE) {
            evasion = KUtils.quartic(distance, -1.09266e-12, 3.99994e-9, -4.98975e-6, 0.0027317, -0.505602);
        } else if (movementMode == ANTI_NORMAL || movementMode == ANTI_ADVANCED) {
            evasion = KUtils.quartic(distance, -1.15151515e-12, 3.68686869e-9, -4.39393939e-6, 0.0032434343, -0.895454545);
        }
        if (antiRamMode == WITHDRAW_MODE) {
            evasion = -0.9;
        }

        double parallelHeading = (Math.abs(Utils.normalRelativeAngle(absoluteBearing - heading + (Math.PI / 2))) < Math.abs(Utils.normalRelativeAngle(absoluteBearing - heading - (Math.PI / 2)))
                ? absoluteBearing + (Math.PI / 2) : absoluteBearing - (Math.PI / 2));

        double targetHeading = parallelHeading + (evasion * orbitDirection);
        double turn = Utils.normalRelativeAngle(targetHeading - heading);
        double nextHeading = heading + RobotPredictor.turnIncrement(turn, velocity);

        return smoother.walkingStickSmooth(location, nextHeading, location.distance(enemyLocation), direction, orbitDirection);

        /*double offset = (nextVelocity == 0 ?
                            (direction == 1 ? 0 : Math.PI) :
                          (KUtils.sign(nextVelocity) == 1 ? 0 : Math.PI));

          return smoother.fancyStickSmooth(Utils.normalAbsoluteAngle(nextHeading + offset), Math.abs(nextVelocity), location.x, location.y, orbitDirection) + offset;*/
    }

    public void updateCurrentState() {
        gameTime = robot.getTime();
        myLocation = new Point2D.Double(robot.getX(), robot.getY());
        myHeading = robot.getHeadingRadians();
        myVelocity = robot.getVelocity();
        myEnergy = robot.getEnergy();
    }

    public void updateWaves() {
        int n = 0;
        while (n < enemyWaves.size()) {
            MovementWave w = (MovementWave) (enemyWaves.get(n));
            n++;

            if ((lastEnemyFireTime == w.fireTime) && !w.isReal) {
                w.normalizedDistance *= w.speed;
                w.vChangeTimer *= w.speed;
                w.wallAhead *= w.maxEscapeAngle;
                w.wallReverse *= w.maxEscapeAngle;

                w.isReal = true;
                w.power = lastEnemyBulletPower;
                w.speed = KUtils.bulletSpeed(lastEnemyBulletPower);
                w.maxEscapeAngle = KUtils.maxEscapeAngle(w.speed);

                w.normalizedDistance /= w.speed;
                w.vChangeTimer /= w.speed;
                w.wallAhead = Math.min(w.wallAhead / w.maxEscapeAngle, 1.5);
                w.wallReverse = Math.min(w.wallReverse / w.maxEscapeAngle, 1.5);

                movement.setWaveFeatures(w);

                surfableWaves.add(w);
                dangerousWaves.add(w);
            }

            w.setRadius(gameTime);
            double angle = KUtils.absoluteBearing(myLocation, w.source);
            w.distance = myLocation.distance(w.source);
            w.impactTime = (int) (Math.max((PreciseUtils.minWaveDistance(angle, myLocation, w) - w.radius) / w.speed, 1));
            w.weight = (w.power + 3) / (w.impactTime + 1);

            if (w.radius > PreciseUtils.minWaveDistance(angle, myLocation, w) && !w.hasLoggedVisit && surfableWaves.contains(w)) {
                movement.logVisit(w.getGF(myLocation), w);
                w.hasLoggedVisit = true;
            }
            if (w.radius > PreciseUtils.maxWaveDistance(angle, myLocation, w) && surfableWaves.contains(w)) {
                surfableWaves.remove(w);
                updateHitCount(w);
            }
            if (w.radius - w.speed > PreciseUtils.maxWaveDistance(angle, myLocation, w)) {
                enemyWaves.remove(w);
                dangerousWaves.remove(w);
                n--;
            }
        }

        setNearWaves();
    }

    public void setNearWaves() {
        secondarySurfableWave = nearestSurfableWave = null;

        long lowestImpactTime = Long.MAX_VALUE;
        long secondLowestImpactTime = Long.MAX_VALUE;
        Iterator i = surfableWaves.iterator();
        while (i.hasNext()) {
            MovementWave w = (MovementWave) (i.next());
            if (w.impactTime <= lowestImpactTime) {
                secondLowestImpactTime = lowestImpactTime;
                secondarySurfableWave = nearestSurfableWave;
                lowestImpactTime = w.impactTime;
                nearestSurfableWave = w;
            } else if (w.impactTime <= secondLowestImpactTime) {
                secondLowestImpactTime = w.impactTime;
                secondarySurfableWave = w;
            }
        }

        surfableWaveExists = surfableWaves.size() > 0;
    }

    public void setMovementMode() {
        if (movementMode == ANTI_HOT) {
            return;
        }

        int newMovementMode = movementMode;
        enemyHitRate = getNormalizedHitRate();
        if ((robot.getRoundNum() >= 1 && enemyHitRate > 0.10) ||
                (robot.getRoundNum() >= 2 && enemyHitRate > 0.05)) {
            newMovementMode = ANTI_NORMAL;
        } else {
            newMovementMode = ANTI_SIMPLE;
        }

        if ((robot.getRoundNum() >= 4 && enemyHitRate > 0.13 && enemyShots > 80) ||
                (robot.getRoundNum() >= 10 && enemyHitRate > 0.108 && lastRoundEnemyHitRate > 0.108 && enemyShots > 200) ||
                (robot.getRoundNum() >= 16 && enemyHitRate > 0.097 && lastRoundEnemyHitRate > 0.097 && enemyShots > 320)) {
            newMovementMode = ANTI_ADVANCED;
        }

        if (newMovementMode != movementMode) {
            movementMode = newMovementMode;
            System.out.println("Switching movement mode to " + getMovementModeName() + " movement");
        }
    }

    public String getMovementModeName() {
        return movementMode == ANTI_HOT ? "anti HOT" : (movementMode == ANTI_SIMPLE ? "anti simple targeters" : (movementMode == ANTI_NORMAL ? "normal" : "flattening"));
    }

    public void onHitByBullet(HitByBulletEvent e) {
        damageRecieved += Math.min(myEnergy, (e.getPower() * 4) + Math.max(2 * (e.getPower() - 1), 0));

        if (gameTime != robot.getTime()) {
            updateCurrentState();
            updateWaves();
        }

        enemyEnergy += e.getPower() * 3;
        enemyHits++;

        if (antiRamMode == FLEE_MODE || enemyDistance < 40.0) {
            return;
        }

        MovementWave hitWave = null;
        Iterator i = dangerousWaves.iterator();
        while (i.hasNext()) {
            MovementWave w = (MovementWave) (i.next());
            if (Math.abs(e.getPower() - w.power) < 0.01 && w.impactTime < 3) {
                hitWave = w;
                break;
            }
        }

        if (hitWave != null) {
            logHit(hitWave.getGF(e.getBullet().getHeadingRadians()), hitWave);
        } else {
            System.out.println("UNREGISTERED HIT");
        }

        setMovementMode();
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        if (gameTime != robot.getTime()) {
            updateCurrentState();
            updateWaves();
        }

        Bullet b = e.getHitBullet();
        Point2D.Double bulletLocation = new Point2D.Double(b.getX(), b.getY());

        MovementWave hitWave = null;
        Iterator i = dangerousWaves.iterator();
        while (i.hasNext()) {
            MovementWave w = (MovementWave) (i.next());

            if (Math.abs(b.getPower() - w.power) < 0.01 && (Math.abs(w.source.distance(bulletLocation) - w.radius) < 0.01) || (Math.abs(w.speed + w.source.distance(bulletLocation) - w.radius) < 0.01)) {
                hitWave = w;
                break;
            }
        }

        bulletCollisions++;
        roundBulletCollisions++;

        if (hitWave != null) {
            logHit(hitWave.getGF(e.getHitBullet().getHeadingRadians()), hitWave);
        } else {
            System.out.println("UNREGISTERED BULLET COLLISION");
        }
    }

    public void logHit(double GF, MovementWave w) {
        if (movementMode == ANTI_HOT && Math.abs(GF) > 0.3 && (isMelee || ++nonHeadOnHits > (robot.getRoundNum() < 5 ? 0 : 1))) {
            movementMode = ANTI_SIMPLE;
            System.out.println("Switching movement mode to: " + getMovementModeName() + " movement");
        }

        movement.logHit(GF, w);

        if (surfableWaves.contains(w)) {
            updateHitCount(w);
        }

        if (roundEnemyShots > 2 && (double) (roundBulletCollisions) / roundEnemyShots > 0.8 && !GunBase.isAntiBulletShielding) {
            System.out.println();
            System.out.println("Activating anti bullet shielding mode");
            System.out.println();

            GunBase.isAntiBulletShielding = true;
            enemyHits = 0;
            enemyShots = 0;
        }

        enemyWaves.remove(w);
        dangerousWaves.remove(w);
        surfableWaves.remove(w);

        setNearWaves();

        Iterator i = surfableWaves.iterator();
        while (i.hasNext()) {
            w = (MovementWave) (i.next());
            movement.setWaveData(w);
        }
    }

    public double getNormalizedHitRate() {
        //double normalizedHitRate =  (totalEscapeAngle / enemyShots) * ((double)enemyHits / (enemyShots - bulletCollisions)) / (totalDistanceFactor / enemyShots);
        //return normalizedHitRate * KUtils.frthrt(KUtils.botWidthAngle(30.0, 450.0)) / KUtils.frthrt(KUtils.maxEscapeAngle(14.0));
        return (double) (enemyHits) / (enemyShots - bulletCollisions);
    }

    public void updateHitCount(Wave w) {
        enemyShots++;
        roundEnemyShots++;
        totalEscapeAngle += KUtils.frthrt(w.maxEscapeAngle);
        totalDistanceFactor += KUtils.frthrt(KUtils.botWidthAngle(30.0, Math.max(133, w.distance)));
    }

    public void onBulletHit(BulletHitEvent e) {
        double power = e.getBullet().getPower();
        enemyEnergy -= (4 * power) + (2 * Math.max(power - 1, 0));
    }

    public void onHitRobot(HitRobotEvent e) {
        enemyEnergy -= 0.6;
    }

    public void onRobotDeath(RobotDeathEvent e) {
        //clearVirtualWave();
    }

    public void clearVirtualWave() {
        enemyGunHeat = Double.POSITIVE_INFINITY;

        if (virtualWave != null) {
            surfableWaves.remove(virtualWave);
            virtualWave.isReal = false;
            virtualWave = null;
        }
    }

    public void printStats() {
        System.out.println("Enemy Hit Rate: " + enemyHits + "/" + enemyShots + " = " + 100f * enemyHits / enemyShots + "%");

        if (isMC) {
            System.out.println("MC Score: " + (float) (100 - (damageRecieved / (1 + robot.getRoundNum()))));
        }
        movement.printStats();
    }

    public void onPaint(java.awt.Graphics2D g) {
        if (gameTime != robot.getTime()) {
            updateCurrentState();
            updateWaves();
        }

        g.setColor(Color.gray);
        Iterator i = enemyWaves.iterator();
        while (i.hasNext()) {
            MovementWave w = (MovementWave) (i.next());
            if (surfableWaves.contains(w)) {

                g.drawOval((int) Math.round(w.source.x - w.radius),
                        (int) Math.round(w.source.y - w.radius),
                        (int) Math.round(2 * w.radius),
                        (int) Math.round(2 * w.radius));

                Point2D.Double projected = KUtils.projectMotion(w.source, w.absoluteBearing, w.radius);
                g.drawLine((int) Math.round(w.source.x),
                        (int) Math.round(w.source.y),
                        (int) Math.round(projected.x),
                        (int) Math.round(projected.y));

                if (w != virtualWave) {
                    if (movementMode != ANTI_HOT) {
                        movement.paint(w, movementMode, g);
                    }
                }
            }
        }

        if (myLocation != null) {
            g.drawRect((int) Math.round(myLocation.x - 18.0), (int) Math.round(myLocation.y - 18.0), 36, 36);
        }
    }
}		