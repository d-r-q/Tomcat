package lxx.targeting.mg4;

import javax.swing.*;

/**
 * User: jdev
 * Date: 12.05.2010
 */
public class Test extends JFrame {

    /*private Map<String, Pattern> predicats;
    private DVCanvas dvCanvas = new DVCanvas();

    public void init() {
        readData();
        test();

        getContentPane().setLayout(new BorderLayout());
        Vector preds = new Vector();
        for (Map.Entry<String, Pattern> e : predicats.entrySet()) {
            if (e.getValue().getFsCount() > 100) {
                preds.add(e.getKey());
            }
        }
        System.out.println("preds.size = " + preds.size());
        final JList lst = new JList(preds);
        lst.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                dvCanvas.setPredicat(predicats.get((String) lst.getSelectedValue()));
                dvCanvas.repaint();
            }
        });
        getContentPane().add(new JScrollPane(lst), BorderLayout.WEST);
        //dvCanvas.setSize(2000, 2000);
        final JScrollPane comp = new JScrollPane(dvCanvas);
        *//*comp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        comp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);*//*
        getContentPane().add(comp, BorderLayout.CENTER);
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void test() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("E:\\lexx\\games\\rc\\robots\\lxx\\UltraMarine.data\\test.dat"));
            Set<Attribute> attrs = new TreeSet<Attribute>(new Comparator<Attribute>() {
                public int compare(Attribute o1, Attribute o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (Segmentation s : SegmentationsManager.getSegmentations()) {
                attrs.add(s.getAttribute());
            }

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] attrValues = line.split(";");
                if (attrValues.length < 31) {
                    continue;
                }
                int alpha = new Double(attrValues[attrValues.length - 2]).intValue();
                int dist = new Integer(attrValues[attrValues.length - 1]);

                final FireSituation fs = getFireSituation(attrs, attrValues);
                if (fs == null) {
                    continue;
                }

                TreeMap<Double, int[]> matches = new TreeMap<Double, int[]>(new Comparator<Double>() {

                    public int compare(Double o1, Double o2) {
                        if (o1 > o2) {
                            return -1;
                        } else if (o1 < o2) {
                            return 1;
                        }
                        return 0;
                    }
                });

                for (Map.Entry<String, Pattern> e : predicats.entrySet()) {
                    double match = e.getValue().match(fs);
                    // " " + alpha + " : " + dist + " "
                    String str = e.getKey().trim();
                    final int colIdx = str.indexOf(":");
                    matches.put(match, new int[]{Integer.parseInt(str.substring(0, colIdx - 1)), Integer.parseInt(str.substring(colIdx + 2))});
                }

                int i = 0;
                Map<Integer, Integer> alphaDists = new HashMap<Integer, Integer>();
                Map<Integer, Integer> distDists = new HashMap<Integer, Integer>();
                for (Iterator<Map.Entry<Double, int[]>> iter = matches.entrySet().iterator(); iter.hasNext() && i < 10; i++) {
                    int[] data1 = iter.next().getValue();
                    int j = 0;
                    int distDist = 0;
                    int alphaDist = 0;
                    for (Iterator<Map.Entry<Double, int[]>> iter2 = matches.entrySet().iterator(); iter2.hasNext() && j < 10; j++) {
                        int[] data2 = iter2.next().getValue();
                        alphaDist += abs(data1[0] - data2[0]);
                        distDist += abs(data1[1] - data2[1]);
                    }
                    Integer oldAlphaDist = alphaDists.get(data1[0]);
                    if (oldAlphaDist == null || oldAlphaDist > alphaDist) {
                        alphaDists.put(data1[0], alphaDist);
                    }
                    Integer oldDistDist = distDists.get(data1[1]);
                    if (oldDistDist == null || oldDistDist > distDist) {
                        distDists.put(data1[1], distDist);
                    }
                }
                int minAlpha = 0;
                int minAlphaDist = Integer.MAX_VALUE;
                for (Map.Entry<Integer, Integer> e : alphaDists.entrySet()) {
                    if (e.getValue() < minAlphaDist) {
                        minAlpha = e.getKey();
                        minAlphaDist = e.getValue();
                    }
                }
                int minDist = 0;
                int minDistDist = Integer.MAX_VALUE;
                for (Map.Entry<Integer, Integer> e : distDists.entrySet()) {
                    if (e.getValue() < minDistDist) {
                        minDist = e.getKey();
                        minDistDist = e.getValue();
                    }
                }

                System.out.println("(" + alpha + " : " + dist + ") - (" + minAlpha + " : " + minDist + ")");
            }
            System.out.println("predicats count = " + predicats.size());

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void readData() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("E:\\lexx\\games\\rc\\robots\\lxx\\UltraMarine.data\\stat.st"));
            Set<Attribute> attrs = new TreeSet<Attribute>(new Comparator<Attribute>() {
                public int compare(Attribute o1, Attribute o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (Segmentation s : SegmentationsManager.getSegmentations()) {
                attrs.add(s.getAttribute());
            }

            String line = null;
            predicats = new HashMap<String, Pattern>();
            while ((line = reader.readLine()) != null) {
                String[] attrValues = line.split(";");
                if (attrValues.length < 31) {
                    continue;
                }
                int alpha = new Double(attrValues[attrValues.length - 2]).intValue();
                int dist = new Integer(attrValues[attrValues.length - 1]);
                Pattern dvPredicat = predicats.get(" " + alpha + " : " + dist + " ");
                if (dvPredicat == null) {
                    //dvPredicat = new Pattern(attributeFactory);
                    predicats.put(" " + alpha + " : " + dist + " ", dvPredicat);
                }
                final FireSituation fs = getFireSituation(attrs, attrValues);
                if (fs == null) {
                    continue;
                }
                dvPredicat.addPredicat(fs);
            }
            System.out.println("predicats count = " + predicats.size());

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private FireSituation getFireSituation(Set<Attribute> attrs, String[] attrValues) {
        int idx = 0;
        Map<Attribute, Integer> fsAttrs = new HashMap<Attribute, Integer>();
        for (Attribute a : attrs) {
            if (idx == attrValues.length) {
                return null;
            }
            final Integer attrValue = new Integer(attrValues[idx++]);
            if (attrValue < a.getMinValue() || attrValue > a.getMaxValue()) {
                return null;
            }
            fsAttrs.put(a, attrValue);
        }

        return null*//*new FireSituation(0, 0, 0, fsAttrs)*//*;
    }

    public static void main(String[] args) {
        new Test().init();
    }*/

}
