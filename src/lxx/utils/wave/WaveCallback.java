/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.wave;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public interface WaveCallback {

    void wavePassing(Wave w);

    void waveBroken(Wave w);

}
