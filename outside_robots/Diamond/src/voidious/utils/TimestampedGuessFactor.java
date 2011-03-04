package voidious.utils;

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

// TODO: genericize
public class TimestampedGuessFactor implements Comparable {
    public double guessFactor;
    public int roundNum;
    public long tick;
    public double weight;

    public TimestampedGuessFactor(int r, long t, double gf) {
        roundNum = r;
        tick = t;
        guessFactor = gf;
        weight = 1;
    }

    public int compareTo(Object o) {
        if (!(o instanceof TimestampedGuessFactor)) {
            return 0;
        }

        TimestampedGuessFactor that = (TimestampedGuessFactor) o;
        if (this.roundNum < that.roundNum) {
            return -1;
        } else if (this.roundNum == that.roundNum) {
            if (this.tick < that.tick) {
                return -1;
            } else if (this.tick == that.tick) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }
}
