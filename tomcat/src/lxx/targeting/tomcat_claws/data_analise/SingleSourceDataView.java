package lxx.targeting.tomcat_claws.data_analise;

import lxx.utils.ps_tree.PSTree;
import lxx.ts_log.TurnSnapshot;
import lxx.ts_log.attributes.Attribute;
import lxx.utils.ps_tree.EntryMatch;
import lxx.utils.ps_tree.PSTreeEntry;
import lxx.utils.Interval;
import lxx.utils.LXXUtils;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.round;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class SingleSourceDataView implements DataView {

    private final PSTree<Serializable> dataSource;
    private final Map<Attribute,Integer> ranges;
    private final Attribute[] attributes;

    public SingleSourceDataView(Attribute[] attributes, Map<Attribute, Integer> ranges) {
        this.ranges = ranges;
        this.attributes = attributes;
        dataSource = new PSTree<Serializable>(attributes, 2, 0.0001);
    }

    public Set<TurnSnapshot> getDataSet(TurnSnapshot ts) {
        final List<EntryMatch> similarEntries = getSimilarSnapshots(ts);
        filterOutCloseByTime(similarEntries);
        final Set<TurnSnapshot> dataSet = new HashSet<TurnSnapshot>();

        for (EntryMatch e : similarEntries) {
            dataSet.add(e.predicate);
        }

        return dataSet;
    }

    private List<EntryMatch> getSimilarSnapshots(TurnSnapshot ts) {
        final List<EntryMatch> similarSnapshots = new ArrayList<EntryMatch>();
        final List<EntryMatch<Serializable>> similarEntries = dataSource.getSortedSimilarEntries(ts, getLimits(ts));
        if (similarEntries.size() == 0) {
            final EntryMatch closestEntry = dataSource.getClosestEntry(ts);
            if (closestEntry != null) {
                similarSnapshots.add(closestEntry);
            }
        }

        for (EntryMatch e : similarEntries) {
            similarSnapshots.add(e);
        }

        return similarSnapshots;
    }

    private void filterOutCloseByTime(List<EntryMatch> similarSnapshots) {
        Collections.sort(similarSnapshots, new Comparator<EntryMatch>() {
            public int compare(EntryMatch o1, EntryMatch o2) {
                if (o1.predicate.getRound() == o2.predicate.getRound()) {
                    return (int) (o1.predicate.getTime() - o2.predicate.getTime());
                }
                return (o1.predicate.getRound() - o2.predicate.getRound());
            }
        });

        for (int i = 0; i < similarSnapshots.size() - 1; i++) {
            EntryMatch em1 = similarSnapshots.get(i);
            EntryMatch em2 = similarSnapshots.get(i + 1);
            if (em1.predicate.getRound() == em2.predicate.getRound() &&
                    em1.predicate.getTime() + 5 > em2.predicate.getTime()) {
                if (em1.match < em2.match) {
                    similarSnapshots.remove(i + 1);
                } else {
                    similarSnapshots.remove(i);
                }
                i--;
            }
        }
    }

    public void addEntry(TurnSnapshot ts) {
        dataSource.addEntry(new PSTreeEntry<Serializable>(ts));
    }

    private Map<Attribute, Interval> getLimits(TurnSnapshot ts) {
        Map<Attribute, Interval> limits = new HashMap<Attribute, Interval>();

        for (Attribute a : attributes) {
            Interval interval = new Interval(
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) - ranges.get(a))),
                    (int) round(LXXUtils.limit(a, ts.getAttrValue(a) + ranges.get(a)))
            );
            limits.put(a, interval);
        }

        return limits;
    }

}
