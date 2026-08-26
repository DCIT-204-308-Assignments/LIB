package com.group2.dispatch;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinHeapTest {

    @Test
    void extractMin_returnsElementsInAscendingOrder() {
        MinHeap<Integer> heap = new MinHeap<>(Comparator.<Integer>naturalOrder());
        for (int value : new int[] {5, 3, 8, 1, 9, 2, 7}) {
            heap.insert(value);
        }

        int previous = Integer.MIN_VALUE;
        while (!heap.isEmpty()) {
            int next = heap.extractMin();
            assertTrue(next >= previous);
            previous = next;
        }
    }

    @Test
    void peek_doesNotRemoveElement() {
        MinHeap<Integer> heap = new MinHeap<>(Comparator.<Integer>naturalOrder());
        heap.insert(4);
        heap.insert(2);
        assertEquals(2, heap.peek());
        assertEquals(2, heap.size());
    }

    @Test
    void extractMin_emptyHeap_throws() {
        MinHeap<Integer> heap = new MinHeap<>(Comparator.<Integer>naturalOrder());
        assertThrows(NoSuchElementException.class, heap::extractMin);
    }

    @Test
    void size_reflectsInsertionsAndExtractions() {
        MinHeap<Integer> heap = new MinHeap<>(Comparator.<Integer>naturalOrder());
        assertTrue(heap.isEmpty());
        heap.insert(1);
        heap.insert(2);
        assertEquals(2, heap.size());
        heap.extractMin();
        assertEquals(1, heap.size());
    }
}
