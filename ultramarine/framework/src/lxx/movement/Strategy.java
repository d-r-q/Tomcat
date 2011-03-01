package lxx.movement;

import lxx.utils.LXXPoint;
import lxx.targeting.TargetChooser;

import java.awt.*;

/**
 * User: jdev
 * Date: 31.10.2009
 */
public interface Strategy {

    boolean match();
    LXXPoint getDestination(boolean newSession);
    void paint(Graphics2D g);
    TargetChooser getTargetChooser();

}
