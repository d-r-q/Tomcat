package lxx.targeting.tomcat_claws.data_analise;

import lxx.utils.ps_tree.EntryMatch;

import java.util.Comparator;

/**
* User: jdev
* Date: 18.06.11
*/
public class ByTimeComparator implements Comparator<EntryMatch> {
    public int compare(EntryMatch o1, EntryMatch o2) {
        if (o1.predicate.getRound() == o2.predicate.getRound()) {
            return (int) (o1.predicate.getTime() - o2.predicate.getTime());
        }
        return (o1.predicate.getRound() - o2.predicate.getRound());
    }
}
