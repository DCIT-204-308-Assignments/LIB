package com.group2.searching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BinarySearchTest {

    private final List<Integer> sortedNumbers = List.of(1, 3, 5, 7, 9, 11, 13);

    @Test
    void indexOf_findsElement_atStart() {
        assertEquals(0, BinarySearch.indexOf(sortedNumbers, 1, n -> n));
    }

    @Test
    void indexOf_findsElement_atEnd() {
        assertEquals(6, BinarySearch.indexOf(sortedNumbers, 13, n -> n));
    }

    @Test
    void indexOf_findsElement_inMiddle() {
        assertEquals(3, BinarySearch.indexOf(sortedNumbers, 7, n -> n));
    }

    @Test
    void indexOf_returnsMinusOne_whenNotFound() {
        assertEquals(-1, BinarySearch.indexOf(sortedNumbers, 4, n -> n));
    }

    @Test
    void indexOf_returnsMinusOne_onEmptyList() {
        assertEquals(-1, BinarySearch.indexOf(List.of(), 1, (Integer n) -> n));
    }

    @Test
    void find_returnsElement_whenPresent() {
        assertEquals(9, BinarySearch.find(sortedNumbers, 9, n -> n));
    }

    @Test
    void find_returnsNull_whenAbsent() {
        assertNull(BinarySearch.find(sortedNumbers, 100, n -> n));
    }
}
