package lxx.targeting.gf;

import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;
import lxx.targeting.Target;

/**
 * User: jdev
 * Date: 05.12.2009
 */
public class DuelGFPredictor extends AbstractDeltaVectorPredictor {

    private static KDTree<GFData, Target> guessFactors = new KDTree<GFData, Target>(new DuelDVKeyExtractor(), new GFData());

    public DuelGFPredictor(WaveManager waveManager) {
        super(waveManager);
    }

    public KDTree getGuessFactors() {
        return guessFactors;
    }
}
