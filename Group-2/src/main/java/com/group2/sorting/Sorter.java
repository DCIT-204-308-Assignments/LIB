package com.group2.sorting;

import java.util.Comparator;
import java.util.List;

/**
 * A sorting algorithm that arranges {@code list} in place according to {@code comparator}.
 * Implemented by each of the algorithm classes in this package, so callers can pick an
 * algorithm (e.g. {@code MergeSort::sort}) independently of what they are sorting by.
 */
@FunctionalInterface
public interface Sorter<T> {

    void sort(List<T> list, Comparator<T> comparator);
}
