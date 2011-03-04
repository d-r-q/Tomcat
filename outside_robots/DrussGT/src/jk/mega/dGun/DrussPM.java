package jk.mega.dGun;

import java.util.ArrayList;
//import java.awt.Point2D.Double;

public class DrussPM {

    static final Point EOR = new Point(
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    ArrayList<Point> points = new ArrayList<Point>();

    public void appendPoint(
            double latVel,
            double advVel,
            double vel,
            double deltaH,
            double wall,
            double dist) {
        points.add(0, new Point(latVel, advVel, vel, deltaH, wall, dist));
    }

    public void endOfRound() {
        points.add(0, EOR);
    }

    double getBestOffset(double bulletPower, double distance, double enemyOffset) {
        double bulletVelocity = 20 - 3 * bulletPower;
        ArrayList<Match> matches = getMatches();
        //System.out.println("No. of Matches: " + matches.size());
        int[] bins = new int[65];
        for (int i = 0; i < matches.size(); i++) {
            Match m = matches.get(i);
            double offset =
                    getOffset(bulletVelocity,
                            distance,
                            enemyOffset,
                            m);
            if (offset != Double.NaN) {

                bins[32 + (int) (32 / 0.8 * offset)] += m.length;
                //System.out.println("returning offset");
                //System.out.println("match length: " + m.length);
                // return offset;
            }
        }
        int maxIndex = 32;
        for (int i = 0; i < bins.length; i++)
            if (bins[i] > bins[maxIndex])
                maxIndex = i;

        return (maxIndex - 32) * (0.8 / 32);
    }


    double getOffset(double bulVel, double distance, double enemyOffset, Match m) {
        double x = 0, y = distance, heading = enemyOffset;
        int time = 0, index = m.start;
        Point p = points.get(m.start);
        double angleMod =
                (Math.signum(p.vel) == Math.signum(points.get(0).vel)) ? 1 : -1;
        do {
            time++;
            index--;
            if (index >= 0)
                p = points.get(index);
            else
                return Double.NaN;

            if (p == EOR)
                return Double.NaN;

            heading += p.deltaH * angleMod;

            x += Math.sin(heading) * p.vel * angleMod;
            y += Math.cos(heading) * p.vel * angleMod;
        } while ((bulVel * bulVel) * (time * time) < x * x + y * y);
        //System.out.println("Time: " + time + "   Length: " + m.length);
        return Math.atan2(x, y);
    }

    ArrayList<Match> getMatches() {
        ArrayList<Match> matches = new ArrayList<Match>();
        ArrayList<Match> partMatch = new ArrayList<Match>();
        Point start = points.get(0);
        for (int i = 1, max = Math.min(points.size(), 30000); i < max; i++) {
            Point p = points.get(i);
            if (Math.abs(start.latVel - p.latVel) < 0.5
                    && Math.abs(start.advVel - p.advVel) < 0.5
                    && Math.abs(start.wall - p.wall) < 0.15
                    && Math.abs(start.dist - p.dist) < 100)
                partMatch.add(new Match(i));
            for (int j = 0; j < partMatch.size(); j++) {
                Match m = partMatch.get(j);
                if (p != EOR
                        && Math.abs(p.latVel - points.get(i - m.start).latVel) < 0.5
                    //&& Math.abs(p.advVel - points.get(i - m.start).advVel) < 0.5
                        )
                    m.length++;
                else {
                    partMatch.remove(j);
                    int k = matches.size() - 1;
                    for (; k >= 0 && matches.get(k).length < m.length;)
                        k--;
                    if (k < 5) {
                        matches.add(k + 1, m);
                        if (matches.size() > 5)
                            matches.remove(5);
                    }
                    //matches.add(m);
                }
            }
        }

        while (partMatch.size() > 0) {
            Match m = partMatch.get(0);
            partMatch.remove(0);
            int k = matches.size() - 1;
            for (; k >= 0 && matches.get(k).length < m.length;)
                k--;
            if (k < 5) {
                matches.add(k + 1, m);
                if (matches.size() > 5)
                    matches.remove(5);
            }
            //matches.add(m);
        }

        //matches.addAll(partMatch);

        return matches;
    }

    static class Match {
        Match(int s) {
            start = s;
            length = 1;
        }

        ;

        int start, length;
    }

    static class Point {
        Point(double lv, double av, double v, double dh, double w, double d) {
            latVel = lv;
            advVel = av;
            vel = v;
            deltaH = dh;
            wall = Math.min(0.3, w);
            dist = d;
        }

        double latVel, advVel, vel, deltaH, wall, dist;
    }
}