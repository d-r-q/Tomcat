/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark;

import robocode.BattleResults;
import robocode.control.events.*;
import robocode.control.snapshot.*;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jdev
 * Date: 03.03.2010
 */
public class BBBattleListener implements IBattleListener {

    private BotBenchmark botBenchmark;
    private long roundStartTime;

    private final List<BattleEvent> events = new ArrayList<BattleEvent>();

    private EventHandler eventHandler;

    public BBBattleListener(BotBenchmark botBenchmark) {
        this.botBenchmark = botBenchmark;
    }

    public void onBattleStarted(BattleStartedEvent event) {
        eventHandler = new EventHandler();
        eventHandler.start();
    }

    public void onBattleFinished(BattleFinishedEvent event) {
        eventHandler.isRunned = false;
    }

    public void onBattleCompleted(BattleCompletedEvent event) {
        final BattleResults[] sortedResults = event.getSortedResults();
        printScores(sortedResults);
    }

    private void printScores(BattleResults[] sortedResults) {
        int totalSocres = 0;
        for (BattleResults br : sortedResults) {
            totalSocres += br.getScore();
        }

        for (BattleResults br : sortedResults) {
            System.out.println(br.getTeamLeaderName() + ": " + br.getScore() + ", " +
                    (int) (((double) br.getScore() / totalSocres) * 100) + "%");
        }
    }

    public void onBattlePaused(BattlePausedEvent event) {
    }

    public void onBattleResumed(BattleResumedEvent event) {
    }

    public void onRoundStarted(RoundStartedEvent event) {
        synchronized (events) {
            events.add(event);
            events.notify();
        }
    }

    public void onRoundEnded(RoundEndedEvent event) {
        synchronized (events) {
            events.add(event);
            events.notify();
        }
    }

    public void onTurnStarted(TurnStartedEvent event) {
    }

    public void onTurnEnded(TurnEndedEvent event) {
        synchronized (events) {
            events.add(event);
            events.notify();
        }
    }

    public void onBattleMessage(BattleMessageEvent event) {
    }

    public void onBattleError(BattleErrorEvent event) {
        System.out.println(event.getError());
    }

    private class EventHandler extends Thread {

        private static final int HITS = 0;
        private static final int TOTAL_BULLETS = 1;

        private final NumberFormat format = NumberFormat.getPercentInstance();

        private final Map<Integer, String> bulletOweners = new HashMap<Integer, String>();
        private final Map<Integer, BulletState> bulletStates = new HashMap<Integer, BulletState>();
        private final Map<String, int[]> hitRates = new HashMap<String, int[]>();

        private boolean isRunned = true;

        private EventHandler() {
            format.setMinimumFractionDigits(1);
        }

        public void run() {
            while (isRunned) {
                BattleEvent event = takeEvent();

                if (event instanceof RoundStartedEvent) {
                    processRoundStartedEvent(event);
                } else if (event instanceof RoundEndedEvent) {
                    System.out.println(", time: " + (System.currentTimeMillis() - roundStartTime) + " ms");

                    Map<String, int[]> roundHitRates = new HashMap<String, int[]>();
                    for (Integer bulletId : bulletStates.keySet()) {
                        String owener = getOwener(bulletId);
                        int[] battleHitRate = hitRates.get(owener);
                        if (battleHitRate == null) {
                            battleHitRate = new int[2];
                            hitRates.put(owener, battleHitRate);
                        }

                        if (bulletStates.get(bulletId) == BulletState.HIT_VICTIM) {
                            battleHitRate[HITS]++;
                        }
                        battleHitRate[TOTAL_BULLETS]++;

                        int[] roundHitRate = roundHitRates.get(owener);
                        if (roundHitRate == null) {
                            roundHitRate = new int[2];
                            roundHitRates.put(owener, roundHitRate);
                        }

                        if (bulletStates.get(bulletId) == BulletState.HIT_VICTIM) {
                            roundHitRate[HITS]++;
                        }
                        roundHitRate[TOTAL_BULLETS]++;
                    }

                    System.out.println("Battle hit rates:");
                    for (String owener : hitRates.keySet()) {
                        int[] hitRate = hitRates.get(owener);
                        System.out.println(owener + ": " + "(" + hitRate[HITS] + "/" + hitRate[TOTAL_BULLETS] + ") " + format.format((double) hitRate[HITS] / hitRate[TOTAL_BULLETS]));
                    }

                    System.out.println("Round hit rates:");
                    for (String owener : roundHitRates.keySet()) {
                        int[] hitRate = roundHitRates.get(owener);
                        System.out.println(owener + ": " + "(" + hitRate[HITS] + "/" + hitRate[TOTAL_BULLETS] + ") " + format.format((double) hitRate[HITS] / hitRate[TOTAL_BULLETS]));
                    }

                    bulletStates.clear();
                    bulletOweners.clear();
                } else if (event instanceof TurnEndedEvent) {
                    processTurnEndEvent(event);
                }
            }
        }

        private void processRoundStartedEvent(BattleEvent event) {
            final RoundStartedEvent rsEvent = (RoundStartedEvent) event;
            System.out.print(new StringBuffer(String.valueOf(rsEvent.getRound()))
                    .append('/').append(botBenchmark.getTotalRounds()).append(": ").toString());


            int totalSocres = HITS;
            for (IScoreSnapshot br : rsEvent.getStartSnapshot().getSortedTeamScores()) {
                totalSocres += br.getTotalScore();
            }

            for (IScoreSnapshot br : rsEvent.getStartSnapshot().getSortedTeamScores()) {
                final double score = br.getTotalScore();
                System.out.print(br.getName() + ": " + score + ", " +
                        (int) (((double) score / totalSocres) * 100) + "% / ");
            }

            roundStartTime = System.currentTimeMillis();
        }

        private BattleEvent takeEvent() {
            BattleEvent event;
            if (events.size() == 0) {
                synchronized (events) {
                    try {
                        events.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            event = events.remove(0);
            return event;
        }

        private void processTurnEndEvent(BattleEvent event) {
            final TurnEndedEvent teEvent = (TurnEndedEvent) event;
            boolean hasEnemy = true;
            for (IRobotSnapshot r : teEvent.getTurnSnapshot().getRobots()) {
                if (r.getState() == RobotState.DEAD) {
                    hasEnemy = false;
                    break;
                }
            }
            final IBulletSnapshot[] bullets = teEvent.getTurnSnapshot().getBullets();

            for (IBulletSnapshot b : bullets) {
                if (getOwener(b.getBulletId()) == null) {
                    bulletOweners.put(b.getBulletId(), getOwener(teEvent, b));
                }

                if (hasEnemy) {
                    bulletStates.put(b.getBulletId(), b.getState());
                } else {
                    if (b.getState() == BulletState.HIT_VICTIM) {
                        bulletStates.put(b.getBulletId(), b.getState());
                    } else {
                        bulletStates.remove(b.getBulletId());
                    }
                }
            }
        }

        private String getOwener(Integer bulletId) {
            return this.bulletOweners.get(bulletId);
        }

        private String getOwener(TurnEndedEvent teEvent, IBulletSnapshot b) {
            Point bulletPos = new Point((int) b.getX(), (int) b.getY());
            double minDist = Integer.MAX_VALUE;
            String owener = "";
            for (IRobotSnapshot r : teEvent.getTurnSnapshot().getRobots()) {
                final double dist = bulletPos.distance(r.getX(), r.getY());
                if (dist < minDist) {
                    minDist = dist;
                    owener = r.getName();
                }
            }
            return owener;
        }
    }

}
