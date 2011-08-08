/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark2;

import robocode.BattleResults;
import robocode.control.*;
import robocode.control.events.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Challenge implements IBattleListener {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private final List<BattleResults[]> battleResults = new ArrayList<BattleResults[]>();
    private final String challengerBotName;
    private final String[] referenceBotsNames;
    private final int seasons;

    private long startTime;
    private BenchmarkResults benchmarkResults;
    private String currentReferenceBot;

    public Challenge(String challengerBotName, String[] referenceBotsName, BenchmarkResults benchmarkResults, int seasons) {
        this.challengerBotName = challengerBotName;
        this.referenceBotsNames = referenceBotsName;
        this.benchmarkResults = benchmarkResults;
        this.seasons = seasons;
    }

    public void execute(RobocodeEngine engine) {
        engine.addBattleListener(this);
        startTime = System.currentTimeMillis();
        try {
            for (String referenceBotName : referenceBotsNames) {
                currentReferenceBot = referenceBotName;
                for (int i = 0; i < seasons; i++) {
                    final BattlefieldSpecification specification1 = new BattlefieldSpecification(800, 600);
                    final RobotSpecification[] specs = new RobotSpecification[2];
                    int idx = 0;
                    for (RobotSpecification rs : engine.getLocalRepository()) {
                        if (rs.getName().equals(challengerBotName)) {
                            specs[idx++] = rs;
                        } else if (rs.getName().equals(currentReferenceBot)) {
                            specs[idx++] = rs;
                        }
                    }
                    final BattleSpecification specification = new BattleSpecification(BotBenchmark2.ROUNDS, specification1, specs);
                    System.gc();
                    System.out.printf("Start battle (%d/%d) (%s: (%d/%d))\n", battleResults.size() + 1, seasons * referenceBotsNames.length, currentReferenceBot, i + 1, seasons);
                    long battleStartTime = System.currentTimeMillis();
                    RandomFactory.setRandom(new Random(i));
                    engine.runBattle(specification);
                    engine.waitTillBattleOver();
                    System.out.printf("Battle ended (%d/%d), execution time: %d secs\n", battleResults.size(), seasons * referenceBotsNames.length,
                            (System.currentTimeMillis() - battleStartTime) / 1000);
                }
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

    public int getSeasons() {
        return seasons;
    }
}
