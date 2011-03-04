package voidious.gun;

import robocode.util.Utils;
import voidious.gfx.ColoredValueSet;
import voidious.gfx.RoboGraphic;
import voidious.utils.DiaUtils;
import voidious.utils.DiaWave;
import voidious.utils.DistanceFormula;
import voidious.utils.KdBucketTree;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Vector;

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

public class TripHammerKNNGun extends TripHammerGun implements DiaGun {
    protected static final int FIRING_ANGLES = 59;
    protected static final int GF_ZERO = (FIRING_ANGLES - 1) / 2;
    protected static final int GF_ONE = FIRING_ANGLES - 1;
    protected EnemyDataGun _enemyData;
    protected Rectangle2D.Double _fieldRect;
    protected Vector<RoboGraphic> _renderables;
    protected HashMap<DiaWave, Double> _firingAngles;
    public KdBucketTree tree;
    public DistanceFormula formula;

    public TripHammerKNNGun(Rectangle2D.Double fieldRect,
                            Vector<RoboGraphic> renderables) {

        _enemyData = null;
        _fieldRect = fieldRect;
        _renderables = renderables;
        _firingAngles = new HashMap<DiaWave, Double>();
        tree = new KdBucketTree();
        formula = new DistanceTripHammer();
    }

    public void setEnemyData(EnemyDataGun edg) {
        _enemyData = edg;
    }

    public double aimWithWave(DiaWave w, boolean painting) {
        if (_firingAngles.containsKey(w)) {
            return _firingAngles.get(w);
        }

        double[] wavePoint =
                formula.dataPointFromWave(w, DiamondFist.WAVE_AIMING);

        int desiredClusterSize = (int) DiaUtils.limit(1,
                _enemyData.guessFactors.size() / 30, DiamondFist.CLUSTER_SIZE_TRIPHAMMER);
        double[][] nearestNeighbors = KdBucketTree.nearestNeighbors(tree,
                wavePoint, desiredClusterSize, formula.weights);

        if (_enemyData == null || _enemyData.guessFactors.isEmpty() ||
                nearestNeighbors == null || nearestNeighbors.length == 0) {
            return w.absBearing;
        }

        int numScans = nearestNeighbors.length;
        double[] firingAngles = new double[numScans];
        for (int x = 0; x < numScans; x++) {
            double guessFactor = _enemyData.guessFactors.get(nearestNeighbors[x]);
            firingAngles[x] = Utils.normalRelativeAngle(
                    (guessFactor * w.orbitDirection * w.preciseEscapeAngle(guessFactor >= 0)));
/*
            Point2D.Double dispVector = 
                _enemyData.displacementVectors.get(clusterPoints[x]);
            Point2D.Double projectedLocation = w.projectLocation(dispVector);
            if (!_fieldRect.contains(projectedLocation)) {
                firingAngles[x] = DiamondFist.NO_FIRING_ANGLE;
            } else {
                firingAngles[x] = Utils.normalRelativeAngle(
                    w.firingAngleFromTargetLocation(projectedLocation) 
                        - w.absBearing);
            }
*/
        }

        double bandwidth = 2 * DiaUtils.botWidthAimAngle(
                w.sourceLocation.distance(w.targetLocation));
        double bestAngle = DiamondFist.NO_FIRING_ANGLE;
        double bestDensity = Double.NEGATIVE_INFINITY;

        ColoredValueSet cvs = new ColoredValueSet();
        if (numScans > FIRING_ANGLES) {
            double[] realAngles = new double[FIRING_ANGLES];
            double maxEscapeAngle = Math.asin(8.0 / w.bulletSpeed);
            for (int x = 0; x < FIRING_ANGLES; x++) {
                realAngles[x] = (((double) (x - GF_ZERO)) / GF_ZERO) *
                        maxEscapeAngle;
            }

            for (int x = 0; x < FIRING_ANGLES; x++) {
                double density = 0;
                for (int y = 0; y < numScans; y++) {
                    if (firingAngles[y] == DiamondFist.NO_FIRING_ANGLE)
                        continue;

                    double ux = (realAngles[x] - firingAngles[y]) / bandwidth;

                    if (Math.abs(ux) < 1) {
                        density += DiaUtils.square(1 - DiaUtils.square(Math.abs(ux)));
                    }
//                    // Gaussian
//                    xDensity += Math.exp(-0.5 * ux * ux);
                }

                if (density > bestDensity) {
                    bestAngle = realAngles[x];
                    bestDensity = density;
                }

                if (painting) {
                    cvs.addValue(density, w.absBearing + realAngles[x]);
                }
            }
        } else {
            for (int x = 0; x < numScans; x++) {
                if (firingAngles[x] == DiamondFist.NO_FIRING_ANGLE) {
                    continue;
                }

                double xFiringAngle = firingAngles[x];

                double xDensity = 0;
                for (int y = 0; y < numScans; y++) {
                    if (x == y || firingAngles[y] == DiamondFist.NO_FIRING_ANGLE)
                        continue;

                    double yFiringAngle = firingAngles[y];

                    double ux = (xFiringAngle - yFiringAngle) / bandwidth;

                    // Gaussian
                    xDensity += Math.exp(-0.5 * ux * ux);
                }

                if (xDensity > bestDensity) {
                    bestAngle = xFiringAngle;
                    bestDensity = xDensity;
                }

                if (painting) {
                    cvs.addValue(xDensity, w.absBearing + xFiringAngle);
                }
            }
        }

        if (bestAngle == DiamondFist.NO_FIRING_ANGLE) {
            return w.absBearing;
        }

        if (painting) {
            DiamondFist.paintGunAngles(_renderables, w, cvs,
                    w.absBearing + bestAngle, bandwidth);
        }

        double firingAngle = Utils.normalAbsoluteAngle(w.absBearing + bestAngle);
        _firingAngles.put(w, firingAngle);

        return firingAngle;
    }

    public void clear() {
    }

    public void clearWave(DiaWave w) {
        _firingAngles.remove(w);
    }

    public String getLabel() {
        return "TripHammer KNN Gun";
    }
}
