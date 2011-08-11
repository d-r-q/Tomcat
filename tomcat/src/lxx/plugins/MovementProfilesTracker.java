/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.plugins;

import lxx.office.Office;

public class MovementProfilesTracker implements Plugin {

    private static boolean enabled = false;

    public void roundStarted(Office office) {
    }

    public void battleEnded() {
    }

    public void tick() {
        if (!enabled) {
            return;
        }

    }

}
