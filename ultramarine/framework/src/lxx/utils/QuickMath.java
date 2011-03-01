package lxx.utils;

import java.awt.geom.Point2D;import static java.lang.Math.sqrt;

/**
 * M - combination of FastTrig, java.lang.Math and robocode.util.Utils
 *
 * The trig section was created by Alexander Schultz, and improved by Julian Kent.
 *
 * The inverse trig section was first created by Julian Kent,
 * and later improved by Nat Pavasant and Starrynte.
 *
 * The angle normalization is originally robocode.util.Utils'
 * so it is Matthew's and Flemming's.
 *
 * Other parts was created by Nat Pavasant to use in his robot.
 *
 * @author Alexander Schultz (a.k.a. Rednaxela)
 * @author Julian Kent (a.k.a. Skilgannon)
 * @author Flemming N. Larsen
 * @author Mathew A. Nelson
 * @author Nat Pavasant
 */

public final class QuickMath {
	public static final double PI = 3.1415926535897932384626433832795D;
	public static final double TWO_PI = 6.2831853071795864769252867665590D;
	public static final double HALF_PI = 1.5707963267948966192313216916398D;
	public static final double QUARTER_PI = 0.7853981633974483096156608458199D;
	public static final double THREE_OVER_TWO_PI = 4.7123889803846898576939650749193D;

	/* Setting for trig */
	private static final int TRIG_DIVISIONS = 8192; /* Must be power of 2 */
	private static final int TRIG_HIGH_DIVISIONS = 131072; /* Must be power of 2 */
	private static final double K = TRIG_DIVISIONS / TWO_PI;
	private static final double ACOS_K = (TRIG_HIGH_DIVISIONS - 1) / 2;
	private static final double TAN_K = TRIG_HIGH_DIVISIONS / PI;

	/* Lookup tables */
	private static final double[] sineTable = new double[TRIG_DIVISIONS];
	private static final double[] tanTable = new double[TRIG_HIGH_DIVISIONS];
	private static final double[] acosTable = new double[TRIG_HIGH_DIVISIONS];

	/* Hide the constructor */
	private QuickMath() {}

	/**
	 * Initializing the lookup table
	 */
	static {
		for (int i = 0; i < TRIG_DIVISIONS; i++) {
			sineTable[i] = Math.sin(i / K);
		}
		for (int i = 0; i < TRIG_HIGH_DIVISIONS; i++) {
			tanTable[i] = Math.tan(i / TAN_K);
			acosTable[i] = Math.acos(i / ACOS_K - 1);
		}
	}

	/* Fast and resonable accurate trig functions */
	public static double sin(double value) { return sineTable[(int) (((value * K + 0.5) % TRIG_DIVISIONS + TRIG_DIVISIONS)) & (TRIG_DIVISIONS - 1)]; }
	public static double cos(double value) { return sineTable[(int) (((value * K + 0.5) % TRIG_DIVISIONS + 1.25 * TRIG_DIVISIONS)) & (TRIG_DIVISIONS - 1)]; }
	public static double tan(double value) { return tanTable[(int) (((value * TAN_K + 0.5) % TRIG_HIGH_DIVISIONS + TRIG_HIGH_DIVISIONS)) & (TRIG_HIGH_DIVISIONS - 1)]; }
	public static double asin(double value) { return HALF_PI - acos(value); }
	public static double acos(double value) { return acosTable[(int) (value * ACOS_K + (ACOS_K + 0.5))]; }
	public static double atan(double value) { return (value >= 0 ? acos(1 / sqrt(value * value + 1)) : -acos(1 / sqrt(value * value + 1))); }
	public static double atan2(double x, double y) { return (x >= 0 ? acos(y / sqrt(x * x + y * y)) : -acos(y / sqrt(x * x + y * y))); }

    public static double getAngle(Point2D.Double source, Point2D.Double target) { return atan2(target.getY() - source.getX(), target.getY() - source.getY()); }

}
