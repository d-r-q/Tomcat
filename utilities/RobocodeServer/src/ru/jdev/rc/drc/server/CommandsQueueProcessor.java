/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

public class CommandsQueueProcessor implements Runnable {

    private final BattleRequestsQueue battleRequestsQueue;
    private final RCBattlesExecutor rcBattlesExecutor;

    private volatile boolean isRunned = true;

    public CommandsQueueProcessor(BattleRequestsQueue battleRequestsQueue,
                                  RCBattlesExecutor rcBattlesExecutor) {
        this.battleRequestsQueue = battleRequestsQueue;
        this.rcBattlesExecutor = rcBattlesExecutor;
    }

    public void run() {
        while (isRunned && !Thread.interrupted()) {
            try {
                final Command command = battleRequestsQueue.getBattleRequest();
                if (!command.client.isAlive()) {
                    continue;
                }
                final BattleRequest battleRequest = command.battleRequest;
                final RSBattleResults rsBattleResults = rcBattlesExecutor.executeBattle(battleRequest.competitors, battleRequest.bfSpec, battleRequest.rounds);
                command.client.sendRSBattleResults(rsBattleResults);
            } catch (InterruptedException e) {
                isRunned = false;
            }
        }
    }

    public void stop() {
        isRunned = false;
    }

}
