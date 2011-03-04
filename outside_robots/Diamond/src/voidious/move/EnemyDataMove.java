package voidious.move;

import robocode.Rules;
import voidious.utils.DataView;
import voidious.utils.DiaUtils;
import voidious.utils.KdBucketTree;
import voidious.utils.TimestampedGuessFactor;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/*
 * General info about the enemy. We just keep one of these around for 
 * each enemy bot the movement is aware of.
 * 
 * Copyright (c) 2009-2010 - Voidious
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software.
 *
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 *
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */

public class EnemyDataMove {
    protected static final double GOLDEN_MEAN = 2.0 / (Math.sqrt(5) - 1);
    public String botName;
    public double distance;
    public double energy;
    public double velocity;
    public Point2D.Double location;
    public long lastScanTime;
    public ArrayList<Point2D.Double> pastLocations;
    public double heading;
    public double absBearing;
    public double damageTaken;
    public double damageGiven;
    boolean alive;
    public double lastBulletPower;
    public long lastBulletFireTime;
    public double totalBulletPower;
    public long totalTimesHit;
    public double totalDistance;
    public long timeAliveTogether;
    public long lastTimeHit;
    public long lastTimeClosest;
    protected HashMap<String, Double> _botDistancesSq;
    public boolean flattenerEnabled;
    public boolean avoidBeingTargeted;
    public boolean stayPerpendicular;
    public double damageFactor;

    public double raw1v1ShotsFired;
    public double raw1v1ShotsHit;
    public double weighted1v1ShotsHit;
    public double raw1v1ShotsFiredThisRound;
    public double raw1v1ShotsHitThisRound;
    public double weighted1v1ShotsHitThisRound;
    public double lastRoundNormalized1v1HitPercentage;

    public ArrayList<DataView> views;
    public HashMap<double[], TimestampedGuessFactor> guessFactors;
    public KdBucketTree powerTree;
    public HashMap<double[], Double> bulletPowers;

    public EnemyDataMove(String name, double dist, double en, Point2D.Double l,
                         double h, double ab, long lst) {

        botName = name;
        distance = dist;
        energy = en;
        velocity = 0;
        location = l;
        heading = h;
        absBearing = ab;
        damageTaken = 0;
        damageGiven = 0;
        alive = true;
        lastBulletPower = 0;
        totalBulletPower = 0;
        totalTimesHit = 0;
        totalDistance = 500;
        timeAliveTogether = 1;
        lastScanTime = lst;
        raw1v1ShotsFired = 0;
        weighted1v1ShotsHit = 0;
        flattenerEnabled = false;
        avoidBeingTargeted = false;
        stayPerpendicular = false;

        _botDistancesSq = new HashMap<String, Double>();
        pastLocations = new ArrayList<Point2D.Double>();

        guessFactors = new HashMap<double[], TimestampedGuessFactor>();
        initViews();
        powerTree = new KdBucketTree();
        bulletPowers = new HashMap<double[], Double>();
    }

    public void initViews() {
        views = new ArrayList<DataView>();

        DataView simple = new DataView(1, new DistanceSimple(), 20,
                DataView.BULLET_HITS_ON, DataView.VISITS_OFF);
        DataView simple2 = new DataView(1, new DistanceSimple2(), 20,
                DataView.BULLET_HITS_ON, DataView.VISITS_OFF);
        DataView simple3 = new DataView(1, new DistanceSimple3(), 20,
                DataView.BULLET_HITS_ON, DataView.VISITS_OFF);

        DataView medium = new DataView(15, new DistanceMedium(), 20,
                DataView.BULLET_HITS_ON, DataView.VISITS_OFF, 3.0,
                DataView.UNLIMITED);

        DataView normal = new DataView(40, new DistanceNormal(), 20,
                DataView.BULLET_HITS_ON, DataView.VISITS_OFF, 4.0,
                DataView.UNLIMITED);

        DataView recent = new DataView(225, new DistanceNormal(), 5,
                DataView.BULLET_HITS_ON, DataView.VISITS_OFF, 5.5, 15,
                GOLDEN_MEAN);
        DataView recent2 = new DataView(225, new DistanceNormal(), 10,
                DataView.BULLET_HITS_ON, DataView.VISITS_OFF, 5.5, 50,
                GOLDEN_MEAN);

        DataView flattener = new DataView(150, new DistanceNormal(), 5,
                DataView.BULLET_HITS_OFF, DataView.VISITS_ON,
                DataView.FLATTENER, 250, GOLDEN_MEAN);
        DataView flattener2 = new DataView(150, new DistanceFunky(), 5,
                DataView.BULLET_HITS_OFF, DataView.VISITS_ON,
                DataView.FLATTENER, 125, GOLDEN_MEAN);

        views.add(simple);
        views.add(simple2);
        views.add(simple3);
        views.add(medium);
        views.add(normal);
        views.add(recent);
        views.add(recent2);
        views.add(flattener);
        views.add(flattener2);
    }

    public double avgBulletPower() {
        if (totalBulletPower == 0) {
            return 3.0;
        }

        return totalBulletPower / totalTimesHit;
    }

    public void setBotDistanceSq(String name, double distance) {
        _botDistancesSq.put(name, distance);
    }

    public void clearDistancesSq() {
        _botDistancesSq.clear();
    }

    public void removeDistanceSq(String botName) {
        _botDistancesSq.remove(botName);
    }

    public double minDistanceSq() {
        double min = Double.POSITIVE_INFINITY;

        Iterator<Double> distIterator = _botDistancesSq.values().iterator();
        while (distIterator.hasNext()) {
            double dist = distIterator.next();
            if (dist < min) {
                min = dist;
            }
        }

        return min;
    }

    public int botsCloser(double distanceSq) {
        int bc = 0;

        Iterator<Double> distIterator = _botDistancesSq.values().iterator();
        while (distIterator.hasNext()) {
            double dist = distIterator.next();
            if (dist < distanceSq) {
                bc++;
            }
        }

        return bc;
    }

    public Point2D.Double getPastLocation(int x) {
        return pastLocations.get(pastLocations.size() - 1 - x);
    }

    public void clearNeighborCache() {
        Iterator<DataView> viewsIterator = views.iterator();
        while (viewsIterator.hasNext()) {
            DataView view = viewsIterator.next();
            view.clearCache();
        }
    }

    public double getGunHeat(long time) {
        double gunHeat;
        if (lastBulletPower == 0) {
            gunHeat = Math.max(0, 3.0 - (time * .1));
        } else {
            gunHeat = Math.max(0, Rules.getGunHeat(lastBulletPower) -
                    ((time - lastBulletFireTime) * .1));
        }

        return DiaUtils.round(gunHeat, 6);
    }
}
