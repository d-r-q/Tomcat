package lxx.targeting.mg4;

import lxx.targeting.TargetManagerListener;
import lxx.targeting.TargetImage;
import lxx.targeting.Target;
import lxx.targeting.predict.Predictor;
import lxx.wave.WaveCallback;
import lxx.wave.Wave;
import lxx.wave.WaveManager;
import lxx.autosegmentation.model.FireSituation;
import lxx.autosegmentation.AttributeFactory;
import static lxx.StaticData.robot;
import lxx.utils.APoint;
import lxx.utils.Utils;
import lxx.utils.LXXPoint;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import static java.lang.Math.round;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import java.awt.*;

import robocode.Event;
import robocode.Rules;

/**
 * User: jdev
 * Date: 17.05.2010
 */
public class MegaGun5 implements TargetManagerListener, WaveCallback, Predictor {

    private static final Map<DeltaVector, List<FireSituation>> fireSituations = new HashMap<DeltaVector, List<FireSituation>>();

    private final Map<Wave, TargetImage> targets = new HashMap<Wave, TargetImage>();
    private final Map<Wave, FireSituation> waveFireSituations = new HashMap<Wave, FireSituation>();

    private final WaveManager waveManager;
    private final AttributeFactory attributeFactory;

    private int maxVisitCount = 0;

    public MegaGun5(WaveManager waveManager, AttributeFactory attributeFactory) {
        this.waveManager = waveManager;
        this.attributeFactory = attributeFactory;
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
        if (deltaVector == null) {
            return;
        }
        List<FireSituation> fses = fireSituations.get(deltaVector);
        if (fses == null) {
            fses = new ArrayList<FireSituation>();
            fireSituations.put(deltaVector, fses);
        }
        fses.add(fs);
    }

    public DeltaVector getDeltaVector(TargetImage source, APoint dest, APoint umPos) {
        final double angle = robocode.util.Utils.normalRelativeAngle(Utils.angle(source, dest) - source.getAbsoluteHeading());
        int alpha = (int) round(toDegrees(angle) / MegaGun4.ANGLE_SCALE);
        double dist = (double) ((int) ((source.aDistance(dest) / source.getMaxEscapeDistance(angle, umPos)) * MegaGun4.DIST_SCALE)) / MegaGun4.DIST_SCALE;
        if (dist > 1) {
            // todo: fix me
            return null;
        }

        return new DeltaVector(alpha, dist);
    }

    public Double predictAngle(Target t) {
        FireSituation curFS = attributeFactory.getFireSituation(t);
        Map<Integer, List<Double>> angleVotes = new HashMap<Integer, List<Double>>();
        final LXXPoint robotPos = robot.getPosition();
        int minAngle = 1000;
        int maxAngle = -1000;
        for (DeltaVector dv : fireSituations.keySet()) {
            double res = 0;
            for (FireSituation fs : fireSituations.get(dv)) {
                res += fs.match(curFS);
            }
            res /= fireSituations.get(dv).size();

            double maxEscapeDist = t.getMaxEscapeDistance(toRadians(dv.alpha * MegaGun4.ANGLE_SCALE), robotPos);
            LXXPoint newPos = new LXXPoint(t.getX() + sin(t.getAbsoluteHeading() + toRadians(dv.alpha * MegaGun4.ANGLE_SCALE)) * dv.dist * maxEscapeDist,
                    t.getY() + cos(t.getAbsoluteHeading() + toRadians(dv.alpha * MegaGun4.ANGLE_SCALE)) * dv.dist * maxEscapeDist);
            //robot.getGraphics().drawLine((int)robotPos.getX(), (int)robotPos.getY(), (int)newPos.getX(), (int)newPos.getY());
            Integer angle = (int) round(toDegrees(robot.angleTo(newPos)) / 4) * 4;
            if (angle < minAngle) {
                minAngle = angle;
            }
            if (angle > maxAngle) {
                maxAngle = angle;
            }
            List<Double> votes = angleVotes.get(angle);
            if (votes == null) {
                votes = new ArrayList<Double>();
                angleVotes.put(angle, votes);
            }
            votes.add(res);

            /*try {
                robot.getGraphics().setColor(new Color((int) (255 - res * 255), 0, (int) (res * 255)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            robot.getGraphics().fillOval((int) newPos.getX() - 2, (int) newPos.getY() - 2, 4, 4);*/
        }

        double minVotes = Integer.MAX_VALUE;
        Double minVotesAngle = 0D;
        //robot.getGraphics().setColor(new Color(0, 0, 255, 75));
        for (Integer angle : angleVotes.keySet()) {
            double votes = 0;
            for (Double vote : angleVotes.get(angle)) {
                votes += vote;
            }
            votes /= angleVotes.get(angle).size();
            if (votes < minVotes) {
                minVotes = votes;
                minVotesAngle = angle.doubleValue();
            }
            /*double a1 = angle - 2;
            LXXPoint pnt1 = new LXXPoint(robotPos.getX() + sin(toRadians(a1)) * 1000,
                    robotPos.getY() + cos(toRadians(a1)) * 1000);
            robot.getGraphics().drawLine((int) robotPos.getX(), (int) robotPos.getY(), (int) pnt1.getX(), (int) pnt1.getY());

            double a2 = angle + 2;
            LXXPoint pnt2 = new LXXPoint(robotPos.getX() + sin(toRadians(a2)) * 1000,
                    robotPos.getY() + cos(toRadians(a2)) * 1000);
            robot.getGraphics().drawLine((int) robotPos.getX(), (int) robotPos.getY(), (int) pnt2.getX(), (int) pnt2.getY());*/
        }

        return toRadians(minVotesAngle);
    }

    public PredictionData getPredictionData(Target t) {
        FireSituation curFS = attributeFactory.getFireSituation(t);
        Map<DeltaVector, Double> matches = new HashMap<DeltaVector, Double>();
        double minMatch = Integer.MAX_VALUE;
        double maxMatch = Integer.MIN_VALUE;
        for (DeltaVector dv : fireSituations.keySet()) {
            double res = 0;
            for (FireSituation fs : fireSituations.get(dv)) {
                res += fs.match(curFS);
            }
            res /= fireSituations.get(dv).size();
            matches.put(dv, res);
            if (res > maxMatch) {
                maxMatch = res;
            }
            if (res < minMatch) {
                minMatch = res;
            }
        }

        return new PredictionData(matches, new TargetImage(t), t, robot.getPosition(), maxMatch, minMatch);
    }

    public void paint(Graphics2D g, Target t) {
    }

    public void onRoundStarted() {
    }

    public String getName() {
        return "MegaGun5";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MegaGun5 megaGun2 = (MegaGun5) o;

        return getName().equals(megaGun2.getName());

    }

    public int hashCode() {
        return getName().hashCode();
    }

    public class PredictionData {

        private final Map<DeltaVector, Double> matches;
        private final TargetImage targetImage;
        private final Target target;
        private final LXXPoint robotPos;
        private final double minMatch;
        private final double maxMatch;

        public PredictionData(Map<DeltaVector, Double> matches, TargetImage targetImage, Target target, LXXPoint robotPos, double maxMatch, double minMatch) {
            this.matches = matches;
            this.targetImage = targetImage;
            this.target = target;
            this.robotPos = robotPos;
            this.minMatch = minMatch;
            this.maxMatch = maxMatch;
        }

        public void paint(Graphics2D g) {
            Map<Integer, List<Double>> angleVotes = new HashMap<Integer, List<Double>>();
            double maxVotes = Integer.MIN_VALUE;
            double minVotes = Integer.MAX_VALUE;
            for (DeltaVector dv : matches.keySet()) {
                double maxEscapeDist = targetImage.getMaxEscapeDistance(toRadians(dv.alpha * MegaGun4.ANGLE_SCALE), robotPos);
                LXXPoint newPos = new LXXPoint(targetImage.getX() + sin(targetImage.getAbsoluteHeading() + toRadians(dv.alpha * MegaGun4.ANGLE_SCALE)) * dv.dist * maxEscapeDist,
                        targetImage.getY() + cos(targetImage.getAbsoluteHeading() + toRadians(dv.alpha * MegaGun4.ANGLE_SCALE)) * dv.dist * maxEscapeDist);
                Integer angle = (int) round(toDegrees(Utils.angle(robotPos, newPos)) / 4) * 4;
                List<Double> votes = angleVotes.get(angle);
                if (votes == null) {
                    votes = new ArrayList<Double>();
                    angleVotes.put(angle, votes);
                }
                final Double res = matches.get(dv);
                votes.add(res);

                try {
                    final double m = (res - minMatch) / (maxMatch - minMatch);
                    robot.getGraphics().setColor(new Color((int) (255 - m * 255), 0, (int) (m * 255)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                robot.getGraphics().fillOval((int) newPos.getX() - 2, (int) newPos.getY() - 2, 4, 4);
            }

            for (Integer angle : angleVotes.keySet()) {
                double votesCnt = 0;
                for (Double vote : angleVotes.get(angle)) {
                    votesCnt += vote;
                }
                votesCnt /= angleVotes.get(angle).size();
                if (votesCnt > maxVotes) {
                    maxVotes = votesCnt;
                }
                if (votesCnt < minVotes) {
                    minVotes = votesCnt;
                }
            }

            Integer maxMatchAngle = 0;
            for (Integer angle : angleVotes.keySet()) {
                robot.getGraphics().setColor(new Color(0, 0, 255, 75));
                double a1 = angle - 2;
                LXXPoint pnt1 = new LXXPoint(robotPos.getX() + sin(toRadians(a1)) * 1000,
                        robotPos.getY() + cos(toRadians(a1)) * 1000);
                robot.getGraphics().drawLine((int) robotPos.getX(), (int) robotPos.getY(), (int) pnt1.getX(), (int) pnt1.getY());

                double a2 = angle + 2;
                LXXPoint pnt2 = new LXXPoint(robotPos.getX() + sin(toRadians(a2)) * 1000,
                        robotPos.getY() + cos(toRadians(a2)) * 1000);
                robot.getGraphics().drawLine((int) robotPos.getX(), (int) robotPos.getY(), (int) pnt2.getX(), (int) pnt2.getY());

                double votes = 0;
                for (Double vote : angleVotes.get(angle)) {
                    votes += vote;
                }
                votes /= angleVotes.get(angle).size();

                final double m = (votes - minVotes) / (maxVotes - minVotes);
                try {
                robot.getGraphics().setColor(new Color((int) (255 - m * 255), 0, 0, 165));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LXXPoint pnt3 = new LXXPoint(robotPos.getX() + sin(toRadians(angle)) * 1000,
                        robotPos.getY() + cos(toRadians(angle)) * 1000);
                robot.getGraphics().drawLine((int) robotPos.getX(), (int) robotPos.getY(), (int) pnt3.getX(), (int) pnt3.getY());
            }

        }

    }

}
