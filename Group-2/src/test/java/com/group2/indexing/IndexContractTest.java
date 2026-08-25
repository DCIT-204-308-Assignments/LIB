package com.group2.indexing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shared behavioural contract that every {@link Index} implementation must satisfy.
 * Each structure gets its own concrete subclass so failures are reported per structure.
 */
public abstract class IndexContractTest {

    protected abstract Index<String, Integer> createIndex();

    @Test
    void isEmpty_trueForNewIndex() {
        assertTrue(createIndex().isEmpty());
        assertEquals(0, createIndex().size());
    }

    @Test
    void put_thenGet_returnsStoredValue() {
        Index<String, Integer> index = createIndex();
        index.put("REST1", 10);
        assertEquals(10, index.get("REST1"));
        assertFalse(index.isEmpty());
        assertEquals(1, index.size());
    }

    @Test
    void put_sameKeyTwice_overwritesValueWithoutGrowingSize() {
        Index<String, Integer> index = createIndex();
        index.put("REST1", 10);
        index.put("REST1", 20);
        assertEquals(20, index.get("REST1"));
        assertEquals(1, index.size());
    }

    @Test
    void get_missingKey_returnsNull() {
        Index<String, Integer> index = createIndex();
        index.put("REST1", 10);
        assertNull(index.get("NOPE"));
    }

    @Test
    void containsKey_reflectsPresence() {
        Index<String, Integer> index = createIndex();
        index.put("REST1", 10);
        assertTrue(index.containsKey("REST1"));
        assertFalse(index.containsKey("REST2"));
    }

    @Test
    void remove_existingKey_removesItAndReturnsTrue() {
        Index<String, Integer> index = createIndex();
        index.put("REST1", 10);
        index.put("REST2", 20);

        assertTrue(index.remove("REST1"));

        assertFalse(index.containsKey("REST1"));
        assertNull(index.get("REST1"));
        assertEquals(1, index.size());
        assertEquals(20, index.get("REST2"));
    }

    @Test
    void remove_missingKey_returnsFalseAndLeavesIndexUnchanged() {
        Index<String, Integer> index = createIndex();
        index.put("REST1", 10);

        assertFalse(index.remove("NOPE"));
        assertEquals(1, index.size());
    }

    @Test
    void manyInsertsAndRemovals_leaveOnlyExpectedKeys() {
        Index<String, Integer> index = createIndex();
        String[] keys = {"K5", "K3", "K8", "K1", "K4", "K7", "K9", "K2", "K6"};
        for (int i = 0; i < keys.length; i++) {
            index.put(keys[i], i);
        }
        assertEquals(keys.length, index.size());

        index.remove("K3");
        index.remove("K8");
        index.remove("K1");

        assertEquals(keys.length - 3, index.size());
        assertFalse(index.containsKey("K3"));
        assertFalse(index.containsKey("K8"));
        assertFalse(index.containsKey("K1"));
        assertTrue(index.containsKey("K5"));
        assertTrue(index.containsKey("K9"));
        assertEquals(8, index.get("K6"));
    }
}
