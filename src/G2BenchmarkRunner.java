import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import ds.BST;
import ds.BTree;
import ds.DynamicArray;
import ds.Graph;
import ds.HashTable;
import ds.RedBlackTree;
import engines.RouteEngine;
import engines.SortingEngine;
import models.Location;
import models.RoadEdge;

/**
 * UG Swift — Group 2 Algorithmic Benchmark Runner
 * 
 * Measures actual execution time (nanoseconds) for all required G2 algorithms
 * across increasing input sizes (N) and multiple trials.
 * 
 * Features:
 * - JVM Warm-up phase to reduce JIT compilation artifacts.
 * - Strict timing isolation (excludes data creation & CSV writes from timing).
 * - Correctness verification for every single execution.
 * - Outputs results to g2_benchmark_results.csv.
 */
public class G2BenchmarkRunner {

    private static final int TRIALS = 5;
    // Written straight to the evidence folder that G2_PERFORMANCE_ANALYSIS.md
    // and tools/performance_stats.py both read. Previously this landed in the
    // working directory, so every re-run needed a manual copy and the
    // published figures could silently describe an older run.
    private static final String CSV_FILENAME = "evidence/performance/g2_benchmark_results.csv";

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("UG SWIFT — GROUP 2 ALGORITHMIC PERFORMANCE BENCHMARK RUNNER");
        System.out.println("=================================================================\n");

        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(CSV_FILENAME))) {
            // Write CSV Header
            csvWriter.write("algorithm,input_size,trial,execution_time_ns,result_correct\n");

            // Warm-up phase
            runWarmup();

            // 1. Benchmark Searching
            System.out.println("--> Benchmarking Searching Algorithms...");
            benchmarkSearching(csvWriter);

            // 2. Benchmark Sorting
            System.out.println("--> Benchmarking Sorting Algorithms...");
            benchmarkSorting(csvWriter);

            // 3. Benchmark Indexing Structures
            System.out.println("--> Benchmarking Indexing Data Structures...");
            benchmarkIndexing(csvWriter);

            // 4. Benchmark Graph & MST Algorithms
            System.out.println("--> Benchmarking Graph & MST Algorithms...");
            benchmarkGraphAlgorithms(csvWriter);

            csvWriter.flush();
            System.out.println("\n=================================================================");
            System.out.println("BENCHMARK COMPLETED SUCCESSFULLY!");
            System.out.println("Results exported to: " + CSV_FILENAME);
            System.out.println("=================================================================\n");

        } catch (IOException e) {
            System.err.println("Error writing benchmark CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── 0. Warm-Up Phase ──────────────────────────────────────────────────
    private static void runWarmup() {
        System.out.print("Executing JVM Warm-up phase... ");
        DynamicArray<Integer> arr = createUnsortedArray(500);
        for (int i = 0; i < 50; i++) {
            DynamicArray<Integer> copy = copyArray(arr);
            SortingEngine.quickSort(copy, Integer::compareTo);
            SortingEngine.binarySearch(copy, 250, Integer::compareTo);
        }
        System.out.println("DONE.\n");
    }

    // ── 1. Searching Benchmarks ───────────────────────────────────────────
    private static void benchmarkSearching(BufferedWriter csvWriter) throws IOException {
        int[] sizes = {100, 500, 1000, 5000, 10000};

        for (int n : sizes) {
            DynamicArray<Integer> sortedData = createSortedArray(n);
            int target = n - 1; // Search for last element (worst-case for linear search)

            // Linear Search
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                int idx = SortingEngine.linearSearch(sortedData, target, Integer::compareTo);
                long elapsed = System.nanoTime() - start;

                boolean correct = (idx == n - 1);
                recordResult(csvWriter, "Linear Search", n, trial, elapsed, correct);
            }

            // Binary Search
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                int idx = SortingEngine.binarySearch(sortedData, target, Integer::compareTo);
                long elapsed = System.nanoTime() - start;

                boolean correct = (idx == n - 1);
                recordResult(csvWriter, "Binary Search", n, trial, elapsed, correct);
            }
        }
    }

    // ── 2. Sorting Benchmarks ─────────────────────────────────────────────
    private static void benchmarkSorting(BufferedWriter csvWriter) throws IOException {
        int[] fastSizes = {100, 500, 1000, 5000, 10000};
        int[] quadraticSizes = {100, 500, 1000, 5000}; // Capped at 5,000 for O(N^2) algorithms to prevent excessive runtime slowness

        // Selection Sort
        for (int n : quadraticSizes) {
            for (int trial = 1; trial <= TRIALS; trial++) {
                DynamicArray<Integer> data = createUnsortedArray(n);
                long start = System.nanoTime();
                SortingEngine.selectionSort(data, Integer::compareTo);
                long elapsed = System.nanoTime() - start;

                boolean correct = SortingEngine.isSorted(data, Integer::compareTo);
                recordResult(csvWriter, "Selection Sort", n, trial, elapsed, correct);
            }
        }

        // Insertion Sort
        for (int n : quadraticSizes) {
            for (int trial = 1; trial <= TRIALS; trial++) {
                DynamicArray<Integer> data = createUnsortedArray(n);
                long start = System.nanoTime();
                SortingEngine.insertionSort(data, Integer::compareTo);
                long elapsed = System.nanoTime() - start;

                boolean correct = SortingEngine.isSorted(data, Integer::compareTo);
                recordResult(csvWriter, "Insertion Sort", n, trial, elapsed, correct);
            }
        }

        // Merge Sort
        for (int n : fastSizes) {
            for (int trial = 1; trial <= TRIALS; trial++) {
                DynamicArray<Integer> data = createUnsortedArray(n);
                long start = System.nanoTime();
                SortingEngine.mergeSort(data, Integer::compareTo);
                long elapsed = System.nanoTime() - start;

                boolean correct = SortingEngine.isSorted(data, Integer::compareTo);
                recordResult(csvWriter, "Merge Sort", n, trial, elapsed, correct);
            }
        }

        // Quick Sort
        for (int n : fastSizes) {
            for (int trial = 1; trial <= TRIALS; trial++) {
                DynamicArray<Integer> data = createUnsortedArray(n);
                long start = System.nanoTime();
                SortingEngine.quickSort(data, Integer::compareTo);
                long elapsed = System.nanoTime() - start;

                boolean correct = SortingEngine.isSorted(data, Integer::compareTo);
                recordResult(csvWriter, "Quick Sort", n, trial, elapsed, correct);
            }
        }
    }

    // ── 3. Indexing Benchmarks ────────────────────────────────────────────
    private static void benchmarkIndexing(BufferedWriter csvWriter) throws IOException {
        int[] sizes = {100, 500, 1000, 5000, 10000};

        for (int n : sizes) {
            // Build index structures outside the timing loop
            BST<Integer, String> bst = new BST<>();
            RedBlackTree<Integer, String> rbt = new RedBlackTree<>();
            BTree<Integer, String> btree = new BTree<>();
            HashTable<Integer, String> ht = new HashTable<>(n);

            for (int i = 1; i <= n; i++) {
                String val = "Value_" + i;
                bst.insert(i, val);
                rbt.insert(i, val);
                btree.insert(i, val);
                ht.put(i, val);
            }

            int targetKey = n; // Search for last inserted key

            // BST Search
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                String res = bst.search(targetKey);
                long elapsed = System.nanoTime() - start;

                boolean correct = ("Value_" + targetKey).equals(res);
                recordResult(csvWriter, "BST Search", n, trial, elapsed, correct);
            }

            // Red-Black Tree Search
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                String res = rbt.search(targetKey);
                long elapsed = System.nanoTime() - start;

                boolean correct = ("Value_" + targetKey).equals(res);
                recordResult(csvWriter, "Red-Black Tree Search", n, trial, elapsed, correct);
            }

            // B-Tree Search
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                String res = btree.search(targetKey);
                long elapsed = System.nanoTime() - start;

                boolean correct = ("Value_" + targetKey).equals(res);
                recordResult(csvWriter, "B-Tree Search", n, trial, elapsed, correct);
            }

            // Hash Table Search
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                String res = ht.get(targetKey);
                long elapsed = System.nanoTime() - start;

                boolean correct = ("Value_" + targetKey).equals(res);
                recordResult(csvWriter, "Hash Table Search", n, trial, elapsed, correct);
            }
        }
    }

    // ── 4. Graph & MST Benchmarks ─────────────────────────────────────────
    private static void benchmarkGraphAlgorithms(BufferedWriter csvWriter) throws IOException {
        int[] graphSizes = {20, 50, 100, 250, 500};

        for (int n : graphSizes) {
            GraphBundle bundle = generateConnectedGraph(n);

            // BFS
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                DynamicArray<Integer> traversal = RouteEngine.bfsReachable(bundle.graph, 1);
                long elapsed = System.nanoTime() - start;

                boolean correct = (traversal != null && traversal.size() == n);
                recordResult(csvWriter, "BFS", n, trial, elapsed, correct);
            }

            // DFS
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                DynamicArray<Integer> traversal = RouteEngine.dfsTraversal(bundle.graph, 1);
                long elapsed = System.nanoTime() - start;

                boolean correct = (traversal != null && traversal.size() == n);
                recordResult(csvWriter, "DFS", n, trial, elapsed, correct);
            }

            // Dijkstra
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                RouteEngine.PathResult pathResult = RouteEngine.dijkstra(bundle.graph, 1, n);
                long elapsed = System.nanoTime() - start;

                boolean correct = (pathResult != null && !pathResult.path.isEmpty() && pathResult.totalWeight >= 0);
                recordResult(csvWriter, "Dijkstra", n, trial, elapsed, correct);
            }

            // Prim MST
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                DynamicArray<RoadEdge> mst = RouteEngine.primMST(bundle.graph);
                long elapsed = System.nanoTime() - start;

                boolean correct = (mst != null && mst.size() == n - 1);
                recordResult(csvWriter, "Prim MST", n, trial, elapsed, correct);
            }

            // Kruskal MST
            for (int trial = 1; trial <= TRIALS; trial++) {
                long start = System.nanoTime();
                DynamicArray<RoadEdge> mst = RouteEngine.kruskalMST(bundle.locations, bundle.roads);
                long elapsed = System.nanoTime() - start;

                boolean correct = (mst != null && mst.size() == n - 1);
                recordResult(csvWriter, "Kruskal MST", n, trial, elapsed, correct);
            }
        }
    }

    // ── Helper Utilities ──────────────────────────────────────────────────
    private static void recordResult(BufferedWriter csvWriter, String algorithm, int inputSize, int trial, long elapsedNs, boolean correct) throws IOException {
        String line = String.format("%s,%d,%d,%d,%b\n", algorithm, inputSize, trial, elapsedNs, correct);
        csvWriter.write(line);
    }

    private static DynamicArray<Integer> createUnsortedArray(int size) {
        DynamicArray<Integer> arr = new DynamicArray<>(size);
        for (int i = 0; i < size; i++) {
            // Pseudo-random deterministic distribution
            int val = (i * 73 + size * 31 + 17) % (size * 10 + 101);
            arr.add(val);
        }
        return arr;
    }

    private static DynamicArray<Integer> createSortedArray(int size) {
        DynamicArray<Integer> arr = new DynamicArray<>(size);
        for (int i = 0; i < size; i++) {
            arr.add(i);
        }
        return arr;
    }

    private static DynamicArray<Integer> copyArray(DynamicArray<Integer> src) {
        DynamicArray<Integer> copy = new DynamicArray<>(src.size());
        for (int i = 0; i < src.size(); i++) {
            copy.add(src.get(i));
        }
        return copy;
    }

    private static class GraphBundle {
        final Graph graph;
        final DynamicArray<Location> locations;
        final DynamicArray<RoadEdge> roads;

        GraphBundle(Graph graph, DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
            this.graph = graph;
            this.locations = locations;
            this.roads = roads;
        }
    }

    private static GraphBundle generateConnectedGraph(int size) {
        Graph graph = new Graph(size);
        DynamicArray<Location> locations = new DynamicArray<>(size);
        DynamicArray<RoadEdge> roads = new DynamicArray<>();

        for (int i = 1; i <= size; i++) {
            Location loc = new Location(i, "Loc_" + i, "Zone", "CAMPUS", 5.65 + i * 0.001, -0.18);
            locations.add(loc);
            graph.addLocation(loc);
        }

        int roadId = 1;
        // Chain edges to guarantee 100% connectivity
        for (int i = 1; i < size; i++) {
            double weight = 1.0 + (i % 5) * 0.2;
            RoadEdge edge = new RoadEdge(roadId++, i, i + 1, "Loc_" + i, "Loc_" + (i + 1), weight, weight * 2.0, "LOW", "GOOD", 1.0, false, weight);
            roads.add(edge);
            graph.addRoad(edge);
        }

        // Additional cross-edges to simulate a real road network
        for (int i = 1; i <= size - 2; i++) {
            double weight = 2.5 + (i % 4) * 0.3;
            RoadEdge edge = new RoadEdge(roadId++, i, i + 2, "Loc_" + i, "Loc_" + (i + 2), weight, weight * 2.0, "LOW", "GOOD", 1.0, false, weight);
            roads.add(edge);
            graph.addRoad(edge);
        }

        return new GraphBundle(graph, locations, roads);
    }
}
