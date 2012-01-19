/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import robocode.Rules;

import static java.lang.Math.toRadians;

/**
 * User: jdev
 * Date: 30.10.2009
 */

@SuppressWarnings({"UnusedDeclaration"})
public abstract class LXXConstants {

    public static final double RADIANS_0 = toRadians(0);
    public static final double RADIANS_0_1 = toRadians(0.1);
    public static final double RADIANS_0_5 = toRadians(0.5);
    public static final double RADIANS_1 = toRadians(1);
    public static final double RADIANS_1_26 = toRadians(1.26);
    public static final double RADIANS_2 = toRadians(2);
    public static final double RADIANS_3 = toRadians(3);
    public static final double RADIANS_4 = toRadians(4);
    public static final double RADIANS_5 = toRadians(5);
    public static final double RADIANS_9 = toRadians(9);
    public static final double RADIANS_10 = toRadians(10);
    public static final double RADIANS_15 = toRadians(15);
    public static final double RADIANS_16 = toRadians(16);
    public static final double RADIANS_20 = toRadians(20);
    public static final double RADIANS_25 = toRadians(25);
    public static final double RADIANS_30 = toRadians(30);
    public static final double RADIANS_35 = toRadians(35);
    public static final double RADIANS_36 = toRadians(36);
    public static final double RADIANS_40 = toRadians(40);
    public static final double RADIANS_42 = toRadians(42);
    public static final double RADIANS_45 = toRadians(45);
    public static final double RADIANS_50 = toRadians(50);
    public static final double RADIANS_55 = toRadians(55);
    public static final double RADIANS_60 = toRadians(60);
    public static final double RADIANS_72 = toRadians(72);
    public static final double RADIANS_75 = toRadians(75);
    public static final double RADIANS_80 = toRadians(80);
    public static final double RADIANS_85 = toRadians(85);
    public static final double RADIANS_90 = toRadians(90);
    public static final double RADIANS_95 = toRadians(95);
    public static final double RADIANS_100 = toRadians(100);
    public static final double RADIANS_110 = toRadians(110);
    public static final double RADIANS_120 = toRadians(120);
    public static final double RADIANS_135 = toRadians(135);
    public static final double RADIANS_140 = toRadians(140);
    public static final double RADIANS_150 = toRadians(150);
    public static final double RADIANS_160 = toRadians(160);
    public static final double RADIANS_170 = toRadians(170);
    public static final double RADIANS_180 = toRadians(180);
    public static final double RADIANS_210 = toRadians(210);
    public static final double RADIANS_270 = toRadians(270);
    public static final double RADIANS_315 = toRadians(315);
    public static final double RADIANS_330 = toRadians(330);
    public static final double RADIANS_340 = toRadians(340);
    public static final double RADIANS_349 = toRadians(349);
    public static final double RADIANS_350 = toRadians(350);
    public static final double RADIANS_360 = toRadians(360);

    public static final int ROBOT_SIDE_SIZE = 36;
    public static final int ROBOT_SIDE_HALF_SIZE = ROBOT_SIDE_SIZE / 2;
    public static final double MIN_BULLET_SPEED = Rules.getBulletSpeed(Rules.MAX_BULLET_POWER);

    public static final double INITIAL_GUN_HEAT = 3.0;
    public static final double ROBOT_HIT_DAMAGE = 0.6;
}
