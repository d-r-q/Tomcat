/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import lxx.ts_log.TurnSnapshot;
import lxx.utils.r_tree.LoadedRTreeEntry;

import static java.lang.Math.random;

public class ShellSortTest {

    public static void main(String[] args) {
        testHeap();
    }

    private static void testShell() {
        for (int i = 0; i < 100; i++) {
            final LoadedRTreeEntry[] arr = getArray();
            final ShellSort ss = new ShellSort(arr);
            ss.sortFirstN(5);
            for (int j = 0; j < arr.length - 1; j++) {
                if (j < 5) {
                    if (arr[j].location.roundTime < arr[j + 1].location.roundTime) {
                        System.out.println("AAAAAAAAAAAAAAAA");
                    }
                } else {
                    if (arr[j].location.roundTime > arr[4].location.roundTime) {
                        System.out.println("AAAAAAAAAAAAAAAA");
                    }
                }
            }
            ss.sortFirstN(arr.length);
            for (int j = 0; j < arr.length - 1; j++) {
                if (arr[j].location.roundTime < arr[j + 1].location.roundTime) {
                    System.out.println("AAAAAAAAAAAAAAAA");
                }
            }
        }
    }

    private static void testHeap() {
        for (int i = 0; i < 100; i++) {
            final LoadedRTreeEntry[] arr = getArray();
            final HeapSort hs = new HeapSort(arr);
            hs.sortLastN(5);
            for (int j = arr.length - 2; j >= 0; j--) {
                if (j < arr.length - 1 - 5) {
                    if (arr[j].location.roundTime > arr[arr.length - 5].location.roundTime) {
                        System.out.println("AAAAAAAAAAAAAAAA");
                    }
                } else {
                    if (arr[j].location.roundTime > arr[j + 1].location.roundTime) {
                        System.out.println("AAAAAAAAAAAAAAAA");
                    }
                }
            }
            hs.sortLastN(arr.length);
            for (int j = 0; j < arr.length - 1; j++) {
                if (arr[j].location.roundTime > arr[j + 1].location.roundTime) {
                    System.out.println("AAAAAAAAAAAAAAAA");
                }
            }
        }
    }

    private static LoadedRTreeEntry[] getArray() {
        final LoadedRTreeEntry[] arr = new LoadedRTreeEntry[(int) (60 + 20 * random())];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new LoadedRTreeEntry(new TurnSnapshot(null, (int) (random() * 1000), (int) (random() * 35), null, null), null);
        }
        return arr;
    }

}
