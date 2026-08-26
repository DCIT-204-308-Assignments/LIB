package engines;

import ds.BST;
import ds.BTree;
import ds.DynamicArray;
import ds.HashTable;
import ds.RedBlackTree;
import models.AlgorithmRun;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Compares insert+search timing for HashTable, BST, RedBlackTree and BTree
 * across increasing dataset sizes (Suggestions.md #35 — Benchmarking).
 * Each row is also persisted as an AlgorithmRun via DatabaseManager so the
 * results show up alongside the app's other recorded algorithm executions.
 */
public class BenchmarkEngine {

    public static class BenchmarkRow {
        public final String structureName;
        public final int inputSize;
        public final long insertTimeNs;
        public final long searchTimeNs;

        public BenchmarkRow(String structureName, int inputSize, long insertTimeNs, long searchTimeNs) {
            this.structureName = structureName;
            this.inputSize = inputSize;
            this.insertTimeNs = insertTimeNs;
            this.searchTimeNs = searchTimeNs;
        }

        @Override
        public String toString() {
            return String.format("%-14s size=%-7d insert=%,10d ns  search=%,10d ns",
                    structureName, inputSize, insertTimeNs, searchTimeNs);
        }
    }

    /** e.g. runAll(new int[]{100, 1000, 10000}) */
    public static DynamicArray<BenchmarkRow> runAll(int[] sizes) {
        DynamicArray<BenchmarkRow> rows = new DynamicArray<>();
        int runId = (int) (System.nanoTime() % 1_000_000);

        for (int size : sizes) {
            int[] keys = randomKeys(size);

            rows.add(benchmarkHashTable(keys, runId++));
            rows.add(benchmarkBST(keys, runId++));
            rows.add(benchmarkRedBlackTree(keys, runId++));
            rows.add(benchmarkBTree(keys, runId++));
        }

        return rows;
    }

    private static int[] randomKeys(int size) {
        // Fixed seed so every structure is benchmarked against the same
        // key set for a fair comparison.
        Random rng = new Random(42);
        int[] keys = new int[size];
        for (int i = 0; i < size; i++) {
            keys[i] = rng.nextInt(Math.max(1, size * 10));
        }
        return keys;
    }

    private static BenchmarkRow benchmarkHashTable(int[] keys, int runId) {
        HashTable<Integer, Integer> table = new HashTable<>();

        long insertStart = System.nanoTime();
        for (int k : keys) table.put(k, k);
        long insertTime = System.nanoTime() - insertStart;

        long searchStart = System.nanoTime();
        for (int k : keys) table.get(k);
        long searchTime = System.nanoTime() - searchStart;

        record("HashTable", keys.length, insertTime, searchTime, runId);
        return new BenchmarkRow("HashTable", keys.length, insertTime, searchTime);
    }

    private static BenchmarkRow benchmarkBST(int[] keys, int runId) {
        BST<Integer, Integer> tree = new BST<>();

        long insertStart = System.nanoTime();
        for (int k : keys) tree.insert(k, k);
        long insertTime = System.nanoTime() - insertStart;

        long searchStart = System.nanoTime();
        for (int k : keys) tree.search(k);
        long searchTime = System.nanoTime() - searchStart;

        record("BST", keys.length, insertTime, searchTime, runId);
        return new BenchmarkRow("BST", keys.length, insertTime, searchTime);
    }

    private static BenchmarkRow benchmarkRedBlackTree(int[] keys, int runId) {
        RedBlackTree<Integer, Integer> tree = new RedBlackTree<>();

        long insertStart = System.nanoTime();
        for (int k : keys) tree.insert(k, k);
        long insertTime = System.nanoTime() - insertStart;

        long searchStart = System.nanoTime();
        for (int k : keys) tree.search(k);
        long searchTime = System.nanoTime() - searchStart;

        record("RedBlackTree", keys.length, insertTime, searchTime, runId);
        return new BenchmarkRow("RedBlackTree", keys.length, insertTime, searchTime);
    }

    private static BenchmarkRow benchmarkBTree(int[] keys, int runId) {
        BTree<Integer, Integer> tree = new BTree<>();

        long insertStart = System.nanoTime();
        for (int k : keys) tree.insert(k, k);
        long insertTime = System.nanoTime() - insertStart;

        long searchStart = System.nanoTime();
        for (int k : keys) tree.search(k);
        long searchTime = System.nanoTime() - searchStart;

        record("BTree", keys.length, insertTime, searchTime, runId);
        return new BenchmarkRow("BTree", keys.length, insertTime, searchTime);
    }

    private static void record(String name, int size, long insertNs, long searchNs, int runId) {
        try {
            DatabaseManager.addAlgorithmRun(new AlgorithmRun(
                    runId,
                    name + ".insert+search",
                    size,
                    insertNs + searchNs,
                    0L,
                    LocalDateTime.now().toString(),
                    size * 2L,
                    0L,
                    "SUCCESS",
                    name + " benchmark over " + size + " keys"
            ));
        } catch (Exception ex) {
            // Benchmarking should not fail the run if persistence is unavailable.
        }
    }
}
