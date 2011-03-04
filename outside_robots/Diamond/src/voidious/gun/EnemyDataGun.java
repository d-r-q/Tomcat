package voidious.gun;

import voidious.utils.DataView;
import voidious.utils.DiaWave;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/*
 * General info about the enemy. We just keep one of these around for 
 * each enemy bot the gun is aware of.
 * 
 * The DataView objects have all the kd-tree's used for targeting. The data
 * from those plus the corresponding displacement vectors are what's used
 * for actual targeting.
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

public class EnemyDataGun {
    public String botName;
    public double distance;
    public double energy;
    public Point2D.Double location;
    public long lastScanTime;
    public ArrayList<Point2D.Double> pastLocations;
    public double damageTaken;
    public double damageGiven;
    public boolean alive;
    public double velocity;
    public double previousVelocity;
    public double heading;
    public double lastNonZeroVelocity;
    public double timeSinceDirectionChange;
    public double timeSinceVelocityChange;
    public HashMap<String, DataView> views;
    public HashMap<double[], Point2D.Double> displacementVectors;
    public HashMap<double[], Double> guessFactors;
    protected HashMap<String, Double> _botDistancesSq;
    public DiaWave lastWaveFired;

    public EnemyDataGun(String name, double dist, double en, Point2D.Double l,
                        long time, double v, double h) {

        botName = name;
        distance = dist;
        energy = en;
        location = l;
        lastScanTime = time;
        velocity = v;
        previousVelocity = 0;
        heading = h;
        damageTaken = 0;
        damageGiven = 0;
        alive = true;
        timeSinceDirectionChange = 0;
        timeSinceVelocityChange = 0;

        views = new HashMap<String, DataView>();
        displacementVectors = new HashMap<double[], Point2D.Double>();
        guessFactors = new HashMap<double[], Double>();
        pastLocations = new ArrayList<Point2D.Double>();
        _botDistancesSq = new HashMap<String, Double>();
        lastWaveFired = null;
    }

    public void registerDataLogView(String s, DataView v) {
        views.put(s, v);
    }

    public void saveDisplacementVector(DiaWave w, Point2D.Double dispVector,
                                       boolean isVisit) {

        Iterator<DataView> viewsIterator = views.values().iterator();
        while (viewsIterator.hasNext()) {
            DataView view = viewsIterator.next();
            if ((isVisit && view.logVisits || !isVisit && view.logBulletHits) &&
                    (view.logVirtual || w.firingWave) &&
                    (view.logMelee || w.enemiesAlive <= 1)) {
                double[] dataPoint = view.logWave(w);
                displacementVectors.put(dataPoint, dispVector);
            }
        }
    }

    public Point2D.Double getPastLocation(int x) {
        return pastLocations.get(pastLocations.size() - 1 - x);
    }

    public void setBotDistanceSq(String name, double distance) {
        _botDistancesSq.put(name, distance);
    }

    public double getBotDistanceSq(String name) {
        if (!_botDistancesSq.containsKey(name)) {
            return Double.NaN;
        }
        return _botDistancesSq.get(name);
    }

    public void clearDistancesSq() {
        _botDistancesSq.clear();
    }

    public void removeDistanceSq(String botName) {
        _botDistancesSq.remove(botName);
    }

    public String closestBot() {
        double min = Double.POSITIVE_INFINITY;
        String botName = null;

        Iterator<String> botNameIterator = _botDistancesSq.keySet().iterator();
        while (botNameIterator.hasNext()) {
            String name = botNameIterator.next();
            double dist = _botDistancesSq.get(name);
            if (dist < min) {
                min = dist;
                botName = name;
            }
        }

        return botName;
    }

}
