/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import ru.jdev.robocode.benchmark.listeners.CreateBattleMenuItemListener;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class BotBenchmark extends JFrame {

    private Properties bbProps;
    private RobocodeEngine engine;
    private Battle currentBattle;
    private BattleView battleView;

    public void init() {
        battleView = new BattleView(this);
        bbProps = new Properties();
        try {
            bbProps.load(new FileInputStream("./config/bb.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (bbProps.getProperty("robocode.home") == null ||
                !new File(bbProps.getProperty("robocode.home") + "\\libs\\robocode.jar").exists()) {
            getRobocodeHome();
        }
        System.out.println("Creating engine...");
        engine = new RobocodeEngine(new File(bbProps.getProperty("robocode.home")));
        System.out.println("Engine created");
        engine.addBattleListener(new BBBattleListener(this));
        engine.addBattleListener(battleView);
    }

    private void getRobocodeHome() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose robocode home direcotry");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int res = 0;

        do {
            JOptionPane.showMessageDialog(this, "Robocode home directoy isn't setted");
            res = fileChooser.showDialog(this, "Select");
            if (res == JFileChooser.APPROVE_OPTION) {
                bbProps.setProperty("robocode.home", fileChooser.getSelectedFile().getAbsolutePath());
                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream("./config/bb.properties");
                    bbProps.store(stream, "");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (stream != null) {
                        try {
                            stream.flush();
                            stream.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
                break;
            }
        } while (true);

    }

    public void createUI() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem item = new JMenuItem("Create battle");
        item.addActionListener(new CreateBattleMenuItemListener(new CreateBattleDialog(), engine.getLocalRepository(), bbProps, this));
        menu.add(item);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(menuBar, BorderLayout.NORTH);
        getContentPane().add(battleView, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setExtendedState(MAXIMIZED_BOTH);

        setVisible(true);
    }

    public void runBattle() {
        final BattlefieldSpecification specification1 = new BattlefieldSpecification(currentBattle.getWidth(), currentBattle.getHeight());
        final BattleSpecification specification = new BattleSpecification(currentBattle.getRounds(), specification1, currentBattle.getParcipitians());
        engine.runBattle(specification);
    }

    public static void main(String[] args) {
        final BotBenchmark bb = new BotBenchmark();
        bb.init();
        bb.createUI();
        //bb.runBattle();
    }

    public int getTotalRounds() {
        return currentBattle.getRounds();
    }

    public void setBattle(Battle battle) {
        this.currentBattle = battle;
        battleView.setBattle(battle);
    }

    public BattleView getBattleView() {
        return battleView;
    }

    public void abortBattle() {
        engine.abortCurrentBattle();
    }
}
