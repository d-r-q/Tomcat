package lxx.autosegmentation.model;

/**
 * Created by IntelliJ IDEA.
* User: pipsi
* Date: 21.02.2010
* Time: 16:00:38
* To change this template use File | Settings | File Templates.
*/
public class Extremum {
    public final double x;
    public final double y;

    public Extremum(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
