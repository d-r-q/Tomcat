/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.util.LinkedList;
import java.util.List;

public class CommandsQueue {

    private final List<Command> priorityQueue = new LinkedList<Command>();
    private final List<Command> commonQueue = new LinkedList<Command>();

    private final String secureToken;

    public CommandsQueue(String secureToken) {
        this.secureToken = secureToken;
    }

    public synchronized void addBattleRequest(Command command) {
        if (command.battleRequest.secureToken.equals(secureToken)) {
            priorityQueue.add(command);
        } else {
            commonQueue.add(command);
        }

        notify();
    }

    public synchronized Command getBattleRequest() throws InterruptedException {
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
