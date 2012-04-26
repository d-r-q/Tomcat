/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils.wave;

import lxx.LXXRobot;
import lxx.LXXRobotSnapshot;
import lxx.RobotListener;
import lxx.events.TickEvent;
import robocode.Event;

import java.util.*;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class WaveManager implements RobotListener {

    private final Map<String, Set<Wave>> waves = new HashMap<String, Set<Wave>>();
    private final Map<Wave, Set<WaveCallback>> waveCallbacks = new HashMap<Wave, Set<WaveCallback>>();

    public Wave launchWave(LXXRobotSnapshot sourceState, LXXRobotSnapshot targetState, LXXRobot target, double speed, WaveCallback callback) {
        final Wave w = new Wave(sourceState, targetState, target, speed, target.getTime());
        addWave(sourceState.getName(), w, callback);

        return w;
    }

    private void addWave(String owner, Wave w, WaveCallback callback) {
        final Set<Wave> waves = getWaves(owner);
        waves.add(w);
        addCallback(callback, w);
    }

    public void addCallback(WaveCallback callback, Wave w) {
        Set<WaveCallback> callbacks = waveCallbacks.get(w);
        if (callbacks == null) {
            callbacks = new HashSet<WaveCallback>();
            waveCallbacks.put(w, callbacks);
        }
        if (callback != null) {
            callbacks.add(callback);
        }
    }

    private Set<Wave> getWaves(String owner) {
        Set<Wave> waves = this.waves.get(owner);
        if (waves == null) {
            waves = new HashSet<Wave>();
            this.waves.put(owner, waves);
        }
        return waves;
    }

    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (Set<Wave> ws : waves.values()) {
                List<Wave> toRemove = new LinkedList<Wave>();
                for (Wave w : ws) {
                    if (!w.getTarget().isAlive()) {
                        toRemove.add(w);
                        continue;
                    }

                    final Set<WaveCallback> callbacks = waveCallbacks.get(w);
                    if (w.check()) {
                        for (WaveCallback callback : callbacks) {
                            try {
                                callback.wavePassing(w);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    } else if (w.isPassed()) {
                        toRemove.add(w);
                        for (WaveCallback callback : callbacks) {
                            try {
                                callback.waveBroken(w);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        waveCallbacks.remove(w);
                    }
                }
                ws.removeAll(toRemove);
            }
        }
    }

}
