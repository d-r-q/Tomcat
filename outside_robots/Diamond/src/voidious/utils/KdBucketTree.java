package voidious.utils;

import java.util.Arrays;
import java.util.Stack;

/*
 * This is a Kd-tree variant, as in http://en.wikipedia.org/wiki/Kd-tree, 
 * useful for efficiently finding nearest neighbors in a k-dimensional
 * space.
 * 
 * Thanks for the ideas, Corbos and Simonton.
 * 
 * Copyright (c) 2009-2010 - Voidious
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software.
 *
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 *
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */

// TODO: after splitting to left and right, null out points[]

public class KdBucketTree {
    public static final int MAX_BUCKET_SIZE = 32;
    double[][] _points;
    int _k;
    int _dimension;
    int _numPoints;
    double _splitValue;
    KdBucketTree _parent, _left, _right;
    boolean _alternate = false;

    public KdBucketTree() {
        this(null, null, 0);
    }

    public KdBucketTree(int d) {
        this(null, null, d);
    }

    public KdBucketTree(KdBucketTree parent, int d) {
        this(parent, null, d);
    }

    public KdBucketTree(KdBucketTree parent, double[][] p, int d) {
        _parent = parent;
        _points = null;
        _numPoints = 0;
        _splitValue = Double.NaN;
        if (p != null) {
            _k = p[0].length;
            _dimension = d % _k;
            _numPoints = p.length;
            _points = new double[MAX_BUCKET_SIZE][_k];
            for (int x = 0; x < p.length; x++) {
                _points[x] = p[x];
            }
        }
    }

    public double[][] getPoints() {
        return _points;
    }

    public int getNumPoints() {
        return _numPoints;
    }

    public int getK() {
        return _k;
    }

    public int getDimension() {
        return _dimension;
    }

    public boolean isParent() {
        return (_parent == null);
    }

    public boolean isLeaf() {
        return (_left == null && _right == null);
    }

    public KdBucketTree getParent() {
        return _parent;
    }

    public KdBucketTree getOtherChild(KdBucketTree child) {
        if (child == _left) {
            return _right;
        }
        return _left;
    }

    public double getSplitValue() {
        return _splitValue;
    }

    public static double[][] nearestNeighbors(KdBucketTree tree,
                                              double[] searchPoint, int numNeighbors) {
        double[] weights = new double[searchPoint.length];
        Arrays.fill(weights, 1);
        return nearestNeighbors(tree, searchPoint, numNeighbors, weights);
    }

    public static double[][] nearestNeighbors(KdBucketTree tree,
                                              double[] searchPoint, int numNeighbors, double[] weights) {

        if (numNeighbors <= 0) {
            return null;
        }

//        lastNnLog = "Finding " + numNeighbors + " nearest neighbors of: " + 
//            pointAsString(searchPoint) + "\n";

        Stack<KdBucketTree> crossed = new Stack<KdBucketTree>();
        crossed.push(new KdBucketTree());
        double[][] nearest = new double[numNeighbors][searchPoint.length];
        double[] nearestDistancesSq = new double[numNeighbors];
        for (int x = 0; x < nearestDistancesSq.length; x++) {
            nearestDistancesSq[x] = Double.POSITIVE_INFINITY;
        }

        KdBucketTree node = tree.findLeaf(searchPoint);
        KdBucketTree lastChild;
        double distanceSqThreshold = Double.POSITIVE_INFINITY;

        distanceSqThreshold = updateFromBucket(node, nearest,
                nearestDistancesSq, searchPoint, distanceSqThreshold,
                weights);

//        lastNnLog += "Starting at node (" + node.getDimension() + "): " + 
//            node.toString() + ", distanceSq: " + 
//            ScanTree.distanceSq(node.getLocation(), searchPoint) + "\n";
        while (!node.isParent()) {
            lastChild = node;
            node = node.getParent();

            if (crossed.peek() == node) {
                crossed.pop();
            } else {
//                lastNnLog += "  Parent (" + node.getDimension() + "): " + 
//                    node.toString() + ", distanceSq: " + parentDistanceSq + 
//                    "\n";

                double z =
                        (node.getSplitValue() - searchPoint[node.getDimension()]) *
                                weights[node.getDimension()];
                double testDistanceSq = z * z;
                if (testDistanceSq < distanceSqThreshold) {
                    KdBucketTree otherSide = node.getOtherChild(lastChild);
                    if (otherSide != null) {
//                        lastNnLog += "  Closest possible distanceSq across " + 
//                            "split dimension " + node.getDimension() + ": " + 
//                            testDistanceSq + "\n";
                        KdBucketTree testNode = otherSide.findLeaf(searchPoint);
                        distanceSqThreshold = updateFromBucket(testNode,
                                nearest, nearestDistancesSq, searchPoint,
                                distanceSqThreshold, weights);
//                        lastNnLog += "    Moving to leaf node (" + 
//                            testNode.getDimension() + "): " + 
//                            testNode.toString() + ", distanceSq: " + 
//                            testDistanceSq + "\n";
                        crossed.push(node);
                        node = testNode;
                    }
                }
            }
        }

        return nearest;

    }

    private static double updateFromBucket(KdBucketTree bucket,
                                           double[][] nearest, double[] nearestDistancesSq,
                                           double[] searchPoint, double distanceSqThreshold,
                                           double[] weights) {

        double[][] bucketPoints = bucket.getPoints();
        int numBucketPoints = bucket.getNumPoints();

        for (int x = 0; x < numBucketPoints; x++) {
            double distanceSq = distanceSq(bucketPoints[x], searchPoint,
                    weights);
            if (distanceSq < distanceSqThreshold) {
                distanceSqThreshold = findAndReplaceLongestDistanceSqSorted(
                        nearest, nearestDistancesSq, bucketPoints[x], distanceSq);
            }
        }

        return distanceSqThreshold;
    }

    public void insert(double[] point) {
/*
        if (isParent()) { 
            System.out.print("Inserting point: " );
            String value = "";
            for (int x = 0; x < point.length; x++) {
                if (x != 0) { value += ", "; }
                value += point[x];
            }
            System.out.println(value);
        }
*/
        if (_points == null && isLeaf()) {
            // Only happens for empty tree, root node.
            _k = point.length;
            _points = new double[MAX_BUCKET_SIZE][_k];
            _dimension = 0;
            _points[_numPoints++] = point;
//            System.out.println("Create root: " + pointAsString(point));
        } else {
            if (isLeaf()) {
                if (_numPoints == MAX_BUCKET_SIZE) {
                    double[] splitDimension = new double[_numPoints];
                    for (int x = 0; x < _numPoints; x++) {
                        splitDimension[x] = _points[x][_dimension];
                    }
                    Arrays.sort(splitDimension);
                    _splitValue = (splitDimension[_numPoints / 2] +
                            splitDimension[(_numPoints / 2) - 1]) / 2;

                    int numLeft = 0;
                    int numRight = 0;
                    boolean alternate = _alternate;
                    for (int x = 0; x < _numPoints; x++) {
                        if (splitDimension[x] < _splitValue) {
                            numLeft++;
                        } else if (splitDimension[x] > _splitValue) {
                            numRight++;
                        } else {
                            if (alternate) {
                                numLeft++;
                            } else {
                                numRight++;
                            }
                            alternate = !alternate;
                        }
                    }
                    alternate = _alternate;
                    double[][] leftPoints = new double[numLeft][_k];
                    double[][] rightPoints = new double[numRight][_k];
                    int l = 0, r = 0;
                    for (int x = 0; x < _numPoints; x++) {
                        if (_points[x][_dimension] < _splitValue) {
                            leftPoints[l++] = _points[x];
                        } else if (_points[x][_dimension] > _splitValue) {
                            rightPoints[r++] = _points[x];
                        } else {
                            if (alternate) {
                                leftPoints[l++] = _points[x];
                            } else {
                                rightPoints[r++] = _points[x];
                            }
                            alternate = !alternate;
                        }
                    }
                    _alternate = !_alternate;

                    _left = new KdBucketTree(this,
                            (l == 0 ? null : leftPoints), _dimension + 1);
                    _right = new KdBucketTree(this,
                            (r == 0 ? null : rightPoints), _dimension + 1);
                    _points = null;
//                    System.out.println("  Splitting bucket at " + _splitValue + 
//                        " (dim=" + _dimension +")");
//                    System.out.println("    " + numLeft + 
//                        " nodes to the left, " + numRight + 
//                        " nodes to the right.");
                    this.insert(point);
                } else {
                    _points[_numPoints++] = point;
//                    System.out.println("    Point " + _numPoints + 
//                        " added: " + pointAsString(point));
                }
            } else {
                if (point[_dimension] < _splitValue) {
//                    System.out.println("Left of " + _splitValue + " (dim " + 
//                        _dimension + ")");
                    _left.insert(point);
                } else {
//                    System.out.println("Right of " + _splitValue + " (dim " + 
//                        _dimension + ")");
                    _right.insert(point);
                }
            }
        }
    }

    public void remove(double[] point) {
        if (isLeaf()) {
            if (_points == null) {
                return;
            }

            for (int x = 0; x < _numPoints; x++) {
                if (_points[x] == point) {
                    _points[x] = _points[_numPoints - 1];
                    _numPoints--;
                    break;
                }
            }
        } else {
            if (point[_dimension] <= _splitValue) {
                _left.remove(point);
            }
            if (point[_dimension] >= _splitValue) {
                _right.remove(point);
            }
        }
    }

    public KdBucketTree findLeaf(double[] point) {
//      if (isParent()) {
//          lastFindLeaf = "";
//      }
//      lastFindLeaf += "At " + pointAsString(_location) + "\n";

        if (isLeaf()) {
            return this;
        }

        if (point[_dimension] < _splitValue) {
            return _left.findLeaf(point);
        } else {
            return _right.findLeaf(point);
        }
    }

    public static double distanceSq(double[] p1, double[] p2) {

        double sum = 0;
        for (int x = 0; x < p1.length; x++) {
            double z = p1[x] - p2[x];
            sum += z * z;
        }

        return sum;
    }

    public static double distanceSq(double[] p1, double[] p2,
                                    double[] weights) {

        double sum = 0;
        for (int x = 0; x < p1.length; x++) {
            double z = (p1[x] - p2[x]) * weights[x];
            sum += z * z;
        }

        return sum;
    }

    public static double distance(double[] p1, double[] p2) {
        return Math.sqrt(distanceSq(p1, p2));
    }

    public static double distance(double[] p1, double[] p2, double[] weights) {
        return Math.sqrt(distanceSq(p1, p2, weights));
    }

    public static double manhattanDistance(double[] p1, double[] p2,
                                           double[] weights) {

        double sum = 0;
        for (int x = 0; x < p1.length; x++) {
            sum += Math.abs(p1[x] - p2[x]) * weights[x];
        }

        return sum;
    }

    public static double manhattanDistance(double[] p1, double[] p2) {
        double sum = 0;
        for (int x = 0; x < p1.length; x++) {
            sum += Math.abs(p1[x] - p2[x]);
        }

        return sum;
    }

    public static double findLongestDistanceSq(double[][] points,
                                               double[] testPoint, double[] weights) {

        double longestDistanceSq = 0;
        for (int x = 0; x < points.length; x++) {
            double distanceSq = KdBucketTree.distanceSq(points[x], testPoint, weights);
            if (distanceSq > longestDistanceSq) {
                longestDistanceSq = distanceSq;
            }
        }

        return longestDistanceSq;
    }

    public static double findLongestDistanceSq(double[] nearestDistancesSq) {

        double longestDistanceSq = Double.NEGATIVE_INFINITY;
        for (int x = 0; x < nearestDistancesSq.length; x++) {
            double distanceSq = nearestDistancesSq[x];
            if (distanceSq > longestDistanceSq) {
                longestDistanceSq = distanceSq;
            }
        }

        return longestDistanceSq;
    }

    public static double findAndReplaceLongestDistanceSq(double[][] points,
                                                         double[] nearestDistances, double[] newPoint,
                                                         double newPointDistanceSq) {

        double longestDistanceSq = 0;
        double newLongestDistanceSq = 0;
        int longestIndex = 0;
        for (int x = 0; x < points.length; x++) {
            double distanceSq = nearestDistances[x];
            if (distanceSq > longestDistanceSq) {
                newLongestDistanceSq = longestDistanceSq;
                longestDistanceSq = distanceSq;
                longestIndex = x;
            } else if (distanceSq > newLongestDistanceSq) {
                newLongestDistanceSq = distanceSq;
            }
        }
        points[longestIndex] = newPoint;
        nearestDistances[longestIndex] = newPointDistanceSq;

        return Math.max(newLongestDistanceSq, newPointDistanceSq);
    }

    public static double findAndReplaceLongestDistanceSqSorted(
            double[][] points, double[] nearestDistances, double[] newPoint,
            double newPointDistanceSq) {

        int x;
        for (x = points.length - 2; x >= 0; x--) {
            double distanceSq = nearestDistances[x];
            if (newPointDistanceSq > distanceSq) {
                nearestDistances[x + 1] = newPointDistanceSq;
                points[x + 1] = newPoint;
                return nearestDistances[points.length - 1];
            } else {
                nearestDistances[x + 1] = nearestDistances[x];
                points[x + 1] = points[x];
            }
        }

        if (x < 0) {
            nearestDistances[0] = newPointDistanceSq;
            points[0] = newPoint;
        }

        return nearestDistances[points.length - 1];
    }

    public static double[] copyPoint(double[] p) {
        double[] copyLocation = new double[p.length];
        for (int x = 0; x < p.length; x++) {
            copyLocation[x] = p[x];
        }

        return copyLocation;
    }
}

