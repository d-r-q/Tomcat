package lxx.autosegmentation.segmentator;

import lxx.autosegmentation.model.Attribute;
import lxx.autosegmentation.model.FireSituation;
import lxx.utils.LXXConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jdev
 * Date: 10.03.2010
 */
public class StaticSegmentator implements Segmentator {

    private final Attribute[] attributes;
    private final Map<String, List<FireSituation>> segmentedFireSituations = new HashMap<String, List<FireSituation>>();

    public StaticSegmentator(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public void addFireSituation(FireSituation fs) {
        StringBuffer key = new StringBuffer(".");
        List<FireSituation> fses = segmentedFireSituations.get(key.toString());
        if (fses == null) {
            fses = new ArrayList<FireSituation>();
            segmentedFireSituations.put(key.toString(), fses);
        }
        fses.add(fs);
        for (Attribute a : attributes) {
            final int segmentIdx = getSegmentIdx(fs, a);
            key.append(segmentIdx).append('.');
            fses = segmentedFireSituations.get(key.toString());
            if (fses == null) {
                fses = new ArrayList<FireSituation>();
                segmentedFireSituations.put(key.toString(), fses);
            }
            fses.add(fs);
        }
    }

    private int getSegmentIdx(FireSituation fs, Attribute a) {
        /*Segmentation seg = SegmentationsManager.getSegmentation(a);
        return seg.getSegmentIdx(fs.getAttributeValue(a));*/
        return -1;
    }

    public void resegment() {
        final List<FireSituation> fses = segmentedFireSituations.get(".");
        if (fses == null) {
            return;
        }
        segmentedFireSituations.clear();
        for (FireSituation fs : fses) {
            addFireSituation(fs);
        }
    }

    public List<FireSituation> getFireSutations(FireSituation fs) {
        List<FireSituation> fses;
        StringBuffer key = new StringBuffer(".");
        fses = segmentedFireSituations.get(key.toString());
        for (Attribute a : attributes) {
            key.append(getSegmentIdx(fs, a)).append('.');
            List<FireSituation> subFses = segmentedFireSituations.get(key.toString());
            if (subFses == null || subFses.size() < LXXConstants.MAX_GUESS_FACTOR * 2.5) {
                break;
            }
            fses = subFses;
        }
        return fses;
    }

}
