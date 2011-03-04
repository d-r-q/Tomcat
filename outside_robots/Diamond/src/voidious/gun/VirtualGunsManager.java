package voidious.gun;

import robocode.util.Utils;
import voidious.utils.DiaUtils;
import voidious.utils.DiaWave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

public class VirtualGunsManager {
    protected static final double TYPICAL_DISTANCE = 400;
    protected static final double TYPICAL_ESCAPE_RANGE = 0.9;
    protected ArrayList<DiaGun> _guns;
    protected HashMap<DiaGun, HashMap<String, GunStats>> _gunRatings;

    public VirtualGunsManager() {
        _guns = new ArrayList<DiaGun>();
        _gunRatings = new HashMap<DiaGun, HashMap<String, GunStats>>();
    }

    public void addGun(DiaGun gun) {
        _guns.add(gun);
        _gunRatings.put(gun, new HashMap<String, GunStats>());
    }

    public boolean contains(DiaGun gun) {
        return _guns.contains(gun);
    }

    public double getRating(DiaGun gun, String botName) {
        if (_guns.contains(gun) && _gunRatings.get(gun).containsKey(botName)) {
            return _gunRatings.get(gun).get(botName).gunRating();
        }

        return 0;
    }

    public int getShotsFired(DiaGun gun, String botName) {
        if (_guns.contains(gun) && _gunRatings.get(gun).containsKey(botName)) {
            return _gunRatings.get(gun).get(botName).shotsFired;
        }

        return 0;
    }

    public void fireVirtualBullets(DiaWave w) {
        Iterator<DiaGun> gunsIterator = _guns.iterator();
        while (gunsIterator.hasNext()) {
            DiaGun gun = gunsIterator.next();
            GunStats stats;
            if (_gunRatings.get(gun).containsKey(w.botName)) {
                stats = _gunRatings.get(gun).get(w.botName);
            } else {
                stats = new GunStats();
                _gunRatings.get(gun).put(w.botName, stats);
            }

            double firingAngle = gun.aimWithWave(w, false);
            stats.virtualBullets.put(w,
                    new VirtualBullet(w.fireTime, firingAngle));
        }

    }

    public void registerWaveBreak(DiaWave w, double hitAngle) {
        Iterator<DiaGun> gunsIterator = _guns.iterator();
        while (gunsIterator.hasNext()) {
            DiaGun gun = gunsIterator.next();
            GunStats stats = _gunRatings.get(gun).get(w.botName);
            VirtualBullet vb = stats.virtualBullets.get(w);

            double hitWeight = (w.targetDistance / TYPICAL_DISTANCE) *
                    (w.escapeAngleRange() / TYPICAL_ESCAPE_RANGE);
            double tolerance = DiaUtils.botWidthAimAngle(w.targetDistance);

            double missFactor =
                    Math.abs(Utils.normalRelativeAngle(vb.firingAngle - hitAngle))
                            / tolerance;
            if (missFactor <= 1) {
                stats.shotsHit += hitWeight * (1 - DiaUtils.square(missFactor));
            }

            stats.shotsFired++;
            stats.virtualBullets.remove(w);
        }
    }

    public void clear() {
        Iterator<DiaGun> gunsIterator = _guns.iterator();
        while (gunsIterator.hasNext()) {
            DiaGun gun = gunsIterator.next();
            Iterator<GunStats> statsIterator =
                    _gunRatings.get(gun).values().iterator();
            while (statsIterator.hasNext()) {
                GunStats stats = statsIterator.next();
                stats.virtualBullets.clear();
            }
            gun.clear();
        }
    }

    public void clearWave(DiaWave w) {
        Iterator<DiaGun> gunsIterator = _guns.iterator();
        while (gunsIterator.hasNext()) {
            DiaGun gun = gunsIterator.next();
            gun.clearWave(w);
        }
    }

    public DiaGun bestGun(String botName) {
        DiaGun bestGun = null;
        double bestRating = 0;

        Iterator<DiaGun> gunsIterator = _guns.iterator();
        while (gunsIterator.hasNext()) {
            DiaGun gun = gunsIterator.next();
            double rating = 0;
            if (_gunRatings.get(gun).containsKey(botName)) {
                rating = _gunRatings.get(gun).get(botName).gunRating();
            }

            if (bestGun == null || rating > bestRating) {
                bestGun = gun;
                bestRating = rating;
            }
        }

        return bestGun;
    }

    public void printGunRatings(String botName) {
        System.out.println("Virtual Gun ratings for " + botName + ":");
        Iterator<DiaGun> gunsIterator = _guns.iterator();
        while (gunsIterator.hasNext()) {
            DiaGun gun = gunsIterator.next();
            if (_gunRatings.get(gun).containsKey(botName)) {
                double rating = _gunRatings.get(gun).get(botName).gunRating();
                System.out.println("  " + gun.getLabel() + ": " +
                        DiaUtils.round(rating * 100, 2));
            } else {
                System.out.println("WARNING: Never logged any Virtual Guns info for " + gun.getLabel());
            }
        }
    }

    class GunStats {
        public int shotsFired;
        public double shotsHit;
        public HashMap<DiaWave, VirtualBullet> virtualBullets;

        public GunStats() {
            shotsFired = 0;
            shotsHit = 0;
            virtualBullets = new HashMap<DiaWave, VirtualBullet>();
        }

        public double gunRating() {
            if (shotsFired == 0)
                return 0;

            return (shotsHit / shotsFired);
        }
    }

    class VirtualBullet {
        public long fireTime;
        public double firingAngle;

        public VirtualBullet(long t, double a) {
            fireTime = t;
            firingAngle = a;
        }
    }
}

