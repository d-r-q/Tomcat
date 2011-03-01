/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark;

import robocode.control.RobotSpecification;
import robocode.control.events.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: jdev
 * Date: 03.03.2010
 */
public class BattleView extends JPanel implements IBattleListener {

    private Battle battle;

    private final JLabel width = new JLabel("    Width: ");
    private final JLabel height = new JLabel("    Height:  ");
    private final JLabel rounds = new JLabel("    Rounds: ");
    private final JLabel parcipitians = new JLabel("    Parcipitians: ");
    private final JProgressBar progress = new JProgressBar();
    private final JButton start = new JButton("Start");
    private final JButton abort = new JButton("Abort");
    private final JTextArea log = new JTextArea("Please, create new battle...");

    private final BotBenchmark botBenchmark;
    private JScrollPane logPane;

    public BattleView(final BotBenchmark botBenchmark) {
        this.botBenchmark = botBenchmark;
        setLayout(new BorderLayout());
        JPanel left = new JPanel();
        final BoxLayout leftPanelLayout = new BoxLayout(left, BoxLayout.Y_AXIS);
        left.setLayout(leftPanelLayout);
        left.setPreferredSize(new Dimension(250, 600));
        left.add(parcipitians);
        left.add(width);
        left.add(height);
        left.add(rounds);

        add(left, BorderLayout.WEST);

        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(progress);
        progress.setPreferredSize(new Dimension(600, 20));
        bottom.add(start);
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                botBenchmark.runBattle();
            }
        });
        bottom.add(abort);
        abort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                botBenchmark.abortBattle();
            }
        });

        add(bottom, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout());
        logPane = new JScrollPane(log);
        center.add(logPane, BorderLayout.SOUTH);
        log.setRows(10);
        center.setSize(2000, 2000);


        add(center, BorderLayout.CENTER);
    }

    public void setBattle(Battle battle) {
        if (battle == null) {
            return;
        }
        this.battle = battle;
        width.setText("    Width: " + battle.getWidth());
        height.setText("    Height: " + battle.getHeight());
        rounds.setText("    Rounds: " + battle.getRounds());

        StringBuffer parcipitians = new StringBuffer("<html>&nbsp;&nbsp;&nbsp;&nbsp;Parcipitians:<br>");
        for (RobotSpecification rs : battle.getParcipitians()) {
            parcipitians.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- ").append(rs.getName()).append("<br>");
        }
        parcipitians.append("<html>");
        this.parcipitians.setText(parcipitians.toString());
        progress.setMinimum(0);
        progress.setMaximum(battle.getRounds() - 1);
    }

    public void onBattleStarted(BattleStartedEvent event) {
    }

    public void onBattleFinished(BattleFinishedEvent event) {
    }

    public void onBattleCompleted(BattleCompletedEvent event) {
    }

    public void onBattlePaused(BattlePausedEvent event) {
    }

    public void onBattleResumed(BattleResumedEvent event) {
    }

    public void onRoundStarted(RoundStartedEvent event) {
    }

    public void onRoundEnded(RoundEndedEvent event) {
        progress.setValue(event.getRound());
        progress.setStringPainted(true);
    }

    public void onTurnStarted(TurnStartedEvent event) {
    }

    public void onTurnEnded(TurnEndedEvent event) {
    }

    public void onBattleMessage(BattleMessageEvent event) {
        log.append("\n[INFO]: " + event.getMessage());
        logPane.getViewport().scrollRectToVisible(new Rectangle(0, log.getHeight(), log.getWidth(), 10));
    }

    public void onBattleError(BattleErrorEvent event) {
        log.append("\n[ERROR]: " + event.getError());
        logPane.getViewport().scrollRectToVisible(new Rectangle(0, log.getHeight(), log.getWidth(), 10));
    }
}
