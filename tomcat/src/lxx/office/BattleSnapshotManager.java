/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.model.BattleSnapshot;
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
public class BattleSnapshotManager implements TargetManagerListener {

    private final Map<String, List<BattleSnapshot>> log = new HashMap<String, List<BattleSnapshot>>();

    private final Office office;
    private final AttributesManager factory;

    public BattleSnapshotManager(Office office) {
        this.office = office;
        this.factory = office.getAttributesManager();
    }

    public List<BattleSnapshot> getLastSnapshots(Target t, int... indexes) {
        List<BattleSnapshot> res = new ArrayList<BattleSnapshot>();

        List<BattleSnapshot> log = this.log.get(t.getName());
        if (log == null) {
            System.out.println("[WARN]: log for " + t.getName() + " not found");
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

    public BattleSnapshot getSnapshotByRoundTime(String targetName, long roundTime) {
        final List<BattleSnapshot> log = this.log.get(targetName);
        final BattleSnapshot res = log.get((int) roundTime);
        if (res != null && res.getTime() != roundTime) {
            // todo (zhidkov): fix me
            //throw new RuntimeException(roundTime + ", " + log.size() + ", " + res.getTime());
        }
        return res;
    }

    public BattleSnapshot getLastSnapshot(Target target, int timeDelta) {
        return getLastSnapshots(target, timeDelta).get(0);
    }

    private void interpolate(List<BattleSnapshot> log, BattleSnapshot battleSnapshot1, BattleSnapshot battleSnapshot2, String targetName) {
        final int steps = (int) (office.getTime() - battleSnapshot1.getTime());
        final long startRoundTime = battleSnapshot1.getTime();
        final long startGlobalTime = battleSnapshot1.getBattleTime();
        for (int i = 1; i < steps; i++) {
            int[] attrValue = new int[AttributesManager.attributesCount()];
            for (Attribute a : AttributesManager.attributes) {
                attrValue[a.getId()] = battleSnapshot1.getAttrValue(a) + (battleSnapshot2.getAttrValue(a) - battleSnapshot1.getAttrValue(a)) / steps * i;
            }
            log.add(new BattleSnapshot(attrValue, startRoundTime + i, startGlobalTime + i, targetName));
        }
    }

    public BattleSnapshot getLastSnapshot(Target target) {
        return getLastSnapshot(target, 0);
    }

    public void targetUpdated(Target target) {
        if (target.getUpdateTime() == 0) {
            return;
        }
        List<BattleSnapshot> log = this.log.get(target.getName());
        if (log == null) {
            log = new ArrayList<BattleSnapshot>();
            this.log.put(target.getName(), log);
        }

        if (log.size() == 0) {
            for (int i = 0; i < office.getTime(); i++) {
                log.add(null);
            }
        }

        final BattleSnapshot battleSnapshot = factory.getBattleSnapshot(target);
        if (log.get(log.size() - 1) != null && log.get(log.size() - 1).getTime() + 1 < office.getTime()) {
            interpolate(log, log.get(log.size() - 1), battleSnapshot, target.getName());
        }

        log.add(battleSnapshot);
    }
}
