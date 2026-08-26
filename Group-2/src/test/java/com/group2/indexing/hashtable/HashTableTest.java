package com.group2.indexing.hashtable;

import com.group2.indexing.Index;
import com.group2.indexing.IndexContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashTableTest extends IndexContractTest {

    @Override
    protected Index<String, Integer> createIndex() {
        return new HashTable<>();
    }

    @Test
    void resizes_whenLoadFactorExceeded_andKeepsAllEntriesRetrievable() {
        HashTable<String, Integer> table = new HashTable<>();
        int initialCapacity = table.capacity();

        for (int i = 0; i < 100; i++) {
            table.put("K" + i, i);
        }

        assertTrue(table.capacity() > initialCapacity, "table should have grown past its initial capacity");
        for (int i = 0; i < 100; i++) {
            assertEquals(i, table.get("K" + i));
        }
        assertEquals(100, table.size());
    }

    @Test
    void handlesCollidingKeys_inSameBucket() {
        HashTable<Integer, String> table = new HashTable<>();
        // With the default capacity of 16, keys 0 and 16 land in the same bucket.
        table.put(0, "zero");
        table.put(16, "sixteen");

        assertEquals("zero", table.get(0));
        assertEquals("sixteen", table.get(16));

        table.remove(0);
        assertEquals(null, table.get(0));
        assertEquals("sixteen", table.get(16));
    }
}
