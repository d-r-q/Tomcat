package lxx.targeting.mg2;

/**
 * User: jdev
 * Date: 21.02.2010
 */
public class MegaGun2 /*implements Predictor, WaveCallback*/ {

    /*private final Map<Wave, Integer[]> fireSituation = new HashMap<Wave, Integer[]>();

    private final static List<Integer[]> fireSituations = new ArrayList<Integer[]>();
    private final Map<String, List<Integer[]>> segmentedFS = new HashMap<String, List<Integer[]>>();
    private final static List<Segmentation> segmentationAttributes = new ArrayList<Segmentation>();

    private final WaveManager waveManager;
    private final SegmentationsManager segmentationsManager;
    private static int segmentationCount = 1;
    private static boolean isNeedResegment = false;
    private List<Segmentation> bestSegmentations;

    public MegaGun2(WaveManager waveManager, SegmentationsManager segmentationsManager) {
        this.waveManager = waveManager;
        this.segmentationsManager = segmentationsManager;

        bestSegmentations = segmentationsManager.getBestSegmentations(segmentationCount);
        if (fireSituations.size() > 0) {
            if (bestSegmentations.size() == segmentationAttributes.size() && bestSegmentations.equals(segmentationAttributes)) {
                return;
            }
            /*for (Integer[] fs : fireSituations) {
                StringBuffer key = new StringBuffer(".");
                List<Integer[]> fses = segmentedFS.get(key.toString());
                if (fses == null) {
                    fses = new ArrayList<Integer[]>();
                    segmentedFS.put(key.toString(), fses);
                }
                fses.add(fs);
                for (Segmentation seg : bestSegmentations) {
                    key.append(seg.getSegmentIdx(fs[SegmentationsManager.getSegmentationIdx(seg)])).append('.');
                    fses = segmentedFS.get(key.toString());
                    if (fses == null) {
                        fses = new ArrayList<Integer[]>();
                        segmentedFS.put(key.toString(), fses);
                    }
                    fses.add(fs);
                }
            }
        }
        System.out.println("Fire situations segmentations count: " + segmentationCount);
        isNeedResegment = false;
    }

    public Double predictAngle(Target t) {
        if (fireSituations.size() == 0) {
            return null;
        }

        Integer[] fireSituation = segmentationsManager.getFireSituation(t);
        final int segmentationsCount = SegmentationsManager.getSegmentationsCount();
        double factors[] = new double[segmentationsCount + 2];
        double scales[] = new double[segmentationsCount + 2];

        double minGF = -LXXConstants.MAX_GUESS_FACTOR;
        double maxGF = LXXConstants.MAX_GUESS_FACTOR;
        final List<Segmentation> segs = bestSegmentations;
        for (int i = 0; i < segmentationsCount; i++) {
            Segmentation segmentation = SegmentationsManager.getSegmentation(i);
            /*final AttributeValue av = segmentation.getAttributeValue(fireSituation[i]);
            if (av != null) {
                double me = av.getMathExpection();
                double sko = av.getSrKVOtkl();
                if (me - sko * 4 > minGF) {
                    minGF = (int) (me - sko * 4);
                }

                me = av.getMathExpection();
                if (me + sko * 4 < maxGF) {
                    maxGF = (int) (me + sko * 4);
                }

            }
            factors[i] = 1;

            int domainSize = segmentation.getMaxAttrValue() - segmentation.getMinAttrValue();
            if (domainSize == 0) {
                domainSize = 1;
            }
            scales[i] = 1000 / domainSize;
        }

        List<Integer[]> fses;
        if (segmentedFS.size() == 0) {
            fses = fireSituations;
        } else {
            fses = getSubFiresituations(fireSituation, segs);
        }
        List<Double[]> gfes = new ArrayList<Double[]>();
        for (Integer[] fs : fses) {
            Integer gf = fs[fs.length - 1];
            /*if (gf < minGF || gf > maxGF) {
                continue;
            }
            double dist = Utils.factoredManhettanDistance(fireSituation, fs, factors, scales);
            if (gfes.size() < 3 || dist < gfes.get(2)[1]) {
                int i = gfes.size() - 1;
                for (; i >= 0; i--) {
                    if (gfes.get(i)[1] < dist) {
                        break;
                    }
                }
                gfes.add(i + 1, new Double[]{gf.doubleValue(), dist});
                if (gfes.size() > 3) {
                    gfes.remove(3);
                }

            }
        }
        int minDistGF;
        if (gfes.size() == 0) {
            minDistGF = 0;
        } else if (gfes.size() < 3) {
            minDistGF = gfes.get(0)[0].intValue();
        } else if ((int) abs(gfes.get(0)[1] - gfes.get(1)[1]) < (int) abs(gfes.get(2)[1] - gfes.get(1)[1])) {
            minDistGF = gfes.get(0)[0].intValue();
        } else if ((int) abs(gfes.get(0)[1] - gfes.get(1)[1]) == (int) abs(gfes.get(2)[1] - gfes.get(1)[1])) {
            minDistGF = gfes.get(1)[0].intValue();
        } else {
            minDistGF = gfes.get(2)[0].intValue();
        }

        final double bearing = toRadians(ceil(abs(toDegrees(t.maxEscapeAngle(minDistGF >= 0 ? 1 : -1))) * minDistGF / LXXConstants.MAX_GUESS_FACTOR));
        return robot.angleTo(t) + bearing;
    }

    private List<Integer[]> getSubFiresituations(Integer[] fireSituation, List<Segmentation> segs) {
        List<Integer[]> fses;
        StringBuffer key = new StringBuffer(".");
        fses = segmentedFS.get(key.toString());
        for (Segmentation seg : segs) {
            key.append(seg.getSegmentIdx(fireSituation[SegmentationsManager.getSegmentationIdx(seg)])).append('.');
            List<Integer[]> subFses = segmentedFS.get(key.toString());
            if (subFses == null || subFses.size() < 5000) {
                break;
            }
            fses = subFses;
        }
        return fses;
    }

    public void paint(Graphics2D g) {
    }

    public void onRoundStarted() {
    }

    public String getName() {
        return "MegaGun_v2";
    }

    public void targetUpdated(Target oldState, Target newState, robocode.Event source) {
        Integer[] fs = segmentationsManager.getFireSituation(newState);

        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), Rules.getBulletSpeed(robot.firePower()), this);
        fireSituation.put(w, fs);
    }

    public void waveBroken(Wave w) {
        Integer[] fs = fireSituation.get(w);
        if (fs == null) {
            return;
        }
        Double angle = Utils.angle(w.sourcePos, w.target) - Utils.angle(w.sourcePos, w.targetPos);
        if (abs(angle) > Math.PI) {
            angle -= Math.PI * 2 * signum(angle);
        }
        final double maxEscapeAnglePos = fs[fs.length - 1];
        final double maxEscapeAngleNeg = fs[fs.length - 2];
        if ((angle >= 0 && toDegrees(angle) > maxEscapeAnglePos) || (angle < 0 && toDegrees(angle) < -maxEscapeAngleNeg)) {
            return;
        }

        if (angle > 0) {
            fs[fs.length - 1] = (int) ((round(toDegrees(angle)) / maxEscapeAnglePos) * LXXConstants.MAX_GUESS_FACTOR);
        } else {
            fs[fs.length - 1] = (int) ((round(toDegrees(angle)) / maxEscapeAngleNeg) * LXXConstants.MAX_GUESS_FACTOR);
        }
        fireSituations.add(fs);

        StringBuffer key = new StringBuffer(".");
        List<Integer[]> fses = segmentedFS.get(key.toString());
        if (fses == null) {
            fses = new ArrayList<Integer[]>();
            segmentedFS.put(key.toString(), fses);
        }
        fses.add(fs);
        int idx = 0;
        for (Segmentation seg : bestSegmentations) {
            key.append(seg.getSegmentIdx(fs[SegmentationsManager.getSegmentationIdx(seg)])).append('.');
            fses = segmentedFS.get(key.toString());
            if (fses == null) {
                fses = new ArrayList<Integer[]>();
                segmentedFS.put(key.toString(), fses);
            }
            fses.add(fs);
            if (fses.size() > 5000 && !isNeedResegment && idx++ == bestSegmentations.size() - 1) {
                segmentationCount++;
                isNeedResegment = true;
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MegaGun2 megaGun2 = (MegaGun2) o;

        return getName().equals(megaGun2.getName());

    }

    public int hashCode() {
        return getName().hashCode();
    }         */
}
