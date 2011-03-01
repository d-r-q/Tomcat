package lxx.targeting.predict;

import static lxx.StaticData.robot;
import lxx.UltraMarine;
import lxx.targeting.Target;
import lxx.targeting.TargetImage;
import lxx.targeting.TargetManager;
import lxx.targeting.TargetManagerListener;
import lxx.utils.LXXPoint;
import lxx.utils.LXXRobot;
import lxx.utils.HitRate;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import lxx.wave.WaveManager;
import robocode.Event;
import robocode.Rules;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jdev
 * Date: 30.10.2009
 */

public class PredictorManager implements TargetManagerListener, WaveCallback {

    private final List<Predictor> predictors = new ArrayList<Predictor>();
    private final Map<Predictor, PredictorData> predictorsData = new HashMap<Predictor, PredictorData>();

    private final TargetManager targetManager;
    private final WaveManager waveManager;

    private final Map<Wave, WaveState> waveStates = new HashMap<Wave, WaveState>();

    public PredictorManager(TargetManager tm, WaveManager waveManager) {
        this.targetManager = tm;
        this.waveManager = waveManager;

        targetManager.addListener(this);
    }

    public void addPredictor(Predictor p) {
        predictors.add(p);
        targetManager.addListener(p);
        predictorsData.put(p, new PredictorData());
    }

    public void removePredictor(Predictor p) {
        predictors.remove(p);
        predictorsData.remove(p);
    }

    public int getPredictorsCount() {
        return predictors.size();
    }

    public List<Predictor> getPredictors() {
        return predictors;
    }

    public void targetUpdated(Target oldState, Target newState, Event source) {

        final double bulletSpeed = Rules.getBulletSpeed(robot.firePower());
        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), bulletSpeed, this);
        WaveState ws = new WaveState();
        ws.gunHeading = robot.getGunHeadingRadians();
        ws.origin = new TargetImage(newState);
        waveStates.put(w, ws);
        for (Predictor p : predictors) {
            ws.predictions.put(p, 0D/*p.predictAngle(newState)*/);
        }
    }

    public Prediction getAngle(Target t) {
        Predictor res = null;
        final Map<Predictor, Double> angles = new HashMap<Predictor, Double>();
        double minHitRate = Integer.MAX_VALUE;
        for (Predictor p : predictorsData.keySet()) {
            final Double angle = p.predictAngle(t);
            angles.put(p, angle);
            final PredictorData data = predictorsData.get(p);
            if (data.getHitRate() < minHitRate) {
                minHitRate = data.getHitRate();
                res = p;
            }
        }
        /*double maxHitRate = 0;
        for (Predictor p : predictorsData.keySet()) {
            final Double angle = p.predictAngle(t);
            angles.put(p, angle);
            final PredictorData data = predictorsData.get(p);
            if (data.getHitRate() > maxHitRate) {
                maxHitRate = data.getHitRate();
                res = p;
            }
        }*/
        if (res == null) {
            return null;
        }
        List<Predictor> predictors = new ArrayList<Predictor>();
        predictors.add(res);

        return new Prediction(angles.get(res), predictors);
    }

    public void paint(Graphics2D g) {
    }

    public void onRoundStarted() {
        for (Predictor p : predictors) {
            p.onRoundStarted();
        }

    }


    public void waveBroken(Wave w) {
        WaveState ws = waveStates.remove(w);
        if (((Target) w.target).getLatency() > 4) {
            return;
        }

        final LXXRobot target = w.getTarget();
        final Graphics2D g = robot.getGraphics();

        for (Predictor p : predictors) {
            Double alpha = ws.predictions.get(p);
            if (alpha == null) {
                continue;
            }

            final LXXPoint point = new LXXPoint(w.sourcePos.getX() + sin(alpha) * w.getTraveledDistance(),
                    w.sourcePos.getY() + cos(alpha) * w.getTraveledDistance());

            Rectangle2D targetBounds = new Rectangle2D.Double((int) (point.getX() - robot.getWidth() / 2),
                    (int) (point.getY() - robot.getHeight() / 2), (int) robot.getWidth(), (int) robot.getHeight());
            PredictorData pd = predictorsData.get(p);
            if (targetBounds.contains((Point2D.Double) target.getPosition())) {
                pd.addHitTime(robot.getTime() - pd.getLastHitTime());
                pd.setLastHitTime(robot.getTime());
                pd.addHitRate(new HitRate(true));
                g.setColor(Color.YELLOW);
            } else {
                pd.addHitRate(new HitRate(false));
                g.setColor(Color.WHITE);
            }

            g.fillOval((int) point.x - 4, (int) point.y - 4, 8, 8);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString(p.getName(), (int) point.x, (int) point.y);
        }
    }

    // todo (zhidkov):fix name
    private class WaveState {
        public TargetImage origin;
        public double gunHeading;
        public Map<Predictor, Double> predictions = new HashMap<Predictor, Double>();
    }

}
                  