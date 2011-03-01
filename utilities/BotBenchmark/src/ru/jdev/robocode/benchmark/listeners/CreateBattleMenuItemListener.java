/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark.listeners;

import robocode.control.RobotSpecification;
import ru.jdev.robocode.benchmark.Battle;
import ru.jdev.robocode.benchmark.BotBenchmark;
import ru.jdev.robocode.benchmark.CreateBattleDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

/**
 * User: jdev
 * Date: 03.03.2010
 */
public class CreateBattleMenuItemListener implements ActionListener {

    private final CreateBattleDialog dialog;
    private final RobotSpecification[] repository;
    private final Properties bbProps;
    private final BotBenchmark botBenchmark;

    public CreateBattleMenuItemListener(CreateBattleDialog dialog, RobotSpecification[] repository, Properties bbProps, BotBenchmark botBenchmark) {
        this.dialog = dialog;
        this.repository = repository;
        this.bbProps = bbProps;
        this.botBenchmark = botBenchmark;
    }

    public void actionPerformed(ActionEvent e) {
        Battle b = dialog.createBattle(repository, Integer.valueOf(bbProps.getProperty("battle.battleField.width")),
                Integer.valueOf(bbProps.getProperty("battle.battleField.height")),
                Integer.valueOf(bbProps.getProperty("battle.rounds")));
        botBenchmark.setBattle(b);
    }
}
