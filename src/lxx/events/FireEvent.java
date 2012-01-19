/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.events;

import lxx.bullets.LXXBullet;
import robocode.Event;

/**
 * User: jdev
 * Date: 13.09.2010
 */
public class FireEvent extends Event {

    private final LXXBullet bullet;

    public FireEvent(LXXBullet bullet) {
        this.bullet = bullet;
    }

    public LXXBullet getBullet() {
        return bullet;
    }
}
