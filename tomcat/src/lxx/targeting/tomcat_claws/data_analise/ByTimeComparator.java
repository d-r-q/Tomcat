/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.utils.ps_tree.PSTreeEntry;

import java.util.Comparator;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class ByTimeComparator implements Comparator<PSTreeEntry> {
    public int compare(PSTreeEntry o1, PSTreeEntry o2) {
        if (o1.predicate.getRound() == o2.predicate.getRound()) {
            return (int) (o2.predicate.getTime() - o1.predicate.getTime());
        }
        return (o2.predicate.getRound() - o1.predicate.getRound());
    }
}
