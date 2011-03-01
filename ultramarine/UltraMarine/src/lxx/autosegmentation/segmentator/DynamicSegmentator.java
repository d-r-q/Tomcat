package lxx.autosegmentation.segmentator;

import lxx.autosegmentation.model.FireSituation;
import lxx.autosegmentation.model.Attribute;
import lxx.autosegmentation.model.Segmentation;
import lxx.autosegmentation.SegmentationsManager;
import lxx.utils.LXXConstants;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * User: jdev
 * Date: 15.03.2010
 */
public class DynamicSegmentator implements Segmentator {

    private Attribute[] attributes;
    private final Map<String, List<FireSituation>> segmentedFireSituations = new HashMap<String, List<FireSituation>>();

    private int segmentationsCount = 1;
    private boolean isSegmentationCountInced = false;
    private SegmentationsManager segmentationsManager;

    public DynamicSegmentator() {
    }

    public void addFireSituation(FireSituation fs) {
        StringBuffer key = new StringBuffer(".");
        List<FireSituation> fses = segmentedFireSituations.get(key.toString());
        if (fses == null) {
            fses = new ArrayList<FireSituation>();
            segmentedFireSituations.put(key.toString(), fses);
        }
        fses.add(fs);
        int idx = 0;
        for (Attribute a : attributes) {
            final int segmentIdx = getSegmentIdx(fs, a);
            key.append(segmentIdx).append('.');
            fses = segmentedFireSituations.get(key.toString());
            if (fses == null) {
                fses = new ArrayList<FireSituation>();
                segmentedFireSituations.put(key.toString(), fses);
            }
            fses.add(fs);
            if (fses.size() > (LXXConstants.MAX_GUESS_FACTOR * 2 + 1) * 3 && idx++ == segmentationsCount - 1 &&
                    !isSegmentationCountInced) {
                isSegmentationCountInced = true;
            }
        }
    }

    private int getSegmentIdx(FireSituation fs, Attribute a) {
        Segmentation seg = null;//SegmentationsManager.getSegmentation(a);
        return seg.getSegmentIdx(fs.getAttributeValue(a));
    }

    public void resegment() {
        if (isSegmentationCountInced) {
            segmentationsCount++;
            isSegmentationCountInced = false;
        }
        resetSegmentationAttributes();
        final List<FireSituation> fses = segmentedFireSituations.get(".");
        if (fses == null) {
            return;
        }
        segmentedFireSituations.clear();
        for (FireSituation fs : fses) {
            addFireSituation(fs);
        }
    }

    private void resetSegmentationAttributes() {
        this.attributes = new Attribute[segmentationsCount];
        final List<Attribute> attributes = new ArrayList<Attribute>();
        /*for (Segmentation s : segmentationsManager.getBestSegmentations(segmentationsCount)) {
            attributes.add(s.getAttribute());
        }*/
        attributes.toArray(this.attributes);
    }

    public List<FireSituation> getFireSutations(FireSituation fs) {
        List<FireSituation> fses;
        StringBuffer key = new StringBuffer(".");
        fses = segmentedFireSituations.get(key.toString());
        for (Attribute a : attributes) {
            key.append(getSegmentIdx(fs, a)).append('.');
            List<FireSituation> subFses = segmentedFireSituations.get(key.toString());
            if (subFses == null || subFses.size() < (LXXConstants.MAX_GUESS_FACTOR * 2 + 1) * 2) {
                break;
            }
            fses = subFses;
        }
        return fses;
    }

    public void setSegmentationsManager(SegmentationsManager segmentationsManager) {
        this.segmentationsManager = segmentationsManager;
    }
}
