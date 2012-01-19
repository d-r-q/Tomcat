/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.strategies;

import lxx.Tomcat;
import lxx.targeting.Target;

/**
 * User: jdev
 * Date: 12.02.2011
 */
public interface FirePowerSelector {

    double selectFirePower(Tomcat robot, Target target);

}
