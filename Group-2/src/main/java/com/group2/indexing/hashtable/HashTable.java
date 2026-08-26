package com.group2.indexing.hashtable;

import com.group2.indexing.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Hash table with separate chaining and dynamic resizing. O(1) average
 * put/get/remove, O(n) worst case under heavy collisions.
 */
public class HashTable<K, V> implements Index<K, V> {

    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    private static final class Entry<K, V> {
        final K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private List<Entry<K, V>>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public HashTable() {
        buckets = new List[INITIAL_CAPACITY];
    }

    private int bucketIndex(K key, int capacity) {
        int h = (key == null) ? 0 : key.hashCode();
        h ^= (h >>> 16);
        return (h & 0x7fffffff) % capacity;
    }

    @Override
    public void put(K key, V value) {
        int index = bucketIndex(key, buckets.length);
        if (buckets[index] == null) {
            buckets[index] = new ArrayList<>();
        }
        for (Entry<K, V> entry : buckets[index]) {
            if (keysEqual(entry.key, key)) {
                entry.value = value;
                return;
            }
        }
        buckets[index].add(new Entry<>(key, value));
        size++;
        if ((double) size / buckets.length > LOAD_FACTOR_THRESHOLD) {
            resize();
        }
    }

    @Override
    public V get(K key) {
        List<Entry<K, V>> bucket = buckets[bucketIndex(key, buckets.length)];
        if (bucket == null) {
            return null;
        }
        for (Entry<K, V> entry : bucket) {
            if (keysEqual(entry.key, key)) {
                return entry.value;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        List<Entry<K, V>> bucket = buckets[bucketIndex(key, buckets.length)];
        if (bucket == null) {
            return false;
        }
        for (Entry<K, V> entry : bucket) {
            if (keysEqual(entry.key, key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(K key) {
        List<Entry<K, V>> bucket = buckets[bucketIndex(key, buckets.length)];
        if (bucket == null) {
            return false;
        }
        for (int i = 0; i < bucket.size(); i++) {
            if (keysEqual(bucket.get(i).key, key)) {
                bucket.remove(i);
                size--;
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        List<Entry<K, V>>[] oldBuckets = buckets;
        buckets = new List[oldBuckets.length * 2];
        for (List<Entry<K, V>> bucket : oldBuckets) {
            if (bucket == null) {
                continue;
            }
            for (Entry<K, V> entry : bucket) {
                int index = bucketIndex(entry.key, buckets.length);
                if (buckets[index] == null) {
                    buckets[index] = new ArrayList<>();
                }
                buckets[index].add(entry);
            }
        }
    }

    private boolean keysEqual(K a, K b) {
        return a == null ? b == null : a.equals(b);
    }

    /** Number of buckets currently allocated. Exposed for tests that verify resizing. */
    public int capacity() {
        return buckets.length;
    }
}
