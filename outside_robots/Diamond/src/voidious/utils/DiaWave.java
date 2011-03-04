package voidious.utils;

import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

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

public class DiaWave {
    public static final Point2D.Double ORIGIN = new Point2D.Double(0, 0);
    public static final double MAX_GUESSFACTOR = 1.0;
    public static final int CLOCKWISE = 1;
    public static final int COUNTERCLOCKWISE = -1;
    public static final boolean FIRING_WAVE = true;
    public static final boolean SURFABLE_WAVE = true;
    public static final boolean ANY_WAVE = false;
    public static final boolean POSITIVE_GUESSFACTOR = true;
    public static final boolean NEGATIVE_GUESSFACTOR = false;
    public static final double NO_CACHED_ESCAPE_ANGLE = -1;
    public static final double ANY_BULLET_POWER = -1;

    public long fireTime;
    public Point2D.Double sourceLocation;
    public Point2D.Double targetLocation;
    public double absBearing;
    public double bulletPower;
    public double bulletSpeed;
    public int orbitDirection;
    public boolean processedBulletHit;
    public boolean processedPassed;
    public boolean processedWaveBreak;
    public boolean firingWave;

    public String botName;
    public double targetHeading;
    public double targetRelativeHeading;
    public double targetVelocity;
    public double targetAccel;
    public int targetVelocitySign;
    public double targetDistance;
    public double targetDistNearest;
    public double targetDchangeTime;
    public double targetVchangeTime;
    public double targetWallDistance;
    public double targetRevWallDistance;
    public double targetDl8t;
    public double targetDl20t;
    public double targetDl40t;
    public double targetAgHeading;
    public double targetAgForce;
    public double targetCornerDistance;
    public double targetCornerBearing;
    public double targetEnergy;
    public double sourceEnergy;
    public double gunHeat;
    public long lastBulletFiredTime;
    public int enemiesAlive;
    public Point2D.Double waveBreakLocation;
    public long waveBreakTime;
    public double waveBreakDistance;
    public Point2D.Double prevLocation;
    public long prevLocationTime;
    public boolean altWave;
    protected Rectangle2D.Double _fieldRect;
    protected double _fieldWidth;
    protected double _fieldHeight;

    // used for data collection
    public int id;
    public int lastSeenId;

    protected double _cachedPositiveEscapeAngle = NO_CACHED_ESCAPE_ANGLE;
    protected double _cachedNegativeEscapeAngle = NO_CACHED_ESCAPE_ANGLE;
    public boolean usedNegativeSmoothingMea = false;
    public boolean usedPositiveSmoothingMea = false;

    public DiaWave(Point2D.Double source, Point2D.Double target,
                   Point2D.Double orbitLocation, long time, double power, String name,
                   double heading, double velocity, double accel, int vSign,
                   double distance, double dtnb, double dChange, double vChange, double wallDist1,
                   double wallDist2, double dl8t, double dl20t, double dl40t, double agh,
                   double agf, double cd, double cb, double energy, double sEnergy,
                   int alive, double gh, long lastBulletFiredTime,
                   Rectangle2D.Double field, double width, double height) {

        sourceLocation = source;
        targetLocation = target;
        fireTime = time;
        this.setBulletPower(power);
        absBearing = DiaUtils.absoluteBearing(source, target);

        botName = name;
        targetHeading = heading;
        targetVelocity = velocity;
        targetAccel = accel;
        targetVelocitySign = vSign;
        targetDistance = distance;
        targetDistNearest = dtnb;
        targetDchangeTime = dChange;
        targetVchangeTime = vChange;
        targetDl8t = dl8t;
        targetDl20t = dl20t;
        targetDl40t = dl40t;
        targetAgForce = agf;
        targetCornerDistance = cd;
        targetEnergy = energy;
        sourceEnergy = sEnergy;
        enemiesAlive = alive;
        gunHeat = gh;
        this.lastBulletFiredTime = lastBulletFiredTime;

        double orbitRelativeHeading = Utils.normalRelativeAngle(
                effectiveHeading() -
                        DiaUtils.absoluteBearing(orbitLocation, targetLocation));

        if (orbitRelativeHeading < 0) {
            orbitDirection = COUNTERCLOCKWISE;
        } else {
            orbitDirection = CLOCKWISE;
        }

        targetRelativeHeading = Math.abs(orbitRelativeHeading);
        targetAgHeading =
                Utils.normalRelativeAngle(agh - effectiveHeading()) * orbitDirection;
        targetCornerBearing =
                Utils.normalRelativeAngle(cb - effectiveHeading()) * orbitDirection;

        this.setWallDistance(sourceLocation, wallDist1, wallDist2);

        _fieldRect = field;
        _fieldWidth = width;
        _fieldHeight = height;

        processedBulletHit = false;
        processedPassed = false;
        processedWaveBreak = false;
        waveBreakLocation = null;
        prevLocation = null;
        firingWave = false;
        altWave = false;
    }

    public void setBulletPower(double power) {
        bulletPower = power;
        bulletSpeed = (20 - (3 * power));
    }

    public void setWallDistance(Point2D.Double orbitLocation,
                                double wallDist1, double wallDist2) {

        if (orbitDirection == COUNTERCLOCKWISE && enemiesAlive == 1) {
            // in 1v1, need to switch these if they're orbiting ccw
            targetWallDistance = wallDist2;
            targetRevWallDistance = wallDist1;
        } else {
            // 1v1 and orbiting clockwise 
            // OR melee and ignoring how they're orbiting
            targetWallDistance = wallDist1;
            targetRevWallDistance = wallDist2;
        }
    }

    public boolean wavePassedInterpolate(Point2D.Double lastScanLocation,
                                         long lastScanTime, long currentTime) {
        return wavePassedInterpolate(lastScanLocation, lastScanTime, currentTime, 0);
    }

    // Note: This method really needs to be called every tick for it to
    //       work correctly.
    public boolean wavePassedInterpolate(Point2D.Double lastScanLocation,
                                         long lastScanTime, long currentTime, int offset) {

        if (processedPassed) {
            return true;
        } else if (sourceLocation.distanceSq(lastScanLocation) <
                DiaUtils.square(offset +
                        (bulletSpeed * (currentTime - fireTime + 1.5))) &&
                lastScanTime == currentTime) {
            if (currentTime - prevLocationTime == 1 ||
                    prevLocation == null) {
                waveBreakLocation = lastScanLocation;
                waveBreakTime = currentTime;
            } else {
                double deltaDistance = lastScanLocation.distance(prevLocation) /
                        (currentTime - prevLocationTime);
                double deltaBearing =
                        DiaUtils.absoluteBearing(prevLocation, lastScanLocation);
                double dbSin = Math.sin(deltaBearing);
                double dbCos = Math.cos(deltaBearing);
                for (long x = prevLocationTime + 1; x <= currentTime; x++) {
                    long interpoTime = x - prevLocationTime;
                    Point2D.Double interpoLocation =
                            DiaUtils.project(prevLocation, dbSin, dbCos,
                                    (interpoTime) * deltaDistance);
                    if (sourceLocation.distanceSq(interpoLocation) <
                            DiaUtils.square(offset +
                                    (bulletSpeed * (x - fireTime + 1.5)))) {
                        waveBreakLocation = interpoLocation;
                        waveBreakTime = x;
                        break;
                    }
                }

                if (waveBreakLocation == null) {
                    System.out.println("WARNING: Anomaly in wave break interpolation.");
                    waveBreakLocation = lastScanLocation;
                    waveBreakTime = lastScanTime;
                }
            }
            processedPassed = true;
            return true;
        } else {
            prevLocation = lastScanLocation;
            prevLocationTime = lastScanTime;
            return false;
        }
    }

    public boolean wavePassed(Point2D.Double enemyLocation, long currentTime,
                              double interceptOffset) {

        double threshold = bulletSpeed * (currentTime - fireTime)
                + interceptOffset;
        if (threshold > 0 &&
                enemyLocation.distanceSq(sourceLocation) <
                        DiaUtils.square(threshold)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean waveGone(Point2D.Double enemyLocation, long currentTime) {

        double bulletDistanceTraveledSq = DiaUtils.square(bulletSpeed *
                (currentTime - fireTime));
        ArrayList<Point2D.Double> corners = DiaUtils.botCorners(enemyLocation);

        for (Point2D.Double corner : corners) {
            if (corner.distanceSq(sourceLocation) > bulletDistanceTraveledSq) {
                return false;
            }
        }

        return true;
    }

    public Point2D.Double waveBreakLocation() {
        if (waveBreakLocation == null) {
            return prevLocation;
        } else {
            return waveBreakLocation;
        }
    }

    public long waveBreakTime() {
        if (waveBreakLocation == null) {
            return prevLocationTime;
        } else {
            return waveBreakTime;
        }
    }

    public long waveBreakBulletTicks() {
        return waveBreakBulletTicks(waveBreakTime());
    }

    public long waveBreakBulletTicks(long waveBreakTime) {
        return waveBreakTime - fireTime;
    }

    public double effectiveHeading() {
        return Utils.normalAbsoluteAngle(targetHeading +
                (targetVelocitySign == 1 ? 0 : Math.PI));
    }

    // Note: This should be called after wavePassed is true. If it isn't,
    //       it will just work as though the last seen location is the
    //       wave break location.
    public Point2D.Double displacementVector() {
        return displacementVector(waveBreakLocation(), waveBreakTime());
    }

    public Point2D.Double displacementVector(Point2D.Double botLocation,
                                             long time) {
        double vectorBearing = Utils.normalRelativeAngle(
                DiaUtils.absoluteBearing(targetLocation, botLocation) -
                        effectiveHeading());
        double vectorDistance = targetLocation.distance(botLocation) /
                waveBreakBulletTicks(time);

        return DiaUtils.project(ORIGIN, vectorBearing * orbitDirection,
                vectorDistance);
    }

    public double firingAngleFromDisplacementVector(Point2D.Double dispVector) {
        return Utils.normalAbsoluteAngle(DiaUtils.absoluteBearing(
                sourceLocation, projectLocation(dispVector)));
    }

    public double firingAngleFromTargetLocation(Point2D.Double firingTarget) {
        return Utils.normalAbsoluteAngle(DiaUtils.absoluteBearing(
                sourceLocation, firingTarget));
    }

    public Point2D.Double projectLocationBlind(Point2D.Double myNextLocation,
                                               Point2D.Double dispVector, long currentTime) {

        return projectLocation(myNextLocation, dispVector,
                currentTime - fireTime + 1);
    }

    public Point2D.Double projectLocation(Point2D.Double dispVector) {
        return projectLocation(sourceLocation, dispVector, 0);
    }

    public Point2D.Double projectLocation(Point2D.Double firingLocation,
                                          Point2D.Double dispVector, long extraTicks) {

        double dispAngle = effectiveHeading() +
                (DiaUtils.absoluteBearing(ORIGIN, dispVector) * orbitDirection);
        double dispDistance = ORIGIN.distance(dispVector);

        Point2D.Double projectedLocation;
        long bulletTicks = DiaUtils.bulletTicksFromSpeed(
                firingLocation.distance(targetLocation), bulletSpeed) - 1;
        long prevBulletTicks = 0;
        long prevPrevBulletTicks;
        int sanityCounter = 0;
        double daSin = Math.sin(dispAngle);
        double daCos = Math.cos(dispAngle);

        do {
            projectedLocation = DiaUtils.project(targetLocation,
                    daSin, daCos, (bulletTicks + extraTicks) * dispDistance);
            prevPrevBulletTicks = prevBulletTicks;
            prevBulletTicks = bulletTicks;
            bulletTicks = DiaUtils.bulletTicksFromSpeed(
                    firingLocation.distance(projectedLocation), bulletSpeed) - 1;

            if (bulletTicks == prevPrevBulletTicks) {
                projectedLocation = DiaUtils.project(targetLocation, daSin, daCos,
                        (((bulletTicks + prevBulletTicks) / 2.0) + extraTicks)
                                * dispDistance);
                break;
            }
        } while (bulletTicks != prevBulletTicks && sanityCounter++ < 20);

        return projectedLocation;
    }

    public double distanceTraveled(long currentTime) {
        return (currentTime - fireTime) * bulletSpeed;
    }

    public double guessFactor(Point2D.Double targetLocation) {
        double bearingToTarget =
                DiaUtils.absoluteBearing(sourceLocation, targetLocation);

        return guessFactor(bearingToTarget);
    }

    public double guessFactor(double bearingToTarget) {
        double guessAngle = orbitDirection * Utils.normalRelativeAngle(
                bearingToTarget - absBearing);
        double maxEscapeAngle = Math.asin(8.0 / bulletSpeed);

        return guessAngle / maxEscapeAngle;
    }

    public double guessFactorPrecise(Point2D.Double targetLocation) {
        double newBearingToTarget =
                DiaUtils.absoluteBearing(sourceLocation, targetLocation);
        double guessAngle = orbitDirection * Utils.normalRelativeAngle(
                newBearingToTarget - absBearing);
        double maxEscapeAngle = preciseEscapeAngle(guessAngle >= 0);

        return guessAngle / maxEscapeAngle;
    }

    public double lateralVelocity() {
        return Math.sin(targetRelativeHeading) * Math.abs(targetVelocity);
    }

    public double escapeAngleRange() {
        return (preciseEscapeAngle(POSITIVE_GUESSFACTOR)
                + preciseEscapeAngle(NEGATIVE_GUESSFACTOR));
    }

    public double preciseEscapeAngle(boolean positiveGuessFactor) {
        if (positiveGuessFactor) {
            if (_cachedPositiveEscapeAngle == NO_CACHED_ESCAPE_ANGLE) {
                _cachedPositiveEscapeAngle =
                        uncachedPreciseEscapeAngle(positiveGuessFactor)
                                * MAX_GUESSFACTOR;
            }
            return _cachedPositiveEscapeAngle;
        } else {
            if (_cachedNegativeEscapeAngle == NO_CACHED_ESCAPE_ANGLE) {
                _cachedNegativeEscapeAngle =
                        uncachedPreciseEscapeAngle(positiveGuessFactor)
                                * MAX_GUESSFACTOR;
            }
            return _cachedNegativeEscapeAngle;
        }
    }

    public double firingAngle(double guessFactor) {
        return absBearing +
                (guessFactor * orbitDirection * Math.asin(8.0 / bulletSpeed));
    }

    public double virtuality() {
        long timeSinceLastBullet = (fireTime - lastBulletFiredTime);
        long timeToNextBullet = Math.round(Math.ceil(gunHeat * 10));

        if (firingWave) {
            return 0;
        } else if (lastBulletFiredTime > 0) {
            return Math.min(timeSinceLastBullet, timeToNextBullet) / 8.0;
        } else {
            return Math.min(1, timeToNextBullet / 8.0);
        }
    }

    public double uncachedPreciseEscapeAngle(boolean positiveGuessFactor) {
        boolean hitWall = false;
        boolean wavePassed = false;

        RobotState predictedState = new RobotState(
                (Point2D.Double) targetLocation.clone(),
                targetHeading, targetVelocity);
        long predictedTime = fireTime;

        boolean clockwisePrediction =
                (orbitDirection == 1 && positiveGuessFactor) ||
                        (orbitDirection == -1 && !positiveGuessFactor);

        double noSmoothingEscapeAngle = 0;
        double bulletVelocity = this.bulletSpeed;

        do {
            predictedState = DiaUtils.nextPerpendicularLocation(
                    predictedState.location, absBearing,
                    predictedState.velocity, predictedState.heading,
                    clockwisePrediction, predictedTime, DiaUtils.IGNORE_WALLS);
            predictedTime = predictedState.time;

            if (!_fieldRect.contains(predictedState.location)) {
                hitWall = true;
            } else if (
                    wavePassed(predictedState.location,
                            predictedTime, bulletVelocity)) {
                wavePassed = true;
            }
        } while (!hitWall && !wavePassed);

        noSmoothingEscapeAngle =
                Math.abs(Utils.normalRelativeAngle(DiaUtils.absoluteBearing(
                        sourceLocation, predictedState.location) - absBearing));

        double withSmoothingEscapeAngle = 0;

        if (hitWall) {
            double wallSmoothingStick = 80;
            double purelyPerpendicularAttackAngle = 0;
            double fullVelocity = 8.0;
            double orbitAbsBearing = absBearing;
            double bestSmoothingEscapeAngle = 0;

            for (int x = 0; x < 3; x++) {
                wavePassed = false;
                predictedState = new RobotState(
                        (Point2D.Double) targetLocation.clone(),
                        targetHeading, targetVelocity);
                predictedTime = fireTime;

                do {
                    predictedState =
                            DiaUtils.nextPerpendicularWallSmoothedLocation(
                                    predictedState.location, orbitAbsBearing,
                                    predictedState.velocity, fullVelocity,
                                    predictedState.heading,
                                    purelyPerpendicularAttackAngle, clockwisePrediction,
                                    predictedTime, _fieldRect,
                                    _fieldWidth, _fieldHeight,
                                    wallSmoothingStick, DiaUtils.OBSERVE_WALL_HITS);
                    predictedTime = predictedState.time;
                    //              orbitAbsBearing = DUtils.absoluteBearing(
                    //                  sourceLocation, predictedState.location);

                    if (
                            wavePassed(predictedState.location,
                                    predictedTime, bulletVelocity)) {
                        wavePassed = true;
                    }
                } while (!wavePassed);

                orbitAbsBearing = DiaUtils.absoluteBearing(targetLocation,
                        predictedState.location) -
                        ((clockwisePrediction ? 1 : -1) * (Math.PI / 2));

                bestSmoothingEscapeAngle = Math.max(bestSmoothingEscapeAngle,
                        Math.abs(Utils.normalRelativeAngle(DiaUtils.absoluteBearing(
                                sourceLocation, predictedState.location) - absBearing)));
            }
            withSmoothingEscapeAngle = bestSmoothingEscapeAngle;
        }

        return Math.max(noSmoothingEscapeAngle, withSmoothingEscapeAngle);
    }

    public void clearCachedPreciseEscapeAngles() {
        _cachedPositiveEscapeAngle = NO_CACHED_ESCAPE_ANGLE;
        _cachedNegativeEscapeAngle = NO_CACHED_ESCAPE_ANGLE;
    }

    public String genAttrString() {
        double sinRealativeHeading = Math.sin(targetRelativeHeading);
        double cosRelativeHeading = Math.cos(targetRelativeHeading);
        double guessFactor = guessFactorPrecise(waveBreakLocation);
        double posMea = preciseEscapeAngle(POSITIVE_GUESSFACTOR);
        double negMea = preciseEscapeAngle(NEGATIVE_GUESSFACTOR);

        String s = id + "," +
                lastSeenId + "," +
                (firingWave ? "1" : "0") + "," +
                DiaUtils.round(bulletPower, 2) + "," +
                DiaUtils.round(bulletSpeed, 2) + "," +
                fireTime + "," +
                DiaUtils.round(waveBreakDistance, 1) + "," +
                DiaUtils.round(targetVelocity, 2) + "," +
                DiaUtils.round(lateralVelocity(), 2) + "," +
                DiaUtils.round(targetDistance, 1) + "," +
                DiaUtils.round(targetDistance / bulletSpeed, 1) + "," +
                DiaUtils.round(targetAccel, 3) + "," +
                DiaUtils.round(targetRelativeHeading, 4) + "," +
                DiaUtils.round(targetWallDistance, 3) + "," +
                DiaUtils.round(targetRevWallDistance, 3) + "," +
                Math.round(targetVchangeTime) + "," +
                Math.round(targetDchangeTime) + "," +
                DiaUtils.round(targetDl8t, 1) + "," +
                DiaUtils.round(targetDl20t, 1) + "," +
                DiaUtils.round(targetDl40t, 1) + "," +
                DiaUtils.round(gunHeat, 2) + "," +
                DiaUtils.round(virtuality(), 3) + "," +
                DiaUtils.round(sinRealativeHeading, 3) + "," +
                DiaUtils.round(cosRelativeHeading, 3) + "," +
                DiaUtils.round(guessFactor, 4) + "," +
                DiaUtils.round(guessFactor * (guessFactor > 0 ? posMea : negMea), 4) + "," +
                DiaUtils.round(preciseEscapeAngle(POSITIVE_GUESSFACTOR), 4) + "," +
                DiaUtils.round(preciseEscapeAngle(NEGATIVE_GUESSFACTOR), 4);

        return s;
    }
}
