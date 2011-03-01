package lxx.autosegmentation.test;


/**
 * User: jdev
 * Date: 18.02.2010
 */
public class ASTest {

    /*private Segmentation[] segmentations = new Segmentation[GFStatistics.CLASTERING_PARAM_NAMES.length];

    private final SegmentationView sgView = new SegmentationView();

    public static void main(String[] args) throws IOException {
        ASTest test = new ASTest();
        test.init();
    }

    private void init() throws IOException {
        System.out.println("please wait...");
        for (int i = 0; i < segmentations.length; i++) {
            segmentations[i] = new Segmentation(i, GFStatistics.segmentationBorders[i * 2], GFStatistics.segmentationBorders[i * 2] + 1);
        }

        int entryCount = 0;

        final BufferedReader br = new BufferedReader(new FileReader("E:\\lexx\\games\\rc\\robots\\lxx\\UltraMarine.data\\davidalves.net.Duelist 0.1.6src_36.dat"));
        int invalidGFCount = 0;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ";");
                int[] res = new int[GFStatistics.CLASTERING_PARAM_NAMES.length + 1];
                int i = 0;
                while (st.hasMoreTokens()) {
                    res[i] = Integer.parseInt(st.nextToken());
                    i++;
                }
                if (i < GFStatistics.CLASTERING_PARAM_NAMES.length + 1) {
                    continue;
                }
                if (abs(res[GFStatistics.CLASTERING_PARAM_NAMES.length]) > MegaGun.MAX_GUESS_FACTOR) {
                    invalidGFCount++;
                    continue;
                }

                for (int j = 0; j < segmentations.length; j++) {
                    segmentations[j].addEntry(res[j], res[res.length - 1]);
                }

                entryCount++;
                if (entryCount % 1000 == 0) {
                    System.out.println(entryCount + " entries processed");
                }
            }
        } finally {
            br.close();
        }

        for (Segmentation s : segmentations) {
            System.out.println("Split segmentation: " + s.getAttributeName());
            s.splitIntoSegments();
        }

        Arrays.sort(segmentations, new Comparator<Segmentation>() {

            public int compare(Segmentation o1, Segmentation o2) {
                final double plotnostLimit = 0.7;
                if (o1.getPlotnost() < plotnostLimit && o2.getPlotnost() >= plotnostLimit) {
                    return 1;
                }
                if (o2.getPlotnost() < plotnostLimit && o1.getPlotnost() >= plotnostLimit) {
                    return -1;
                }
                return o1.getSegmentsMESum() >= o2.getSegmentsMESum() ? -1 : 1;
            }
        });

        System.out.println("invalidGFCount = " + invalidGFCount);
        System.out.println("Data loaded preparing gui");

        getRootPane().setLayout(new BorderLayout());

        final JPanel buttonsPanel = new JPanel(new GridLayout(segmentations.length, 1));
        JScrollPane buttonsScrollPane = new JScrollPane(buttonsPanel);
        *//*buttonsScrollPane.setMaximumSize(new Dimension(200, getHeight()));
        buttonsScrollPane.setMinimumSize(new Dimension(200, getHeight()));
        buttonsScrollPane.setPreferredSize(new Dimension(200, getHeight()));*//*
        for (int i = 0; i < segmentations.length; i++) {
            JButton button = new JButton(segmentations[i].getAttributeName());
            button.addActionListener(new ButtonActionListener(segmentations[i]));
            buttonsPanel.add(button);
            System.out.println(segmentations[i].getAttributeName() + "' me sum = " + segmentations[i].getMESum());

        }
        getRootPane().add(buttonsScrollPane, BorderLayout.WEST);

        sgView.setSegmentation(segmentations[0]);
        getRootPane().add(sgView, BorderLayout.CENTER);
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private class ButtonActionListener implements ActionListener {

        private final Segmentation segmentation;

        public ButtonActionListener(Segmentation segmentation) {
            this.segmentation = segmentation;
        }

        public void actionPerformed(ActionEvent e) {
            sgView.setSegmentation(segmentation);
            sgView.repaint();
        }
    }*/

}
