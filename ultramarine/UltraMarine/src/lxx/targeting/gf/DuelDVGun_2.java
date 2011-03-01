package lxx.targeting.gf;

import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;
import lxx.targeting.Target;
import lxx.targeting.mg.DuelKeyExtractor_2;

/**
 * User: jdev
 * Date: 14.02.2010
 */
public class DuelDVGun_2 extends AbstractDeltaVectorPredictor {

    private static KDTree<GFData, Target> guessFactors = new KDTree<GFData, Target>(new DuelKeyExtractor_2(), new GFData());

    public DuelDVGun_2(WaveManager waveManager) {
        super(waveManager);
    }

    public KDTree getGuessFactors() {
        return guessFactors;
    }
}
