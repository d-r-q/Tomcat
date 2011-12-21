/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.utils.r_tree.RTreeEntry;

public class HeapSort {

    private final RTreeEntry[] array;
    private int i;

    public HeapSort(RTreeEntry[] array) {
        this.array = array;
        for (int i = array.length / 2; i >= 0; i--) {
            downHeap(i, array.length - 1);
        }
        i = array.length;
    }

    public void sortLastN(int n) {
        for (; i >= 0 && i >= array.length - n; i--) {
            final RTreeEntry temp = array[i];
            array[i] = array[0];
            array[0] = temp;

            downHeap(0, i);
        }
    }

    private void downHeap(int k, int n) {
        RTreeEntry newElem = array[k];
        int child;

        while (k <= n / 2) {
            child = 2 * k;
            if (child < n && array[child].location.roundTime < array[child + 1].location.roundTime) {
                child++;
            }
            if (newElem.location.roundTime >= array[child].location.roundTime) {
                break;
            }
            array[k] = array[child];
            k = child;
        }
        array[k] = newElem;
    }

}
