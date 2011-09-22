/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.utils.ps_tree.EntryMatch;

import java.util.Comparator;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class ByTimeComparator implements Comparator<EntryMatch> {
    public int compare(EntryMatch o1, EntryMatch o2) {
        return (o1.predicate.roundTime - o2.predicate.roundTime);
    }
}
