/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.data_analysis.DataPoint;
import lxx.data_analysis.LxxDataPoint;

public class HeapSort {

    private final DataPoint[] array;
    private int i;

    public HeapSort(DataPoint[] array) {
        this.array = array;
        for (int i = array.length / 2; i >= 0; i--) {
            downHeap(i, array.length);
        }
        i = array.length - 1;
    }

    public void sortLastN(int n) {
        for (; i > 0 && i >= array.length - n; i--) {
            final DataPoint temp = array[i];
            array[i] = array[0];
            array[0] = temp;

            downHeap(0, i);
        }
    }

    private void downHeap(int k, int n) {
        DataPoint newElem = array[k];
        int child;

        while (k < n / 2) {
            child = (2 * k) + 1;
            if (child < n - 1 && ((LxxDataPoint) array[child]).ts.roundTime < ((LxxDataPoint) array[child + 1]).ts.roundTime) {
                child++;
            }
            if (((LxxDataPoint) newElem).ts.roundTime > ((LxxDataPoint) array[child]).ts.roundTime) {
                break;
            }
            array[k] = array[child];
            k = child;
        }
        array[k] = newElem;
    }

}
