/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.fire_log;

import lxx.model.BattleSnapshot;
import lxx.model.attributes.Attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: jdev
 * Date: 28.09.2010
 */
public class FireLog<T extends Serializable> {

    protected final List<FireLogEntry<T>> allEntries = new ArrayList<FireLogEntry<T>>();

    protected final FireLogNode<T> root;

    protected int entryCount;

    public FireLog(Attribute[] splitAttributes, int loadFactor, double maxIntervalLength) {
        root = new FireLogNode<T>(loadFactor, splitAttributes[0].getRange(), -1, splitAttributes, maxIntervalLength);
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
        final List<EntryMatch<T>> matches = new LinkedList<EntryMatch<T>>();

        int idx = 0;
        for (FireLogEntry<T> e : getEntries(predicate, limit)) {
            matches.add(new EntryMatch<T>(e.result, idx++, e.predicate));
        }

        return matches;
    }

    public int getEntryCount() {
        return entryCount;
    }
}
