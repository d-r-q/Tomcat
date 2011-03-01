package lxx.targeting;

import robocode.Event;

/**
 * User: pipsi
 * Date: 29.10.2009
 */
public interface TargetManagerListener {

    void targetUpdated(Target oldState, Target newState, Event source);

}
