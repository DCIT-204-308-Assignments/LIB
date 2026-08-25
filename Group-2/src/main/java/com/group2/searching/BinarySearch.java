package com.group2.searching;

import java.util.List;
import java.util.function.Function;

/**
 * Generic binary search. O(log n) time. Requires {@code data} to already be
 * sorted in ascending order by the key extracted with {@code keyExtractor}.
 */
public final class BinarySearch {

    private BinarySearch() {
    }

    /**
     * Returns the index of the element whose extracted key equals {@code key}, or -1 if not found.
     */
    public static <T, K extends Comparable<K>> int indexOf(List<T> sortedData, K key, Function<T, K> keyExtractor) {
        int low = 0;
        int high = sortedData.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            K midKey = keyExtractor.apply(sortedData.get(mid));
            int comparison = midKey.compareTo(key);

            if (comparison == 0) {
                return mid;
            } else if (comparison < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Returns the element whose extracted key equals {@code key}, or null if not found.
     */
    public static <T, K extends Comparable<K>> T find(List<T> sortedData, K key, Function<T, K> keyExtractor) {
        int index = indexOf(sortedData, key, keyExtractor);
        return index == -1 ? null : sortedData.get(index);
    }
}
