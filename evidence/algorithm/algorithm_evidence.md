# UG Swift Algorithm Evidence

This document provides algorithm trace tables, correctness proof sketches, counterexamples and precondition evidence, dataset verification, unit-test evidence, and performance analysis for the UG Swift campus delivery system.

---

## 1. Dataset Verification

The current UG Swift dataset satisfies the required minimum dataset sizes.

| Dataset item     | Current count | Required minimum | Status    |
| ---------------- | ------------: | ---------------: | --------- |
| Locations        |            95 |               50 | Satisfied |
| Roads            |           382 |              100 | Satisfied |
| Service requests |           391 |              300 | Satisfied |
| Resources/Riders |            30 |               30 | Satisfied |
| Algorithm runs   |            42 |               30 | Satisfied |

The performance benchmark generated **42 algorithm-run records** and persisted them in the SQLite `algorithm_runs` table.

The corresponding CSV evidence is stored at:

`evidence/performance/performance_results.csv`

---

## 2. Trace-Test Graph

The graph traces use the same deterministic four-location graph used by the permanent routing unit tests.

Vertices:

`1, 2, 3, 4`

| Edge  | Weight | Distance (km) | Travel time (min) |
| ----- | -----: | ------------: | ----------------: |
| 1 - 2 |      1 |             1 |                 2 |
| 2 - 3 |      2 |             2 |                 3 |
| 1 - 3 |      5 |             5 |                 7 |
| 3 - 4 |      1 |             1 |                 2 |
| 2 - 4 |      6 |             6 |                 8 |

---

## 3. Trace Table 1 — Breadth-First Search

Start vertex: `1`

BFS uses a queue and explores connected locations level by level.

| Step | Vertex removed | Newly discovered | Queue after processing | Visited order |
| ---: | -------------: | ---------------- | ---------------------- | ------------- |
|    1 |              1 | 2, 3             | 2, 3                   | 1             |
|    2 |              2 | 4                | 3, 4                   | 1, 2          |
|    3 |              3 | None             | 4                      | 1, 2, 3       |
|    4 |              4 | None             | Empty                  | 1, 2, 3, 4    |

Final traversal:

`1 -> 2 -> 3 -> 4`

All four locations in the connected component are reached.

**Complexity:** `O(V + E)` time and `O(V)` auxiliary space.

---

## 4. Trace Table 2 — Depth-First Search

Start vertex: `1`

UG Swift performs iterative DFS using the custom stack implementation. Neighbours are pushed in adjacency-list order, so the most recently pushed neighbour is processed first.

| Step | Vertex popped | Action                        | Stack after processing | Visited order |
| ---: | ------------: | ----------------------------- | ---------------------- | ------------- |
|    1 |             1 | Visit; push 2, then 3         | 2, 3                   | 1             |
|    2 |             3 | Visit; push unvisited 2 and 4 | 2, 2, 4                | 1, 3          |
|    3 |             4 | Visit; push unvisited 2       | 2, 2, 2                | 1, 3, 4       |
|    4 |             2 | Visit                         | 2, 2                   | 1, 3, 4, 2    |
|    5 |             2 | Already visited; skip         | 2                      | unchanged     |
|    6 |             2 | Already visited; skip         | Empty                  | unchanged     |

Final traversal:

`1 -> 3 -> 4 -> 2`

Every connected location is eventually visited.

**Complexity:** `O(V + E)` time and `O(V)` auxiliary space.

---

## 5. Trace Table 3 — Dijkstra's Shortest Path

Goal: find the minimum-weight route from location `1` to location `4`.

Initial state:

| Vertex | Distance | Parent |
| -----: | -------: | ------ |
|      1 |        0 | -      |
|      2 | Infinity | -      |
|      3 | Infinity | -      |
|      4 | Infinity | -      |

| Step | Extracted vertex | Relaxation                    | Distances `(1,2,3,4)` |
| ---: | ---------------: | ----------------------------- | --------------------- |
|    1 |                1 | 1→2 gives 1; 1→3 gives 5      | `(0,1,5,∞)`           |
|    2 |                2 | 2→3 improves 5→3; 2→4 gives 7 | `(0,1,3,7)`           |
|    3 |                3 | 3→4 improves 7→4              | `(0,1,3,4)`           |
|    4 |                4 | Destination finalised         | `(0,1,3,4)`           |

Parent links are:

`parent[2] = 1`
`parent[3] = 2`
`parent[4] = 3`

Reconstructed shortest path:

`1 -> 2 -> 3 -> 4`

The final route has:

- Total weight = `1 + 2 + 1 = 4`
- Total distance = `4 km`
- Total travel time = `7 minutes`

Alternative routes have greater weights:

`1 -> 3 -> 4 = 6`

`1 -> 2 -> 4 = 7`

Therefore the selected path is the minimum-weight route.

**Complexity with adjacency lists and a minimum heap:** `O((V + E) log V)`.

---

## 6. Trace Table 4 — Prim's Minimum Spanning Tree

Prim starts at location `1`.

Initial key values are:

`key(1)=0`, while every other key is infinity.

| Step | Vertex added | Selected edge | Important key updates | MST weight |
| ---: | -----------: | ------------- | --------------------- | ---------: |
|    1 |            1 | Start         | key(2)=1, key(3)=5    |          0 |
|    2 |            2 | 1-2 (1)       | key(3)=2, key(4)=6    |          1 |
|    3 |            3 | 2-3 (2)       | key(4)=1              |          3 |
|    4 |            4 | 3-4 (1)       | None                  |          4 |

Final MST edges:

`1-2`, `2-3`, `3-4`

Total MST weight:

`1 + 2 + 1 = 4`

The MST contains exactly `V - 1 = 3` edges.

**Complexity:** approximately `O(E log V)` with the minimum heap.

---

## 7. Trace Table 5 — Kruskal's Minimum Spanning Tree

Kruskal first sorts all road edges in ascending weight order:

1. `1-2`, weight 1
2. `3-4`, weight 1
3. `2-3`, weight 2
4. `1-3`, weight 5
5. `2-4`, weight 6

The custom Disjoint Set prevents cycles.

| Step | Edge considered | Weight | Same component? | Action | MST weight |
| ---: | --------------- | -----: | --------------- | ------ | ---------: |
|    1 | 1-2             |      1 | No              | Accept |          1 |
|    2 | 3-4             |      1 | No              | Accept |          2 |
|    3 | 2-3             |      2 | No              | Accept |          4 |

Three edges have now been selected, equal to `V - 1`, so the algorithm stops.

Final MST:

`{1-2, 3-4, 2-3}`

Total weight:

`4`

Prim and Kruskal therefore independently produce minimum spanning trees with the same total weight.

**Complexity:** `O(E log E)`, dominated by sorting the edges. Union-Find operations are near-constant amortized time.

---

## 8. Trace Table 6 — Dynamic Programming Knapsack

The optimisation engine uses 0/1 knapsack to maximise total request priority while respecting rider capacity.

Capacity:

`2.0 kg`

The implementation scales kilograms by 10, therefore:

`W = 20`

Requests used by the permanent optimisation test:

| Request | Category  | Weight | Scaled weight | Priority |
| ------- | --------- | -----: | ------------: | -------: |
| 301     | Documents | 0.3 kg |             3 |    58.10 |
| 302     | Pizza     | 0.8 kg |             8 |    50.88 |
| 303     | Waakye    | 1.2 kg |            12 |    48.10 |
| 304     | Groceries | 5.0 kg |            50 |    63.10 |

The request priorities are calculated by the application's `ServiceRequest` model from urgency and deadline.

Request 304 cannot fit because its weight exceeds the total capacity.

Important feasible combinations are:

| Combination        | Total weight | Total priority |
| ------------------ | -----------: | -------------: |
| Documents          |       0.3 kg |          58.10 |
| Pizza              |       0.8 kg |          50.88 |
| Waakye             |       1.2 kg |          48.10 |
| Documents + Pizza  |       1.1 kg |         108.98 |
| Documents + Waakye |       1.5 kg |         106.20 |
| Pizza + Waakye     |       2.0 kg |          98.98 |

The best feasible combination is therefore:

`Documents + Pizza`

with total priority:

`108.98`

The DP recurrence is:

`dp[i][j] = max(value[i] + dp[i-1][j-weight[i]], dp[i-1][j])`

when the request fits, otherwise:

`dp[i][j] = dp[i-1][j]`

Backtracking selects requests 301 and 302.

The independent brute-force implementation checks every feasible subset and obtains the same optimum. Permanent unit test T71 verifies that both methods produce equal optimal priority.

**Complexity:** `O(nW)` time and `O(nW)` space, where `W` is the discretised capacity.

---

## 9. Correctness Proof Sketch 1 — Dijkstra

### Claim

Dijkstra returns a minimum-weight route from the source to a reachable destination when all edge weights are non-negative.

### Argument

The source begins with distance zero and all other vertices begin with infinity. At each stage, the algorithm extracts the unvisited vertex with the smallest tentative distance.

Suppose vertex `u` is extracted. Any alternative route to `u` through an unvisited vertex cannot be cheaper than the current tentative distance because all remaining edge weights are non-negative. Therefore the distance assigned to `u` can safely be treated as final.

Relaxing every outgoing edge from finalised vertices propagates improved distances to their neighbours. Repeating this argument means that when the destination is extracted, no shorter path can exist.

UG Swift additionally ignores negative and non-finite edge weights during relaxation, preserving Dijkstra's required precondition.

---

## 10. Correctness Proof Sketch 2 — Minimum Spanning Trees

### Prim

Prim maintains a partial tree containing some graph vertices. At each step it chooses the minimum-weight edge crossing from the current tree to a vertex outside the tree.

By the **cut property**, a minimum-weight edge crossing such a cut is safe: at least one MST contains that edge. Therefore each chosen edge preserves the possibility of reaching an optimal spanning tree.

After exactly `V - 1` edges, every vertex is connected without a cycle, giving an MST.

### Kruskal

Kruskal examines edges from smallest to largest weight. It accepts an edge only if its endpoints belong to different Disjoint Set components.

This prevents cycles. By the cut property, the minimum-weight edge connecting separate components is safe to add to an MST.

After `V - 1` accepted edges, all vertices are connected and the result is a minimum spanning tree.

The permanent test suite provides an additional consistency check: Prim and Kruskal both produce MST total weight `4` on the deterministic graph.

---

## 11. Correctness Proof Sketch 3 — Dynamic Programming Knapsack

For every request prefix and every capacity value, the DP table stores the highest priority achievable using only that prefix.

For request `i`, there are only two valid decisions:

1. Exclude the request and keep the best previous value.
2. Include the request, if it fits, and add its priority to the best solution for the remaining capacity.

The recurrence chooses the maximum of those alternatives. These cases cover every possible valid solution involving request `i`.

The zero-request base case has value zero. Assuming row `i-1` is optimal, the recurrence therefore produces an optimal row `i`. By induction, the final table entry is optimal for all requests and the full rider capacity.

Backtracking reconstructs the requests responsible for that optimum.

For small instances, the brute-force implementation enumerates all subsets. Permanent test T71 confirms that the brute-force and DP implementations obtain equal optimal priority.

---

## 12. Counterexamples and Preconditions

### Binary Search Requires Sorted Input

Binary search assumes its input is sorted.

For example:

`[64, 34, 25, 12, 22, 11, 90]`

Applying ordinary binary search directly to this collection can incorrectly discard the section containing the target because the ordering assumption does not hold.

UG Swift explicitly checks this precondition and throws an `IllegalStateException` for unsorted input. This is tested by T56.

An implementation consequence is that the current public binary-search method first performs an `O(n)` sortedness check and then performs the `O(log n)` binary search.

This explains the measured comparisons:

| Input size | Linear Search | Binary Search public method |
| ---------: | ------------: | --------------------------: |
|      1,000 |         1,000 |                       1,009 |
|      5,000 |         5,000 |                       5,012 |
|     10,000 |        10,000 |                      10,013 |

The binary-search phase remains logarithmic, but the complete public method is dominated asymptotically by its defensive linear precondition validation.

### Dijkstra Requires Reachability

If two valid locations exist but no sequence of roads connects them, no shortest path exists.

UG Swift returns `null` when the destination remains unreachable. Permanent test T77 verifies this behaviour.

### Dijkstra Requires Non-Negative Weights

Consider:

`A -> B = 2`

`A -> C = 5`

`C -> B = -10`

A normal Dijkstra implementation could finalise B at cost 2 before later discovering the route through C with total cost -5. Negative edges therefore invalidate Dijkstra's correctness argument.

UG Swift ignores negative and non-finite weights during relaxation.

### Brute Force Has Exponential Growth

Brute-force batching examines `2^n` subsets.

| Requests | Possible subsets |
| -------: | ---------------: |
|        8 |              256 |
|       12 |            4,096 |
|       16 |           65,536 |
|       20 |        1,048,576 |

The application therefore restricts brute-force batching to at most 20 requests. Permanent unit test T73 verifies the guard.

---

## 13. Performance Experiment Method

The benchmark runner is:

`src/AlgorithmBenchmark.java`

It measures:

- Selection Sort
- Insertion Sort
- Merge Sort
- Quick Sort
- Linear Search
- Binary Search
- BFS
- DFS
- Dijkstra
- Prim
- Kruskal
- Greedy Nearest Neighbor
- Dynamic Programming Knapsack
- Brute Force Batching

For each benchmark case, the runner performs a JVM warm-up followed by three timed executions and stores the median execution time.

It also records memory change, comparison counts where comparator instrumentation is available, execution status, a result summary, and the benchmark timestamp.

All generated records are persisted in SQLite and exported to:

`evidence/performance/performance_results.csv`

The latest benchmark produced:

`42 algorithm-run records`

---

## 14. Sorting Performance

Latest measurements:

| Algorithm      |      n=100 |      n=500 |     n=1000 |
| -------------- | ---------: | ---------: | ---------: |
| Selection Sort | 375,292 ns | 991,834 ns | 888,750 ns |
| Insertion Sort | 179,875 ns | 677,750 ns | 865,500 ns |
| Merge Sort     |  81,166 ns |  73,792 ns | 144,417 ns |
| Quick Sort     |  37,042 ns |  81,458 ns | 309,125 ns |

Selection Sort comparison counts were:

`4,950`, `124,750`, and `499,500`

These match approximately:

`n(n-1)/2`

and clearly demonstrate quadratic comparison growth.

Insertion Sort also shows much higher comparison growth than Merge Sort and Quick Sort as input size increases.

Merge Sort and Quick Sort use substantially fewer comparisons on this deterministic data, consistent with their expected `O(n log n)` average behaviour.

The fact that some raw timing points do not increase perfectly monotonically does not contradict the complexity analysis. Nanosecond measurements are affected by JVM optimisation, operating-system scheduling, CPU caching, garbage collection, and other machine activity. Operation and comparison counts are therefore useful alongside wall-clock timing.

Graph:

`evidence/performance/graphs/sorting_runtime.png`

---

## 15. Search Performance

Latest runtime measurements:

| Algorithm                   |   n=1,000 |    n=5,000 |   n=10,000 |
| --------------------------- | --------: | ---------: | ---------: |
| Linear Search               | 76,708 ns | 238,167 ns | 480,875 ns |
| Binary Search public method | 64,000 ns | 316,084 ns | 159,458 ns |

The Linear Search target is deliberately placed near the end of the input, producing exactly:

`1,000`, `5,000`, and `10,000` comparisons.

That behaviour is consistent with `O(n)` worst-case searching.

The current Binary Search API performs the linear sortedness validation discussed earlier, producing:

`1,009`, `5,012`, and `10,013` comparisons.

Graphs:

`evidence/performance/graphs/search_runtime.png`

`evidence/performance/graphs/search_comparisons.png`

---

## 16. Graph Algorithm Performance

Latest measurements:

| Algorithm |      n=20 |      n=50 |       n=90 |
| --------- | --------: | --------: | ---------: |
| BFS       | 23,666 ns | 28,042 ns |  36,542 ns |
| DFS       | 21,416 ns | 31,125 ns |  45,791 ns |
| Dijkstra  | 50,625 ns | 89,959 ns | 105,666 ns |
| Prim      | 84,209 ns | 87,250 ns | 195,334 ns |
| Kruskal   | 23,791 ns | 78,083 ns | 174,417 ns |

BFS and DFS grow relatively gently because each reachable vertex and edge is processed only a bounded number of times.

Dijkstra requires priority-queue operations in addition to edge relaxation.

Prim also repeatedly uses a priority queue to select inexpensive connecting edges.

Kruskal must sort its road edges before performing Disjoint Set operations, explaining its stronger growth as the number of graph edges increases.

Graph:

`evidence/performance/graphs/graph_runtime.png`

---

## 17. Greedy and Dynamic Programming Performance

### Greedy Nearest Neighbor

| Requests | Median runtime |
| -------: | -------------: |
|        5 |     152,167 ns |
|       10 |     461,625 ns |
|       20 |   3,980,583 ns |

The greedy strategy repeatedly considers remaining requests and evaluates routes through Dijkstra. Therefore its cost grows with both the number of candidate requests and the graph size.

### Dynamic Programming Knapsack

| Requests | Median runtime |
| -------: | -------------: |
|       20 |      59,625 ns |
|       50 |     256,250 ns |
|      100 |     609,709 ns |

The measured growth is consistent with the `O(nW)` dynamic-programming formulation, where `n` is the number of requests and `W` is scaled carrying capacity.

Graph:

`evidence/performance/graphs/optimisation_runtime.png`

---

## 18. Brute Force Performance

Latest Brute Force Batching measurements:

| Requests | Subsets | Median runtime |
| -------: | ------: | -------------: |
|        8 |     256 |     378,500 ns |
|       12 |   4,096 |   6,356,416 ns |
|       16 |  65,536 |  22,774,875 ns |

The number of candidate subsets follows:

`2^n`

The substantial runtime increase as `n` grows demonstrates why brute force is suitable only for small exact comparisons.

The DP benchmark handled 100 requests in `609,709 ns`, while brute force required `22,774,875 ns` for only 16 requests in the latest experiment.

This supports the use of brute force as an exact baseline and dynamic programming as the practical optimisation strategy for larger instances.

Graph:

`evidence/performance/graphs/brute_force_runtime.png`

---

## 19. Unit-Test Evidence

The complete current test suite contains **80 tests**.

Latest result:

`80 passed, 0 failed`

The additional routing and optimisation coverage includes T69 through T80:

- brute-force optimal batch selection
- overweight-request exclusion
- DP/brute-force optimum agreement
- invalid-capacity handling
- exponential-input guard
- BFS reachability
- DFS traversal
- Dijkstra shortest path
- unreachable Dijkstra destination
- Prim MST
- Kruskal MST
- Prim/Kruskal MST-weight agreement

---

## 20. Reproduction Commands

Compile without modifying the repository's tracked `bin` directory:

```bash
BUILD_DIR=$(mktemp -d)
find src -name "*.java" > /tmp/ugswift-sources.txt
javac -d "$BUILD_DIR" @/tmp/ugswift-sources.txt
```

Run the unit tests:

```bash
java -cp "${BUILD_DIR}:sqlite-jdbc-3.42.0.0.jar" UGSwiftTestSuite
```

Run the performance benchmark:

```bash
java -cp "${BUILD_DIR}:sqlite-jdbc-3.42.0.0.jar" AlgorithmBenchmark
```

Generate the performance graphs after installing Matplotlib in a Python environment:

```bash
python tools/generate_performance_graphs.py
```

Clean the temporary Java build:

```bash
rm /tmp/ugswift-sources.txt
rm -rf "$BUILD_DIR"
```

---

## 21. Evidence Files

The repository evidence produced by this work is located at:

`evidence/algorithm/algorithm_evidence.md`

`evidence/performance/performance_results.csv`

`evidence/performance/graphs/sorting_runtime.png`

`evidence/performance/graphs/search_runtime.png`

`evidence/performance/graphs/search_comparisons.png`

`evidence/performance/graphs/graph_runtime.png`

`evidence/performance/graphs/optimisation_runtime.png`

`evidence/performance/graphs/brute_force_runtime.png`

The benchmark-generation source is:

`src/AlgorithmBenchmark.java`

The graph-generation utility is:

`tools/generate_performance_graphs.py`

Together these files provide the required algorithm trace, correctness, counterexample, testing, performance, CSV, graph, and dataset evidence.
