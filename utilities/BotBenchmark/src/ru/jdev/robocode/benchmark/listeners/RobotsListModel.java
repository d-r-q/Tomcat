/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark.listeners;

import robocode.control.RobotSpecification;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * User: jdev
 * Date: 03.03.2010
 */
public class RobotsListModel extends DefaultListModel {

    public Object getElementAt(int index) {
        return ((RobotSpecification) super.getElementAt(index)).getName();
    }

    public Enumeration elements() {
        List<String> names = new ArrayList<String>();

        for (Enumeration e = super.elements(); e.hasMoreElements();) {
            names.add(((RobotSpecification) e.nextElement()).getName());
        }

        return Collections.enumeration(names);
    }

    public Object elementAt(int index) {
        return ((RobotSpecification) super.getElementAt(index)).getName();
    }

    public RobotSpecification getSpec(int idx) {
        return (RobotSpecification) super.elementAt(idx);
    }

    public RobotSpecification[] getSpecs() {
        RobotSpecification[] res = new RobotSpecification[size()];

        int idx = 0;
        for (Enumeration e = super.elements(); e.hasMoreElements();) {
            res[idx++] = (RobotSpecification) e.nextElement();
        }

        return res;
    }
}
