package voidious.radar;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import voidious.gfx.RoboGraphic;
import voidious.gfx.RoboPainter;
import voidious.utils.DiaUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
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

public class DiamondEyes implements RoboPainter {
    protected static final boolean ENABLE_DEBUGGING_GRAPHICS = true;
    protected static final double MAX_RADAR_TRACKING_AMOUNT = Math.PI / 4;
    protected static final long BOT_NOT_FOUND = -1;
    protected static final double BOT_WIDTH = 36;

    protected AdvancedRobot _robot;
    protected HashMap<String, RadarScan> _scans;
    protected Point2D.Double _myLocation;
    protected String _targetName = null;
    protected boolean _lockMode = false;
    protected long _resetTime;
    protected Point2D.Double _centerField;
    protected int _radarDirection;
    protected double _lastRadarHeading;

    protected Vector<RoboGraphic> _renderables;
    protected boolean _painting;
    protected boolean _robocodePainting;

    public DiamondEyes(AdvancedRobot robot) {
        _robot = robot;
        _scans = new HashMap<String, RadarScan>();
        _centerField = new Point2D.Double(_robot.getBattleFieldWidth() / 2,
                _robot.getBattleFieldHeight() / 2);
        _radarDirection = 1;
        _renderables = new Vector<RoboGraphic>();
        _painting = false;
        _robocodePainting = false;
    }

    public void initRound(AdvancedRobot robot) {
        _robot = robot;
        _scans.clear();
        _lockMode = false;
        _resetTime = 0;
        _lastRadarHeading = _robot.getRadarHeadingRadians();
        _renderables.clear();
    }

    public void execute() {
        _myLocation = new Point2D.Double(_robot.getX(), _robot.getY());
        checkScansIntegrity();
        if (_robot.getOthers() == 1 && !_lockMode && !_scans.isEmpty()) {
            setRadarLock((String) _scans.keySet().toArray()[0]);
        }
        directRadar();

        _lastRadarHeading = _robot.getRadarHeadingRadians();
        _myLocation = DiaUtils.nextLocation(_robot);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        Point2D.Double enemyLocation = DiaUtils.project(_myLocation,
                e.getBearingRadians() + _robot.getHeadingRadians(),
                e.getDistance());

        _scans.put(e.getName(),
                new RadarScan(_robot.getTime(), enemyLocation));
    }

    public void onRobotDeath(RobotDeathEvent e) {
        _scans.remove(e.getName());
        if (_targetName != null && _targetName.equals(e.getName())) {
            _lockMode = false;
        }
    }

    public void onPaint(Graphics2D g) {
        if (paintStatus()) {
            drawLastKnownBotLocations();

            Iterator<RoboGraphic> i = _renderables.iterator();
            while (i.hasNext()) {
                RoboGraphic r = i.next();
                r.render(g);
            }
            _renderables.clear();
        }
    }

    public void directRadar() {
        if (_lockMode && !_scans.containsKey(_targetName)) {
            System.out.println("WARNING: Radar locked onto dead or " +
                    "non-existent bot, releasing lock.");
            _lockMode = false;
        }

        double radarTurnAmount;
        if (_lockMode &&
                _scans.get(_targetName).lastScanTime == _robot.getTime()) {
            radarTurnAmount = Utils.normalRelativeAngle(
                    DiaUtils.absoluteBearing(_myLocation,
                            _scans.get(_targetName).lastLocation) -
                            _robot.getRadarHeadingRadians());
            _radarDirection = DiaUtils.nonZeroSign(radarTurnAmount);
            radarTurnAmount += _radarDirection *
                    (MAX_RADAR_TRACKING_AMOUNT / 2);
        } else {
            _radarDirection = nextRadarDirection();
            radarTurnAmount =
                    _radarDirection * MAX_RADAR_TRACKING_AMOUNT;
        }
        _robot.setTurnRadarRightRadians(radarTurnAmount);
    }

    public void setRadarLock(String botName) {
        if (_scans.containsKey(botName)) {
            _targetName = botName;
            _lockMode = true;
        }
    }

    public void releaseRadarLock() {
        _lockMode = false;
    }

    public long minTicksToScan(String botName) {
        if (!_scans.containsKey(botName)) {
            return BOT_NOT_FOUND;
        }

        double absBearing = DiaUtils.absoluteBearing(_myLocation,
                _scans.get(botName).lastLocation);
        double shortestAngleToScan = Math.abs(
                Utils.normalRelativeAngle(absBearing
                        - _robot.getRadarHeadingRadians()));
        long minTicks = Math.round(
                Math.ceil(shortestAngleToScan / MAX_RADAR_TRACKING_AMOUNT));

        return minTicks;
    }

    protected int nextRadarDirection() {
        if (_scans.isEmpty() || _scans.size() < _robot.getOthers()) {
            return 1;
        }

        String stalestBot = findStalestBotName();
        Point2D.Double radarTarget;

        if (minTicksToScan(stalestBot) == 4) {
            radarTarget = _centerField;
        } else {
            radarTarget = _scans.get(findStalestBotName()).lastLocation;
        }

        double absBearingRadarTarget =
                DiaUtils.absoluteBearing(_myLocation, radarTarget);

        if (justScannedThatSpot(absBearingRadarTarget)) {
            return _radarDirection;
        }

        if (Utils.normalRelativeAngle(absBearingRadarTarget -
                _robot.getRadarHeadingRadians()) > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    protected String findStalestBotName() {
        long oldestTime = Long.MAX_VALUE;
        String botName = null;

        Iterator<String> botNameIterator = _scans.keySet().iterator();
        while (botNameIterator.hasNext()) {
            String thisName = botNameIterator.next();
            if (_scans.get(thisName).lastScanTime < oldestTime) {
                oldestTime = _scans.get(thisName).lastScanTime;
                botName = thisName;
            }
        }

        return botName;
    }

    protected long timeSinceLastScan(String botName) {
        return _robot.getTime() - _scans.get(botName).lastScanTime;
    }

    protected void checkScansIntegrity() {
        if (_scans.size() != _robot.getOthers() &&
                _robot.getTime() - _resetTime > 25 &&
                _robot.getOthers() > 0) {
            _scans.clear();
            _lockMode = false;
            _resetTime = _robot.getTime();
            System.out.println("WARNING: Radar integrity failure detected " +
                    "(time = " + _resetTime + "), resetting.");
        }
    }

    public boolean justScannedThatSpot(double absBearing) {
        if ((DiaUtils.nonZeroSign(Utils.normalRelativeAngle(
                absBearing - _lastRadarHeading)) == _radarDirection) &&
                (DiaUtils.nonZeroSign(Utils.normalRelativeAngle(
                        _robot.getRadarHeadingRadians() - absBearing))
                        == _radarDirection)) {
            return true;
        } else {
            return false;
        }
    }

    public void drawLastKnownBotLocations() {
        Iterator<RadarScan> radarScans = _scans.values().iterator();
        while (radarScans.hasNext()) {
            RadarScan rs = radarScans.next();
            _renderables.addAll(Arrays.asList(
                    RoboGraphic.drawRectangle(rs.lastLocation, BOT_WIDTH, BOT_WIDTH,
                            Color.gray)));
        }
    }

    public void paintOn() {
        _painting = ENABLE_DEBUGGING_GRAPHICS;
    }

    public void paintOff() {
        _renderables.clear();
        _painting = false;
    }

    public void robocodePaintOn() {
        _robocodePainting = true;
    }

    public void robocodePaintOff() {
        _renderables.clear();
        _robocodePainting = false;
    }

    public String paintLabel() {
        return "Radar";
    }

    public boolean paintStatus() {
        return (_painting && _robocodePainting);
    }
}
