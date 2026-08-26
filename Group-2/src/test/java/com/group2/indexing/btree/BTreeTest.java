package com.group2.indexing.btree;

import com.group2.indexing.Index;
import com.group2.indexing.IndexContractTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BTreeTest extends IndexContractTest {

    @Override
    protected Index<String, Integer> createIndex() {
        return new BTree<>(3);
    }

    @Test
    void constructor_rejectsMinDegreeBelowTwo() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new BTree<Integer, Integer>(1));
    }

    @Test
    void staysValid_afterManySequentialInserts_forcingRepeatedSplits() {
        BTree<Integer, Integer> tree = new BTree<>(2);
        for (int i = 0; i < 1000; i++) {
            tree.put(i, i);
            assertTrue(tree.isValidBTree(), "tree invalid after inserting " + i);
        }
        assertEquals(1000, tree.size());
        assertEquals(0, tree.get(0));
        assertEquals(999, tree.get(999));
    }

    @Test
    void staysValid_afterRandomInsertsAndDeletes() {
        BTree<Integer, Integer> tree = new BTree<>(3);
        Random random = new Random(7);
        List<Integer> keys = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            int key = random.nextInt(2000);
            tree.put(key, key);
            if (!keys.contains(key)) {
                keys.add(key);
            }
            assertTrue(tree.isValidBTree(), "tree invalid after inserting " + key);
        }

        java.util.Collections.shuffle(keys, random);
        for (int key : keys) {
            assertTrue(tree.remove(key));
            assertTrue(tree.isValidBTree(), "tree invalid after removing " + key);
        }

        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
    }

    @Test
    void inOrderKeys_returnsKeysInAscendingOrder() {
        BTree<Integer, String> tree = new BTree<>(2);
        int[] insertionOrder = {50, 30, 80, 10, 40, 70, 90, 20, 60, 5, 15, 25, 35};
        for (int key : insertionOrder) {
            tree.put(key, "v" + key);
        }
        assertEquals(List.of(5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 80, 90), tree.inOrderKeys());
    }

    @Test
    void delete_triggeringMergeAcrossLevels_keepsRemainingKeysCorrect() {
        BTree<Integer, String> tree = new BTree<>(2);
        int[] insertionOrder = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130};
        for (int key : insertionOrder) {
            tree.put(key, "v" + key);
        }

        for (int key : new int[] {10, 20, 30, 40, 50, 60, 70}) {
            assertTrue(tree.remove(key));
            assertTrue(tree.isValidBTree(), "tree invalid after removing " + key);
        }

        assertEquals(List.of(80, 90, 100, 110, 120, 130), tree.inOrderKeys());
        assertEquals("v100", tree.get(100));
    }
}
