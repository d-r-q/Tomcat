/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.office;

import lxx.RobotListener;
import lxx.events.TickEvent;
import lxx.utils.LXXRobotState;
import lxx.wave.Wave;
import lxx.wave.WaveCallback;
import robocode.Event;

import java.util.*;

/**
 * User: jdev
 * Date: 07.11.2009
 */
public class WaveManager implements RobotListener {

    private final Map<String, Set<Wave>> waves = new HashMap<String, Set<Wave>>();
    private final Map<Wave, Set<WaveCallback>> waveCallbacks = new HashMap<Wave, Set<WaveCallback>>();

    public Wave launchWave(LXXRobotState source, LXXRobotState target, double speed, WaveCallback callback) {
        Wave w = new Wave(source, target, speed, source.getRobot().getTime());
        addWave(source.getRobot().getName(), w, callback);

        return w;
    }

    public Wave launchWaveOnNextTick(LXXRobotState source, LXXRobotState target, double bulletSpeed) {
        return launchWaveOnTick(source, target, bulletSpeed, source.getRobot().getTime() + 1, null);
    }

    private Wave launchWaveOnTick(LXXRobotState source, LXXRobotState target, double bulletSpeed,
                                  long tick, WaveCallback callback) {
        Wave w = new Wave(source, target, bulletSpeed, tick);
        addWave(source.getRobot().getName(), w, callback);

        return w;
    }

    private void addWave(String owner, Wave w, WaveCallback callback) {
        Set<Wave> waves = getWaves(owner);
        waves.add(w);
        addCallback(callback, w);
    }

    private void addCallback(WaveCallback callback, Wave w) {
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
                List<Wave> toRemove = new ArrayList<Wave>();
                for (Wave w : ws) {
                    try {
                        if (!w.getTargetStateAtFireTime().getRobot().isAlive() || !w.getSourceStateAtFireTime().getRobot().isAlive()) {
                            toRemove.add(w);
                            continue;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    Set<WaveCallback> callbacks = waveCallbacks.get(w);
                    if (w.check()) {
                        for (WaveCallback callback : callbacks) {
                            callback.wavePassing(w);
                        }
                    } else if (w.getTraveledDistance() > w.getSourceStateAtFireTime().aDistance(w.getTargetStateAtFireTime().getRobot())) {
                        toRemove.add(w);
                        for (WaveCallback callback : callbacks) {
                            callback.waveBroken(w);
                        }
                        waveCallbacks.remove(w);
                    }
                }
                ws.removeAll(toRemove);
            }
        }
    }

}
