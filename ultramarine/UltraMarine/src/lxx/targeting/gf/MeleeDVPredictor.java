package lxx.targeting.gf;

import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;
import lxx.targeting.Target;

/**
 * User: jdev
 * Date: 05.01.2010
 */
public class MeleeDVPredictor extends AbstractDeltaVectorPredictor {

    private static KDTree<GFData, Target> guessFactors = new KDTree<GFData, Target>(new MeleeDVKeyExtractor(), new GFData());

    public MeleeDVPredictor(WaveManager waveManager) {
        super(waveManager);
    }

    public KDTree getGuessFactors() {
        return guessFactors;
    }
}
