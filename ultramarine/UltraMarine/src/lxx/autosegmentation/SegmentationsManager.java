package lxx.autosegmentation;

/**
 * User: jdev
 * Date: 23.02.2010
 */
public class SegmentationsManager/* implements TargetManagerListener, WaveCallback*/ {

    /*private final Map<Wave, Integer[]> fireSituation = new HashMap<Wave, Integer[]>();
    private final WaveManager waveManager;

    public static final Attribute lateralVelocityAttr = new Attribute("Lateral velocity", -4, 4, new LateralVelocityVE());
    public static final Attribute enemyTravelTimeAttr = new Attribute("Enemy travel time", 0, 255, new EnemyTravelTimeVE());
    public static final Attribute distBetweenAttr = new Attribute("Distance between", 0, 100, new DistanceBetweenVE());
    // todo: fix me
    public static final Attribute distToHOWallAttr = new Attribute("Heading wall distance", 0, 80, new HOWallDistVE());
    public static final Attribute distToCenterAttr = new Attribute("Distance to center", 0, 72, new DistToCenterVE());
    public static final Attribute lastVisitedGFAttr = new Attribute("Enemy last visited gf", -LXXConstants.MAX_GUESS_FACTOR, LXXConstants.MAX_GUESS_FACTOR, new EnemyLastGFVE());

    public static final Attribute enemyVelocityAttr = new Attribute("Enemy velocity", -8, 8, new EnemyVelocityVE());
    public static final Attribute bulletFlightTime = new Attribute("Bullet flight time", 0, 80, new BulletFlightTimeVE());
    public static final Attribute angleToTargetAttr = new Attribute("Angle to target", 0, 18, new AngleToTargetVE());
    public static final Attribute enemyHeadingAttr = new Attribute("Enemy heading", 0, 18, new EnemyHeadingVE());
    public static final Attribute enemyXAttr = new Attribute("Enemy x", 0, 80, new EnemyXVE());
    public static final Attribute enemyYAttr = new Attribute("Enemy y", 0, 60, new EnemyYVE());

    public static final Attribute gunBearing = new Attribute("Gun bearing", -18, 18, new GunBearingVE());
    public static final Attribute enemyStopTime = new Attribute("Enemy stop time", 0, 255, new EnemyStopTimeVE());

    public static final Attribute timeSinceMyLastFire = new Attribute("Time since my last fire", 0, 21, new TimeSinceMyLastFireVE());
    public static final Attribute timeSinceLastLatVelChange = new Attribute("Time since lateral velocity dir change", 0, 255, new TimeSinceLateralVelocityDirChangeVE());
    private static final Segmentation[] segmentations = {
            new Segmentation(angleToTargetAttr),
            new Segmentation(new Attribute("Avg bullet bearing1", -45, 46, new AvgBearing1VE())),
            new Segmentation(new Attribute("Avg bullet bearing2", -45, 46, new AvgBearing2VE())),
            new Segmentation(new Attribute("Bearing to closest wall", -90, 90, new BearingToClosestWallVE())),
            new Segmentation(distBetweenAttr),
            new Segmentation(distToCenterAttr),
            new Segmentation(enemyHeadingAttr),
            new Segmentation(enemyStopTime),
            new Segmentation(enemyTravelTimeAttr),
            new Segmentation(gunBearing),
            new Segmentation(distToHOWallAttr),
            new Segmentation(timeSinceMyLastFire),
            new Segmentation(lateralVelocityAttr),
            new Segmentation(timeSinceLastLatVelChange),
            new Segmentation(bulletFlightTime),
            // todo fix me
            new Segmentation(enemyXAttr),
            new Segmentation(enemyYAttr),
            new Segmentation(enemyVelocityAttr),
            new Segmentation(new Attribute("Enemy acceleration", -1, 2, new EnemyAccelerationVE())),
            new Segmentation(new Attribute("Enemy turn rate", -10, 10, new EnemyTurnRateVE())),
            new Segmentation(lastVisitedGFAttr),
            new Segmentation(new Attribute("First bullet flight time", 0, 21, new LateralAccelerationVE.FirstBulletFlightTimeVE())),
            new Segmentation(new Attribute("Last bullet flight time", 0, 21, new LateralAccelerationVE.LastBulletFlightTimeVE())),
            new Segmentation(new Attribute("Fire power", 0, 3, new FirePowerVE())),
            new Segmentation(new Attribute("First bullet bearing", -18, 19, new FistBulletBearingVE())),
            new Segmentation(new Attribute("Last bullet bearing", -18, 19, new LastBulletBearingVE())),
            new Segmentation(new Attribute("Dist traveled on last wave", 0, 100, new DistTravelledLastWaveVE())),
            new Segmentation(new Attribute("Time since last hit", 0, 255, new LastHitTimeVE())),
            new Segmentation(new Attribute("Dist to closest wall", 0, 50, new DistToClosestWallVE())),
    };
    private BulletManager bulletManager;
    private RobocodeFileWriter fileWriter;
    private final TargetManager targetManager;
    private List<Segmentation> sortedSegmenations;

    public SegmentationsManager(WaveManager waveManager, BulletManager bulletManager, TargetManager targetManager) {
        this.waveManager = waveManager;
        this.bulletManager = bulletManager;
        this.targetManager = targetManager;

        for (Segmentation s : segmentations) {
            int attrCount = s.getAttributeValues().size();
            s.resegment();
            if (attrCount != s.getAttributeValues().size()) {
                throw new RuntimeException("Something wrong: " + s.getAttributeName() + "'s attribute_extractors count was " +
                        attrCount + ", and become after resplit " + s.getAttributeValues().size());
            }
        }

        sortedSegmenations = new ArrayList(Arrays.asList(segmentations));
        Collections.sort(sortedSegmenations, new Comparator<Segmentation>() {

            public int compare(Segmentation o1, Segmentation o2) {
                return getSegmentationWeight(o1) > getSegmentationWeight(o2) ? -1 : 1;
            }
        });
    }


    public void targetUpdated(Target oldState, Target newState, Event source) {
        Integer[] fs = getFireSituation(newState);

        Wave w = waveManager.launchWave(robot, newState, robot.getTime(), robot.angleTo(newState), Rules.getBulletSpeed(robot.firePower()), this);
        fireSituation.put(w, fs);
    }

    public void waveBroken(Wave w) {
        if (robot.getOthers() == 0) {
            return;
        }
        Integer[] fs = fireSituation.get(w);
        if (fs == null) {
            return;
        }
        Double angle = Utils.angle(w.sourcePos, w.target) - Utils.angle(w.sourcePos, w.targetPos);
        if (abs(angle) > Math.PI) {
            angle -= Math.PI * 2 * signum(angle);
        }
        final double maxEscapeAngleNeg = fs[fs.length - 2];
        final double maxEscapeAnglePos = fs[fs.length - 1];
        if ((angle >= 0 && toDegrees(angle) > maxEscapeAnglePos) || (angle < 0 && toDegrees(angle) < -maxEscapeAngleNeg)) {
            return;
        }

        int gf = 0;
        for (int i = 0; i < segmentations.length; i++) {
            if (angle > 0) {
                gf = (int) (toDegrees(angle) / maxEscapeAnglePos * LXXConstants.MAX_GUESS_FACTOR);
                segmentations[i].addEntry(fs[i], gf);
            } else {
                gf = (int) (toDegrees(angle) / maxEscapeAngleNeg * LXXConstants.MAX_GUESS_FACTOR);
                segmentations[i].addEntry(fs[i], gf);
            }
        }

        ((Target) w.getTarget()).setLastVisitedGF(gf);
        ((Target) w.getTarget()).setDistTravelledLastWave(w.getSourcePos().aDistance(w.getTarget().getPosition()));
        //writeEntry(fs);
    }

    public Integer[] getFireSituation(Target target) {
        Integer[] fs = new Integer[segmentations.length + 2];
        for (int i = 0; i < segmentations.length; i++) {
            fs[i] = segmentations[i].getExtractor().getAttributeValue(target, robot, bulletManager);
        }
        fs[fs.length - 2] = (int) abs(ceil(toDegrees(target.maxEscapeAngle(-1))));
        fs[fs.length - 1] = (int) ceil(toDegrees(target.maxEscapeAngle(1)));
        return fs;
    }

    public FireSituation getFireSituation0(Target t) {
        Map<Attribute, Integer> fsAttributes = new TreeMap<Attribute, Integer>(new Comparator<Attribute>() {

            public int compare(Attribute o1, Attribute o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (int i = 0; i < segmentations.length; i++) {
            final Attribute a = segmentations[i].getAttribute();
            final int av = segmentations[i].getExtractor().getAttributeValue(t, robot, bulletManager);
            if (av < a.getMinValue() || av > a.getMaxValue()) {
                throw new RuntimeException(a + " = " + av);
            }
            fsAttributes.put(a, av);
        }

        return null*//*new FireSituation((int)-ceil(toDegrees(t.maxEscapeAngle(-1))),
                (int)ceil(toDegrees(t.maxEscapeAngle(-1))), 0, fsAttributes)*//*;
    }

    private void writeEntry(Integer[] fireSituations) {
        if (fileWriter == null) {
            return;
        }

        try {
            for (int i = 0; i < fireSituations.length; i++) {
                fileWriter.write(fireSituations[i] + ";");
            }
            fileWriter.write("\n");
            fileWriter.flush();
        } catch (IOException e) {
            try {
                fileWriter.close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }

            fileWriter = null;
        }
    }

    public static int getSegmentationsCount() {
        return segmentations.length;
    }

    public static Segmentation getSegmentation(int i) {
        return segmentations[i];
    }

    public void paint(Graphics2D g) {
        if (targetManager.getTargets().size() != 1) {
            return;
        }


        final int graphCount = Math.min(8, segmentations.length);
        java.util.List<Segmentation> segs = getBestSegmentations(graphCount);
        int height = (int) (robot.getBattleFieldHeight() / graphCount);
        int width = height * 2;

        final Target target = targetManager.getTargets().values().iterator().next();
        for (int i = 0; i < graphCount && i < segs.size(); i++) {
            segs.get(i).paint(g, width, height, 0, i * height, bulletManager, robot, target);
        }

    }

    public Segmentation getBestSegmentation() {
        java.util.List<Segmentation> segs = getSortedSegmentations();
        return segs.get(0);
    }

    public java.util.List<Segmentation> getBestSegmentations(int count) {
        java.util.List<Segmentation> segs = getSortedSegmentations();

        return segs.subList(0, min(count, segs.size()));
    }

    public java.util.List<Segmentation> getSortedSegmentations() {
        return sortedSegmenations;
    }

    public double getSegmentationWeight(Segmentation o1) {
        double sko = o1.getSegmentsSrKvOtkl();
        if (o1.getSegmentsIntersection() == 0 || sko == 0) {
            return 1000;
        }
        return o1.getSegmentsMESum() * o1.getSegmentsMEDiff() / (o1.getSegmentsIntersection() * sko)*//* * o1.getPlotnost()*//*;
    }

    public static int getSegmentationIdx(Segmentation s) {
        for (int i = 0; i < segmentations.length; i++) {
            if (s.equals(segmentations[i])) {
                return i;
            }
        }

        return -1;
    }

    public static Segmentation getSegmentation(Attribute a) {
        for (Segmentation s : segmentations) {
            if (s.getAttribute().equals(a)) {
                return s;
            }
        }

        return null;
    }

    public int getMaxGuessFator(Target target, UltraMarine robot, BulletManager bulletManager) {
        int maxGF = LXXConstants.MAX_GUESS_FACTOR;
        for (Segmentation s : segmentations) {
            AttributeValue av = s.getAttributeValue(s.getExtractor().getAttributeValue(target, robot, bulletManager));
            if (av == null) {
                continue;
            }

            double me = av.getMathExpection();
            double sko = av.getSrKVOtkl();
            if (me + sko * 3 < maxGF) {
                maxGF = (int) (me + sko * 3);
            }
        }
        return maxGF;
    }

    public int getMinGuessFator(Target target, UltraMarine robot, BulletManager bulletManager) {
        int minGF = -LXXConstants.MAX_GUESS_FACTOR;
        for (Segmentation s : segmentations) {
            AttributeValue av = s.getAttributeValue(s.getExtractor().getAttributeValue(target, robot, bulletManager));
            if (av == null) {
                continue;
            }

            double me = av.getMathExpection();
            double sko = av.getSrKVOtkl();
            if (me - sko * 3 > minGF) {
                minGF = (int) (me - sko * 3);
            }
        }
        return minGF;
    }

    public static int getSegmentationIdx(Attribute attribute) {
        int idx = 0;
        for (Segmentation s : segmentations) {
            if (s.getExtractor().equals(attribute.getExtractor())) {
                return idx;
            }
        }

        return -1;
    }

    public static Segmentation[] getSegmentations() {
        return segmentations;
    }*/
}
