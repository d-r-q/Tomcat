package lxx.utils;

import junit.framework.TestCase;

import java.util.List;

import static java.lang.Math.random;

/**
 * User: jdev
 * Date: 14.06.11
 */
public class MedianTest extends TestCase {

    public void testMedian() throws Exception {
        Median median = new Median(10000);

        for (int i = 0; i < 100000; i++) {
            median.addValue((int) (random() * 50));

            median.getMedian();
            List<Double> values = median.getValues();
            for (int j = 0; j < values.size() - 1; j++) {
                assertTrue(values.get(j) <= values.get(j + 1));
            }
        }
        System.out.println("test");
    }
}
