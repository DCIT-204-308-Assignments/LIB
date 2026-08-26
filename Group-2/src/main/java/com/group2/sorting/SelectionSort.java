package com.group2.sorting;

import java.util.Comparator;
import java.util.List;

/**
 * Selection sort. O(n^2) comparisons and O(n) swaps, in place.
 */
public final class SelectionSort {

    private SelectionSort() {
    }

    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (comparator.compare(list.get(j), list.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swap(list, i, minIndex);
            }
        }
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
