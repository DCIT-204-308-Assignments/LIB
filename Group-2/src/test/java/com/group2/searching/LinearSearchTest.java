package com.group2.searching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LinearSearchTest {

    private final List<String> names = List.of("Kimathi", "Olive", "Kingsley", "Melvin", "Jeremy", "Henry");

    @Test
    void indexOf_returnsIndex_whenMatchExists() {
        assertEquals(2, LinearSearch.indexOf(names, n -> n.equals("Kingsley")));
    }

    @Test
    void indexOf_returnsMinusOne_whenNoMatch() {
        assertEquals(-1, LinearSearch.indexOf(names, n -> n.equals("Nobody")));
    }

    @Test
    void find_returnsElement_whenMatchExists() {
        assertEquals("Melvin", LinearSearch.find(names, n -> n.startsWith("Mel")));
    }

    @Test
    void find_returnsNull_whenNoMatch() {
        assertNull(LinearSearch.find(names, n -> n.startsWith("Z")));
    }

    @Test
    void findAll_returnsAllMatches_inOrder() {
        List<String> result = LinearSearch.findAll(names, n -> n.length() > 5);
        assertEquals(List.of("Kimathi", "Kingsley", "Melvin", "Jeremy"), result);
    }

    @Test
    void findAll_returnsEmptyList_whenNoMatches() {
        assertEquals(List.of(), LinearSearch.findAll(names, n -> n.isEmpty()));
    }
}
