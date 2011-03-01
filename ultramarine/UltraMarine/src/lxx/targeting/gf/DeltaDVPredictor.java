package lxx.targeting.gf;

import lxx.utils.kd_tree.KDTree;
import lxx.utils.Utils;
import lxx.wave.WaveManager;
import lxx.wave.Wave;
import lxx.targeting.Target;

import static java.lang.Math.round;
import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 05.01.2010
 */
public class DeltaDVPredictor extends AbstractDeltaVectorPredictor {

    private static KDTree<GFData, Target> guessFactors = new KDTree<GFData, Target>(new DeltaDVKeyExtractor(), new GFData());

    public DeltaDVPredictor(WaveManager waveManager) {
        super(waveManager);
    }

    public KDTree getGuessFactors() {
        return guessFactors;
    }

    public void waveBroken(Wave w) {
        double alpha = round(toDegrees(w.targetHeading - Utils.angle(w.targetPos, w.target))) / 10;
        double dist = round(w.targetPos.distance(w.target.getX(), w.target.getY())) / 10;
        //getGuessFactors().addStat(targetImages.remove(w), alpha, dist);
    }

}
