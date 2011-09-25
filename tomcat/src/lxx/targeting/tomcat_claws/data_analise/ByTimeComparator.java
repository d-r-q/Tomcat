/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.targeting.tomcat_claws.data_analise;

import lxx.utils.sp_tree.SPTreeEntry;

import java.util.Comparator;

/**
 * User: jdev
 * Date: 18.06.11
 */
public class ByTimeComparator implements Comparator<SPTreeEntry> {
    public int compare(SPTreeEntry o1, SPTreeEntry o2) {
        return (o1.location.roundTime - o2.location.roundTime);
    }
}
