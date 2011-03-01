/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.fire_log.Pattern;
import lxx.model.BattleSnapshot;
import lxx.utils.APoint;
import lxx.utils.DeltaVector;
import lxx.utils.LXXUtils;
import robocode.util.Utils;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.toRadians;

public class PatternManager {

    private static final Map<BattleSnapshot, Pattern> patterns = new HashMap<BattleSnapshot, Pattern>();

    private final Office office;
    private final BattleSnapshotManager battleSnapshotManager;

    public PatternManager(Office office) {
        this.office = office;
        this.battleSnapshotManager = office.getBattleSnapshotManager();
    }

    public Pattern getPattern(BattleSnapshot bs) {
        if (bs == null) {
            return null;
        }
        if (patterns.containsKey(bs)) {
            return patterns.get(bs);
        }
        final Pattern p = new Pattern();
        patterns.put(bs, p);
        final long currentTime = office.getTime();

        final APoint targetPos = LXXUtils.getEnemyPos(bs);
        final double enemyHeadingRadians = toRadians(bs.getAttrValue(AttributesManager.enemyAbsoluteHeading));
        for (long time = bs.getTime(); time < currentTime; time++) {
            final BattleSnapshot bst = battleSnapshotManager.getSnapshotByRoundTime(bs.getTargetName(), time);
            if (bst == null) {
                return null;
            }

            APoint pos = LXXUtils.getEnemyPos(bst);
            p.add(new DeltaVector(Utils.normalRelativeAngle(targetPos.angleTo(pos) -
                    enemyHeadingRadians), targetPos.aDistance(pos)));
        }

        return p;
    }

}
