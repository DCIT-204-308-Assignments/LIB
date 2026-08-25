import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import ds.DynamicArray;
import ds.Graph;
import engines.DatabaseManager;
import engines.OptimisationEngine;
import engines.RouteEngine;
import engines.SortingEngine;
import models.AlgorithmRun;
import models.Location;
import models.RoadEdge;
import models.ServiceRequest;

public class AlgorithmBenchmark {

    private static final int REPETITIONS = 3;

    private static final String CSV_PATH =
            "evidence/performance/performance_results.csv";

    private static class CountingComparator
            implements Comparator<Integer> {

        long comparisons = 0;

        @Override
        public int compare(Integer a, Integer b) {
            comparisons++;
            return Integer.compare(a, b);
        }
    }

    private static class Measurement {
        final long timeNs;
        final long memoryKb;
        final long operations;
        final long comparisons;
        final String status;
        final String summary;

        Measurement(
                long timeNs,
                long memoryKb,
                long operations,
                long comparisons,
                String status,
                String summary
        ) {
            this.timeNs = timeNs;
            this.memoryKb = memoryKb;
            this.operations = operations;
            this.comparisons = comparisons;
            this.status = status;
            this.summary = summary;
        }
    }

    private static class GraphBundle {
        final Graph graph;
        final DynamicArray<Location> locations;
        final DynamicArray<RoadEdge> roads;

        GraphBundle(
                Graph graph,
                DynamicArray<Location> locations,
                DynamicArray<RoadEdge> roads
        ) {
            this.graph = graph;
            this.locations = locations;
            this.roads = roads;
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println(
                "UG Swift Algorithm Performance Benchmark"
        );
        System.out.println(
                "========================================"
        );

        DatabaseManager.initializeDatabase(
                "data/locations.csv",
                "data/roads.csv"
        );

        // Ensure a repeatable evidence set.
        DatabaseManager.clearAlgorithmRuns();

        Path csvPath = Paths.get(CSV_PATH);
        Files.createDirectories(csvPath.getParent());

        try (PrintWriter csv = new PrintWriter(
                Files.newBufferedWriter(csvPath)
        )) {
            csv.println(
                    "algorithmName,inputSize,timeNs,memoryKb,"
                            + "operationsCount,comparisonsCount,status,"
                            + "resultSummary,dateRun"
            );

            benchmarkSorting(csv);
            benchmarkSearching(csv);
            benchmarkGraphs(csv);
            benchmarkOptimisation(csv);
        }

        int storedRuns =
                DatabaseManager.loadAlgorithmRuns().size();

        System.out.println();
        System.out.println(
                "Benchmark complete."
        );

        System.out.println(
                "Algorithm-run records stored: "
                        + storedRuns
        );

        System.out.println(
                "CSV written to: " + CSV_PATH
        );

        if (storedRuns < 30) {
            throw new IllegalStateException(
                    "Expected at least 30 algorithm-run records."
            );
        }

        System.out.println(
                "Requirement satisfied: at least 30 runs."
        );
    }

    // ── Sorting ───────────────────────────────────────────────────────

    private static void benchmarkSorting(
            PrintWriter csv
    ) {
        int[] sizes = {100, 500, 1000};

        String[] algorithms = {
                "Selection Sort",
                "Insertion Sort",
                "Merge Sort",
                "Quick Sort"
        };

        for (String algorithm : algorithms) {
            for (int size : sizes) {

                Measurement measurement =
                        measureSort(
                                algorithm,
                                size
                        );

                store(
                        csv,
                        algorithm,
                        size,
                        measurement
                );
            }
        }
    }

    private static Measurement measureSort(
            String algorithm,
            int size
    ) {
        // JVM warm-up.
        DynamicArray<Integer> warmup =
                createUnsortedData(size);

        CountingComparator warmComparator =
                new CountingComparator();

        runSort(
                algorithm,
                warmup,
                warmComparator
        );

        long[] times =
                new long[REPETITIONS];

        long[] memories =
                new long[REPETITIONS];

        long[] comparisons =
                new long[REPETITIONS];

        boolean success = true;

        for (int r = 0; r < REPETITIONS; r++) {

            DynamicArray<Integer> data =
                    createUnsortedData(size);

            CountingComparator comparator =
                    new CountingComparator();

            long memoryBefore =
                    usedMemoryKb();

            long start =
                    System.nanoTime();

            runSort(
                    algorithm,
                    data,
                    comparator
            );

            long end =
                    System.nanoTime();

            long memoryAfter =
                    usedMemoryKb();

            times[r] =
                    end - start;

            memories[r] =
                    Math.max(
                            0L,
                            memoryAfter - memoryBefore
                    );

            comparisons[r] =
                    comparator.comparisons;

            if (!SortingEngine.isSorted(
                    data,
                    Integer::compareTo
            )) {
                success = false;
            }
        }

        return new Measurement(
                median3(times),
                median3(memories),
                0L,
                median3(comparisons),
                success ? "SUCCESS" : "FAILED",
                "Median of 3 timed executions"
        );
    }

    private static void runSort(
            String algorithm,
            DynamicArray<Integer> data,
            Comparator<Integer> comparator
    ) {
        switch (algorithm) {

            case "Selection Sort":
                SortingEngine.selectionSort(
                        data,
                        comparator
                );
                break;

            case "Insertion Sort":
                SortingEngine.insertionSort(
                        data,
                        comparator
                );
                break;

            case "Merge Sort":
                SortingEngine.mergeSort(
                        data,
                        comparator
                );
                break;

            case "Quick Sort":
                SortingEngine.quickSort(
                        data,
                        comparator
                );
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown sorting algorithm: "
                                + algorithm
                );
        }
    }

    // ── Searching ─────────────────────────────────────────────────────

    private static void benchmarkSearching(
            PrintWriter csv
    ) {
        int[] sizes = {
                1000,
                5000,
                10000
        };

        for (int size : sizes) {

            store(
                    csv,
                    "Linear Search",
                    size,
                    measureSearch(
                            "Linear Search",
                            size
                    )
            );

            store(
                    csv,
                    "Binary Search",
                    size,
                    measureSearch(
                            "Binary Search",
                            size
                    )
            );
        }
    }

    private static Measurement measureSearch(
            String algorithm,
            int size
    ) {
        DynamicArray<Integer> data =
                createSortedData(size);

        int target =
                size - 1;

        // JVM warm-up.
        CountingComparator warmComparator =
                new CountingComparator();

        runSearch(
                algorithm,
                data,
                target,
                warmComparator
        );

        long[] times =
                new long[REPETITIONS];

        long[] memories =
                new long[REPETITIONS];

        long[] comparisons =
                new long[REPETITIONS];

        boolean success = true;

        for (int r = 0; r < REPETITIONS; r++) {

            CountingComparator comparator =
                    new CountingComparator();

            long memoryBefore =
                    usedMemoryKb();

            long start =
                    System.nanoTime();

            int index =
                    runSearch(
                            algorithm,
                            data,
                            target,
                            comparator
                    );

            long end =
                    System.nanoTime();

            long memoryAfter =
                    usedMemoryKb();

            times[r] =
                    end - start;

            memories[r] =
                    Math.max(
                            0L,
                            memoryAfter - memoryBefore
                    );

            comparisons[r] =
                    comparator.comparisons;

            if (index != target) {
                success = false;
            }
        }

        return new Measurement(
                median3(times),
                median3(memories),
                0L,
                median3(comparisons),
                success ? "SUCCESS" : "FAILED",
                "Target near end of sorted input; median of 3 executions"
        );
    }

    private static int runSearch(
            String algorithm,
            DynamicArray<Integer> data,
            int target,
            Comparator<Integer> comparator
    ) {
        if ("Linear Search".equals(algorithm)) {
            return SortingEngine.linearSearch(
                    data,
                    target,
                    comparator
            );
        }

        if ("Binary Search".equals(algorithm)) {
            return SortingEngine.binarySearch(
                    data,
                    target,
                    comparator
            );
        }

        throw new IllegalArgumentException(
                "Unknown search algorithm: "
                        + algorithm
        );
    }

    // ── Graph Algorithms ───────────────────────────────────────────────

    private static void benchmarkGraphs(
            PrintWriter csv
    ) {
        int[] sizes = {
                20,
                50,
                90
        };

        for (int size : sizes) {

            store(
                    csv,
                    "BFS",
                    size,
                    measureGraphAlgorithm(
                            "BFS",
                            size
                    )
            );

            store(
                    csv,
                    "DFS",
                    size,
                    measureGraphAlgorithm(
                            "DFS",
                            size
                    )
            );

            store(
                    csv,
                    "Dijkstra",
                    size,
                    measureGraphAlgorithm(
                            "Dijkstra",
                            size
                    )
            );

            store(
                    csv,
                    "Prim",
                    size,
                    measureGraphAlgorithm(
                            "Prim",
                            size
                    )
            );

            store(
                    csv,
                    "Kruskal",
                    size,
                    measureGraphAlgorithm(
                            "Kruskal",
                            size
                    )
            );
        }
    }

    private static Measurement measureGraphAlgorithm(
            String algorithm,
            int size
    ) {
        // JVM warm-up.
        GraphBundle warmup =
                createGraph(size);

        runGraphAlgorithm(
                algorithm,
                warmup,
                size
        );

        long[] times =
                new long[REPETITIONS];

        long[] memories =
                new long[REPETITIONS];

        boolean success = true;

        String summary = "";

        for (int r = 0; r < REPETITIONS; r++) {

            GraphBundle bundle =
                    createGraph(size);

            long memoryBefore =
                    usedMemoryKb();

            long start =
                    System.nanoTime();

            String result =
                    runGraphAlgorithm(
                            algorithm,
                            bundle,
                            size
                    );

            long end =
                    System.nanoTime();

            long memoryAfter =
                    usedMemoryKb();

            times[r] =
                    end - start;

            memories[r] =
                    Math.max(
                            0L,
                            memoryAfter - memoryBefore
                    );

            summary = result;

            if (result.startsWith("FAILED")) {
                success = false;
            }
        }

        return new Measurement(
                median3(times),
                median3(memories),
                0L,
                0L,
                success ? "SUCCESS" : "FAILED",
                summary
                        + "; median of 3 timed executions"
        );
    }

    private static String runGraphAlgorithm(
            String algorithm,
            GraphBundle bundle,
            int size
    ) {
        switch (algorithm) {

            case "BFS": {
                DynamicArray<Integer> traversal =
                        RouteEngine.bfsReachable(
                                bundle.graph,
                                1
                        );

                if (traversal.size() != size) {
                    return "FAILED: visited="
                            + traversal.size();
                }

                return "Visited locations="
                        + traversal.size();
            }

            case "DFS": {
                DynamicArray<Integer> traversal =
                        RouteEngine.dfsTraversal(
                                bundle.graph,
                                1
                        );

                if (traversal.size() != size) {
                    return "FAILED: visited="
                            + traversal.size();
                }

                return "Visited locations="
                        + traversal.size();
            }

            case "Dijkstra": {
                RouteEngine.PathResult result =
                        RouteEngine.dijkstra(
                                bundle.graph,
                                1,
                                size
                        );

                if (result == null
                        || result.path.isEmpty()) {
                    return "FAILED: no path";
                }

                return "Path nodes="
                        + result.path.size()
                        + " totalWeight="
                        + result.totalWeight;
            }

            case "Prim": {
                DynamicArray<RoadEdge> mst =
                        RouteEngine.primMST(
                                bundle.graph
                        );

                if (mst.size() != size - 1) {
                    return "FAILED: MST edges="
                            + mst.size();
                }

                return "MST edges="
                        + mst.size();
            }

            case "Kruskal": {
                DynamicArray<RoadEdge> mst =
                        RouteEngine.kruskalMST(
                                bundle.locations,
                                bundle.roads
                        );

                if (mst.size() != size - 1) {
                    return "FAILED: MST edges="
                            + mst.size();
                }

                return "MST edges="
                        + mst.size();
            }

            default:
                throw new IllegalArgumentException(
                        "Unknown graph algorithm: "
                                + algorithm
                );
        }
    }

    // ── Optimisation ──────────────────────────────────────────────────

    private static void benchmarkOptimisation(
            PrintWriter csv
    ) {
        int[] greedySizes = {
                5,
                10,
                20
        };

        for (int size : greedySizes) {

            store(
                    csv,
                    "Greedy Nearest Neighbor",
                    size,
                    measureGreedyNearestNeighbor(
                            size
                    )
            );
        }

        int[] dpSizes = {
                20,
                50,
                100
        };

        for (int size : dpSizes) {

            store(
                    csv,
                    "Dynamic Programming Knapsack",
                    size,
                    measureDp(size)
            );
        }

        int[] bruteSizes = {
                8,
                12,
                16
        };

        for (int size : bruteSizes) {

            store(
                    csv,
                    "Brute Force Batching",
                    size,
                    measureBruteForce(size)
            );
        }
    }

    private static Measurement measureGreedyNearestNeighbor(
            int requestCount
    ) {
        int graphSize =
                Math.max(
                        20,
                        requestCount * 4
                );

        // JVM warm-up.
        GraphBundle warmup =
                createGraph(graphSize);

        OptimisationEngine.greedyNearestNeighbor(
                warmup.graph,
                1,
                createRoutingRequests(
                        requestCount,
                        graphSize
                )
        );

        long[] times =
                new long[REPETITIONS];

        long[] memories =
                new long[REPETITIONS];

        boolean success = true;

        int selected = 0;

        for (int r = 0; r < REPETITIONS; r++) {

            GraphBundle bundle =
                    createGraph(graphSize);

            DynamicArray<ServiceRequest> requests =
                    createRoutingRequests(
                            requestCount,
                            graphSize
                    );

            long memoryBefore =
                    usedMemoryKb();

            long start =
                    System.nanoTime();

            DynamicArray<ServiceRequest> result =
                    OptimisationEngine
                            .greedyNearestNeighbor(
                                    bundle.graph,
                                    1,
                                    requests
                            );

            long end =
                    System.nanoTime();

            long memoryAfter =
                    usedMemoryKb();

            times[r] =
                    end - start;

            memories[r] =
                    Math.max(
                            0L,
                            memoryAfter - memoryBefore
                    );

            selected =
                    result.size();

            if (selected != requestCount) {
                success = false;
            }
        }

        return new Measurement(
                median3(times),
                median3(memories),
                0L,
                0L,
                success ? "SUCCESS" : "FAILED",
                "Requests routed="
                        + selected
                        + "; graphLocations="
                        + graphSize
                        + "; median of 3 executions"
        );
    }

    private static Measurement measureDp(
            int size
    ) {
        double capacityKg =
                Math.max(
                        5.0,
                        size / 5.0
                );

        // JVM warm-up.
        OptimisationEngine.dpKnapsackBatching(
                createRequests(
                        size,
                        false
                ),
                capacityKg
        );

        long[] times =
                new long[REPETITIONS];

        long[] memories =
                new long[REPETITIONS];

        int selected = 0;

        for (int r = 0; r < REPETITIONS; r++) {

            DynamicArray<ServiceRequest> requests =
                    createRequests(
                            size,
                            false
                    );

            long memoryBefore =
                    usedMemoryKb();

            long start =
                    System.nanoTime();

            DynamicArray<ServiceRequest> result =
                    OptimisationEngine
                            .dpKnapsackBatching(
                                    requests,
                                    capacityKg
                            );

            long end =
                    System.nanoTime();

            long memoryAfter =
                    usedMemoryKb();

            times[r] =
                    end - start;

            memories[r] =
                    Math.max(
                            0L,
                            memoryAfter - memoryBefore
                    );

            selected =
                    result.size();
        }

        return new Measurement(
                median3(times),
                median3(memories),
                0L,
                0L,
                "SUCCESS",
                "Selected requests="
                        + selected
                        + "; capacityKg="
                        + capacityKg
                        + "; median of 3 executions"
        );
    }

    private static Measurement measureBruteForce(
            int size
    ) {
        // High capacity deliberately exercises all subsets.
        double capacityKg =
                1000.0;

        // JVM warm-up.
        OptimisationEngine.bruteForceBatching(
                createRequests(
                        size,
                        true
                ),
                capacityKg
        );

        long[] times =
                new long[REPETITIONS];

        long[] memories =
                new long[REPETITIONS];

        int selected = 0;

        for (int r = 0; r < REPETITIONS; r++) {

            DynamicArray<ServiceRequest> requests =
                    createRequests(
                            size,
                            true
                    );

            long memoryBefore =
                    usedMemoryKb();

            long start =
                    System.nanoTime();

            DynamicArray<ServiceRequest> result =
                    OptimisationEngine
                            .bruteForceBatching(
                                    requests,
                                    capacityKg
                            );

            long end =
                    System.nanoTime();

            long memoryAfter =
                    usedMemoryKb();

            times[r] =
                    end - start;

            memories[r] =
                    Math.max(
                            0L,
                            memoryAfter - memoryBefore
                    );

            selected =
                    result.size();
        }

        return new Measurement(
                median3(times),
                median3(memories),
                0L,
                0L,
                "SUCCESS",
                "Enumerated subsets="
                        + (1L << size)
                        + "; selected="
                        + selected
                        + "; median of 3 executions"
        );
    }

    // ── Evidence Storage ───────────────────────────────────────────────

    private static void store(
            PrintWriter csv,
            String algorithm,
            int inputSize,
            Measurement measurement
    ) {
        String dateRun =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss"
                ).format(new Date());

        AlgorithmRun run =
                new AlgorithmRun(
                        0,
                        algorithm,
                        inputSize,
                        measurement.timeNs,
                        measurement.memoryKb,
                        dateRun,
                        measurement.operations,
                        measurement.comparisons,
                        measurement.status,
                        measurement.summary
                );

        DatabaseManager.addAlgorithmRun(run);

        csv.printf(
                "%s,%d,%d,%d,%d,%d,%s,%s,%s%n",
                csvEscape(algorithm),
                inputSize,
                measurement.timeNs,
                measurement.memoryKb,
                measurement.operations,
                measurement.comparisons,
                csvEscape(measurement.status),
                csvEscape(measurement.summary),
                csvEscape(dateRun)
        );

        System.out.printf(
                "%-30s n=%-6d time=%-12dns comparisons=%d%n",
                algorithm,
                inputSize,
                measurement.timeNs,
                measurement.comparisons
        );
    }

    // ── Deterministic Test Data ────────────────────────────────────────

    private static DynamicArray<Integer>
    createUnsortedData(
            int size
    ) {
        DynamicArray<Integer> data =
                new DynamicArray<>(size);

        for (int i = 0; i < size; i++) {

            int value =
                    (i * 73
                            + size * 31
                            + 17)
                            % (size * 10 + 101);

            data.add(value);
        }

        return data;
    }

    private static DynamicArray<Integer>
    createSortedData(
            int size
    ) {
        DynamicArray<Integer> data =
                new DynamicArray<>(size);

        for (int i = 0; i < size; i++) {
            data.add(i);
        }

        return data;
    }

    private static DynamicArray<ServiceRequest>
    createRequests(
            int size,
            boolean documentsOnly
    ) {
        DynamicArray<ServiceRequest> requests =
                new DynamicArray<>(size);

        String[] categories = {
                "Documents",
                "Pizza",
                "Waakye",
                "Pharmacy"
        };

        for (int i = 0; i < size; i++) {

            String category =
                    documentsOnly
                            ? "Documents"
                            : categories[
                                    i
                                            % categories.length
                            ];

            int urgency =
                    1 + (i % 5);

            double submitted =
                    480.0 + i;

            double deadline =
                    600.0 + (i % 300);

            requests.add(
                    new ServiceRequest(
                            10000 + i,
                            1,
                            2,
                            category,
                            urgency,
                            submitted,
                            deadline,
                            "PENDING",
                            -1
                    )
            );
        }

        return requests;
    }

    private static DynamicArray<ServiceRequest>
    createRoutingRequests(
            int count,
            int graphSize
    ) {
        DynamicArray<ServiceRequest> requests =
                new DynamicArray<>(count);

        for (int i = 0; i < count; i++) {

            int destination =
                    2 + (
                            (i * 7)
                                    % (graphSize - 1)
                    );

            requests.add(
                    new ServiceRequest(
                            20000 + i,
                            1,
                            destination,
                            "Documents",
                            1 + (i % 5),
                            480.0 + i,
                            700.0 + i,
                            "PENDING",
                            -1
                    )
            );
        }

        return requests;
    }

    private static GraphBundle createGraph(
            int size
    ) {
        Graph graph =
                new Graph(size);

        DynamicArray<Location> locations =
                new DynamicArray<>(size);

        DynamicArray<RoadEdge> roads =
                new DynamicArray<>();

        for (int i = 1; i <= size; i++) {

            Location location =
                    new Location(
                            i,
                            "Benchmark Location " + i,
                            "Benchmark Zone",
                            "TEST",
                            5.6500 + i * 0.0001,
                            -0.1870
                    );

            locations.add(location);
            graph.addLocation(location);
        }

        int roadId = 1;

        // Chain guarantees connectivity.
        for (int i = 1; i < size; i++) {

            double weight =
                    1.0
                            + (i % 5) * 0.1;

            RoadEdge road =
                    createRoad(
                            roadId++,
                            i,
                            i + 1,
                            weight
                    );

            roads.add(road);
            graph.addRoad(road);
        }

        // Extra edges create alternative routes.
        for (int i = 1; i <= size - 2; i++) {

            double weight =
                    2.4
                            + (i % 4) * 0.1;

            RoadEdge road =
                    createRoad(
                            roadId++,
                            i,
                            i + 2,
                            weight
                    );

            roads.add(road);
            graph.addRoad(road);
        }

        return new GraphBundle(
                graph,
                locations,
                roads
        );
    }

    private static RoadEdge createRoad(
            int roadId,
            int from,
            int to,
            double weight
    ) {
        return new RoadEdge(
                roadId,
                from,
                to,
                "Benchmark Location " + from,
                "Benchmark Location " + to,
                weight,
                weight * 2.0,
                "LOW",
                "GOOD",
                1.0,
                false,
                weight
        );
    }

    // ── Utility ────────────────────────────────────────────────────────

    private static long usedMemoryKb() {

        Runtime runtime =
                Runtime.getRuntime();

        return (
                runtime.totalMemory()
                        - runtime.freeMemory()
        ) / 1024L;
    }

    private static long median3(
            long[] values
    ) {
        if (values.length != 3) {

            throw new IllegalArgumentException(
                    "Expected exactly 3 measurements."
            );
        }

        long a = values[0];
        long b = values[1];
        long c = values[2];

        if (a > b) {
            long temp = a;
            a = b;
            b = temp;
        }

        if (b > c) {
            long temp = b;
            b = c;
            c = temp;
        }

        if (a > b) {
            long temp = a;
            a = b;
            b = temp;
        }

        return b;
    }

    private static String csvEscape(
            String value
    ) {
        if (value == null) {
            return "";
        }

        String escaped =
                value.replace(
                        "\"",
                        "\"\""
                );

        if (escaped.contains(",")
                || escaped.contains("\"")
                || escaped.contains("\n")) {

            return "\""
                    + escaped
                    + "\"";
        }

        return escaped;
    }
}