/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.debug;

import lxx.office.Office;

/**
 * User: jdev
 * Date: 30.09.2010
 */
public interface Debugger {

    void roundStarted(Office office);

    void roundEnded();

    void battleEnded();

    void tick();
}
