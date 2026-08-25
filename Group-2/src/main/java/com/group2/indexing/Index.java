package com.group2.indexing;

/**
 * A key-value index supporting lookup, insertion, deletion and membership tests.
 * Implemented by every indexing structure (BST, red-black tree, B-tree, hash table)
 * so they can be exercised through a shared test contract.
 */
public interface Index<K, V> {

    void put(K key, V value);

    V get(K key);

    boolean remove(K key);

    boolean containsKey(K key);

    int size();

    boolean isEmpty();
}
