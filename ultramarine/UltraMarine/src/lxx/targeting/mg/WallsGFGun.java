package lxx.targeting.mg;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;

/**
 * User: jdev
 * Date: 17.02.2010
 */
public class WallsGFGun extends MegaGun {

    private static KDTree<MGData, Target> guessFactors;
    private double factor;

    public WallsGFGun(WaveManager waveManager, double factor) {
        super(waveManager);

        this.factor = factor;
        if (guessFactors == null) {
            guessFactors = new KDTree<MGData, Target>(new WallsKeyExtractor(), new MGData(factor));
        }
    }

    public KDTree<MGData, Target> getGuessFactors() {
        return guessFactors;
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
