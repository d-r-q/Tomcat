package ru.jdev.rc.drc.server;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jdev
 * Date: 13.08.11
 */
public class BattleResultsBuffer {

    private final Map<Integer, RSBattleResults> battleResults = new HashMap<>();

    public synchronized void addBattleReult(Integer battleRequestId, RSBattleResults battleResults) {
        this.battleResults.put(battleRequestId, battleResults);
    }

    public RSBattleResults getResults(Integer battleRequestId) {
        return null;
    }

}
