/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.utils.r_tree.LoadedRTreeEntry;

public class ShellSort {

    private final LoadedRTreeEntry[] array;

    private int left;
    private int right;

    public ShellSort(LoadedRTreeEntry[] array) {
        this.array = array;
        left = 0;
        right = array.length - 1;
    }

    public void sortFirstN(int n) {
        int count = n - left;
        for (int i = 0; i < count; i++) {
            if (left > right) {
                return;
            }
            int minIdx = left;
            int maxIdx = right;

            for (int j = left; j <= right; j++) {
                if (array[j].location.roundTime < array[minIdx].location.roundTime) {
                    minIdx = j;
                } else if (array[j].location.roundTime > array[maxIdx].location.roundTime) {
                    maxIdx = j;
                }
            }
            LoadedRTreeEntry tmp = array[left];
            array[left] = array[minIdx];
            array[minIdx] = tmp;

            tmp = array[right];
            array[right] = array[minIdx];
            array[minIdx] = tmp;

            left++;
            right--;
        }
    }

}
