package lxx.targeting.mg4;

import lxx.autosegmentation.model.FireSituation;
import lxx.autosegmentation.model.Attribute;
import lxx.autosegmentation.AttributeFactory;
import lxx.targeting.mg4.clusterezation.Clusterezation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * User: jdev
 * Date: 17.05.2010
 */
public class Test2 {

    public static void main(String[] args) {
        try {
            AttributeFactory attributeFactory = new AttributeFactory(null);
            BufferedReader reader = new BufferedReader(new FileReader("E:\\lexx\\games\\rc\\robots\\lxx\\UltraMarine.data\\walls.dat"));

            String line;
            Map<DeltaVector, Map<Attribute, Clusterezation>> dvClusterezations = new HashMap<DeltaVector, Map<Attribute, Clusterezation>>();
            while ((line = reader.readLine()) != null) {
                String[] attrValues = line.split(";");
                final Attribute[] attributes = attributeFactory.getAttributes();
                if (attrValues.length < attributes.length + 2) {
                    continue;
                }
                int alpha = new Double(attrValues[attrValues.length - 2]).intValue();
                int dist = new Integer(attrValues[attrValues.length - 1]);

                int[] fsAttributes = new int[attributes.length];

                for (Attribute a : attributeFactory.getAttributes()) {
                    final int attrValue = Integer.parseInt(attrValues[a.getId()]);
                    fsAttributes[a.getId()] = attrValue;
                    if (attrValue > a.getActualMax()) {
                        a.setActualMax(attrValue);
                    }
                    if (attrValue < a.getActualMin()) {
                        a.setActualMin(attrValue);
                    }
                }

                final FireSituation fs = new FireSituation(0, 0, 0, fsAttributes, attributeFactory);
                DeltaVector dv = new DeltaVector(alpha, dist);
                Map<Attribute, Clusterezation> clusterezations = dvClusterezations.get(dv);
                if (clusterezations == null) {
                    clusterezations = new HashMap<Attribute, Clusterezation>();
                    for (Attribute a : attributeFactory.getAttributes()) {
                        clusterezations.put(a, new Clusterezation(a));
                    }
                    dvClusterezations.put(dv, clusterezations);
                }
                for (Attribute a : attributeFactory.getAttributes()) {
                    Clusterezation clusterezation = clusterezations.get(a);
                    if (clusterezation == null) {
                        clusterezation = new Clusterezation(AttributeFactory.distToHOWallAttr);
                    }
                    clusterezation.addFireSituation(fs);
                }
            }
            List<Object[]> intersections = new ArrayList<Object[]>();
            Map<Attribute, Double> attributeIntersections = new HashMap<Attribute, Double>();
            for (Attribute a : attributeFactory.getAttributes()) {
                double intersection = 0;
                List<Clusterezation> cls = new ArrayList<Clusterezation>();
                for (DeltaVector dv : dvClusterezations.keySet()) {
                    cls.add(dvClusterezations.get(dv).get(a));
                }
                for (int i = 0; i < cls.size(); i++) {
                    for (int j = i + 1; j < cls.size(); j++) {
                        intersection += cls.get(i).getIntersection(cls.get(j));
                    }
                }
                intersections.add(new Object[]{a, intersection});
                attributeIntersections.put(a, intersection);
            }
            Collections.sort(intersections, new Comparator<Object[]>() {

                public int compare(Object[] o1, Object[] o2) {
                    return ((Double) o1[1]).compareTo((Double)o2[1]); 
                }
            });

            Double minIntersection = (Double) intersections.get(0)[1];
            for (Object[] o : intersections) {
                System.out.println(o[0] + ": " + o[1] + "," + (minIntersection/(Double)o[1]));
            }

            NumberFormat numberFormat = new DecimalFormat();
            numberFormat.setMaximumFractionDigits(3);
            for (Attribute a : attributeFactory.getAttributes()) {
                System.out.print(numberFormat.format(minIntersection / attributeIntersections.get(a)) + ";");
            }

            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

/**
 Heading wall distance: 2161.443037973975
Bearing to closest wall: 2277.3888888875936
Bullet flight time: 2293.9000000001997
Distance between: 2486.4909090903147
Dist to closest wall: 2668.816666669711
Enemy stop time: 3565.2307692279987
Avg bullet bearing1: 3723.4923076873597
Enemy turn rate: 3852.666666672654
Avg bullet bearing2: 3965.8441558469676
Distance to center: 4219.854166667724
Time since lateral velocity dir change: 4386.296875
Gun bearing: 4562.72222222196
Enemy x: 4946.024691360371
Enemy y: 5136.229508192059
Enemy velocity: 5477.235294113569
Time since last hit: 6238.68359375
Last bullet flight time: 7691.772727280058
First bullet flight time: 8583.545454549201
First bullet bearing: 10202.270270263383
Angle to target: 10359.777777772777
Last bullet bearing: 10457.891891885012
Time since my last fire: 10486.409090910072
Enemy travel time: 11827.891089099956
Enemy heading: 12999.666666657387
Lateral velocity: 15343.42857141571
Enemy acceleration: 42255.5
Enemy last visited gf: 84255.0
Fire power: 84255.0
Dist traveled on last wave: 84255.0
 */