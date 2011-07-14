/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import robocode.BattleResults;
import robocode.control.snapshot.IScoreSnapshot;

import java.io.Serializable;
import java.util.List;

public class RSBattleResults implements Serializable {

    // todo(zhidkov): make serializable
    public final transient List<IScoreSnapshot[]> roundResults;
    public final BattleResults[] currentBattleResults;

    public long requestId;

    public RSBattleResults(List<IScoreSnapshot[]> roundResults, BattleResults[] currentBattleResults) {
        this.roundResults = roundResults;
        this.currentBattleResults = currentBattleResults;
    }
}
