package com.group2.sorting;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Shared behavioural contract that every {@link Sorter} implementation must satisfy.
 * Each algorithm gets its own concrete subclass so failures are reported per algorithm.
 */
abstract class SortAlgorithmContractTest {

    protected abstract Sorter<Integer> sorter();

    @Test
    void sorts_randomOrderIntegers_ascending() {
        List<Integer> list = new ArrayList<>(List.of(5, 3, 8, 1, 9, 2, 7));
        sorter().sort(list, Comparator.naturalOrder());
        assertEquals(List.of(1, 2, 3, 5, 7, 8, 9), list);
    }

    @Test
    void sorts_alreadySortedList_staysSorted() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        sorter().sort(list, Comparator.naturalOrder());
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }

    @Test
    void sorts_reverseOrderedList() {
        List<Integer> list = new ArrayList<>(List.of(5, 4, 3, 2, 1));
        sorter().sort(list, Comparator.naturalOrder());
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }

    @Test
    void sorts_listWithDuplicates() {
        List<Integer> list = new ArrayList<>(List.of(4, 2, 4, 1, 2));
        sorter().sort(list, Comparator.naturalOrder());
        assertEquals(List.of(1, 2, 2, 4, 4), list);
    }

    @Test
    void sorts_emptyList_staysEmpty() {
        List<Integer> list = new ArrayList<>();
        sorter().sort(list, Comparator.naturalOrder());
        assertEquals(List.of(), list);
    }

    @Test
    void sorts_singleElementList() {
        List<Integer> list = new ArrayList<>(List.of(42));
        sorter().sort(list, Comparator.naturalOrder());
        assertEquals(List.of(42), list);
    }

    @Test
    void sorts_descending_withReversedComparator() {
        List<Integer> list = new ArrayList<>(List.of(3, 1, 4, 1, 5, 9, 2, 6));
        sorter().sort(list, Comparator.<Integer>naturalOrder().reversed());
        assertEquals(List.of(9, 6, 5, 4, 3, 2, 1, 1), list);
    }
}
