package kc.serpent.movement;

import kc.serpent.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SHLMovement implements MovementSystem {
    ArrayList allBuffers = new ArrayList();
    ArrayList[] buffers = new ArrayList[3];
    HashMap[] waveStats = new HashMap[3];
    HashMap[] maxWeights = new HashMap[3];

    double[] bufferTypeWeights = new double[3];

    public void init(MovementBase base) {
        for (int i = 0; i < 3; i++) {
            buffers[i] = new ArrayList();
            waveStats[i] = new HashMap();
            maxWeights[i] = new HashMap();
        }

        double[][][] metaSlices = new double[MovementBuffer.DIMENSIONS][][];
        double[][][][] metaIndexStats = new double[MovementBuffer.DIMENSIONS][][][];
        double[][] bounds = new double[MovementBuffer.DIMENSIONS][];

        bounds[0] = new double[]{0.0, MovementBase.MAX_DISTANCE};
        bounds[1] = new double[]{0.0, 8.0};
        bounds[2] = new double[]{-8.0, 1.0};
        bounds[3] = new double[]{0.0, 10.0};
        bounds[4] = new double[]{0.0, 80.0};
        bounds[5] = new double[]{0.0, 1.0};
        bounds[6] = new double[]{0.0, 1.0};

        metaSlices[0] = new double[6][];
        metaSlices[0][1] = new double[]{450.0};
        metaSlices[0][2] = new double[]{300.0, 600.0};
        metaSlices[0][3] = new double[]{200.0, 400.0, 600.0};
        metaSlices[0][4] = new double[]{150.0, 300.0, 450.0, 600.0};
        metaSlices[0][5] = new double[]{120.0, 240.0, 360.0, 480.0, 600.0};

        metaSlices[1] = new double[9][];
        metaSlices[1][1] = new double[]{4.5};
        metaSlices[1][2] = new double[]{2.5, 5.5};
        metaSlices[1][3] = new double[]{1.5, 4.5, 6.5};
        metaSlices[1][4] = new double[]{1.5, 3.5, 5.5, 7.5};
        metaSlices[1][5] = new double[]{0.5, 2.5, 4.5, 6.5, 7.5};
        metaSlices[1][8] = new double[]{0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5};

        metaSlices[2] = new double[3][];
        metaSlices[2][1] = new double[]{-0.01, 0.01};
        metaSlices[2][2] = new double[]{-1.01, -0.01, 0.01};

        metaSlices[3] = new double[6][];
        metaSlices[3][1] = new double[]{0.30};
        metaSlices[3][2] = new double[]{0.25, 0.50};
        metaSlices[3][3] = new double[]{0.20, 0.40, 0.60};
        metaSlices[3][4] = new double[]{0.15, 0.30, 0.45, 0.60};
        metaSlices[3][5] = new double[]{0.14, 0.28, 0.42, 0.56, 0.70};

        metaSlices[4] = new double[6][];
        metaSlices[4][1] = new double[]{40.0};
        metaSlices[4][2] = new double[]{27.0, 54.0};
        metaSlices[4][3] = new double[]{20.0, 40.0, 60.0};
        metaSlices[4][4] = new double[]{16.0, 32.0, 48.0, 64.0};
        metaSlices[4][5] = new double[]{13.0, 26.0, 40.0, 53.0, 67.0};

        metaSlices[5] = new double[6][];
        metaSlices[5][1] = new double[]{0.99};
        metaSlices[5][2] = new double[]{0.50, 0.999};
        metaSlices[5][3] = new double[]{0.33, 0.66, 0.999};
        metaSlices[5][4] = new double[]{0.25, 0.50, 0.75, 0.999};
        metaSlices[5][5] = new double[]{0.20, 0.40, 0.60, 0.80, 0.999};

        metaSlices[6] = new double[6][];
        metaSlices[6][1] = new double[]{0.99};
        metaSlices[6][2] = new double[]{0.50, 0.999};
        metaSlices[6][3] = new double[]{0.33, 0.66, 0.999};
        metaSlices[6][4] = new double[]{0.25, 0.50, 0.75, 0.999};
        metaSlices[6][5] = new double[]{0.20, 0.40, 0.60, 0.80, 0.999};

        metaIndexStats = MovementBuffer.getIndexStats(metaSlices, bounds);


        buffers[0].add(new MovementBuffer(new int[]{0, 0, 0, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 0));
        buffers[0].add(new MovementBuffer(new int[]{0, 3, 0, 0, 0, 0, 0}, 15, metaSlices, metaIndexStats, 0));
        buffers[0].add(new MovementBuffer(new int[]{0, 4, 0, 0, 0, 0, 0}, 30, metaSlices, metaIndexStats, 0));
        buffers[0].add(new MovementBuffer(new int[]{0, 8, 0, 0, 0, 0, 0}, 40, metaSlices, metaIndexStats, 0));
        buffers[0].add(new MovementBuffer(new int[]{0, 8, 1, 0, 0, 0, 0}, 50, metaSlices, metaIndexStats, 0));
        buffers[0].add(new MovementBuffer(new int[]{0, 4, 0, 0, 0, 2, 0}, 50, metaSlices, metaIndexStats, 0));
        buffers[0].add(new MovementBuffer(new int[]{2, 4, 0, 0, 0, 0, 0}, 45, metaSlices, metaIndexStats, 0));
        buffers[0].add(new MovementBuffer(new int[]{1, 4, 0, 0, 0, 2, 0}, 65, metaSlices, metaIndexStats, 0));

        buffers[1].add(new MovementBuffer(new int[]{0, 0, 0, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 0, 0, 0, 5, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 8, 0, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 1, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 0, 0, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 1));

        buffers[1].add(new MovementBuffer(new int[]{0, 4, 0, 0, 0, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 1, 0, 0, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 0, 0, 0, 5, 3}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{5, 0, 0, 0, 0, 5, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 0, 0, 3, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 0, 2, 0, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 4, 1, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{5, 5, 0, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 4, 0, 0, 4, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 1, 0, 4, 0, 0}, 1, metaSlices, metaIndexStats, 1));

        buffers[1].add(new MovementBuffer(new int[]{0, 3, 1, 0, 0, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 4, 0, 0, 0, 4, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 3, 0, 0, 0, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 3, 0, 0, 1, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 0, 0, 0, 0, 3, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 0, 0, 0, 2, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 0, 0, 0, 2, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 4, 1, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 3, 0, 0, 2, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 0, 1, 0, 2, 0, 0}, 1, metaSlices, metaIndexStats, 1));

        buffers[1].add(new MovementBuffer(new int[]{2, 4, 1, 0, 0, 3, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 4, 0, 0, 0, 4, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{0, 3, 0, 0, 2, 2, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 4, 0, 0, 2, 3, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 0, 1, 0, 0, 3, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 0, 1, 0, 2, 3, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 0, 0, 0, 2, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 0, 0, 2, 0, 4, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 4, 1, 0, 2, 0, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 3, 0, 2, 2, 0, 0}, 1, metaSlices, metaIndexStats, 1));

        buffers[1].add(new MovementBuffer(new int[]{2, 4, 1, 0, 0, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 4, 1, 0, 2, 3, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 3, 1, 2, 0, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 2, 0, 0, 2, 4, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 3, 0, 2, 0, 3, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 0, 1, 0, 2, 3, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 0, 1, 2, 0, 3, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 0, 1, 2, 2, 4, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 0, 0, 2, 2, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 4, 1, 2, 2, 0, 0}, 1, metaSlices, metaIndexStats, 1));

        buffers[1].add(new MovementBuffer(new int[]{2, 4, 1, 0, 2, 4, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 3, 1, 0, 2, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 4, 1, 2, 0, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 2, 1, 2, 0, 4, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{1, 4, 1, 2, 2, 2, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 3, 1, 1, 2, 3, 0}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 3, 0, 2, 2, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 3, 0, 2, 3, 4, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 0, 1, 2, 2, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{1, 0, 1, 3, 1, 4, 1}, 1, metaSlices, metaIndexStats, 1));

        buffers[1].add(new MovementBuffer(new int[]{1, 1, 1, 1, 1, 1, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{1, 1, 1, 1, 1, 2, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{1, 2, 1, 1, 1, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 3, 1, 1, 1, 3, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 3, 1, 2, 2, 4, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{3, 4, 1, 2, 2, 4, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{4, 4, 1, 2, 3, 4, 2}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{1, 2, 1, 3, 3, 1, 1}, 1, metaSlices, metaIndexStats, 1));
        buffers[1].add(new MovementBuffer(new int[]{2, 2, 1, 1, 1, 5, 3}, 1, metaSlices, metaIndexStats, 1));

        buffers[2].add(new MovementBuffer(new int[]{5, 5, 1, 0, 0, 5, 4}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{4, 4, 1, 0, 0, 3, 2}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{4, 3, 1, 0, 0, 2, 0}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{5, 4, 1, 4, 0, 2, 0}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{0, 8, 1, 3, 5, 1, 0}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{5, 5, 1, 4, 0, 5, 3}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{4, 4, 1, 3, 0, 3, 2}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{4, 5, 1, 0, 3, 4, 2}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{3, 4, 1, 0, 2, 3, 2}, 1, metaSlices, metaIndexStats, 2));

        buffers[2].add(new MovementBuffer(new int[]{2, 2, 1, 2, 1, 3, 1}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{2, 3, 1, 2, 2, 4, 2}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{3, 4, 1, 2, 3, 4, 2}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{4, 5, 1, 3, 3, 5, 3}, 1, metaSlices, metaIndexStats, 2));
        buffers[2].add(new MovementBuffer(new int[]{5, 5, 1, 4, 4, 5, 4}, 1, metaSlices, metaIndexStats, 2));

        for (int n = 0; n < 3; n++) {
            Iterator i = buffers[n].iterator();
            while (i.hasNext()) {
                allBuffers.add(i.next());
            }
        }
    }

    public void reset() {
        for (int i = 0; i < 3; i++) {
            waveStats[i].clear();
            maxWeights[i].clear();
        }

        Iterator i = allBuffers.iterator();
        while (i.hasNext()) {
            MovementBuffer b = (MovementBuffer) (i.next());
            b.clearWaveStatsLists();
        }
    }

    public void setBufferTypeWeights() {
        bufferTypeWeights[0] = 1;

        double[] totalWeight = new double[3];
        for (int n = 1; n < 3; n++) {
            Iterator i = buffers[n].iterator();
            while (i.hasNext()) {
                MovementBuffer b = (MovementBuffer) (i.next());
                totalWeight[n] += b.bufferWeight;
            }

            bufferTypeWeights[n] = totalWeight[n] / ((n == 1 ? 3 : 5) * KUtils.sqr(buffers[n].size()));
        }
        bufferTypeWeights[2] = Math.min(bufferTypeWeights[2], 1.5 * buffers[1].size() * bufferTypeWeights[1] / buffers[2].size());
    }

    public void setWaveFeatures(MovementWave w) {
        Iterator i = allBuffers.iterator();
        while (i.hasNext()) {
            MovementBuffer b = (MovementBuffer) (i.next());
            b.setStatList(w);
        }

        setBufferTypeWeights();
        for (int n = 0; n < 3; n++) {
            HashMap currentHits = new HashMap();
            i = buffers[n].iterator();
            while (i.hasNext()) {
                MovementBuffer b = (MovementBuffer) (i.next());
                currentHits = b.getHits(currentHits, w, bufferTypeWeights[n]);
            }
            waveStats[n].put(w, currentHits);
        }
    }

    public void setWaveData(MovementWave w) {
        setBufferTypeWeights();
        for (int n = 0; n < 2; n++) {
            HashMap currentHits = new HashMap();
            Iterator i = buffers[n].iterator();
            while (i.hasNext()) {
                MovementBuffer b = (MovementBuffer) (i.next());
                currentHits = b.getHits(currentHits, w, bufferTypeWeights[n]);
            }
            waveStats[n].put(w, currentHits);
        }
    }

    public void logHit(double GF, MovementWave w) {
        Iterator i = allBuffers.iterator();
        while (i.hasNext()) {
            MovementBuffer b = (MovementBuffer) (i.next());

            b.logHit(GF, w, 1);
        }
    }

    public void logVisit(double GF, MovementWave w) {
        Iterator i = buffers[2].iterator();
        while (i.hasNext()) {
            MovementBuffer b = (MovementBuffer) (i.next());

            b.logHit(GF, w, 0);
        }
    }

    public double getRisk(double GF, int movementMode, MovementWave w) {
        double risk = 0;

        HashMap hits = (HashMap) ((HashMap) waveStats[movementMode - 1]).get(w);
        Iterator i = hits.keySet().iterator();
        while (i.hasNext()) {
            Double key = (Double) (i.next());
            Double entry = (Double) (hits.get(key));

            risk += entry.doubleValue() * KUtils.fourth(2 - Math.abs(GF - key.doubleValue()));
        }

        if (movementMode == 3) {
            hits = (HashMap) ((HashMap) waveStats[1]).get(w);
            ;
            i = hits.keySet().iterator();
            while (i.hasNext()) {
                Double key = (Double) (i.next());
                Double entry = (Double) (hits.get(key));

                risk += entry.doubleValue() * KUtils.fourth(2 - Math.abs(GF - key.doubleValue()));
            }
        }

        return risk;
    }

    public double getRisk(double[] window, int movementMode, MovementWave w) {
        double risk = 0;

        HashMap hits = (HashMap) ((HashMap) waveStats[movementMode - 1]).get(w);
        Iterator i = hits.keySet().iterator();
        while (i.hasNext()) {
            Double key = (Double) (i.next());
            Double entry = (Double) (hits.get(key));
            double minDiff = Math.max(window[0] - key.doubleValue(), key.doubleValue() - window[1]);

            risk += entry.doubleValue() * 0.8 / Math.max(minDiff + 0.01, 0.02);
        }

        if (movementMode == 3) {
            hits = (HashMap) ((HashMap) waveStats[1]).get(w);
            ;
            i = hits.keySet().iterator();
            while (i.hasNext()) {
                Double key = (Double) (i.next());
                Double entry = (Double) (hits.get(key));
                double minDiff = Math.max(window[0] - key.doubleValue(), key.doubleValue() - window[1]);

                risk += entry.doubleValue() * 0.8 / Math.max(minDiff + 0.01, 0.02);
            }
        }

        return risk;
    }

    public void printStats() {
        /*setBufferTypeWeights();
          double normalWeight = bufferTypeWeights[1] * buffers[1].size();
          double flattenerWeight = bufferTypeWeights[2] * buffers[2].size();
          System.out.println("Flattener Weight Percentage: " + (float)(100 * flattenerWeight / (normalWeight + flattenerWeight)) + "%");*/
        /*double maxWeight = 0;
          Iterator i = allBuffers.iterator();
          while(i.hasNext()) {
              MovementBuffer b = (MovementBuffer)(i.next());
              if(b.bufferWeight > maxWeight) {
                  maxWeight = b.bufferWeight;
              }
          }
          i = allBuffers.iterator();
          while(i.hasNext()) {
              MovementBuffer b = (MovementBuffer)(i.next());
              if(buffers[0].contains(b) || buffers[1].contains(b)) {
                  continue;
              }
              System.out.println((int)Math.round(1000 * b.bufferWeight / maxWeight));
          }*/
    }

    public void paint(MovementWave w, int movementMode, java.awt.Graphics2D g) {
    }

    public class MovementBuffer extends AABuffer {
        boolean dynamicWeighting = true;

        HashMap[][][][][][][] hitStats;
        double minHitWeight;

        int bufferType;
        double rolling;
        double bufferWeight;
        double scoreTotal;
        double weightTotal;

        Double lastHitGF;
        Wave lastHitWave;
        boolean repeatWave;

        public MovementBuffer(int[] sliceIndexes, double bufferWeight, double[][][] metaSlices, double[][][][] metaIndexStats, int bufferType) {
            super(sliceIndexes, metaSlices, metaIndexStats);
            hitStats = new HashMap[slices[0].length + 1]
                    [slices[1].length + 1]
                    [slices[2].length + 1]
                    [slices[3].length + 1]
                    [slices[4].length + 1]
                    [slices[5].length + 1]
                    [slices[6].length + 1];

            this.bufferType = bufferType;

            if (bufferType == 0) {
                dynamicWeighting = false;
                this.bufferWeight = bufferWeight;
                rolling = 0;
            } else {
                dynamicWeighting = true;
                this.bufferWeight = 1.0e-50;

                double totalStatLists = 1;
                for (int i = 0; i < DIMENSIONS; i++) {
                    totalStatLists *= slices[i].length + 1;
                }

                rolling = Math.pow(bufferType == 1 ? 0.97 : 0.99, Math.sqrt(totalStatLists));
            }
            minHitWeight = 0.002;
        }

        public void setStatList(ArrayList statList, int[] currentIndexes, double weight) {
            HashMap currentHitStats = hitStats[currentIndexes[0]]
                    [currentIndexes[1]]
                    [currentIndexes[2]]
                    [currentIndexes[3]]
                    [currentIndexes[4]]
                    [currentIndexes[5]]
                    [currentIndexes[6]];
            if (currentHitStats == null) {
                currentHitStats = hitStats[currentIndexes[0]]
                        [currentIndexes[1]]
                        [currentIndexes[2]]
                        [currentIndexes[3]]
                        [currentIndexes[4]]
                        [currentIndexes[5]]
                        [currentIndexes[6]] = new HashMap();
            }
            statList.add(currentHitStats);
        }

        public void logHit(double GF, Wave w, double waveWeight) {
            if (w == lastHitWave) {
                repeatWave = true;
            } else {
                repeatWave = false;
            }

            super.logHit(GF, w, waveWeight);

            lastHitWave = w;
        }

        public void logHit(double GF, ArrayList statList, double waveWeight) {
            Double GFKey = new Double(GF);

            for (int i = 0; i < statList.size(); i += 2) {
                HashMap currentHits = (HashMap) (statList.get(i));
                double weight = ((Double) statList.get(i + 1)).doubleValue();
                logHit(GFKey, currentHits, weight, waveWeight);
            }

            lastHitGF = GFKey;
        }

        public void logHit(Double GFKey, HashMap hitStats, double weight, double waveWeight) {
            if (dynamicWeighting && waveWeight != 0) {
                double GF = GFKey.doubleValue();
                double weightMod = 1;

                Iterator i = hitStats.keySet().iterator();
                while (i.hasNext()) {
                    Double key = (Double) (i.next());
                    Double entry = (Double) (hitStats.get(key));
                    if (repeatWave && key == lastHitGF) {
                        continue;
                    }

                    scoreTotal += weight * entry.doubleValue() * KUtils.eighth(2 - Math.abs(GF - key.doubleValue()));
                    weightTotal += weight * entry.doubleValue();
                }

                if (weightTotal != 0) {
                    bufferWeight = (bufferType == 1 ? 1 : 1) * KUtils.eighth(scoreTotal / weightTotal);
                }
            }
            if (bufferType == 2 && waveWeight != 0) {
                return;
            }

            ArrayList toBeRemoved = new ArrayList();

            weight *= 1 - rolling;
            double currentRolling = 1 - weight;
            if (rolling != 0) {
                Iterator i = hitStats.keySet().iterator();
                while (i.hasNext()) {
                    Double key = (Double) (i.next());

                    Double oldValue = (Double) (hitStats.get(key));
                    double newValue = oldValue.doubleValue() * currentRolling;

                    if (newValue > minHitWeight) {
                        hitStats.put(key, new Double(newValue));
                    } else {
                        toBeRemoved.add(key);
                    }
                }
            }

            Iterator i = toBeRemoved.iterator();
            while (i.hasNext()) {
                hitStats.remove((Double) (i.next()));
            }

            hitStats.put(GFKey, new Double(weight));
        }

        public HashMap getHits(HashMap hits, Wave w, double weightMod) {
            Double currentMaxWeight;
            currentMaxWeight = (Double) (maxWeights[bufferType].get(w));

            double max = 0;
            if (currentMaxWeight != null) {
                max = currentMaxWeight.doubleValue();
            }

            if (!waveStatLists.containsKey(w)) {
                setStatList(w);
            }
            ArrayList statList = (ArrayList) waveStatLists.get(w);

            for (int i = 0; i < statList.size(); i += 2) {
                HashMap currentHits = (HashMap) (statList.get(i));
                double weight = ((Double) statList.get(i + 1)).doubleValue();

                Iterator i2 = currentHits.keySet().iterator();
                while (i2.hasNext()) {
                    Double key = (Double) (i2.next());

                    Double currentWeight = (Double) (currentHits.get(key));
                    Double addedWeight = new Double(currentWeight.doubleValue() * weight);
                    Double newWeight;
                    if (!hits.containsKey(key)) {
                        newWeight = new Double(weightMod * bufferWeight * addedWeight.doubleValue());
                    } else {
                        Double initialWeight = (Double) (hits.get(key));
                        newWeight = new Double(initialWeight.doubleValue() + (weightMod * bufferWeight * addedWeight.doubleValue()));
                    }
                    hits.put(key, newWeight);

                    if (newWeight.doubleValue() > max) {
                        max = newWeight.doubleValue();
                        maxWeights[bufferType].put(w, newWeight);
                    }
                }
            }

            return hits;
        }
    }
}