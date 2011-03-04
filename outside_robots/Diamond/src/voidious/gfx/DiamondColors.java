package voidious.gfx;

import robocode.AdvancedRobot;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Copyright (c) 2009-2010 - Voidious
 * <p/>
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * <p/>
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * <p/>
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software.
 * <p/>
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * <p/>
 * 3. This notice may not be removed or altered from any source
 * distribution.
 */

public class DiamondColors {
    private AdvancedRobot _robot;
    protected static RoboPainter _radar;
    protected static RoboPainter _move;
    protected static RoboPainter _gun;
    private boolean _tcMode;
    private boolean _mcMode;
    private HashMap<String, RoboPainter> _paintKeys;

    protected static long _lastPaintRound = 0;
    protected static long _lastPaintTime = -2;
    protected Vector<RoboGraphic> _renderables;

    public DiamondColors(AdvancedRobot robot, RoboPainter radar,
                         RoboPainter move, RoboPainter gun, boolean isTc, boolean isMc) {

        _robot = robot;
        _radar = radar;
        _move = move;
        _gun = gun;
        _tcMode = isTc;
        _mcMode = isMc;
        _paintKeys = new HashMap<String, RoboPainter>();
        _renderables = new Vector<RoboGraphic>();
    }

    public void registerPainter(String key, RoboPainter p) {
        _paintKeys.put(key.toLowerCase(), p);
        p.paintOn();
    }

    public void onKeyPressed(KeyEvent e) {
        char c = e.getKeyChar();
        Iterator<String> keysIterator = _paintKeys.keySet().iterator();
        while (keysIterator.hasNext()) {
            String keyCommand = keysIterator.next();
            if (keyCommand.toLowerCase().charAt(0) == c ||
                    keyCommand.toUpperCase().charAt(0) == c) {
                RoboPainter p = _paintKeys.get(keyCommand);
                if (p.paintStatus()) {
                    p.paintOff();
                } else {
                    p.paintOn();
                }
            }
        }
    }

    public void onPaint(Graphics2D g) {
        _lastPaintRound = _robot.getRoundNum();
        _lastPaintTime = _robot.getTime();

        _radar.onPaint(g);
        if (!_tcMode) {
            _move.onPaint(g);
        }
        if (!_mcMode) {
            _gun.onPaint(g);
        }

        if (robocodePaintingOn()) {
            drawMenu();

            Iterator<RoboGraphic> i = _renderables.iterator();
            while (i.hasNext()) {
                RoboGraphic r = i.next();
                r.render(g);
            }
            _renderables.clear();
        }
    }

    public void updatePaintProcessing() {
        if (robocodePaintingOn()) {
            _radar.robocodePaintOn();
            _move.robocodePaintOn();
            _gun.robocodePaintOn();
        } else {
            _radar.robocodePaintOff();
            _move.robocodePaintOff();
            _gun.robocodePaintOff();
        }
    }

    protected void drawMenu() {
        int numComponents = _paintKeys.size();
        double height = 20 + (numComponents * 30);
        int x = 0;

        Iterator<String> keysIterator = _paintKeys.keySet().iterator();
        while (keysIterator.hasNext()) {
            String keyCommand = keysIterator.next();
            RoboPainter p = _paintKeys.get(keyCommand);
            _renderables.add(RoboGraphic.drawText(
                    keyCommand.toUpperCase() + ": " + p.paintLabel(), 20,
                    height - (x++ * 30),
                    (p.paintStatus() ? Color.green : Color.red)));
        }
    }

    protected boolean robocodePaintingOn() {
        return (_lastPaintRound == _robot.getRoundNum() &&
                _robot.getTime() - _lastPaintTime <= 1);
    }
}
