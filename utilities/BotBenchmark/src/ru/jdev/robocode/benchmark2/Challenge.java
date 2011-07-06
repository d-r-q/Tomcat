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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Challenge implements IBattleListener {

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
                        } else {
                            if (rs.getName().equals(currentReferenceBot)) {
                                specs[idx++] = rs;
                            }
                        }
                    }
                    final BattleSpecification specification = new BattleSpecification(BotBenchmark2.ROUNDS, specification1, specs);
                    startTime = System.currentTimeMillis();
                    engine.runBattle(specification);
                    engine.waitTillBattleOver();
                }
            }
        } finally {
            engine.removeBattleListener(this);
        }
    }

    public void onBattleCompleted(BattleCompletedEvent event) {
        battleResults.add(event.getSortedResults());
    }

    public void printBattleResults() {
        for (BattleResults[] brs : battleResults) {
            int totalScore = 0;
            for (BattleResults br : brs) {
                totalScore += br.getScore();
            }

            System.out.println("=====================================");
            for (BattleResults br : brs) {
                final int percentageScore = (int) ((double) br.getScore() / totalScore * 100);
                final int percentageSurv = (int) ((double) br.getFirsts() / BotBenchmark2.ROUNDS * 100);
                System.out.println(br.getTeamLeaderName() + ": " +
                        percentageScore + "%, " +
                        br.getScore() + ", " + br.getBulletDamage() + ", " +
                        br.getSurvival());

                if (br.getTeamLeaderName().indexOf(challengerBotName) != -1) {
                    benchmarkResults.avgBulletDamage.addValue(br.getBulletDamage());
                    benchmarkResults.avgPS.addValue(percentageScore);
                    benchmarkResults.avgScore.addValue(br.getScore());
                    benchmarkResults.avgSS.addValue(percentageSurv);
                }
            }
            final long execTime = System.currentTimeMillis() - startTime;
            System.out.println("Exec time: " + BotBenchmark2.dateFormat.format(new Date(execTime)));
            System.out.println("Avg round time: " + BotBenchmark2.dateFormat.format(new Date(execTime / BotBenchmark2.ROUNDS)));
            benchmarkResults.avgExecTime.addValue(execTime);
            System.out.println("=====================================");
        }
    }

    public void onRoundEnded(RoundEndedEvent event) {
        System.out.println(challengerBotName + " vs " + currentReferenceBot + " " + (event.getRound() + 1) + " round ended");
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
