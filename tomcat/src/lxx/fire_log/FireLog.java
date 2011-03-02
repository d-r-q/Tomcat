/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.fire_log;

import lxx.model.BattleSnapshot;
import lxx.model.attributes.Attribute;
import lxx.office.AttributesManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.signum;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class FireLog<T extends Serializable> {

    protected final List<FireLogEntry<T>> allEntries = new ArrayList<FireLogEntry<T>>();

    protected final FireLogNode<T> root;

    private final Attribute[] matchAttributes;
    private final int[] attributeIds;
    private final double[] attributeWeights;

    protected int entryCount;

    public FireLog(Attribute[] splitAttributes, Attribute[] matchAttributes, double[] weights,
                   int loadFactor, double maxIntervalLength) {
        this.matchAttributes = matchAttributes;
        root = new FireLogNode<T>(loadFactor, splitAttributes[0].getRange(), -1, splitAttributes, maxIntervalLength);

        attributeIds = new int[matchAttributes.length];
        for (int i = 0; i < matchAttributes.length; i++) {
            attributeIds[i] = matchAttributes[i].getId();
        }
        attributeWeights = weights;
    }

    public void addEntry(FireLogEntry<T> fireLogEntry) {
        root.addEntry(fireLogEntry);
        allEntries.add(fireLogEntry);
        entryCount++;
    }

    public List<FireLogEntry<T>> getEntries(BattleSnapshot bs, int limit) {
        return root.getEntries(bs, limit);
    }

    public List<EntryMatch<T>> getSimilarEntries(BattleSnapshot predicate, int limit) {
        final double[] scaledFactors = new double[AttributesManager.attributesCount()];
        for (Attribute a : matchAttributes) {
            scaledFactors[a.getId()] = (1000D / a.getActualRange()) * attributeWeights[a.getId()];
        }
        final List<EntryMatch<T>> matches = new ArrayList<EntryMatch<T>>();

        for (FireLogEntry<T> e : getEntries(predicate, limit)) {
            matches.add(new EntryMatch<T>(e.result, e.predicate.quickDistance(attributeIds, predicate, scaledFactors), e.predicate));
        }

        Collections.sort(matches, new Comparator<EntryMatch>() {

            public int compare(EntryMatch o1, EntryMatch o2) {
                return (int) signum(o1.match - o2.match);
            }
        });


        return matches;
    }

    public int getEntryCount() {
        return entryCount;
    }
}
