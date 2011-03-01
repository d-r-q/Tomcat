package lxx.targeting;

import lxx.targeting.predict.Predictor;
import lxx.utils.LXXPoint;
import lxx.StaticData;

import java.awt.*;import static java.lang.Math.toDegrees;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public class HeadOnPredictor implements Predictor {

    public Double predictAngle(Target t) {
        //System.out.println(toDegrees(StaticData.robot.angleTo(t)));
        return StaticData.robot.angleTo(t);
    }

    public void paint(Graphics2D g, Target t) {
    }

    public void onRoundStarted() {
    }

    public void targetUpdated(Target oldState, Target newState, robocode.Event source) {
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeadOnPredictor that = (HeadOnPredictor) o;

        return !(getName() != null ? !getName().equals(that.getName()) : that.getName() != null);

    }

    public int hashCode() {
        return (getName() != null ? getName().hashCode() : 0);
    }

}
