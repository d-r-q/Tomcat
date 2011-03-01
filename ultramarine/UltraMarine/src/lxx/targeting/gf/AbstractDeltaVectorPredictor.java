package lxx.targeting.gf;

import static lxx.StaticData.robot;
import lxx.UltraMarine;
import lxx.targeting.Target;
import lxx.targeting.TargetImage;
import lxx.targeting.predict.Predictor;
import lxx.utils.LXXPoint;
import lxx.utils.Utils;
import lxx.utils.kd_tree.KDNode;
import lxx.utils.kd_tree.KDTree;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import lxx.wave.WaveManager;
import robocode.Rules;

import java.awt.*;
import static java.lang.Math.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public abstract class AbstractDeltaVectorPredictor implements Predictor, WaveCallback {

    private final WaveManager waveManager;

    private final Map<Wave, TargetImage> targetImages = new HashMap<Wave, TargetImage>();

    public AbstractDeltaVectorPredictor(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    public Double predictAngle(Target t) {
        KDNode<GFData, Target> node = getGuessFactors().getNode(t);
        //System.out.println(getClass().getSimpleName() + ": use data from level " + node.getLevel());
        GFData data = node.getData();
        Map<String, Double> stat = data.getStat();
        double maxCount = -1;
        LXXPoint p = null;
        for (Map.Entry<String, Double> e : stat.entrySet()) {
            double alpha = t.getAbsoluteHeading() - toRadians(Double.parseDouble(e.getKey().substring(0, e.getKey().indexOf(":"))) * 10);
            double dist = Double.parseDouble(e.getKey().substring(e.getKey().indexOf(":") + 1)) * 10;

            LXXPoint candidate = new LXXPoint(t.getX() + sin(alpha) * dist, t.getY() + cos(alpha) * dist);

            if ((candidate.getX() > -10 && candidate.getY() > -10 && candidate.getX() < robot.getBattleFieldWidth() + 10 &&
                    candidate.getY() < robot.getBattleFieldHeight() + 10) &&
                    abs(robot.angleTo(candidate) - robot.angleTo(t)) <
                            t.maxEscapeAngle((int)signum(robot.angleTo(t) - robot.angleTo(candidate))) && 
                    e.getValue() > maxCount) {
                maxCount = e.getValue();
                p = candidate;
            }

        }

        if (p == null) {
            return null;
        }

        return robot.angleTo(p);
    }

    public void paint(Graphics2D g, Target t) {
       /* Target t = ((UltraMarine)robot).getTargetChooser().getBestTarget();
        LXXPoint enemyPos = t.getPosition();
        LXXPoint prediction = predictAngle(t);
        g.setColor(Color.YELLOW);
        g.drawOval((int)prediction.getX() - 5, (int)prediction.getY() - 5, 10, 10);

        KDNode<GFData, Target> node = getGuessFactors().getNode(t);
        GFData data = node.getData();
        Map<String, Double> stat = data.getStat();
        g.setColor(Color.WHITE);
        g.drawString(getClass().getSimpleName(), 0, 0);
        g.setColor(Color.RED);
        for (Map.Entry<String, Double> e : stat.entrySet()) {
            double alpha = t.getAbsoluteHeading() - toRadians(Double.parseDouble(e.getKey().substring(0, e.getKey().indexOf(":"))) * 10);
            double dist = Double.parseDouble(e.getKey().substring(e.getKey().indexOf(":") + 1)) * 10;

            LXXPoint candidate = new LXXPoint(t.getX() + sin(alpha) * dist, t.getY() + cos(alpha) * dist);

            g.drawLine((int)enemyPos.getX(), (int)enemyPos.getY(), (int)candidate.getX(), (int)candidate.getY());
            g.drawString(e.getValue().toString(), (int)candidate.getX(), (int)candidate.getY());
        }*/

        //System.out.println(getClass().getSimpleName() + " stat: " + data);
    }

    public void onRoundStarted() {
        System.out.println(getClass().getSimpleName() + " guessFactors: " + getGuessFactors().getNodeCount());
    }

    public void targetUpdated(Target oldState, Target newState, robocode.Event source) {
        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.getGunHeadingRadians(),
                Rules.getBulletSpeed(((UltraMarine) robot).firePower()), this);
        TargetImage ti = new TargetImage(newState);
        targetImages.put(w, ti);
    }

    public void waveBroken(Wave w) {
        double alpha = round(toDegrees(w.targetHeading - Utils.angle(w.targetPos, w.target))) / 10;
        double dist = round(w.targetPos.distance(w.target.getX(), w.target.getY())) / 10;
        final TargetImage image = targetImages.remove(w);
        // todo: fix
        if (image == null) {
            return;
        }
        getGuessFactors().addStat(image, alpha, dist);
    }

    public abstract KDTree getGuessFactors();

    public String getName() {
        return getClass().getSimpleName();
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractDeltaVectorPredictor megaGun2 = (AbstractDeltaVectorPredictor) o;

        return getName().equals(megaGun2.getName());

    }

    public int hashCode() {
        return getName().hashCode();
    }

}
