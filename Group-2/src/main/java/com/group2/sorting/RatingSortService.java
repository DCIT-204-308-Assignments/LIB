package com.group2.sorting;

import com.group2.model.Rated;

import java.util.Comparator;
import java.util.List;

/**
 * Sorts any {@link Rated} entity (e.g. restaurants, riders) by rating, as required
 * by the project scope. Caller picks the algorithm.
 */
public final class RatingSortService {

    private RatingSortService() {
    }

    public static <T extends Rated> void sortByRating(List<T> items, Sorter<T> sorter) {
        sorter.sort(items, Comparator.comparingDouble(Rated::getRating));
    }
}
