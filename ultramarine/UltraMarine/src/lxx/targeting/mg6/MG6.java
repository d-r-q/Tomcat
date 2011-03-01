package lxx.targeting.mg6;

import lxx.targeting.predict.Predictor;
import lxx.targeting.Target;
import lxx.targeting.TargetImage;
import lxx.targeting.mg4.DeltaVector;
import lxx.targeting.mg4.MegaGun4;
import lxx.autosegmentation.AttributeFactory;
import lxx.autosegmentation.model.FireSituation;
import lxx.autosegmentation.model.Attribute;
import static lxx.StaticData.robot;
import lxx.wave.WaveCallback;
import lxx.wave.Wave;
import lxx.wave.WaveManager;
import lxx.utils.APoint;
import lxx.utils.Utils;
import lxx.utils.LXXPoint;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import static java.lang.Math.round;
import static java.lang.Math.toDegrees;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static java.lang.Math.cos;
import static java.lang.Math.abs;

import robocode.*;
import robocode.Event;

/**
 * User: jdev
 * Date: 19.05.2010
 */
public class MG6 implements Predictor, WaveCallback {

    private final Map<String, double[]> enemyFactors = new HashMap<String,double[]>();

    private static final List<FireSituationDV> fireSituations = new ArrayList<FireSituationDV>();

    private final Map<Wave, TargetImage> targets = new HashMap<Wave, TargetImage>();
    private final Map<Wave, FireSituation> waveFireSituations = new HashMap<Wave, FireSituation>();

    private final WaveManager waveManager;
    private final AttributeFactory attributeFactory;
    private static final int FIRESITUATIONS_COUNT = 3;

    public MG6(AttributeFactory attributeFactory, WaveManager waveManager) {
        this.attributeFactory = attributeFactory;
        this.waveManager = waveManager;
        double[] factors = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(robot.getDataDirectory().getAbsolutePath() + "/factors.dat"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                int eqIdx = line.indexOf('=');
                factors = new double[attributeFactory.getAttributes().length];
                String[] factorsStrings = line.substring(eqIdx + 1).split(";");
                if (factorsStrings.length != factors.length) {
                    throw new RuntimeException("Something wrong");
                }

                for (int i = 0; i < factors.length; i++) {
                    factors[i] = Double.parseDouble(factorsStrings[i]);
                }
                this.enemyFactors.put(line.substring(0, eqIdx), factors);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Double predictAngle(Target t) {
        double scales[] = new double[attributeFactory.getAttributes().length];

        for (int i = 0; i < scales.length; i++) {
            final Attribute a = attributeFactory.getAttribute(i);
            int domainSize = a.getMaxValue() - a.getMinValue() + 1;
            if (domainSize == 0) {
                domainSize = 1;
            }
            scales[i] = 1000 / domainSize;
        }

        final List<Match> bestMatches = new ArrayList<Match>();
        FireSituation fs = attributeFactory.getFireSituation(t);
        final LXXPoint robotPos = robot.getPosition();
        for (FireSituationDV fsdv : fireSituations) {
            double maxDist = t.getMaxEscapeDistance(toRadians(fsdv.deltaVector.alpha), robotPos);
            LXXPoint newPos = new LXXPoint(t.getX() + sin(t.getAbsoluteHeading() + toRadians(fsdv.deltaVector.alpha)) * fsdv.deltaVector.dist * maxDist,
                    t.getY() + cos(t.getAbsoluteHeading() + toRadians(fsdv.deltaVector.alpha)) * fsdv.deltaVector.dist * maxDist);
            if (!robot.getBattleField().contains(newPos)) {
                continue;
            }
            double match = Utils.factoredManhettanDistance(fs.getFsAttributes(), fsdv.fireSituation.getFsAttributes(),
                    enemyFactors.get(t.getName() + ".targeting"), scales);
            Match m = new Match(fsdv, match);
            bestMatches.add(m);
            Collections.sort(bestMatches, new Comparator<Match>() {

                public int compare(Match o1, Match o2) {
                    return o1.match.compareTo(o2.match);
                }
            });

            if (bestMatches.size() > FIRESITUATIONS_COUNT) {
                bestMatches.remove(bestMatches.size() - 1);
            }
        }

        int[] angles = new int[FIRESITUATIONS_COUNT];
        int idx = 0;
        robot.getGraphics().setColor(Color.WHITE);
        for (Match m : bestMatches) {
            double maxDist = t.getMaxEscapeDistance(toRadians(m.fireSituationDv.deltaVector.alpha), robotPos);
            LXXPoint newPos = new LXXPoint(t.getX() + sin(t.getAbsoluteHeading() + toRadians(m.fireSituationDv.deltaVector.alpha)) * m.fireSituationDv.deltaVector.dist * maxDist,
                    t.getY() + cos(t.getAbsoluteHeading() + toRadians(m.fireSituationDv.deltaVector.alpha)) * m.fireSituationDv.deltaVector.dist * maxDist);
            robot.getGraphics().drawLine((int)robotPos.getX(), (int)robotPos.getY(), (int)newPos.getX(), (int)newPos.getY());
            int angle = (int) toDegrees(robot.angleTo(newPos));
            angles[idx++] = angle;
        }

        int kernelAngle = 0;
        int kernelAngleDist = Integer.MAX_VALUE;
        for (int i = 0; i < angles.length; i++) {
            int dist = 0;
            for (int j = 0; j < angles.length; j++) {
                dist += abs(angles[i] - angles[j]);
            }
            if (dist < kernelAngleDist) {
                kernelAngle = angles[i];
                kernelAngleDist = dist;
            }
        }

        return toRadians(kernelAngle);
    }

    public void paint(Graphics2D g, Target t) {
    }

    public void onRoundStarted() {
    }

    public String getName() {
        return "MG6";
    }

    public void targetUpdated(Target oldState, Target newState, Event source) {
        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), Rules.getBulletSpeed(2), this);
        targets.put(w, new TargetImage(newState));
        waveFireSituations.put(w, attributeFactory.getFireSituation(newState));
    }

    public void waveBroken(Wave w) {
        TargetImage ti = targets.remove(w);
        FireSituation fs = waveFireSituations.remove(w);
        final DeltaVector deltaVector = getDeltaVector(ti, w.target.getPosition(), w.sourcePos);
        fireSituations.add(new FireSituationDV(fs, deltaVector));
    }

    public DeltaVector getDeltaVector(TargetImage source, APoint dest, APoint umPos) {
        final double angle = robocode.util.Utils.normalRelativeAngle(Utils.angle(source, dest) - source.getAbsoluteHeading());
        int alpha = (int) round(toDegrees(angle));
        double dist = (source.aDistance(dest) / source.getMaxEscapeDistance(angle, umPos));
        /*if (dist > 1) {
            throw new RuntimeException("Something wrong");
        }*/

        return new DeltaVector(alpha, dist);
    }

    private class FireSituationDV {
        public final FireSituation fireSituation;
        public final DeltaVector deltaVector;

        private FireSituationDV(FireSituation fireSituation, DeltaVector deltaVector) {
            this.fireSituation = fireSituation;
            this.deltaVector = deltaVector;
        }
    }

    private class Match {

        public final FireSituationDV fireSituationDv;
        public final Double match;

        private Match(FireSituationDV fireSituationDv, double match) {
            this.fireSituationDv = fireSituationDv;
            this.match = match;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MG6 mg6 = (MG6) o;

        return getName().equals(mg6.getName());

    }

    public int hashCode() {
        return getName().hashCode();
    }

}
