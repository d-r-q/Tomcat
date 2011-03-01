package lxx.targeting.mg;

import static lxx.StaticData.robot;
import lxx.targeting.Target;
import lxx.targeting.TargetImage;
import lxx.targeting.TargetManagerListener;
import lxx.targeting.predict.Predictor;
import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.utils.LXXConstants;
import lxx.utils.kd_tree.KDNode;
import lxx.utils.kd_tree.KDTree;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import lxx.wave.WaveManager;
import robocode.Event;
import robocode.Rules;

import java.awt.*;
import static java.lang.Math.*;
import static java.lang.Math.abs;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: jdev
 * Date: 27.01.2010
 */
public abstract class MegaGun implements TargetManagerListener, WaveCallback, Predictor {

    private final WaveManager waveManager;
    private final Map<Wave, Object[]> targets = new HashMap<Wave, Object[]>();

    public MegaGun(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    public void targetUpdated(Target oldState, Target newState, Event source) {
        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.getGunHeadingRadians(),
                Rules.getBulletSpeed(robot.firePower()), this);
        targets.put(w, new Object[]{new TargetImage(newState), abs(newState.maxEscapeAngle(-1)),  newState.maxEscapeAngle(1)});
    }

    public void waveBroken(Wave w) {

        double sourceAngle = Utils.angle(w.sourcePos, w.targetPos);
        double angle = Utils.angle(w.sourcePos, w.target) - sourceAngle;
        final Double maxEscapeAngle = angle < 0 ? (Double) targets.get(w)[1] : (Double) targets.get(w)[2];
        if (abs(angle) > maxEscapeAngle) {
            angle -= Math.PI * 2 * signum(angle);
        }

        final double factor = (short) ceil(angle / maxEscapeAngle * LXXConstants.MAX_GUESS_FACTOR);
        if (abs(factor) > LXXConstants.MAX_GUESS_FACTOR) {
            return;
        }

        getGuessFactors().addStat((Target)targets.get(w)[0], factor);
    }

    public Double predictAngle(Target t) {
        LXXPoint p = null;
        KDNode<MGData, Target> node = getGuessFactors().getNode(t);
        MGData data = node.getData();
        Map<Double, Double> stat = data.getStat();
        double maxCount = -1;

        //System.out.println(stat);
        double resGF = 0;
        for (Map.Entry<Double, Double> e : stat.entrySet()) {
            double guessFactor = e.getKey();
            double dist = robot.distance(t);
            double alpha = robot.angleTo(t) + t.maxEscapeAngle((int)signum(guessFactor)) * guessFactor / LXXConstants.MAX_GUESS_FACTOR;

            LXXPoint candidate = new LXXPoint(robot.getX() + sin(alpha) * dist, robot.getY() + cos(alpha) * dist);

            if (e.getValue() > maxCount) {
                maxCount = e.getValue();
                p = candidate;
                resGF = guessFactor;
            }
        }

        if (p == null) {
            return null;
        }
        //System.out.println("GF = " + resGF);

        return robot.angleTo(p);
    }

    public void paint(Graphics2D g, Target t) {
        /*if (robot.maxEscapeAngle(null) > 0) {
            Target t = ((UltraMarine) robot).getTargetChooser().getBestTarget();

            KDNode<MGData, Target> node = getGuessFactors().getNode(t);
            MGData data = node.getData();
            Map<Double, Double> stat = data.getStat();
            g.setColor(Color.WHITE);
            g.drawString(getClass().getSimpleName(), 0, 0);
            g.setColor(Color.RED);
            for (Map.Entry<Double, Double> e : stat.entrySet()) {
                double guessFactor = e.getKey();
                double dist = robot.distance(t);
                double alpha = robot.angleTo(t) + robot.maxEscapeAngle(null) * guessFactor / MAX_GUESS_FACTOR;

                LXXPoint candidate = new LXXPoint(robot.getX() + sin(alpha) * dist, robot.getY() + cos(alpha) * dist);

                if (guessFactor < 0) {
                    g.setColor(Color.BLUE);
                } else {
                    g.setColor(Color.RED);
                }
                g.drawString(guessFactor + " - " + e.getValue().toString(), (int) candidate.getX(), (int) candidate.getY());
                g.drawOval((int) candidate.getX() - 5, (int) candidate.getY() - 5, 10, 10);
            }
        }*/
    }

    public void onRoundStarted() {
    }

    public abstract KDTree<MGData, Target> getGuessFactors();

    public abstract String getName();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MegaGun megaGun = (MegaGun) o;

        return !(getName() != null ? !getName().equals(megaGun.getName()) : megaGun.getName() != null);

    }

    public int hashCode() {
        return (getName() != null ? getName().hashCode() : 0);
    }
}
