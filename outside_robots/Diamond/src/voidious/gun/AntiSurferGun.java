package voidious.gun;

import robocode.util.Utils;
import voidious.gfx.ColoredValueSet;
import voidious.gfx.RoboGraphic;
import voidious.utils.DataView;
import voidious.utils.DiaUtils;
import voidious.utils.DiaWave;
import voidious.utils.KdBucketTree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
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

public class AntiSurferGun implements DiaGun {
    protected HashMap<String, EnemyDataGun> _enemies;
    protected Rectangle2D.Double _fieldRect;
    protected Vector<RoboGraphic> _renderables;
    protected HashMap<DiaWave, Double> _firingAngles;

    protected static final String[] VIEW_NAMES = new String[]{
            DiamondFist.VIEW_1V1_ANTISURFER + "1",
            DiamondFist.VIEW_1V1_ANTISURFER + "2",
            DiamondFist.VIEW_1V1_ANTISURFER + "3"
//        DiamondFist.VIEW_1V1_ANTISURFER + "4"
    };

    public AntiSurferGun(HashMap<String, EnemyDataGun> enemies,
                         Rectangle2D.Double fieldRect, Vector<RoboGraphic> renderables,
                         int enemiesTotal) {

        _enemies = enemies;
        _fieldRect = fieldRect;
        _renderables = renderables;
        _firingAngles = new HashMap<DiaWave, Double>();
    }

    public void clear() {
        _firingAngles.clear();
    }

    public void clearWave(DiaWave w) {
        _firingAngles.remove(w);
    }

    public String getLabel() {
        return "Anti-Surfer Gun";
    }

    public double aimWithWave(DiaWave w, boolean painting) {
        if (_firingAngles.containsKey(w)) {
            return _firingAngles.get(w);
        }

        EnemyDataGun edg = _enemies.get(w.botName);
        double[][] nearestNeighbors = null;
        double[] neighborWeights = null;
        for (int x = 0; x < VIEW_NAMES.length; x++) {
            DataView view = edg.views.get(VIEW_NAMES[x]);
            KdBucketTree scanTree = view.tree;
            if (view.treeSize < 10) {
                continue;
            }

            int clusterSize = (int) Math.min(view.treeSize / 10, view.clusterSize);
            double[] wavePoint =
                    view.formula.dataPointFromWave(w, DiamondFist.WAVE_AIMING);
            double[][] thisNeighbors = KdBucketTree.nearestNeighbors(
                    scanTree, wavePoint, clusterSize, view.formula.weights);
            double[] thisWeights = new double[thisNeighbors.length];
            Arrays.fill(thisWeights, view.weight);

            if (nearestNeighbors == null) {
                nearestNeighbors = thisNeighbors;
                neighborWeights = thisWeights;
            } else {
                int totalNeighbors =
                        thisNeighbors.length + nearestNeighbors.length;
                double[][] combinedNeighbors =
                        new double[totalNeighbors][wavePoint.length];
                double[] combinedWeights = new double[totalNeighbors];
                for (int y = 0; y < nearestNeighbors.length; y++) {
                    combinedNeighbors[y] = nearestNeighbors[y];
                    combinedWeights[y] = neighborWeights[y];
                }
                for (int y = 0; y < thisNeighbors.length; y++) {
                    combinedNeighbors[y + nearestNeighbors.length] =
                            thisNeighbors[y];
                    combinedWeights[y + nearestNeighbors.length] =
                            thisWeights[y];
                }
                nearestNeighbors = combinedNeighbors;
                neighborWeights = combinedWeights;
            }
        }

        if (nearestNeighbors == null || nearestNeighbors.length == 0) {
            return w.absBearing;
        }

        int numScans = nearestNeighbors.length;
        double[] firingAngles = new double[numScans];
        for (int x = 0; x < numScans; x++) {
            Point2D.Double dispVector =
                    edg.displacementVectors.get(nearestNeighbors[x]);
            Point2D.Double projectedLocation = w.projectLocation(dispVector);
            if (!_fieldRect.contains(projectedLocation)) {
                firingAngles[x] = DiamondFist.NO_FIRING_ANGLE;
            } else {
                firingAngles[x] = Utils.normalRelativeAngle(
                        w.firingAngleFromTargetLocation(projectedLocation)
                                - w.absBearing);
            }
        }

        double bestAngle = DiamondFist.NO_FIRING_ANGLE;
        double bestDensity = Double.NEGATIVE_INFINITY;
        double bandwidth = DiaUtils.botWidthAimAngle(
                w.sourceLocation.distance(w.targetLocation)) * 2;
        ColoredValueSet cvs = new ColoredValueSet();

        for (int x = 0; x < numScans; x++) {
            if (firingAngles[x] == DiamondFist.NO_FIRING_ANGLE) {
                continue;
            }

            double xFiringAngle = firingAngles[x];

            double xDensity = 0;
            for (int y = 0; y < numScans; y++) {
                if (firingAngles[y] == DiamondFist.NO_FIRING_ANGLE)
                    continue;

                double yFiringAngle = firingAngles[y];

                double ux = (xFiringAngle - yFiringAngle) / bandwidth;

                // Gaussian
                xDensity += Math.exp(-0.5 * ux * ux) * neighborWeights[y];
            }

            if (xDensity > bestDensity) {
                bestAngle = xFiringAngle;
                bestDensity = xDensity;
            }

            if (painting) {
                cvs.addValue(xDensity, w.absBearing + xFiringAngle);
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
}
