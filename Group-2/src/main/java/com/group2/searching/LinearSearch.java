package com.group2.searching;

import java.util.List;
import java.util.function.Predicate;

/**
 * Generic linear (sequential) search. O(n) time, works on unsorted data.
 */
public final class LinearSearch {

    private LinearSearch() {
    }

    /**
     * Returns the index of the first element matching the predicate, or -1 if none match.
     */
    public static <T> int indexOf(List<T> data, Predicate<T> matcher) {
        for (int i = 0; i < data.size(); i++) {
            if (matcher.test(data.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first element matching the predicate, or null if none match.
     */
    public static <T> T find(List<T> data, Predicate<T> matcher) {
        int index = indexOf(data, matcher);
        return index == -1 ? null : data.get(index);
    }

    /**
     * Returns all elements matching the predicate, preserving order.
     */
    public static <T> List<T> findAll(List<T> data, Predicate<T> matcher) {
        return data.stream().filter(matcher).toList();
    }
}
