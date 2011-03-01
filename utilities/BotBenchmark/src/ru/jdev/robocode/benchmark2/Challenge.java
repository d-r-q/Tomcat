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
    private final String referenceBotName;
    private final int seasons;

    private long startTime;
    private BenchmarkResults benchmarkResults;

    public Challenge(String challengerBotName, String referenceBotName, BenchmarkResults benchmarkResults, int seasons) {
        this.challengerBotName = challengerBotName;
        this.referenceBotName = referenceBotName;
        this.benchmarkResults = benchmarkResults;
        this.seasons = seasons;
    }

    public void execute(RobocodeEngine engine) {
        engine.addBattleListener(this);
        try {
            for (int i = 0; i < seasons; i++) {
                final BattlefieldSpecification specification1 = new BattlefieldSpecification(800, 600);
                final RobotSpecification[] specs = new RobotSpecification[2];
                int idx = 0;
                for (RobotSpecification rs : engine.getLocalRepository()) {
                    if (rs.getName().equals(challengerBotName)) {
                        specs[idx++] = rs;
                    } else if (rs.getName().equals(referenceBotName)) {
                        specs[idx++] = rs;
                    }
                }
                final BattleSpecification specification = new BattleSpecification(BotBenchmark2.ROUNDS, specification1, specs);
                startTime = System.currentTimeMillis();
                engine.runBattle(specification);
                engine.waitTillBattleOver();
            }
        } finally {
            engine.removeBattleListener(this);
        }
    }

    @Override
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

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        System.out.println(challengerBotName + " vs " + referenceBotName + " " + (event.getRound() + 1) + " round ended");
    }

    @Override
    public void onBattleStarted(BattleStartedEvent event) {
    }

    @Override
    public void onBattleFinished(BattleFinishedEvent event) {
    }

    @Override
    public void onBattlePaused(BattlePausedEvent event) {
    }

    @Override
    public void onBattleResumed(BattleResumedEvent event) {
    }

    @Override
    public void onRoundStarted(RoundStartedEvent event) {
    }

    @Override
    public void onTurnStarted(TurnStartedEvent event) {
    }

    @Override
    public void onTurnEnded(TurnEndedEvent event) {
    }

    @Override
    public void onBattleMessage(BattleMessageEvent event) {
    }

    @Override
    public void onBattleError(BattleErrorEvent event) {
        System.out.println("onBattleError: " + event.getError());
    }
}
