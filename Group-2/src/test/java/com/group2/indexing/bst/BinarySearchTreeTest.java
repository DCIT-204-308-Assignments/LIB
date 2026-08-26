package com.group2.indexing.bst;

import com.group2.indexing.Index;
import com.group2.indexing.IndexContractTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinarySearchTreeTest extends IndexContractTest {

    @Override
    protected Index<String, Integer> createIndex() {
        return new BinarySearchTree<>();
    }

    @Test
    void inOrderKeys_returnsKeysInAscendingOrder() {
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<>();
        int[] insertionOrder = {5, 3, 8, 1, 4, 7, 9};
        for (int key : insertionOrder) {
            tree.put(key, "v" + key);
        }
        assertEquals(List.of(1, 3, 4, 5, 7, 8, 9), tree.inOrderKeys());
    }

    @Test
    void remove_nodeWithTwoChildren_replacesWithInOrderSuccessor() {
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<>();
        for (int key : new int[] {5, 3, 8, 1, 4, 7, 9}) {
            tree.put(key, "v" + key);
        }

        assertEquals(true, tree.remove(5));

        assertEquals(List.of(1, 3, 4, 7, 8, 9), tree.inOrderKeys());
        assertEquals("v7", tree.get(7));
    }
}
