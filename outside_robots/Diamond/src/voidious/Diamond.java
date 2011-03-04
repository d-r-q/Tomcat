package voidious;

import robocode.*;
import voidious.gfx.DiamondColors;
import voidious.gun.DiamondFist;
import voidious.move.DiamondWhoosh;
import voidious.radar.DiamondEyes;
import voidious.utils.DiaUtils;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Diamond - a robot by Voidious
 * <p/>
 * A melee specialist. Employs Minimum Risk movement in general and Wave
 * Surfing (derived from Dookious) in 1v1 scenarios.
 * <p/>
 * Uses k-nearest neighbors and kernel density (aka "Dynamic Clustering")
 * for surfing and targeting data analysis. Uses displacement vectors for
 * recording / reconstructing firing angles in the gun, GuessFactors in the
 * surfing movement.
 * <p/>
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

public class Diamond extends AdvancedRobot {
    protected static final boolean _TC = false;
    protected static final boolean _MC = false;
    protected static final boolean _TRACE = false;

    protected static final String[] _TRACE_INCLUDE = new String[]{
            "GunAnalyzer\\.register.*"

    };
    protected static final String[] _TRACE_EXCLUDE = new String[]{
            "DiamondWhoosh.*"
    };

    static {
        DiaUtils.traceEnabled = _TRACE;
    }

    protected static DiamondEyes _radar;
    protected static DiamondWhoosh _move;
    protected static DiamondFist _gun;
    protected static DiamondColors _gfx;

    protected static long _lastPaintRound = 0;
    protected static long _lastPaintTime = -2;
    protected static double _randColors = Math.random();

    public void run() {
        DiaUtils.initTrace(_TRACE_INCLUDE, _TRACE_EXCLUDE);

        DiaUtils.log("Diamond", "run", "", true);

        if (_radar == null) {
            _radar = new DiamondEyes(this);
        }
        if (_move == null) {
            _move = new DiamondWhoosh(this);
        }
        if (_gun == null) {
            _gun = new DiamondFist(this, _radar, _TC);
        }
        if (_gfx == null) {
            _gfx = new DiamondColors(this, _radar, _move, _gun, _TC, _MC);
            _gfx.registerPainter("r", _radar);
            _gfx.registerPainter("g", _gun);
            _gfx.registerPainter("m", _move);
        }

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        if (_randColors < .05) {
            setBlueStreakColors();
        } else if (_randColors < .1) {
            this.setMillenniumGuardColors();
        } else {
            setColors(Color.black, Color.black, new Color(255, 255, 170));
        }

        _radar.initRound(this);
        _move.initRound(this);
        _gun.initRound(this);

        while (true) {
            _gfx.updatePaintProcessing();
            if (!_TC) {
                _move.execute();
            }
            if (!_MC) {
                _gun.execute();
            }
            _radar.execute();
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        DiaUtils.log("Diamond", "onScannedRobot", "", true);
        _radar.onScannedRobot(e);
        if (!_TC) {
            _move.onScannedRobot(e);
        }
        if (!_MC) {
            _gun.onScannedRobot(e);
        }
        DiaUtils.log("Diamond", "onScannedRobot", "", false);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        _radar.onRobotDeath(e);
        if (!_TC) {
            _move.onRobotDeath(e);
        }
        if (!_MC) {
            _gun.onRobotDeath(e);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        DiaUtils.log("Diamond", "onHitByBullet", "", true);
        if (!_TC) {
            _move.onHitByBullet(e);
        }
        if (!_MC) {
            _gun.onHitByBullet(e);
        }
        DiaUtils.log("Diamond", "onHitByBullet", "", false);
    }

    public void onBulletHit(BulletHitEvent e) {
        if (!_TC) {
            _move.onBulletHit(e);
        }
        if (!_MC) {
            _gun.onBulletHit(e);
        }
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        if (!_TC) {
            _move.onBulletHitBullet(e);
        }
    }

    public void onHitWall(HitWallEvent e) {
        System.out.println("WARNING: I hit a wall (" + getTime() + ").");
    }

    public void onWin(WinEvent e) {
        if (!_MC) {
            _gun.onWin(e);
        }
        if (!_TC) {
            _move.onWin(e);
        }
    }

    public void onDeath(DeathEvent e) {
        if (!_MC) {
            _gun.onDeath(e);
        }
        if (!_TC) {
            _move.onDeath(e);
        }
    }

    public void onPaint(Graphics2D g) {
        _gfx.onPaint(g);
    }

    public void onKeyPressed(KeyEvent e) {
        _gfx.onKeyPressed(e);
    }

    public void onSkippedTurn(SkippedTurnEvent e) {
        System.out.println("WARNING: Turn skipped at: " + e.getTime());
    }

    public void setMillenniumGuardColors() {
        if (getRoundNum() == 0) {
            System.out.println("Activating Millennium Guard colors.");
        }

        Color bloodRed = new Color(120, 0, 0);
        Color gold = new Color(240, 235, 170);
        setColors(bloodRed, gold, bloodRed);
    }

    public void setBlueStreakColors() {
        if (getRoundNum() == 0) {
            System.out.println("Activating Blue Streak colors.");
        }

        Color denimGrey = new Color(101, 108, 128);
        Color gloveRed = new Color(150, 20, 30);
        setColors(denimGrey, Color.white, gloveRed);
    }
}
