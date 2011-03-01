package lxx.autosegmentation.model;

/**
 * User: jdev
 * Date: 07.03.2010
 */
public class Interval {

    public final int a;
    public final int b;

    public Interval(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getIntersection(Interval i) {
        if (a <= i.a && b >= i.b) {
            return i.b - i.a;
        }

        if (a >= i.a && b <= i.b) {
            return b - a;
        }

        if (a >= i.a && a < i.b) {
            return i.b - a;
        }

        if (b > i.a && b <= i.b) {
            return b - i.a;
        }

        return 0;
    }

}
