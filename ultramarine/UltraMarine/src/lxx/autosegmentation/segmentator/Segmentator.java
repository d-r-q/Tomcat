package lxx.autosegmentation.segmentator;

import lxx.autosegmentation.model.FireSituation;

import java.util.List;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public interface Segmentator {
    void addFireSituation(FireSituation fs);

    void resegment();

    List<FireSituation> getFireSutations(FireSituation fs);
}
