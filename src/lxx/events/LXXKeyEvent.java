/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.events;

import robocode.Event;

public class LXXKeyEvent extends Event {

    private final char keyChar;

    public LXXKeyEvent(char keyChar) {
        this.keyChar = keyChar;
    }

    public char getKeyChar() {
        return keyChar;
    }
}
