import ds.*;
import engines.*;
import models.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * UG Swift — Empirical Efficiency Lab (Section 9 of the project brief).
 *
 * Measures runtime (and an approximate memory delta) for every required
 * experiment, at every required input size, averaged over 3 trials each:
 *
 *   1. Search comparison       : linear vs binary search
 *   2. Sorting comparison      : selection, insertion, merge, quicksort
 *   3. Hash table load factor  : collision count / time vs load factor
 *   4. BST vs balanced tree    : height + search time (sorted-input worst case)
 *   5. Heap priority dispatch  : insert/extractMin time
 *   6. Graph algorithms        : BFS/DFS/Dijkstra/Kruskal/Prim runtime
 *
 * Every row is written to data/performance_results/benchmark_results.csv
 * AND inserted into the algorithm_runs table (DatabaseManager.addAlgorithmRun),
 * matching the brief's "record results in the database or exported CSV"
 * requirement. Open the CSV in Excel/Python to produce the required line
 * graphs (runtime vs input size).
 *
 * NOTE ON MEMORY FIGURES: JVM memory measurement via Runtime.totalMemory()-
 * freeMemory() is inherently approximate (GC timing is not deterministic).
 * Treat memoryKb as indicative, not exact — say so in the report.
 */
public class Performancebenchmark {

    private static final int[] SEARCH_SORT_SIZES = {100, 500, 1000, 5000, 10000};
    private static final int[] HASH_HEAP_SIZES = {100, 1000, 5000, 10000, 20000};
    private static final int[] GRAPH_SIZES = {50, 100, 200, 500};
    private static final double[] LOAD_FACTORS = {0.25, 0.5, 0.7};
    private static final int TRIALS = 3;

    // Fixed seed so results are reproducible across runs/machines when comparing.
    // Swap this for an index-number-derived seed per the brief's Section 2 requirement.
    private static final long SEED = 42L;

    private static PrintWriter csv;
    private static int runIdCounter = 1;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================");
        System.out.println("  UG Swift -- Empirical Efficiency Lab (Section 9)");
        System.out.println("======================================================");
        System.out.println("Each experiment runs " + TRIALS + " trials per input size; the average is recorded.");
        System.out.println("Results -> data/performance_results/benchmark_results.csv and the algorithm_runs table.\n");

        DatabaseManager.initializeDatabase("data/locations.csv", "data/roads.csv");
        new File("data/performance_results").mkdirs();
        csv = new PrintWriter(new FileWriter("data/performance_results/benchmark_results.csv"));
        csv.println("experiment,algorithm,inputSize,avgTimeNs,avgTimeMs,avgMemoryKb,extraMetric,dateRun");

        benchmarkSearch();
        benchmarkSorting();
        benchmarkHashLoadFactor();
        benchmarkBstVsBalancedTree();
        benchmarkHeap();
        benchmarkGraph();

        csv.close();
        System.out.println("\nAll experiments complete.");
        System.out.println("CSV: data/performance_results/benchmark_results.csv");
        System.out.println("Open it in Excel (Insert -> Line Chart) or plot with Python/matplotlib for the report graphs.");
    }

    // ── 1. Search comparison ────────────────────────────────────────────
    private static void benchmarkSearch() {
        System.out.println("-- Search comparison (linear vs binary) --");
        Random rng = new Random(SEED);
        for (int n : SEARCH_SORT_SIZES) {
            DynamicArray<Integer> sorted = randomSortedArray(n, rng);
            int target = sorted.get(rng.nextInt(n)); // guaranteed present

            long[] linTimes = new long[TRIALS];
            long[] binTimes = new long[TRIALS];
            for (int t = 0; t < TRIALS; t++) {
                long s1 = System.nanoTime();
                SortingEngine.linearSearch(sorted, target, Integer::compareTo);
                linTimes[t] = System.nanoTime() - s1;

                long s2 = System.nanoTime();
                SortingEngine.binarySearch(sorted, target, Integer::compareTo);
                binTimes[t] = System.nanoTime() - s2;
            }
            record("SearchComparison", "LinearSearch", n, linTimes, 0, "target=" + target);
            record("SearchComparison", "BinarySearch", n, binTimes, 0, "target=" + target);
        }
        System.out.println();
    }

    // ── 2. Sorting comparison ───────────────────────────────────────────
    private static void benchmarkSorting() {
        System.out.println("-- Sorting comparison (selection, insertion, merge, quicksort) --");
        Random rng = new Random(SEED);
        for (int n : SEARCH_SORT_SIZES) {
            int[] base = randomInts(n, rng);

            long[] selTimes = new long[TRIALS];
            long[] insTimes = new long[TRIALS];
            long[] mrgTimes = new long[TRIALS];
            long[] qckTimes = new long[TRIALS];
            for (int t = 0; t < TRIALS; t++) {
                selTimes[t] = timeSort(base, SortAlgo.SELECTION);
                insTimes[t] = timeSort(base, SortAlgo.INSERTION);
                mrgTimes[t] = timeSort(base, SortAlgo.MERGE);
                qckTimes[t] = timeSort(base, SortAlgo.QUICK);
            }
            record("SortingComparison", "SelectionSort", n, selTimes, 0, "");
            record("SortingComparison", "InsertionSort", n, insTimes, 0, "");
            record("SortingComparison", "MergeSort", n, mrgTimes, 0, "");
            record("SortingComparison", "QuickSort", n, qckTimes, 0, "");
        }
        System.out.println();
    }

    private enum SortAlgo { SELECTION, INSERTION, MERGE, QUICK }

    private static long timeSort(int[] base, SortAlgo algo) {
        DynamicArray<Integer> arr = toDynamicArray(base); // fresh unsorted copy each trial
        long start = System.nanoTime();
        switch (algo) {
            case SELECTION: SortingEngine.selectionSort(arr, Integer::compareTo); break;
            case INSERTION: SortingEngine.insertionSort(arr, Integer::compareTo); break;
            case MERGE:     SortingEngine.mergeSort(arr, Integer::compareTo); break;
            case QUICK:     SortingEngine.quickSort(arr, Integer::compareTo); break;
        }
        return System.nanoTime() - start;
    }

    // ── 3. Hash table load factor ───────────────────────────────────────
    private static void benchmarkHashLoadFactor() {
        System.out.println("-- Hash table load factor vs collisions/time --");
        Random rng = new Random(SEED);
        for (int n : HASH_HEAP_SIZES) {
            int[] keys = randomInts(n, rng);
            for (double lf : LOAD_FACTORS) {
                // Capacity chosen so the load factor stays fixed and BELOW the
                // table's 0.75 auto-resize threshold throughout insertion.
                int capacity = (int) Math.ceil(n / lf);
                long[] times = new long[TRIALS];
                int lastCollisions = 0;
                for (int t = 0; t < TRIALS; t++) {
                    HashTable<Integer, Integer> table = new HashTable<>(capacity);
                    long start = System.nanoTime();
                    for (int k : keys) table.put(k, k);
                    times[t] = System.nanoTime() - start;
                    lastCollisions = table.getCollisionCount();
                }
                record("HashLoadFactor", "HashTable(loadFactor=" + lf + ")", n, times, 0,
                        "capacity=" + capacity + ";collisions=" + lastCollisions);
            }
        }
        System.out.println();
    }

    // ── 4. BST vs balanced (red-black) tree ─────────────────────────────
    private static void benchmarkBstVsBalancedTree() {
        System.out.println("-- BST vs Red-Black Tree (sorted-input worst case) --");
        for (int n : SEARCH_SORT_SIZES) {
            // Ascending sorted keys deliberately trigger the BST's worst case
            // (degenerates toward a linked list); RBT stays balanced regardless.
            long[] bstInsertTimes = new long[TRIALS];
            long[] rbtInsertTimes = new long[TRIALS];
            long[] bstSearchTimes = new long[TRIALS];
            long[] rbtSearchTimes = new long[TRIALS];
            int bstHeight = -1; // BST has no height() accessor -- reported separately below
            int rbtHeight = -1;

            for (int t = 0; t < TRIALS; t++) {
                BST<Integer, Integer> bst = new BST<>();
                long s1 = System.nanoTime();
                for (int i = 0; i < n; i++) bst.insert(i, i);
                bstInsertTimes[t] = System.nanoTime() - s1;

                RedBlackTree<Integer, Integer> rbt = new RedBlackTree<>();
                long s2 = System.nanoTime();
                for (int i = 0; i < n; i++) rbt.insert(i, i);
                rbtInsertTimes[t] = System.nanoTime() - s2;
                rbtHeight = rbt.height();

                int probe = n / 2;
                long s3 = System.nanoTime();
                bst.search(probe);
                bstSearchTimes[t] = System.nanoTime() - s3;

                long s4 = System.nanoTime();
                rbt.search(probe);
                rbtSearchTimes[t] = System.nanoTime() - s4;
            }
            record("BSTvsRBT", "BST-insert(sorted-input)", n, bstInsertTimes, 0, "");
            record("BSTvsRBT", "RBT-insert(sorted-input)", n, rbtInsertTimes, 0, "height=" + rbtHeight);
            record("BSTvsRBT", "BST-search(sorted-input)", n, bstSearchTimes, 0, "");
            record("BSTvsRBT", "RBT-search(sorted-input)", n, rbtSearchTimes, 0, "height=" + rbtHeight);
            System.out.println("  n=" + n + "  RBT height=" + rbtHeight +
                    "  (theoretical BST worst-case height on sorted input = n = " + n + ")");
        }
        System.out.println();
    }

    // ── 5. Heap priority dispatch ────────────────────────────────────────
    private static void benchmarkHeap() {
        System.out.println("-- MinHeap insert/extractMin --");
        Random rng = new Random(SEED);
        for (int n : HASH_HEAP_SIZES) {
            int[] keys = randomInts(n, rng);
            long[] insertTimes = new long[TRIALS];
            long[] extractTimes = new long[TRIALS];
            for (int t = 0; t < TRIALS; t++) {
                MinHeap<Integer> heap = new MinHeap<>(n + 1, Integer::compareTo);
                long s1 = System.nanoTime();
                for (int k : keys) heap.insert(k);
                insertTimes[t] = System.nanoTime() - s1;

                long s2 = System.nanoTime();
                while (!heap.isEmpty()) heap.extractMin();
                extractTimes[t] = System.nanoTime() - s2;
            }
            record("HeapDispatch", "MinHeap-insert", n, insertTimes, 0, "");
            record("HeapDispatch", "MinHeap-extractAll", n, extractTimes, 0, "");
        }
        System.out.println();
    }

    // ── 6. Graph algorithms ─────────────────────────────────────────────
    private static void benchmarkGraph() {
        System.out.println("-- Graph algorithms (BFS/DFS/Dijkstra/Kruskal/Prim) --");
        Random rng = new Random(SEED);
        for (int n : GRAPH_SIZES) {
            Graph g = new Graph(n + 1);
            DynamicArray<Location> locs = new DynamicArray<>();
            DynamicArray<RoadEdge> roads = new DynamicArray<>();
            for (int i = 1; i <= n; i++) {
                Location loc = new Location(i, "Node" + i, "Zone", "STOP", 0, 0);
                g.addLocation(loc);
                locs.add(loc);
            }
            // Ring (guarantees connectivity) plus random extra edges (~2n total edges).
            int roadId = 1;
            for (int i = 1; i <= n; i++) {
                int next = (i % n) + 1;
                double w = 1 + rng.nextInt(20);
                RoadEdge e = new RoadEdge(roadId++, i, next, "Node" + i, "Node" + next, w, (int) w, "LOW", "GOOD", 1.0, false, w);
                g.addRoad(e);
                roads.add(e);
            }
            int extraEdges = n; // roughly doubles edge count
            for (int e = 0; e < extraEdges; e++) {
                int a = 1 + rng.nextInt(n);
                int b = 1 + rng.nextInt(n);
                if (a == b) continue;
                double w = 1 + rng.nextInt(20);
                RoadEdge edge = new RoadEdge(roadId++, a, b, "Node" + a, "Node" + b, w, (int) w, "LOW", "GOOD", 1.0, false, w);
                g.addRoad(edge);
                roads.add(edge);
            }

            long[] bfsTimes = new long[TRIALS];
            long[] dfsTimes = new long[TRIALS];
            long[] dijkstraTimes = new long[TRIALS];
            long[] kruskalTimes = new long[TRIALS];
            long[] primTimes = new long[TRIALS];
            for (int t = 0; t < TRIALS; t++) {
                long s1 = System.nanoTime();
                RouteEngine.bfsReachable(g, 1);
                bfsTimes[t] = System.nanoTime() - s1;

                long s2 = System.nanoTime();
                RouteEngine.dfsTraversal(g, 1);
                dfsTimes[t] = System.nanoTime() - s2;

                long s3 = System.nanoTime();
                RouteEngine.dijkstra(g, 1, n);
                dijkstraTimes[t] = System.nanoTime() - s3;

                long s4 = System.nanoTime();
                RouteEngine.kruskalMST(locs, roads);
                kruskalTimes[t] = System.nanoTime() - s4;

                long s5 = System.nanoTime();
                RouteEngine.primMST(g);
                primTimes[t] = System.nanoTime() - s5;
            }
            String meta = "locations=" + n + ";roads=" + roads.size();
            record("GraphAlgorithms", "BFS", n, bfsTimes, 0, meta);
            record("GraphAlgorithms", "DFS", n, dfsTimes, 0, meta);
            record("GraphAlgorithms", "Dijkstra", n, dijkstraTimes, 0, meta);
            record("GraphAlgorithms", "Kruskal", n, kruskalTimes, 0, meta);
            record("GraphAlgorithms", "Prim", n, primTimes, 0, meta);
        }
        System.out.println();
    }

    // ── Shared helpers ───────────────────────────────────────────────────

    private static int[] randomInts(int n, Random rng) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rng.nextInt(n * 10);
        return arr;
    }

    private static DynamicArray<Integer> randomSortedArray(int n, Random rng) {
        int[] arr = randomInts(n, rng);
        java.util.Arrays.sort(arr);
        return toDynamicArray(arr);
    }

    private static DynamicArray<Integer> toDynamicArray(int[] src) {
        DynamicArray<Integer> arr = new DynamicArray<>(src.length);
        for (int v : src) arr.add(v);
        return arr;
    }

    /**
     * Averages the trial times, estimates a memory delta, writes one CSV row,
     * inserts one row into the algorithm_runs table, and prints a summary line.
     */
    private static void record(String experiment, String algorithm, int inputSize, long[] timesNs, long memoryKbHint, String extra) {
        long sum = 0;
        for (long v : timesNs) sum += v;
        long avgNs = sum / timesNs.length;
        double avgMs = avgNs / 1_000_000.0;

        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long memKb = (rt.totalMemory() - rt.freeMemory()) / 1024;

        String dateRun = DATE_FMT.format(new Date());
        csv.printf("%s,%s,%d,%d,%.4f,%d,%s,%s%n", experiment, algorithm, inputSize, avgNs, avgMs, memKb, extra, dateRun);
        csv.flush();

        DatabaseManager.addAlgorithmRun(new AlgorithmRun(runIdCounter++, experiment + ":" + algorithm, inputSize, avgNs, memKb, dateRun));

        System.out.printf("  [%s] %-28s n=%-6d avg=%,10d ns (%.3f ms)  %s%n", experiment, algorithm, inputSize, avgNs, avgMs, extra);
    }
}