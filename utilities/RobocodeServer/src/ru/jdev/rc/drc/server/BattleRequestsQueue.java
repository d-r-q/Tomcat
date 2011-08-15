/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.util.LinkedList;
import java.util.List;

public class BattleRequestsQueue {

    private final List<BattleRequest> priorityQueue = new LinkedList<>();
    private final List<BattleRequest> commonQueue = new LinkedList<>();

    private final String secureToken;

    public BattleRequestsQueue(String secureToken) {
        this.secureToken = secureToken;
    }

    public synchronized void addBattleRequest(BattleRequest battleRequest) {
        if (battleRequest.secureToken.equals(secureToken)) {
            priorityQueue.add(battleRequest);
        } else {
            commonQueue.add(battleRequest);
        }

        notify();
    }

    public synchronized BattleRequest getBattleRequest() throws InterruptedException {
        while (priorityQueue.size() == 0 && commonQueue.size() == 0) {
            wait();
        }

        if (priorityQueue.size() > 0) {
            return priorityQueue.remove(0);
        } else {
            return commonQueue.remove(0);
        }
    }

}
