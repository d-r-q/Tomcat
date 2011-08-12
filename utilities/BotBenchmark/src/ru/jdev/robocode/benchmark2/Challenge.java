/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark2;

import robocode.BattleResults;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import robocode.control.events.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Challenge implements IBattleListener {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final List<BattleResults[]> battleResults = new ArrayList<>();
    private final String challengerBotName;
    private final Pairing[] pairings;

    private long startTime;

    public Challenge(String challengerBotName, Pairing[] referenceBotsName) {
        this.challengerBotName = challengerBotName;
        this.pairings = referenceBotsName;

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void execute(RobocodeEngine engine) {
        engine.addBattleListener(this);
        startTime = System.currentTimeMillis();
        try {
            final List<BattleSpecification> battleSpecs = new ArrayList<>();
            boolean hasAllBots = true;
            for (Pairing pairing : pairings) {
                String currentReferenceBot = pairing.botName;
                for (int i = 0; i < pairing.seasons; i++) {
                    final BattlefieldSpecification specification1 = new BattlefieldSpecification(800, 600);
                    final RobotSpecification[] specs = new RobotSpecification[2];
                    for (RobotSpecification rs : engine.getLocalRepository()) {
                        if (rs.getName().equals(challengerBotName)) {
                            specs[0] = rs;
                        } else if (rs.getNameAndVersion().equals(currentReferenceBot)) {
                            specs[1] = rs;
                        }
                    }
                    if (specs[0] == null) {
                        System.out.println("Challenger bot not found");
                        hasAllBots = false;
                        break;
                    }

                    if (specs[1] == null) {
                        System.out.printf("Reference bot %s not found\n", currentReferenceBot);
                        hasAllBots = false;
                        break;
                    }
                    final BattleSpecification specification = new BattleSpecification(BotBenchmark2.ROUNDS, specification1, specs);
                    battleSpecs.add(specification);
                }
            }

            if (!hasAllBots) {
                return;
            }

            for (BattleSpecification bs : battleSpecs) {
                System.gc();
                System.out.printf("Start battle (%d/%d) (%s)\n", battleResults.size() + 1, battleSpecs.size(), bs.getRobots()[1].getNameAndVersion());
                long battleStartTime = System.currentTimeMillis();
                engine.runBattle(bs);
                engine.waitTillBattleOver();
                System.out.printf("Battle ended (%d/%d), execution time: %d secs\n", battleResults.size(), battleSpecs.size(),
                        (System.currentTimeMillis() - battleStartTime) / 1000);
                System.out.printf("Estimated remaining time: %s\n", dateFormat.format(new Date((System.currentTimeMillis() - startTime) / battleResults.size() * (battleSpecs.size() - battleResults.size()))));
            }
        } finally {
            engine.removeBattleListener(this);
        }
        System.out.println("Challenge finished, execution time: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime)));
    }

    public void onBattleCompleted(BattleCompletedEvent event) {
        battleResults.add(event.getSortedResults());
    }

    public void onRoundEnded(RoundEndedEvent event) {
    }

    public void onBattleStarted(BattleStartedEvent event) {
    }

    public void onBattleFinished(BattleFinishedEvent event) {
    }

    public void onBattlePaused(BattlePausedEvent event) {
    }

    public void onBattleResumed(BattleResumedEvent event) {
    }

    public void onRoundStarted(RoundStartedEvent event) {
    }

    public void onTurnStarted(TurnStartedEvent event) {
    }

    public void onTurnEnded(TurnEndedEvent event) {
    }

    public void onBattleMessage(BattleMessageEvent event) {
    }

    public void onBattleError(BattleErrorEvent event) {
        System.out.println("onBattleError: " + event.getError());
    }

    public List<BattleResults[]> getBattleResults() {
        return battleResults;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getChallengerBotName() {
        return challengerBotName;
    }

}
