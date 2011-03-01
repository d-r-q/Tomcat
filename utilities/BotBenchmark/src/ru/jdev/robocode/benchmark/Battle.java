/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.robocode.benchmark;

import robocode.control.RobotSpecification;

/**
 * User: jdev
 * Date: 03.03.2010
 */
public class Battle {

    private int width;
    private int height;

    private RobotSpecification[] parcipitians;

    private int rounds;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public RobotSpecification[] getParcipitians() {
        return parcipitians;
    }

    public void setParcipitians(RobotSpecification[] parcipitians) {
        this.parcipitians = parcipitians;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }
}
