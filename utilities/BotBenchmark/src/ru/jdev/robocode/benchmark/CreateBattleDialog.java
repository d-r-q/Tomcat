/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark;

import robocode.control.RobotSpecification;
import ru.jdev.robocode.benchmark.listeners.RobotsListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: jdev
 * Date: 03.03.2010
 */
public class CreateBattleDialog extends JDialog {

    private JList robots;
    private java.util.List<RobotSpecification> sortedRep;
    private JTextField robotName;
    private JList pacipitians;
    private JButton add;
    private JButton remove;
    private JTextField widthTF;
    private JTextField heightTF;
    private JTextField roundsTF;
    private boolean isConfirmed = false;

    public CreateBattleDialog() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        Rectangle bounds = new Rectangle(new Dimension(700, 400));
        bounds.setLocation((toolkit.getScreenSize().width - bounds.width) / 2,
                (toolkit.getScreenSize().height - bounds.height) / 2);
        setBounds(bounds);

        JPanel lists = new JPanel(new FlowLayout());
        lists.add(createRepositoryList());
        lists.add(createButtonsPanel());
        lists.add(createPacipitiansList());
        lists.setPreferredSize(new Dimension(1000, 1000));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(lists, BorderLayout.CENTER);
        getContentPane().add(createDialogPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDialogPanel() {
        JPanel dialogPanel = new JPanel();

        final JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isConfirmed = true;
                setVisible(false);
            }
        });
        dialogPanel.add(ok);

        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isConfirmed = false;
                setVisible(false);
            }
        });
        dialogPanel.add(cancel);

        return dialogPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setPreferredSize(new Dimension(100, 200));
        buttonsPanel.setMaximumSize(new Dimension(100, 200));

        add = new JButton(">>");
        add.addActionListener(new AddActionListener());
        buttonsPanel.add(add);

        remove = new JButton("<<");
        remove.addActionListener(new RemoveActionListener());
        buttonsPanel.add(remove);

        return buttonsPanel;
    }

    private JPanel createPacipitiansList() {
        JPanel parcipitiansPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        parcipitiansPanel.setPreferredSize(new Dimension(250, 300));
        parcipitiansPanel.setMaximumSize(new Dimension(250, 300));

        pacipitians = new JList(new RobotsListModel());
        pacipitians.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                int selectedIdx = pacipitians.getSelectedIndex();
                if (selectedIdx == -1) {
                    JOptionPane.showMessageDialog(null, "Pick robot in pacipitians list");
                    return;
                }

                final RobotsListModel model = (RobotsListModel) pacipitians.getModel();
                model.removeElementAt(selectedIdx);

                if (model.size() >= 2) {
                    add.setEnabled(false);
                } else {
                    add.setEnabled(true);
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        parcipitiansPanel.add(new JScrollPane(pacipitians));

        JLabel widthLabel = new JLabel("BF Width:");
        parcipitiansPanel.add(widthLabel);
        widthTF = new JTextField();
        widthTF.setColumns(4);
        parcipitiansPanel.add(widthTF);

        JLabel heightLabel = new JLabel("BF Height:");
        parcipitiansPanel.add(heightLabel);
        heightTF = new JTextField();
        heightTF.setColumns(4);
        parcipitiansPanel.add(heightTF);

        JLabel roundsLabel = new JLabel("Rounds:");
        parcipitiansPanel.add(roundsLabel);
        roundsTF = new JTextField();
        roundsTF.setColumns(4);
        parcipitiansPanel.add(roundsTF);

        return parcipitiansPanel;
    }

    private JPanel createRepositoryList() {
        robots = new JList(new RobotsListModel());
        robots.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                int selectedIdx = robots.getSelectedIndex();
                if (selectedIdx == -1 || pacipitians.getModel().getSize() >= 2) {
                    JOptionPane.showMessageDialog(null, "Pick robot in repository list");
                    return;
                }

                final RobotsListModel model = (RobotsListModel) robots.getModel();
                RobotSpecification spec = model.getSpec(selectedIdx);
                ((RobotsListModel) pacipitians.getModel()).addElement(spec);

                if (((RobotsListModel) pacipitians.getModel()).size() >= 2) {
                    add.setEnabled(false);
                } else {
                    add.setEnabled(true);
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        JPanel repList = new JPanel(new BorderLayout());
        repList.setPreferredSize(new Dimension(250, 300));
        repList.setMaximumSize(new Dimension(250, 300));
        repList.add(new JScrollPane(robots), BorderLayout.CENTER);

        FlowLayout spLayout = new FlowLayout();
        spLayout.setAlignment(FlowLayout.LEFT);
        JPanel searchPanel = new JPanel(spLayout);
        JLabel searchLabel = new JLabel("Search: ");
        searchPanel.add(searchLabel);
        robotName = new JTextField();
        robotName.setColumns(10);
        robotName.addKeyListener(new NameKeyListener());
        searchPanel.add(searchLabel);
        searchPanel.add(robotName);
        repList.add(searchPanel, BorderLayout.SOUTH);

        setModal(true);

        return repList;
    }

    public Battle createBattle(RobotSpecification[] repository, int defaultBFWidth,
                               int defaultBFHeight, int defaultRounds) {

        ((DefaultListModel) robots.getModel()).removeAllElements();

        sortedRep = new ArrayList<RobotSpecification>();
        sortedRep.addAll(Arrays.asList(repository));
        Collections.sort(sortedRep, new Comparator<RobotSpecification>() {
            public int compare(RobotSpecification o1, RobotSpecification o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        for (RobotSpecification spec : sortedRep) {
            ((DefaultListModel) robots.getModel()).addElement(spec);
        }

        widthTF.setText(String.valueOf(defaultBFWidth));
        heightTF.setText(String.valueOf(defaultBFHeight));
        roundsTF.setText(String.valueOf(defaultRounds));

        setVisible(true);

        if (!isConfirmed) {
            return null;
        }

        Battle battle = new Battle();

        battle.setHeight(Integer.valueOf(heightTF.getText()));
        battle.setWidth(Integer.valueOf(widthTF.getText()));
        battle.setRounds(Integer.valueOf(roundsTF.getText()));
        battle.setParcipitians(((RobotsListModel) pacipitians.getModel()).getSpecs());

        return battle;
    }

    private class NameKeyListener implements KeyListener {

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            java.util.List<RobotSpecification> subList = new ArrayList<RobotSpecification>();

            for (RobotSpecification spec : sortedRep) {
                if (spec.getName().toLowerCase().indexOf(robotName.getText().toLowerCase()) != -1) {
                    subList.add(spec);
                }
            }

            ((RobotsListModel) robots.getModel()).removeAllElements();
            for (RobotSpecification spec : subList) {
                ((RobotsListModel) robots.getModel()).addElement(spec);
            }

            robots.repaint();
        }
    }

    private class AddActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedIdx = robots.getSelectedIndex();
            if (selectedIdx == -1) {
                JOptionPane.showMessageDialog(null, "Pick robot in repository list");
                return;
            }

            final RobotsListModel model = (RobotsListModel) robots.getModel();
            RobotSpecification spec = model.getSpec(selectedIdx);
            ((RobotsListModel) pacipitians.getModel()).addElement(spec);

            if (((RobotsListModel) pacipitians.getModel()).size() >= 2) {
                add.setEnabled(false);
            } else {
                add.setEnabled(true);
            }
        }
    }

    private class RemoveActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedIdx = pacipitians.getSelectedIndex();
            if (selectedIdx == -1) {
                JOptionPane.showMessageDialog(null, "Pick robot in pacipitians list");
                return;
            }

            final RobotsListModel model = (RobotsListModel) pacipitians.getModel();
            model.removeElementAt(selectedIdx);

            if (model.size() >= 2) {
                add.setEnabled(false);
            } else {
                add.setEnabled(true);
            }
        }
    }

}
