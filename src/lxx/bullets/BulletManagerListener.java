/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.bullets;

/**
 * User: jdev
 * Date: 15.02.2010
 */
public interface BulletManagerListener {

    void bulletFired(LXXBullet bullet);

    void bulletHit(LXXBullet bullet);

    void bulletMiss(LXXBullet bullet);

    void bulletIntercepted(LXXBullet bullet);

    void bulletPassing(LXXBullet bullet);

}
