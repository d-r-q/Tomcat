package lxx.targeting.predict;

import lxx.utils.LXXPoint;
import lxx.targeting.Target;
import lxx.targeting.TargetManagerListener;
import lxx.BasicRobot;

import java.awt.*;

/**
 * User: jdev
 * Date: 30.10.2009
 */

public interface Predictor extends TargetManagerListener {

    Double predictAngle(Target t);

    void paint(Graphics2D g, Target t);

    void onRoundStarted();

    String getName();

    

}
