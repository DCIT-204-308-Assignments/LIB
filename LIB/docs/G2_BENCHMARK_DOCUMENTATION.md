# UG Swift — Group 2 Algorithmic Performance Benchmark Documentation

## 1. Overview & Objectives

This document explains the empirical performance evaluation methodology for the Data Structures and Algorithms implemented in **UG Swift (Group 2)**. 

The primary objective is to evaluate execution runtimes ($T(N)$) across varying input sizes ($N$) and verify that the empirical behavior matches theoretical Asymptotic Time Complexity ($O$-notation).

---

## 2. Algorithms & Data Structures Benchmarked

### A. Searching Algorithms
- **Linear Search**: $O(N)$ worst-case search over unsorted/sorted inputs.
- **Binary Search**: $O(\log N)$ search over sorted arrays (`ds.DynamicArray`).

### B. Sorting Algorithms
- **Selection Sort**: $O(N^2)$ comparison-based sort.
- **Insertion Sort**: $O(N^2)$ worst-case, $O(N)$ best-case sort.
- **Merge Sort**: $O(N \log N)$ divide-and-conquer stable sort.
- **Quick Sort**: $O(N \log N)$ average-case in-place sort using partition.

### C. Indexing Data Structures
- **Binary Search Tree (BST)**: $O(\log N)$ average search.
- **Red-Black Tree (RBT)**: $O(\log N)$ guaranteed height-balanced search.
- **B-Tree ($T=3$)**: $O(\log N)$ multi-way search tree search.
- **Hash Table**: $O(1)$ average search with separate chaining collision resolution.

### D. Graph & MST Algorithms
- **Breadth-First Search (BFS)**: $O(V + E)$ unweighted shortest path / reachability traversal.
- **Depth-First Search (DFS)**: $O(V + E)$ recursive graph traversal.
- **Dijkstra's Algorithm**: $O((V + E) \log V)$ weighted shortest path using custom `MinHeap`.
- **Prim's MST Algorithm**: $O((V + E) \log V)$ Minimum Spanning Tree using `MinHeap`.
- **Kruskal's MST Algorithm**: $O(E \log E)$ Minimum Spanning Tree using `DisjointSet` (Union-Find).

---

## 3. Benchmark Methodology & Quality Controls

To ensure high scientific accuracy and prevent timing noise, the benchmark runner (`G2BenchmarkRunner.java`) implements the following controls:

1. **JVM Warm-up Phase**:
   - Executes 50 warm-up runs of sorting and searching prior to recording timing data to allow the Java Virtual Machine (JVM) Just-In-Time (JIT) compiler to optimize bytecode into native code.

2. **Timing Isolation**:
   - High-resolution timing is captured using `System.nanoTime()`.
   - Data generation, array copying, graph construction, memory allocations, and CSV writing are strictly excluded from the timed section.

3. **Multiple Trials**:
   - Each algorithm is measured across **5 independent trials** per input size $N$.

4. **Correctness Verification**:
   - Every benchmark execution verifies the accuracy of the result before recording `result_correct = true`:
     - Sorting: Verified using `SortingEngine.isSorted`.
     - Searching & Indexing: Verified that returned value matches the expected target key.
     - Graph: Verified traversal coverage and non-negative path weights.
     - MST: Verified that edge count equals $V-1$.

5. **Input Size Selection & Sizing Controls**:
   - **Searching & Indexing**: $N \in \{100, 500, 1000, 5000, 10000\}$
   - **Fast Sorts (Merge, Quick)**: $N \in \{100, 500, 1000, 5000, 10000\}$
   - **Quadratic Sorts (Selection, Insertion)**: $N \in \{100, 500, 1000, 5000\}$ (capped at 5,000 to prevent impractically long execution delays while retaining 4 data points up to $N=5,000$).
   - **Graph & MST Algorithms**: $N \in \{20, 50, 100, 250, 500\}$ (number of vertices in connected graph).

---

## 4. Output Format (`g2_benchmark_results.csv`)

The benchmark exports raw data to `g2_benchmark_results.csv` in the root workspace directory:

```csv
algorithm,input_size,trial,execution_time_ns,result_correct
Linear Search,100,1,1200,true
Linear Search,100,2,800,true
...
```

---

## 5. How to Reproduce Benchmarks

### Prerequisites
- JDK 8 or higher installed and configured in system PATH.
- Shell / Terminal in the project root directory (`LIB`).

### Step 1: Compile the Project
```powershell
javac -cp "sqlite-jdbc-3.42.0.0.jar;src" -d bin src/ds/*.java src/models/*.java src/engines/*.java src/UGSwiftTestSuite.java src/G2BenchmarkRunner.java
```

### Step 2: Run the Benchmark Suite
```powershell
java -cp "sqlite-jdbc-3.42.0.0.jar;bin" G2BenchmarkRunner
```

### Step 3: Run the Complete Unit Test Suite
To confirm that all existing functionality remains 100% operational:
```powershell
java -cp "sqlite-jdbc-3.42.0.0.jar;bin" UGSwiftTestSuite
```
