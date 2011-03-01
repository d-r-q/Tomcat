package lxx.targeting.gf;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;

/**
 * User: jdev
 * Date: 05.12.2009
 */
public class PlainStatGFPredictor extends AbstractDeltaVectorPredictor {

     private static KDTree<GFData, Target> guessFactors = new KDTree<GFData, Target>(new PlainStatKeyExtractor(), new GFData());

    public PlainStatGFPredictor(WaveManager waveManager) {
        super(waveManager);
    }

    public KDTree getGuessFactors() {
        return guessFactors;
    }

}
