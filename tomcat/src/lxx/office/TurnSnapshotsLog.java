/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.model.TurnSnapshot;
import lxx.model.attributes.Attribute;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jdev
 * Date: 05.08.2010
 */
public class TurnSnapshotsLog implements TargetManagerListener {

    private final Map<Target, List<TurnSnapshot>> logs = new HashMap<Target, List<TurnSnapshot>>();

    private final Office office;
    private final AttributesManager factory;

    public TurnSnapshotsLog(Office office) {
        this.office = office;
        this.factory = office.getAttributesManager();
    }

    public List<TurnSnapshot> getLastSnapshots(Target t, int... indexes) {
        List<TurnSnapshot> res = new ArrayList<TurnSnapshot>();

        List<TurnSnapshot> log = this.logs.get(t);
        if (log == null) {
            System.out.println("[WARN]: logs for " + t.getName() + " not found");
            return null;
        }
        for (int index : indexes) {
            int idx = log.size() - 1 - index;
            if (idx >= 0 && idx < log.size()) {
                res.add(log.get(idx));
            } else {
                res.add(null);
            }
        }

        return res;
    }

    public TurnSnapshot getLastSnapshot(Target target, int timeDelta) {
        return getLastSnapshots(target, timeDelta).get(0);
    }

    private void interpolate(List<TurnSnapshot> log, TurnSnapshot turnSnapshot1, TurnSnapshot turnSnapshot2, String targetName) {
        final int steps = (int) (office.getTime() - turnSnapshot1.getTime());
        final long startRoundTime = turnSnapshot1.getTime();
        final long startGlobalTime = turnSnapshot1.getBattleTime();
        for (int i = 1; i < steps; i++) {
            int[] attrValue = new int[AttributesManager.attributesCount()];
            for (Attribute a : AttributesManager.attributes) {
                attrValue[a.getId()] = turnSnapshot1.getAttrValue(a) + (turnSnapshot2.getAttrValue(a) - turnSnapshot1.getAttrValue(a)) / steps * i;
            }
            log.add(new TurnSnapshot(attrValue, startRoundTime + i, startGlobalTime + i, targetName));
        }
    }

    public TurnSnapshot getLastSnapshot(Target target) {
        return getLastSnapshot(target, 0);
    }

    public void targetUpdated(Target target) {
        if (target.getUpdateTime() == 0) {
            return;
        }
        List<TurnSnapshot> log = this.logs.get(target);
        if (log == null) {
            log = new ArrayList<TurnSnapshot>();
            this.logs.put(target, log);
        }

        if (log.size() == 0) {
            for (int i = 0; i < office.getTime(); i++) {
                log.add(null);
            }
        }

        final TurnSnapshot turnSnapshot = factory.getBattleSnapshot(target);
        if (log.get(log.size() - 1) != null && log.get(log.size() - 1).getTime() + 1 < office.getTime()) {
            interpolate(log, log.get(log.size() - 1), turnSnapshot, target.getName());
        }

        log.add(turnSnapshot);
    }
}
