package lxx.targeting.mg;

import lxx.targeting.Target;
import lxx.utils.kd_tree.KDTree;
import lxx.wave.WaveManager;

/**
 * Created by IntelliJ IDEA.
 * User: pipsi
 * Date: 12.02.2010
 * Time: 2:05:47
 * To change this template use File | Settings | File Templates.
 */
public class PatternGFGun extends MegaGun {

    private static KDTree<MGData, Target> guessFactors;
    private double factor;

    public PatternGFGun(WaveManager waveManager, double factor) {
        super(waveManager);

        this.factor = factor;
        if (guessFactors == null) {
            guessFactors = new KDTree<MGData, Target>(new PatternGFKeyExtractor(), new MGData(factor));
        }
    }

    public KDTree<MGData, Target> getGuessFactors() {
        return guessFactors;
    }

    public String getName() {
        return "PatternGFGun: " + factor;
    }
}
