/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import robocode.BattleResults;
import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;
import robocode.control.events.*;
import robocode.control.snapshot.IScoreSnapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RCBattlesExecutor implements IBattleListener {

    private final List<IScoreSnapshot[]> roundResults = new ArrayList<IScoreSnapshot[]>();

    private final RobocodeEngine robocodeEngine;

    private BattleResults[] currentBattleResults;
    private IScoreSnapshot[] currentSortedTeamScores;

    public RCBattlesExecutor() {
        this.robocodeEngine = new RobocodeEngine(new File(".\\rc\\"));
        robocodeEngine.addBattleListener(this);
    }

    public synchronized RSBattleResults executeBattle(Competitor[] competitors, BattlefieldSpecification bfSpec, int rounds) {
        roundResults.clear();
        currentBattleResults = null;

        final BattleSpecification battleSpecification = new BattleSpecification(rounds, bfSpec, getRobotSpecs(competitors));
        robocodeEngine.runBattle(battleSpecification);
        robocodeEngine.waitTillBattleOver();

        return new RSBattleResults(new ArrayList<IScoreSnapshot[]>(roundResults), currentBattleResults);
    }

    private RobotSpecification[] getRobotSpecs(Competitor[] competitors) {
        final RobotSpecification[] specs = new RobotSpecification[competitors.length];

        int specsIdx = 0;
        final RobotSpecification[] localRepository = robocodeEngine.getLocalRepository();
        for (Competitor competitor : competitors) {
            for (RobotSpecification spec : localRepository) {
                if (spec.getNameAndVersion().equals(competitor.name + "* " + competitor.version)) {
                    specs[specsIdx++] = spec;
                }
            }
        }

        return specs;
    }

    public void onBattleStarted(BattleStartedEvent battleStartedEvent) {
    }

    public void onBattleFinished(BattleFinishedEvent battleFinishedEvent) {
    }

    public void onBattleCompleted(BattleCompletedEvent battleCompletedEvent) {
        currentBattleResults = battleCompletedEvent.getSortedResults();
    }

    public void onBattlePaused(BattlePausedEvent battlePausedEvent) {
    }

    public void onBattleResumed(BattleResumedEvent battleResumedEvent) {
    }

    public void onRoundStarted(RoundStartedEvent roundStartedEvent) {
    }

    public void onRoundEnded(RoundEndedEvent roundEndedEvent) {
        roundResults.add(currentSortedTeamScores);
    }

    public void onTurnStarted(TurnStartedEvent turnStartedEvent) {
    }

    public void onTurnEnded(TurnEndedEvent turnEndedEvent) {
        currentSortedTeamScores = turnEndedEvent.getTurnSnapshot().getSortedTeamScores();
    }

    public void onBattleMessage(BattleMessageEvent battleMessageEvent) {
    }

    public void onBattleError(BattleErrorEvent battleErrorEvent) {
    }

    public static void main(String[] args) {
        try {
            new CodeManager().storeCompetitor(null);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        final RCBattlesExecutor rcBattlesExecutor = new RCBattlesExecutor();
        final Competitor c = new Competitor("lxx.Tomcat", "3.7.95");
        rcBattlesExecutor.executeBattle(new Competitor[]{c, c},
                new BattlefieldSpecification(800, 600), 10);
    }

}
