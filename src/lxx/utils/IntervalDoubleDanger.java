package lxx.utils;

/**
 * User: jdev
 * Date: 11.02.12
 */
public class IntervalDoubleDanger extends IntervalDouble {

    public final double danger;

    public IntervalDoubleDanger(double danger) {
        this.danger = danger;
    }

    public IntervalDoubleDanger(double a, double b, double danger) {
        super(a, b);
        this.danger = danger;
    }

    public IntervalDoubleDanger(IntervalDouble ival, double danger) {
        super(ival);
        this.danger = danger;
    }
}
