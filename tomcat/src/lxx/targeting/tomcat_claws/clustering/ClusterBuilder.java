package lxx.targeting.tomcat_claws.clustering;

import lxx.utils.APoint;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.min;

/**
 * User: jdev
 * Date: 17.06.11
 */
public class ClusterBuilder {

    public Set<Cluster2D> createCluster(List<APoint> points) {
        final Iterator<Color> colorsIterator = Arrays.asList(new Color(255, 0, 0, 155), new Color(0, 255, 0, 155),
                new Color(0, 0, 255, 155)).iterator();
        Set<Cluster2D> clusters = new HashSet<Cluster2D>();
        if (points.size() < 4) {
            for (APoint pnt : points) {
                final Cluster2D c = new Cluster2D(colorsIterator.next());
                c.addEntry(pnt);
                clusters.add(c);
            }
            return clusters;
        }

        clusters = createClusters(points);
        fillClusters(points, clusters);

        return clusters;
    }

    private Set<Cluster2D> createClusters(List<APoint> futurePoses) {
        final Set<APoint> clustersCenters = findFarestPoints(futurePoses);
        clustersCenters.add(findFarestPoint(clustersCenters, futurePoses));

        final Iterator<Color> colorsIterator = Arrays.asList(new Color(255, 0, 0, 155), new Color(0, 255, 0, 155),
                new Color(0, 0, 255, 155)).iterator();
        final Set<Cluster2D> clusters = new HashSet<Cluster2D>();
        for (APoint clusterCenter : clustersCenters) {
            final Cluster2D c1 = new Cluster2D(colorsIterator.next());
            c1.addEntry(clusterCenter);
            clusters.add(c1);
        }

        return clusters;
    }

    private APoint findFarestPoint(Set<APoint> toPoints, List<APoint> candidates) {
        APoint farestPoint = null;
        double maxMinDist = Integer.MIN_VALUE;
        for (APoint candidate : candidates) {
            double minDist = Integer.MAX_VALUE;
            for (APoint pnt : toPoints) {
                minDist = min(minDist, candidate.aDistance(pnt));
            }

            if (minDist > maxMinDist) {
                maxMinDist = minDist;
                farestPoint = candidate;
            }
        }

        return farestPoint;
    }

    private Set<APoint> findFarestPoints(List<APoint> poses) {
        APoint pnt1;
        APoint pnt2;
        APoint farestPnt1 = null;
        APoint farestPnt2 = null;
        double maxDist = Integer.MIN_VALUE;

        for (int i = 0; i < poses.size(); i++) {
            pnt1 = poses.get(i);
            for (int j = i + 1; j < poses.size(); j++) {
                pnt2 = poses.get(j);

                final double dist = pnt1.aDistance(pnt2);
                if (dist > maxDist) {
                    maxDist = dist;
                    farestPnt1 = pnt1;
                    farestPnt2 = pnt2;
                }
            }
        }

        return new HashSet<APoint>(Arrays.asList(farestPnt1, farestPnt2));
    }

    private void fillClusters(List<APoint> futurePoses, Set<Cluster2D> clusters) {
        for (APoint futurePoint : futurePoses) {
            double minDist = Integer.MAX_VALUE;
            Cluster2D minDistCluster = null;
            for (Cluster2D c : clusters) {
                final double dist = c.distance(futurePoint);
                if (dist < minDist) {
                    minDist = dist;
                    minDistCluster = c;
                }
            }

            if (minDistCluster != null) {
                minDistCluster.addEntry(futurePoint);
            }
        }
    }

}
