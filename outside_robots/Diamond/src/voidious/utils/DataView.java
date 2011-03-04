package voidious.utils;

import java.util.ArrayList;
import java.util.LinkedList;

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

public class DataView {
    public double weight;
    public DistanceFormula formula;
    public int clusterSize;
    public int maxDataPoints;
    public boolean logBulletHits;
    public boolean logVisits;
    public boolean logVirtual;
    public boolean logMelee;
    public double hitThreshold;
    public String label = "default";
    public double decayRate;

    public KdBucketTree tree;
    public ArrayList<double[][]> cachedNeighbors;
    public int treeSize;

    public static final int UNLIMITED = 0;
    public static final double ALWAYS_ON = 0;
    public static final double FLATTENER = 999;
    public static final boolean BULLET_HITS_ON = true;
    public static final boolean BULLET_HITS_OFF = false;
    public static final boolean VISITS_ON = true;
    public static final boolean VISITS_OFF = false;
    public static final boolean VIRTUAL_ON = true;
    public static final boolean VIRTUAL_OFF = false;
    public static final boolean MELEE_ON = true;
    public static final boolean MELEE_OFF = false;
    public static final double NO_DECAY = 0;

    protected LinkedList<double[]> dataPointLog;

    public DataView(double w, DistanceFormula df, int cs, boolean hits,
                    boolean visits) {

        this(w, df, cs, hits, visits, ALWAYS_ON, UNLIMITED);
    }

    public DataView(double w, DistanceFormula df, int cs,
                    boolean hits, boolean visits, double thresh, int max) {

        this(w, df, cs, hits, visits, VIRTUAL_OFF, MELEE_OFF, thresh, max,
                NO_DECAY);
    }

    public DataView(double w, DistanceFormula df, int cs,
                    boolean hits, boolean visits, double thresh, int max, double dr) {

        this(w, df, cs, hits, visits, VIRTUAL_OFF, MELEE_OFF, thresh, max,
                dr);
    }

    public DataView(double w, DistanceFormula df, int cs, boolean hits,
                    boolean visits, boolean virtual, boolean melee, double thresh, int max,
                    double dr) {

        weight = w;
        formula = df;
        clusterSize = cs;
        logBulletHits = hits;
        logVisits = visits;
        logVirtual = virtual;
        logMelee = melee;
        hitThreshold = thresh;
        maxDataPoints = max;
        decayRate = dr;

        tree = new KdBucketTree();
        treeSize = 0;
        dataPointLog = new LinkedList<double[]>();
        cachedNeighbors = new ArrayList<double[][]>();
    }

    public double[] logWave(DiaWave w) {
        double[] dataPoint = formula.dataPointFromWave(w);

        return logDataPoint(dataPoint);
    }

    public double[] logDataPoint(double[] dataPoint) {
        if (maxDataPoints != UNLIMITED) {
/*
            if (smartCycle) {
                while (treeSize >= maxDataPoints) {
                    double[] oldPoint = KdBucketTree.nearestNeighbors(tree, 
                        dataPoint, 1, formula.weights)[0];
                    tree.remove(oldPoint);
                    treeSize--;
                }
            }
*/
            while (treeSize >= maxDataPoints) {
                double[] oldPoint = dataPointLog.removeFirst();
                tree.remove(oldPoint);
                treeSize--;
            }
            dataPointLog.addLast(dataPoint);
        }

        tree.insert(dataPoint);
        treeSize++;

        return dataPoint;
    }

    public void clearCache() {
        cachedNeighbors.clear();
    }

    public boolean enabled(double hitPercentage, boolean flattenerEnabled) {
        if (hitThreshold == FLATTENER) {
            return flattenerEnabled;
        }

        if (hitPercentage >= hitThreshold) {
            return true;
        }

        return false;
    }
}
