package lxx.targeting.mg;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;

/**
 * User: jdev
 * Date: 17.02.2010
 */
public class DuelistGFGun extends MegaGun {

        private static KDTree<MGData, Target> guessFactors;
    private double factor;

    public DuelistGFGun(WaveManager waveManager, double factor) {
        super(waveManager);
        this.factor = factor;
        if (guessFactors == null) {
            guessFactors = new KDTree<MGData, Target>(new DuelistKeyExtractor(), new MGData(factor));
        }
    }

    public KDTree<MGData, Target> getGuessFactors() {
        return guessFactors;
    }

    public String getName() {
        return "DuelistGFGun: " + factor;
    }

}
