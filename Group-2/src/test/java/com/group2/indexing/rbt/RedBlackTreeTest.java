package com.group2.indexing.rbt;

import com.group2.indexing.Index;
import com.group2.indexing.IndexContractTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedBlackTreeTest extends IndexContractTest {

    @Override
    protected Index<String, Integer> createIndex() {
        return new RedBlackTree<>();
    }

    @Test
    void staysBalanced_afterAscendingInserts_whichWouldDegenerateAPlainBst() {
        RedBlackTree<Integer, Integer> tree = new RedBlackTree<>();
        for (int i = 0; i < 1000; i++) {
            tree.put(i, i);
            assertTrue(tree.isValidRedBlackTree(), "tree invalid after inserting " + i);
        }
        assertEquals(1000, tree.size());
        assertEquals(0, tree.get(0));
        assertEquals(999, tree.get(999));
    }

    @Test
    void staysBalanced_afterDescendingInserts() {
        RedBlackTree<Integer, Integer> tree = new RedBlackTree<>();
        for (int i = 1000; i >= 0; i--) {
            tree.put(i, i);
            assertTrue(tree.isValidRedBlackTree(), "tree invalid after inserting " + i);
        }
        assertEquals(1001, tree.size());
    }

    @Test
    void staysBalanced_afterRandomInsertsAndDeletes() {
        RedBlackTree<Integer, Integer> tree = new RedBlackTree<>();
        Random random = new Random(42);
        List<Integer> keys = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            int key = random.nextInt(2000);
            tree.put(key, key);
            if (!keys.contains(key)) {
                keys.add(key);
            }
            assertTrue(tree.isValidRedBlackTree(), "tree invalid after inserting " + key);
        }

        java.util.Collections.shuffle(keys, random);
        for (int key : keys) {
            tree.remove(key);
            assertTrue(tree.isValidRedBlackTree(), "tree invalid after removing " + key);
        }

        assertEquals(0, tree.size());
        assertTrue(tree.isEmpty());
    }

    @Test
    void inOrderKeys_returnsKeysInAscendingOrder() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        int[] insertionOrder = {50, 30, 80, 10, 40, 70, 90, 20, 60};
        for (int key : insertionOrder) {
            tree.put(key, "v" + key);
        }
        assertEquals(List.of(10, 20, 30, 40, 50, 60, 70, 80, 90), tree.inOrderKeys());
    }

    @Test
    void remove_reducesSize_andKeepsRemainingKeysRetrievable() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        int[] insertionOrder = {50, 30, 80, 10, 40, 70, 90, 20, 60};
        for (int key : insertionOrder) {
            tree.put(key, "v" + key);
        }

        for (int key : new int[] {30, 80, 50}) {
            assertTrue(tree.remove(key));
        }

        assertEquals(6, tree.size());
        assertEquals(List.of(10, 20, 40, 60, 70, 90), tree.inOrderKeys());
        assertTrue(tree.isValidRedBlackTree());
    }
}
