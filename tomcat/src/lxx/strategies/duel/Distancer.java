package lxx.strategies.duel;

import lxx.LXXRobotState;
import lxx.utils.APoint;

/**
 * User: jdev
 * Date: 20.06.11
 */
public interface Distancer {

    double getDesiredHeading(APoint surfPoint, LXXRobotState robot, WaveSurfingMovement.OrbitDirection orbitDirection);

}
