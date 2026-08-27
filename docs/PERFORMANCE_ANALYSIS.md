# UG Swift — Group 2 Empirical Performance Analysis & Complexity Evaluation

## 1. Executive Summary & Evaluation Methodology

This document presents the empirical performance analysis of the Data Structures and Algorithms implemented in **UG Swift (Group 2)**. Execution runtimes were captured using the dedicated `G2BenchmarkRunner` across controlled input datasets, and visualised using the plots saved in `evidence/performance/`.

### Benchmark Parameters

* **Source data**: `evidence/performance/g2_benchmark_results.csv` (365 trial rows, 73 algorithm/size combinations)
* **Trial configuration**: 5 independent trials per algorithm and dataset size (N)
* **Measured categories**: Searching, Sorting, Indexing Data Structures, Graph Traversal, Minimum Spanning Trees
* **Timing unit**: `System.nanoTime()`, reported in microseconds (µs) or milliseconds (ms)
* **Warm-up protocol**: 50 warm-up iterations before recording, to stabilise JIT compilation
* **Correctness**: every one of the 365 trials verified its result before recording. **0 incorrect results.**

### Reproducing every number in this document

Each figure below is generated directly from the CSV — none are typed by hand:

```bash
python tools/performance_stats.py
```

> **Methodological note on asymptotic complexity.** Empirical benchmarks provide evidence *consistent with* theoretical Big-O growth rates. Runtimes on finite datasets cannot *prove* asymptotic bounds, because constant factors and system overheads dominate at small N. Strong empirical alignment supports implementation correctness; it does not substitute for the proofs in `evidence/algorithm/algorithm_evidence.md`.

---

## 2. Why the median is reported, not the mean

Microbenchmark timing error is **one-sided**. JIT compilation, garbage collection, and OS thread scheduling can only ever *add* time to a trial — never remove it. A single interrupted trial therefore drags the mean far upward while barely moving the median.

The clearest example in this dataset is Quick Sort at N = 1,000:

| Statistic | Value |
| --- | ---: |
| Median of 5 trials | **522.3 µs** |
| Mean of 5 trials | 11.72 ms |
| Ratio | **22.4x** |

Quoting 11.72 ms would misrepresent the algorithm by a factor of twenty-two. **All headline figures in this document are medians.** Means are shown alongside so the scale of the outlier problem stays visible, and any row where mean/median is at least 2.0 is marked with a warning sign — read the mean on those rows with caution.

---

## 3. Category-by-Category Performance Evaluation

### A. Searching Algorithms (`search_performance.png`)

| Algorithm | N | Median | Mean | Mean/Median |
| --- | ---: | ---: | ---: | ---: |
| Linear Search | 100 | 39.4 µs | 658.8 µs | 16.7x (!) |
| Linear Search | 500 | 61.3 µs | 65.9 µs | 1.1x |
| Linear Search | 1,000 | 239.8 µs | 218.2 µs | 0.9x |
| Linear Search | 5,000 | 1.27 ms | 1.20 ms | 0.9x |
| Linear Search | 10,000 | 2.46 ms | 3.51 ms | 1.4x |
| Binary Search | 100 | 79.8 µs | 326.5 µs | 4.1x (!) |
| Binary Search | 500 | 86.5 µs | 87.2 µs | 1.0x |
| Binary Search | 1,000 | 160.7 µs | 186.1 µs | 1.2x |
| Binary Search | 5,000 | 1.27 ms | 1.55 ms | 1.2x |
| Binary Search | 10,000 | 425.9 µs | 883.3 µs | 2.1x (!) |

**Linear Search** grows monotonically with N — 39.4 µs to 2.46 ms across a 100-fold increase in input size. Consistent with O(N).

**Binary Search does not show logarithmic behaviour in this benchmark, and this is an implementation property rather than a measurement artefact.** At N = 5,000 it is no faster than Linear Search (1.27 ms for both), and its medians are not monotonic.

The cause is in `SortingEngine.binarySearch` (`src/engines/SortingEngine.java:28`), which validates its precondition before searching:

```java
public static <T> int binarySearch(DynamicArray<T> list, T target, Comparator<? super T> comp) {
    if (!isSorted(list, comp)) {            // <-- O(N) scan of the whole list
        throw new IllegalStateException("Precondition failed: Binary Search requires a sorted list.");
    }
    // ... O(log N) halving loop follows
}
```

`isSorted` walks every element, so the **public method costs O(N) + O(log N) = O(N)**. The halving loop itself is correct; the guard wrapped around it is not sublinear.

This is confirmed independently by the deterministic comparison counts recorded in `evidence/performance/performance_results.csv`, which do not vary with machine load:

| N | Binary Search comparisons | Expected if O(log N) |
| ---: | ---: | ---: |
| 1,000 | 1,009 | about 10 |
| 5,000 | 5,012 | about 13 |
| 10,000 | 10,013 | about 14 |

The counts are N + log2(N) — the N from the precondition scan, the remainder from the actual search. **The comparison counts prove the O(N) wrapper cost far more convincingly than the timings do.**

*Conclusion:* the binary search *algorithm* is correctly implemented as O(log N), but the *API as currently exposed* is O(N). Timing the halving loop in isolation, or providing an unchecked variant for callers that already guarantee sortedness, would be required to demonstrate logarithmic scaling empirically.

---

### B. Sorting Algorithms (`sorting_performance.png`)

| Algorithm | N | Median | Mean | Mean/Median |
| --- | ---: | ---: | ---: | ---: |
| Selection Sort | 100 | 1.64 ms | 1.67 ms | 1.0x |
| Selection Sort | 500 | 7.36 ms | 10.20 ms | 1.4x |
| Selection Sort | 1,000 | 5.34 ms | 7.44 ms | 1.4x |
| Selection Sort | 5,000 | **118.69 ms** | 113.78 ms | 1.0x |
| Insertion Sort | 100 | 483.8 µs | 1.87 ms | 3.9x (!) |
| Insertion Sort | 500 | 2.78 ms | 5.22 ms | 1.9x |
| Insertion Sort | 1,000 | 5.72 ms | 6.31 ms | 1.1x |
| Insertion Sort | 5,000 | **114.84 ms** | 110.56 ms | 1.0x |
| Merge Sort | 100 | 256.8 µs | 1.25 ms | 4.9x (!) |
| Merge Sort | 500 | 379.5 µs | 411.2 µs | 1.1x |
| Merge Sort | 1,000 | 687.5 µs | 696.6 µs | 1.0x |
| Merge Sort | 5,000 | 6.44 ms | 7.25 ms | 1.1x |
| Merge Sort | 10,000 | **12.10 ms** | 12.01 ms | 1.0x |
| Quick Sort | 100 | 111.6 µs | 463.3 µs | 4.2x (!) |
| Quick Sort | 500 | 997.1 µs | 4.66 ms | 4.7x (!) |
| Quick Sort | 1,000 | 522.3 µs | 11.72 ms | 22.4x (!) |
| Quick Sort | 5,000 | 12.78 ms | 12.86 ms | 1.0x |
| Quick Sort | 10,000 | **56.40 ms** | 62.00 ms | 1.1x |

**The headline result is the separation between the quadratic and the O(N log N) sorts, and it is unambiguous:**

> At N = 5,000, Selection Sort takes **118.69 ms** and Insertion Sort **114.84 ms**.
> At N = 10,000 — *twice the input* — Merge Sort takes **12.10 ms**: roughly **10x less work on 2x the data.**

Merge Sort outperforms Quick Sort here (12.10 ms vs 56.40 ms at N = 10,000). This is worth stating precisely rather than attributing it vaguely to cache behaviour: `SortingEngine.quickSort` uses **Lomuto partitioning with the last element as the pivot** (`src/engines/SortingEngine.java:580`), which degrades toward O(N²) on sorted or nearly-sorted input. Merge Sort has no such input sensitivity — it is O(N log N) on every input.

*Caveat:* Selection Sort's and Quick Sort's medians are **not monotonic** across N (both dip at N = 1,000). At small N the runtimes are dominated by JVM warm-up rather than algorithmic work. The quadratic-versus-linearithmic separation at N = 5,000 and N = 10,000 is large enough to be unaffected by this.

---

### C. Indexing Data Structures (`indexing_performance.png`)

**This is the cleanest result in the dataset** — the structures separate exactly as theory predicts.

| Structure | N=100 | N=500 | N=1,000 | N=5,000 | N=10,000 | Behaviour |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Hash Table | 5.1 µs | 2.3 µs | 2.0 µs | 2.0 µs | **1.9 µs** | flat — O(1) average |
| Red-Black Tree | 2.2 µs | 2.2 µs | 2.0 µs | 2.4 µs | **2.6 µs** | flat — O(log N) |
| B-Tree (T=3) | 2.2 µs | 4.5 µs | 3.0 µs | 5.8 µs | **3.5 µs** | flat — O(log_T N) |
| BST (unbalanced) | 22.5 µs | 26.1 µs | 100.8 µs | 137.9 µs | **423.0 µs** | **grows — degrades toward O(N)** |

* **Hash Table** search is essentially independent of N (1.9 µs at N = 10,000; a 2.7x spread across the whole range) — the expected O(1) average behaviour from separate chaining with a prime-sized bucket array.
* **Red-Black Tree** is the flattest of all (1.3x spread), because its height is bounded at 2·log2(N+1) regardless of insertion order.
* **B-Tree** is comparably flat (2.6x spread); its shallower, wider shape keeps the number of node visits low.
* **Unbalanced BST** is the outlier, and the pedagogically important one: it grows **19x** from N = 100 to N = 10,000 while the balanced structures stay flat. With sequential or partially ordered keys a BST degenerates toward a linked list, giving O(N) search. This is precisely the failure mode Red-Black Trees exist to prevent, and the data shows it directly.

---

### D. Graph Algorithms (`graph_performance.png`)

| Algorithm | V=20 | V=50 | V=100 | V=250 | V=500 | Monotonic? |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| BFS | 78.3 µs | 166.0 µs | 337.8 µs | 250.8 µs | 3.45 ms | no |
| DFS | 431.6 µs | 123.6 µs | 265.3 µs | 343.3 µs | 673.9 µs | no |
| Dijkstra | 896.5 µs | 226.2 µs | 346.3 µs | 944.2 µs | 1.57 ms | no |

**None of the three graph algorithms produced a monotonic median curve, so this dataset cannot be used to demonstrate their growth rates.** Each series decreases at least once as V increases, which is impossible algorithmically and therefore indicates that measurement noise exceeds the signal at these graph sizes.

What the data *does* support:

* At the largest size (V = 500) the ordering is BFS (3.45 ms), then Dijkstra (1.57 ms), then DFS (673.9 µs). Note that BFS being slowest **contradicts** the expectation that Dijkstra — which performs the same traversal *plus* MinHeap operations — should cost more than an unweighted traversal. That inversion is itself evidence that the measurement is unreliable here.
* Absolute runtimes are all under 4 ms, i.e. the graphs are too small for asymptotic behaviour to dominate constant factors.

*Required follow-up:* re-run this category with larger V (2,000+), more trials, and per-category warm-up before making any complexity claim about BFS, DFS, or Dijkstra from timing alone. The trace tables and correctness proofs in `evidence/algorithm/algorithm_evidence.md` remain the sound evidence for these algorithms in the meantime.

---

### E. Minimum Spanning Tree Algorithms (`mst_performance.png`)

| Algorithm | V=20 | V=50 | V=100 | V=250 | V=500 |
| --- | ---: | ---: | ---: | ---: | ---: |
| Prim MST (median) | 362.9 µs | 852.6 µs | 2.69 ms | 1.15 ms | **3.96 ms** |
| Kruskal MST (median) | 190.2 µs | 300.8 µs | 844.5 µs | 1.27 ms | **4.58 ms** |
| Prim MST (mean) | 3.75 ms | 894.9 µs | 5.56 ms | 7.33 ms | **6.70 ms** |
| Kruskal MST (mean) | 2.78 ms | 336.9 µs | 2.45 ms | 7.08 ms | **5.85 ms** |

**This dataset does not establish a winner between Prim and Kruskal, and the two statistics disagree:**

* By **median** at V = 500, Prim is faster (3.96 ms vs 4.58 ms) — a 14% gap.
* By **mean** at V = 500, Kruskal is faster (5.85 ms vs 6.70 ms) — a 13% gap in the *opposite* direction.

Since the two aggregations point in opposite directions, and Prim's medians are **not monotonic** (dropping from 2.69 ms at V = 100 to 1.15 ms at V = 250), the honest conclusion is that **the difference is within measurement noise at these graph sizes.**

Kruskal's medians *are* monotonic (190.2 µs rising to 4.58 ms), making it the better-behaved of the two series.

*What theory predicts, for contrast:* on dense graphs Prim with a heap should win, because Kruskal must first sort all E edges at O(E log E), and E approaches V² when the graph is dense. Confirming that prediction requires a cleaner experiment than this one. It is **not** demonstrated by the data above, and this document does not claim that it is.

---

## 4. Data Quality, Anomalies and Limitations

This section records the known weaknesses of the dataset so the conclusions above are read with the right level of confidence.

1. **Outlier-dominated means.** 20 of the 73 algorithm/size combinations have a mean at least 2x their median. The worst is B-Tree Search at N = 5,000 (median 5.8 µs, mean 750.9 µs — a **129x ratio**), where a single interrupted trial inflated the mean by two orders of magnitude. Medians are used throughout for this reason.

2. **Non-monotonic series.** 8 of the 15 algorithms produced medians that decrease at least once as N increases: BFS, DFS, Dijkstra, Prim MST, Binary Search, Quick Sort, Selection Sort, and B-Tree Search. For these, no growth-rate claim is made from timing alone.

3. **Warm-up covers sorting and searching only.** The 50-iteration warm-up in `G2BenchmarkRunner` runs before the sorting and searching benchmarks. The graph, MST, and indexing categories are measured against a comparatively cold JIT, which is a likely contributor to the anomalies in sections C (B-Tree), D, and E.

4. **Five trials is few.** Five samples cannot characterise a heavy-tailed distribution. 20 or more trials with the first discarded would materially reduce the noise seen here.

5. **Generic boxing overhead.** The custom structures store `Integer`/`Double` objects rather than primitives, adding allocation and pointer-chasing costs absent from a primitive-array implementation. This inflates all absolute numbers, but affects the structures comparably, so relative comparisons remain valid.

6. **Comparison counts are the stronger evidence.** Where available (`performance_results.csv`), operation and comparison counts are deterministic and machine-independent. Selection Sort's counts of 4,950 / 124,750 / 499,500 match n(n-1)/2 exactly — evidence of quadratic growth that no amount of timing noise can obscure. Future analysis should lead with counts and use timing as corroboration.

---

## 5. Verification Checklist

- [x] All 5 PNG plots generated under `evidence/performance/`.
- [x] CSV dataset `evidence/performance/g2_benchmark_results.csv` used as the single source of truth.
- [x] Every figure in this document regenerated from that CSV via `tools/performance_stats.py`.
- [x] All 365 benchmark trials recorded `result_correct = true` (0 incorrect results).
- [x] `UGSwiftTestSuite` executed: **198 assertions passed, 0 failed.**
- [ ] Graph / MST / indexing categories re-run with per-category warm-up and larger inputs (see section 4.3).
- [ ] Binary Search re-benchmarked with the precondition check excluded from the timed section (see section 3.A).
