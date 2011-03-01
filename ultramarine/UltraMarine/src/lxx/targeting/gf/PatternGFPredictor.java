package lxx.targeting.gf;

import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;
import lxx.targeting.Target;

/**
 * User: jdev
 * Date: 07.12.2009
 */
public class PatternGFPredictor extends AbstractDeltaVectorPredictor {

    private static KDTree<GFData, Target> guessFactors = new KDTree<GFData, Target>(new PatternGFKeyExtractor(), new GFData());

    public PatternGFPredictor(WaveManager waveManager) {
        super(waveManager);
    }

    public KDTree getGuessFactors() {
        return guessFactors;
    }
}
