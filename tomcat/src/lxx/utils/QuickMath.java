/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

/**
 * M - combination of FastTrig, java.lang.Math and robocode.util.Utils
 * <p/>
 * The trig section was created by Alexander Schultz, and improved by Julian Kent.
 * <p/>
 * The inverse trig section was first created by Julian Kent,
 * and later improved by Nat Pavasant and Starrynte.
 * <p/>
 * The angle normalization is originally robocode.util.Utils'
 * so it is Matthew's and Flemming's.
 * <p/>
 * Other parts was created by Nat Pavasant to use in his robot.
 *
 * @author Alexander Schultz (a.k.a. Rednaxela)
 * @author Julian Kent (a.k.a. Skilgannon)
 * @author Flemming N. Larsen
 * @author Mathew A. Nelson
 * @author Nat Pavasant
 */

public final class QuickMath {

    /* Setting for trig */
    private static final int TRIG_HIGH_DIVISIONS = 131072; /* Must be power of 2 */
    private static final int SINE_TABLE_DELTA1 = (TRIG_HIGH_DIVISIONS - 1);
    private static final double SIN_TABLE_DELTA2 = 1.25 * TRIG_HIGH_DIVISIONS;
    private static final double ACOS_K = SINE_TABLE_DELTA1 / 2;
    private static final double ACOS_TABLE_DELTA = (ACOS_K + 0.5);

    public static final double PI = 3.1415926535897932384626433832795D;
    public static final double TWO_PI = 6.2831853071795864769252867665590D;
    public static final double HALF_PI = 1.5707963267948966192313216916398D;

    private static final double K = TRIG_HIGH_DIVISIONS / TWO_PI;
    private static final double TAN_K = TRIG_HIGH_DIVISIONS / PI;

    /* Lookup tables */
    private static final double[] sineTable = new double[TRIG_HIGH_DIVISIONS];
    private static final double[] acosTable = new double[TRIG_HIGH_DIVISIONS];
    private static final double[] tanTable = new double[TRIG_HIGH_DIVISIONS];

    /* Hide the constructor */
    private QuickMath() {
    }

    /**
     * Initializing the lookup table
     */
    public static void init() {
        for (int i = 0; i < TRIG_HIGH_DIVISIONS; i++) {
            sineTable[i] = Math.sin(i / K);
            acosTable[i] = Math.acos(i / ACOS_K - 1);
            tanTable[i] = Math.tan(i / TAN_K);
        }
    }

    public static double sin(double value) {
        return sineTable[(int) (((value * K + 0.5) % TRIG_HIGH_DIVISIONS + TRIG_HIGH_DIVISIONS)) & SINE_TABLE_DELTA1];
    }

    public static double cos(double value) {
        return sineTable[(int) (((value * K + 0.5) % TRIG_HIGH_DIVISIONS + SIN_TABLE_DELTA2)) & SINE_TABLE_DELTA1];
    }

    public static double tan(double value) {
        return tanTable[(int) (((value * TAN_K + 0.5) % TRIG_HIGH_DIVISIONS + TRIG_HIGH_DIVISIONS)) & (TRIG_HIGH_DIVISIONS - 1)];
    }

    public static double asin(double value) {
        return HALF_PI - acosTable[(int) (value * ACOS_K + ACOS_TABLE_DELTA)];
    }

    public static double acos(double value) {
        return acosTable[(int) (value * ACOS_K + ACOS_TABLE_DELTA)];
    }

}
