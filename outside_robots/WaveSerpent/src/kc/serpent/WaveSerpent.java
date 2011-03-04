package kc.serpent;

import kc.serpent.gun.*;
import kc.serpent.movement.*;
import kc.serpent.utils.*;
import robocode.*;
import robocode.util.Utils;
import wiki.mc2k7.RaikoGun;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

/**
 * WaveSerpent - a robot by Kevin Clark (Kev)
 * See http://robowiki.net/wiki/WaveSerpent for details.
 * <p/>
 * Code is released under the RoboWiki Public Code Licence, (see http://robowiki.net/wiki/RWPCL)
 */
public class WaveSerpent extends AdvancedRobot {
    static boolean isTC = false;
    static boolean isMC = false;
    static boolean useRaikoGun = false;
    static boolean isMelee = false;
    static boolean isPainting = false;

    int radarTurnDirection;
    long lastScannedTime = -1;
    ScannedRobotEvent lastScanEvent;

    static int[] finishes;
    static int skippedTurns = 0;
    static int wallHits = 0;
    static int enemyIndex = 0;

    RobotPredictor robotPredictor = new RobotPredictor();
    RaikoGun raikoGun;
    static GunBase[] duelGun;
    static MovementBase[] duelMovement;

    boolean hasWon = false;

    public void run() {
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        setColors(new Color(0, 0, 90), new Color(90, 90, 90), new Color(255, 255, 255));
        hasWon = false;

        if (getOthers() > 1) {
            isMelee = true;
        }

        if (useRaikoGun) {
            raikoGun = new RaikoGun(this);
            isTC = false;
            isMC = true;
        }

        if (getRoundNum() == 0) {
            finishes = new int[getOthers() + 1];

            if (!isMC) {
                duelGun = new GunBase[getOthers()];
                GunBase.isMelee = isMelee;
                GunBase.isTC = isTC;
            }
            if (!isTC) {
                duelMovement = new MovementBase[getOthers()];
                MovementBase.isMelee = isMelee;
                MovementBase.isMC = isMC;
                if (useRaikoGun) {
                    MovementBase.isMC = false;
                }
            }

            for (int i = 0; i < getOthers(); i++) {
                if (!isMC) {
                    duelGun[i] = new GunBase(this, robotPredictor);
                    duelGun[i].init();
                }
                if (!isTC) {
                    duelMovement[i] = new MovementBase(this);
                    duelMovement[i].init();
                }
            }
        }

        for (int i = 0; i < getOthers(); i++) {
            if (!isMC) {
                duelGun[i].reset();
            }
            if (!isTC) {
                duelMovement[i].reset();
            }
        }

        radarTurnDirection = KUtils.sign(Utils.normalRelativeAngle(KUtils.absoluteBearing(new Point2D.Double(getX(), getY()), new Point2D.Double(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2)) - getRadarHeadingRadians()));
        do {
            if (getOthers() > 1) {
            } else {
                if (lastScannedTime + 1 <= getTime()) {
                    setTurnRadarRightRadians(radarTurnDirection * Double.POSITIVE_INFINITY);
                }
                if (getOthers() == 0 && !isTC) {
                    duelMovement[enemyIndex].onScannedRobot(lastScanEvent);
                }
            }
            execute();
        } while (true);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        lastScanEvent = e;
        lastScannedTime = getTime();

        if (getOthers() > 1) {
        } else {
            if (isMelee) {
            }
            if (useRaikoGun) {
                raikoGun.onScannedRobot(e);
            }
            if (!isTC) {
                duelMovement[enemyIndex].onScannedRobot(e);
            }
            if (!isMC) {
                duelGun[enemyIndex].onScannedRobot(e);
            }

            double radarTurn = Utils.normalRelativeAngle(getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians());
            radarTurnDirection = KUtils.sign(radarTurn);
            setTurnRadarRightRadians(radarTurn + radarTurnDirection * Math.PI / 12);
            radarTurnDirection *= -1;
        }
    }

    public void onBulletHit(BulletHitEvent e) {
        if (getOthers() < 2 && !hasWon) {
            if (!isTC) {
                duelMovement[enemyIndex].onBulletHit(e);
            }
            if (!isMC) {
                duelGun[enemyIndex].onBulletHit(e);
            }
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (getOthers() < 2 && !isTC) {
            duelMovement[enemyIndex].onHitByBullet(e);
        }
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        if (getOthers() < 2 && !isTC) {
            duelMovement[enemyIndex].onBulletHitBullet(e);
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (getOthers() == 0) {
            if (!isTC) {
                duelMovement[enemyIndex].onRobotDeath(e);
            }
        }
    }

    public void onDeath(DeathEvent e) {
        if (getOthers() < 2) {
            Vector v = getAllEvents();
            Iterator i = v.iterator();
            while (i.hasNext()) {
                Object obj = i.next();
                if (obj instanceof HitByBulletEvent) {
                    onHitByBullet((HitByBulletEvent) obj);
                }
            }
        }

        if (!hasWon) {
            printStats();
            out.println();
        }
    }

    public void onWin(WinEvent e) {
        Vector v = getAllEvents();
        Iterator i = v.iterator();
        while (i.hasNext()) {
            Object obj = i.next();
            if (obj instanceof BulletHitEvent) {
                onBulletHit((BulletHitEvent) obj);
            }
        }

        hasWon = true;
        printStats();
    }

    public void printStats() {
        out.println();
        if (!isMelee) {
            if (!isMC) {
                duelGun[enemyIndex].printStats();
            }
            if (!isTC) {
                duelMovement[enemyIndex].printStats();
                out.println();
            }
        } else {
        }
        out.println("Skipped Turns: " + skippedTurns);
        out.println("Wall Hits: " + wallHits);

        finishes[getOthers()]++;
        out.print("Finishes: ");
        for (int i = 0; i < finishes.length; i++) {
            out.print((i == getOthers() ? "*" : "") + finishes[i] + " ");
        }
        out.println();
    }

    public void onPaint(java.awt.Graphics2D g) {
        if (isPainting) {
            if (getOthers() > 1) {
            } else {
                if (!isMC) {
                    duelGun[enemyIndex].onPaint(g);
                }
                if (!isTC) {
                    duelMovement[enemyIndex].onPaint(g);
                }
            }
        }
    }

    public void onSkippedTurn(SkippedTurnEvent e) {
        skippedTurns++;
    }

    public void onHitWall(HitWallEvent e) {
        wallHits++;
    }

    public void onHitRobot(HitRobotEvent e) {
        if (!isTC && getOthers() == 1) {
            duelMovement[enemyIndex].onHitRobot(e);
        }
    }

    public void setTurnRightRadians(double turn) {
        robotPredictor.setTurnRightRadians(turn);
        super.setTurnRightRadians(turn);
    }

    public void setAhead(double d) {
        robotPredictor.setAhead(d);
        super.setAhead(d);
    }

    public void setMaxVelocity(double v) {
        robotPredictor.setMaxVelocity(v);
        super.setMaxVelocity(v);
    }
}
