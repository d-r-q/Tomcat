package kc.serpent.gun;

import kc.serpent.utils.*;

import java.util.ArrayList;
import java.util.Iterator;

public class VCSGunStandard implements GunSystem {
    static final double NONREAL_WAVE_WEIGHT = 0.25;

    ArrayList buffers = new ArrayList();

    public void init(GunBase g) {
        double[][][] metaSlices = new double[GunBuffer.DIMENSIONS][][];
        double[][][][] metaIndexStats = new double[GunBuffer.DIMENSIONS][][][];
        double[][] bounds = new double[GunBuffer.DIMENSIONS][];

        bounds[0] = new double[]{0.0, GunBase.MAX_DISTANCE};
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

        metaSlices[1] = new double[6][];
        metaSlices[1][1] = new double[]{4.5};
        metaSlices[1][2] = new double[]{2.5, 5.5};
        metaSlices[1][3] = new double[]{1.5, 4.5, 6.5};
        metaSlices[1][4] = new double[]{1.5, 3.5, 5.5, 7.5};
        metaSlices[1][5] = new double[]{0.5, 2.5, 4.5, 6.5, 7.5};

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
        metaSlices[5][1] = new double[]{0.50};
        metaSlices[5][2] = new double[]{0.33, 0.66};
        metaSlices[5][3] = new double[]{0.33, 0.66, 0.999};
        metaSlices[5][4] = new double[]{0.25, 0.50, 0.75, 0.999};
        metaSlices[5][5] = new double[]{0.20, 0.40, 0.60, 0.80, 0.999};

        metaSlices[6] = new double[6][];
        metaSlices[6][1] = new double[]{0.50};
        metaSlices[6][2] = new double[]{0.33, 0.66};
        metaSlices[6][3] = new double[]{0.33, 0.66, 0.999};
        metaSlices[6][4] = new double[]{0.25, 0.50, 0.75, 0.999};
        metaSlices[6][5] = new double[]{0.20, 0.40, 0.60, 0.80, 0.999};

        metaIndexStats = GunBuffer.getIndexStats(metaSlices, bounds);

        buffers.add(new GunBuffer(new int[]{0, 0, 0, 0, 0, 0, 0}, 1, metaSlices, metaIndexStats));

        buffers.add(new GunBuffer(new int[]{0, 0, 1, 0, 0, 3, 0}, 40, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 0, 0, 0, 0, 3, 1}, 28, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{2, 3, 0, 0, 0, 0, 0}, 17, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 0, 0, 0, 0, 3, 0}, 27, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 4, 1, 0, 0, 0, 0}, 25, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{5, 0, 1, 0, 0, 0, 0}, 26, metaSlices, metaIndexStats));

        buffers.add(new GunBuffer(new int[]{3, 3, 1, 0, 0, 0, 0}, 29, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 0, 1, 0, 0, 3, 0}, 46, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 4, 1, 3, 0, 0, 0}, 28, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 0, 2, 0, 0, 4, 2}, 53, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 4, 2, 0, 3, 0, 0}, 27, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{4, 0, 0, 2, 0, 4, 3}, 44, metaSlices, metaIndexStats));

        buffers.add(new GunBuffer(new int[]{3, 0, 1, 0, 0, 4, 1}, 55, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 4, 1, 3, 3, 0, 0}, 29, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{2, 5, 2, 0, 5, 0, 0}, 31, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{4, 0, 1, 4, 0, 3, 0}, 53, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 3, 0, 0, 0, 5, 4}, 53, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{5, 0, 0, 3, 3, 3, 1}, 45, metaSlices, metaIndexStats));

        buffers.add(new GunBuffer(new int[]{5, 2, 1, 0, 0, 5, 4}, 86, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{4, 0, 1, 0, 5, 3, 1}, 61, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 3, 2, 4, 0, 5, 4}, 81, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 4, 2, 2, 4, 2, 0}, 56, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{5, 0, 0, 4, 1, 4, 2}, 51, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 5, 1, 2, 3, 3, 0}, 56, metaSlices, metaIndexStats));

        buffers.add(new GunBuffer(new int[]{5, 4, 2, 3, 3, 3, 0}, 71, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 3, 1, 3, 0, 4, 2}, 78, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{4, 4, 1, 0, 4, 3, 1}, 70, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{4, 4, 0, 2, 2, 4, 3}, 72, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 0, 2, 4, 4, 2, 1}, 57, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{0, 5, 2, 5, 4, 4, 3}, 82, metaSlices, metaIndexStats));

        buffers.add(new GunBuffer(new int[]{2, 3, 1, 1, 1, 2, 1}, 56, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{2, 3, 1, 1, 2, 4, 3}, 78, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 3, 1, 4, 2, 3, 2}, 76, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{3, 4, 2, 2, 3, 3, 1}, 72, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{4, 4, 2, 3, 3, 4, 3}, 92, metaSlices, metaIndexStats));
        buffers.add(new GunBuffer(new int[]{4, 5, 2, 4, 4, 5, 4}, 100, metaSlices, metaIndexStats));
    }

    public void reset() {
        Iterator i = buffers.iterator();
        while (i.hasNext()) {
            GunBuffer b = (GunBuffer) (i.next());
            b.waveStatLists.clear();
        }
    }

    public double getFiringAngle(GunWave w) {
        double[] bins = new double[GunBuffer.AIM_FACTORS];

        Iterator i = buffers.iterator();
        while (i.hasNext()) {
            GunBuffer b = (GunBuffer) (i.next());
            bins = b.getBins(bins, w);
        }

        int bestFactor = GunBuffer.MIDDLE_FACTOR;
        for (int n = 0; n < GunBuffer.AIM_FACTORS; n++) {
            if (bins[n] > bins[bestFactor]) {
                bestFactor = n;
            }
        }

        double bestGF = KUtils.toGF(bestFactor, GunBuffer.AIM_FACTORS);

        return w.absoluteBearing + (bestGF * w.orbitDirection * w.maxEscapeAngle);
    }

    public void wavePassed(double GF, GunWave w) {
        Iterator i = buffers.iterator();
        while (i.hasNext()) {
            GunBuffer b = (GunBuffer) (i.next());
            b.logHit(GF, w, w.isReal ? 1 : NONREAL_WAVE_WEIGHT);
        }
    }

    public void printStats() {
        /*double maxWeight = 0;
          Iterator i = buffers.iterator();
          while(i.hasNext()) {
              GunBuffer b = (GunBuffer)(i.next());
              if(b.bufferWeight > maxWeight) {
                  maxWeight = b.bufferWeight;
              }
          }
          i = buffers.iterator();
          while(i.hasNext()) {
              GunBuffer b = (GunBuffer)(i.next());
              System.out.println((int)Math.round(1000 * b.bufferWeight / maxWeight));
          }*/
    }

    public String getName() {
        return "main gun";
    }

    public class GunBuffer extends VCSAABuffer {
        {
            AIM_FACTORS = 31;
            MIDDLE_FACTOR = (AIM_FACTORS - 1) / 2;
            SMOOTHING_WINDOW = 6;
            SMOOTHING = 0.3;
            dynamicWeighting = true;
        }

        public GunBuffer(int[] sliceIndexes, double bufferWeight, double[][][] metaSlices, double[][][][] metaIndexStats) {
            super(sliceIndexes, metaSlices, metaIndexStats);
            setHitStats();

            if (dynamicWeighting) {
                this.bufferWeight = 1.0e-50;
            } else {
                this.bufferWeight = bufferWeight;
            }

            rolling = 0;
            desiredWeight = 10;
        }
    }
}
		