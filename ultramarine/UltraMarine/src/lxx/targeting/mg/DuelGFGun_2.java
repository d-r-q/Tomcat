package lxx.targeting.mg;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;

/**
 * User: jdev
 * Date: 14.02.2010
 */
public class DuelGFGun_2 extends MegaGun {

    private static KDTree<MGData, Target> guessFactors;
    private double factor;

    public DuelGFGun_2(WaveManager waveManager, double factor) {
        super(waveManager);
        this.factor = factor;
        if (guessFactors == null) {
            guessFactors = new KDTree<MGData, Target>(new DuelKeyExtractor_2(), new MGData(factor));
        }
    }

    public KDTree<MGData, Target> getGuessFactors() {
        return guessFactors;
    }

    public String getName() {
        return "DuelGFGun_2: " + factor;
    }

}
