package lxx.targeting.mg4;

import static lxx.StaticData.robot;
import lxx.autosegmentation.AttributeFactory;
import lxx.autosegmentation.model.FireSituation;
import lxx.targeting.Target;
import lxx.targeting.TargetImage;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.predict.Predictor;
import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.utils.APoint;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import lxx.wave.WaveManager;
import robocode.Event;
import robocode.Rules;

import java.awt.*;
import static java.lang.Math.*;
import static java.lang.Math.round;
import java.util.*;

/**
 * User: jdev
 * Date: 14.05.2010
 */
public class MegaGun4 implements TargetManagerListener, WaveCallback, Predictor {

    public static final int ANGLE_SCALE = 9;
    public static final int DIST_SCALE = 15;

    private static Map<DeltaVector, Pattern> patterns = new HashMap<DeltaVector, Pattern>();

    private final Map<Wave, TargetImage> targets = new HashMap<Wave, TargetImage>();
    private final Map<Wave, FireSituation> fireSituations = new HashMap<Wave, FireSituation>();

    private final WaveManager waveManager;
    private final AttributeFactory attributeFactory;

    private int maxVisitCount = 0;

    public MegaGun4(WaveManager waveManager, AttributeFactory attributeFactory) {
        this.waveManager = waveManager;
        this.attributeFactory = attributeFactory;
    }

    public void targetUpdated(Target oldState, Target newState, Event source) {
        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), Rules.getBulletSpeed(2), this);
        targets.put(w, new TargetImage(newState));
        fireSituations.put(w, attributeFactory.getFireSituation(newState));
    }

    public void waveBroken(Wave w) {
        TargetImage ti = targets.remove(w);
        FireSituation fs = fireSituations.remove(w);
        final DeltaVector deltaVector = getDeltaVector(ti, w.target.getPosition(), w.sourcePos);
        if (deltaVector == null) {
            return;
        }
        Pattern pattern = patterns.get(deltaVector);
        if (pattern == null) {
            pattern = new Pattern(attributeFactory);
            patterns.put(deltaVector, pattern);
        }
        pattern.addPredicat(fs);
        if (pattern.getFsCount() > maxVisitCount) {
            maxVisitCount = pattern.getFsCount();
        }
    }

    public DeltaVector getDeltaVector(TargetImage source, APoint dest, APoint umPos) {
        final double angle = robocode.util.Utils.normalRelativeAngle(Utils.angle(source, dest) - source.getAbsoluteHeading());
        int alpha = (int) round(toDegrees(angle) / ANGLE_SCALE);
        double dist = (double)((int) ((source.aDistance(dest) / source.getMaxEscapeDistance(angle, umPos)) * DIST_SCALE)) / DIST_SCALE;
        if (dist > 1) {
            // todo: fix me
            return null;
        }

        return new DeltaVector(alpha, dist);
    }

    public Pattern getPattern(DeltaVector dv) {
        return patterns.get(dv);
    }

    public Double predictAngle(Target t) {
        /*TreeMap<Double, int[]> matches = new TreeMap<Double, int[]>(new Comparator<Double>() {

            public int compare(Double o1, Double o2) {
                if (o1 > o2) {
                    return -1;
                } else if (o1 < o2) {
                    return 1;
                }
                return 0;
            }
        });*/

        FireSituation fs = attributeFactory.getFireSituation(t);
        double maxMatch = -1;
        int minAlpha = 0;
        double minDist = 0;
        for (Map.Entry<DeltaVector, Pattern> e : patterns.entrySet()) {
            if (e.getValue().getFsCount() < maxVisitCount * 0.01 || e.getValue().getFsCount() < 10) {
                continue;
            }
            double match = match(fs, e);
            // " " + alpha + " : " + dist + " "
            if (match > maxMatch) {
                minAlpha = e.getKey().alpha;
                minDist = e.getKey().dist;
                maxMatch = match;
            }
        }
        /*if (maxMatch < attributeFactory.getAttributes().length / 2) {
            return null;
        }*/


        /*int i = 0;
        Map<Integer, Integer> alphaDists = new HashMap<Integer, Integer>();
        Map<Integer, Integer> distDists = new HashMap<Integer, Integer>();
        robot.getGraphics().setColor(Color.WHITE);
        for (Iterator<Map.Entry<Double, int[]>> iter = matches.entrySet().iterator(); iter.hasNext() && i < 1; i++) {
            int[] data1 = iter.next().getValue();
            int j = 0;
            int distDist = 0;
            int alphaDist = 0;
            for (Iterator<Map.Entry<Double, int[]>> iter2 = matches.entrySet().iterator(); iter2.hasNext() && j < 1; j++) {
                int[] data2 = iter2.next().getValue();
                alphaDist += abs(data1[0] - data2[0]);
                distDist += abs(data1[1] - data2[1]);
            }
            Integer oldAlphaDist = alphaDists.get(data1[0]);
            if (oldAlphaDist == null || oldAlphaDist > alphaDist) {
                alphaDists.put(data1[0], alphaDist);
            }
            Integer oldDistDist = distDists.get(data1[1]);
            if (oldDistDist == null || oldDistDist > distDist) {
                distDists.put(data1[1], distDist);
            }

            double maxEscapeDist = t.getMaxEscapeDistance(toRadians(data1[0] * 5), robot);
            LXXPoint newPos = new LXXPoint(t.getX() + sin(t.getAbsoluteHeading() + toRadians(data1[0] * 5)) * ((double)data1[1] / DIST_SCALE) * maxEscapeDist,
                    t.getY() + cos(t.getAbsoluteHeading() + toRadians(data1[0] * 5)) * ((double)data1[1] / DIST_SCALE) * maxEscapeDist);
            robot.getGraphics().fillOval((int)newPos.getX() - 3, (int)newPos.getY() - 3, 6, 6);
            robot.getGraphics().drawLine((int)t.getX(), (int)t.getY(), (int)newPos.getX(), (int)newPos.getY());
        }
        int minAlpha = 0;
        int minAlphaDist = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> e : alphaDists.entrySet()) {
            if (e.getValue() < minAlphaDist) {
                minAlpha = e.getKey();
                minAlphaDist = e.getValue();
            }
        }
        int minAlpha = 0;
        int minAlphaDist = Integer.MAX_VALUE;
        int minDist = 0;
        int minDistDist = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> e : distDists.entrySet()) {
            if (e.getValue() < minDistDist) {
                minDist = e.getKey();
                minDistDist = e.getValue();
            }
        }*/

        double maxEscapeDist = t.getMaxEscapeDistance(toRadians(minAlpha * ANGLE_SCALE), robot);
        LXXPoint newPos = new LXXPoint(t.getX() + sin(t.getAbsoluteHeading() + toRadians(minAlpha * ANGLE_SCALE)) * minDist * maxEscapeDist,
                t.getY() + cos(t.getAbsoluteHeading() + toRadians(minAlpha * ANGLE_SCALE)) * minDist * maxEscapeDist);



        return robot.angleTo(newPos);
    }

    public PredictionData getPredictionData(Target t) {
        FireSituation fs = attributeFactory.getFireSituation(t);
        double maxMatch = -1;
        Pattern bestPattern = null;
        Map<DeltaVector, Double> matches = new HashMap<DeltaVector, Double>();
        for (Map.Entry<DeltaVector, Pattern> e : patterns.entrySet()) {
            if (maxVisitCount == 0/* || e.getValue().getFsCount() / maxVisitCount < 0.05 || e.getValue().getFsCount() < 10*/) {
                continue;
            }
            double match = match(fs, e);
            matches.put(e.getKey(), match);
            if (match > maxMatch) {
                maxMatch = match;
                bestPattern = e.getValue();
            }
        }

        return new PredictionData(matches, bestPattern, new TargetImage(t), t, fs, new LXXPoint(robot.getPosition()), this);
    }

    private double match(FireSituation fs, Map.Entry<DeltaVector, Pattern> e) {
        return e.getValue().match(fs)/* +  (((double)e.getValue().getFsCount() / maxVisitCount) * 20)*/;
    }

    public void paint(Graphics2D g, Target t) {
    }

    public void onRoundStarted() {
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MegaGun4 megaGun2 = (MegaGun4) o;

        return getName().equals(megaGun2.getName());

    }

    public int hashCode() {
        return getName().hashCode();
    }


}
