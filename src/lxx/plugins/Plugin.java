/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.plugins;

import lxx.office.Office;

/**
 * User: jdev
 * Date: 30.09.2010
 */
public interface Plugin {

    void roundStarted(Office office);

    void battleEnded();

    void tick();
}
