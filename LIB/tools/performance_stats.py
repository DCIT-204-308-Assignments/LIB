"""
UG Swift - Performance statistics generator.

WHY THIS SCRIPT EXISTS
----------------------
The performance documents used to quote runtime figures that were typed in by
hand. Every time a benchmark was re-run the CSV changed but the documents did
not, so the published numbers slowly drifted away from the evidence they
claimed to describe.

This script removes the hand-typing step. It reads the benchmark CSVs and
prints ready-to-paste Markdown tables. Re-run the benchmark, re-run this
script, paste the output -> the documents and the data can never disagree.

WHY MEDIAN AND NOT MEAN
-----------------------
These are microbenchmarks measured with System.nanoTime(). A single trial can
be inflated by JIT compilation, garbage collection, or the operating system
scheduling another process onto the core. Those events add time; they never
remove it. So the error is one-sided, and one bad trial drags the MEAN a long
way up while barely moving the MEDIAN.

Real example from g2_benchmark_results.csv (Quick Sort, n=1000):
    trials: 0.42ms, 0.52ms, 0.55ms, 0.61ms, 56.5ms   <- last trial is an outlier
    mean   = 11.72 ms  (dominated by the one bad trial)
    median =  0.52 ms  (represents typical behaviour)

The median is therefore the headline figure. The mean is printed alongside it
so the size of the outlier problem stays visible rather than hidden.

USAGE
-----
    python tools/performance_stats.py

Run it from the project root (the folder containing `evidence/`).
"""

import csv
import os
import statistics
import sys
from collections import defaultdict

# The tables below contain "µ" (microseconds) and "⚠" (outlier warning). The
# default Windows console encoding (cp1252) cannot represent either, which
# would crash the script mid-table. Force UTF-8 so the output is identical on
# Windows, macOS and Linux.
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# ---------------------------------------------------------------------------
# CSV locations, relative to the project root.
# ---------------------------------------------------------------------------
G2_CSV = os.path.join("evidence", "performance", "g2_benchmark_results.csv")
ALGO_CSV = os.path.join("evidence", "performance", "performance_results.csv")

# The order algorithms should appear in, grouped by category. Anything found in
# the CSV but missing from this list is still reported, just at the end.
G2_GROUPS = [
    ("Searching", ["Linear Search", "Binary Search"]),
    ("Sorting", ["Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"]),
    ("Indexing", ["Hash Table Search", "BST Search",
                  "Red-Black Tree Search", "B-Tree Search"]),
    ("Graph traversal / shortest path", ["BFS", "DFS", "Dijkstra"]),
    ("Minimum spanning tree", ["Prim MST", "Kruskal MST"]),
]


def load_g2(path):
    """
    Read the G2 benchmark CSV.

    Format: algorithm,input_size,trial,execution_time_ns,result_correct
    One row per TRIAL, so several rows share an (algorithm, input_size) pair.

    Returns:
        samples  -- {(algorithm, input_size): [time_ns, ...]}
        incorrect -- number of rows whose result_correct flag was not "true"
    """
    samples = defaultdict(list)
    incorrect = 0

    with open(path, newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            key = (row["algorithm"], int(row["input_size"]))
            samples[key].append(int(row["execution_time_ns"]))
            if row["result_correct"].strip().lower() != "true":
                incorrect += 1

    return samples, incorrect


def load_algo(path):
    """
    Read the AlgorithmBenchmark CSV.

    This file is already aggregated -- AlgorithmBenchmark computes its own
    median of 3 executions before writing, so there is exactly ONE row per
    (algorithm, input_size) and no further averaging is needed here.

    Returns:
        {(algorithmName, inputSize): {"timeNs":.., "comparisons":.., "summary":..}}
    """
    records = {}

    with open(path, newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            key = (row["algorithmName"], int(row["inputSize"]))
            records[key] = {
                "timeNs": int(row["timeNs"]),
                "comparisons": int(row["comparisonsCount"]),
                "summary": row["resultSummary"],
            }

    return records


def fmt_ns(nanoseconds):
    """
    Format a nanosecond count using whichever unit keeps it readable.

    Below 1 microsecond   -> nanoseconds
    Below 1 millisecond   -> microseconds
    Otherwise             -> milliseconds
    """
    if nanoseconds < 1_000:
        return f"{nanoseconds:,.0f} ns"
    if nanoseconds < 1_000_000:
        return f"{nanoseconds / 1_000:,.1f} µs"
    return f"{nanoseconds / 1_000_000:,.2f} ms"


def outlier_ratio(values):
    """
    How badly is the mean distorted by outliers for this set of trials?

    Returns mean / median. A value near 1.0 means the trials agreed. A large
    value means at least one trial was far slower than the rest, and the mean
    should not be quoted without comment.
    """
    median = statistics.median(values)
    if median == 0:
        return float("inf")
    return (sum(values) / len(values)) / median


def print_g2_tables(samples):
    """Print one Markdown table per algorithm group from the G2 trial data."""
    seen = set()

    for group_name, algorithms in G2_GROUPS:
        print(f"\n#### {group_name}\n")
        print("| Algorithm | N | Median | Mean | Trials | Mean/Median |")
        print("| --- | ---: | ---: | ---: | ---: | ---: |")

        for algorithm in algorithms:
            sizes = sorted(n for (name, n) in samples if name == algorithm)

            for size in sizes:
                values = samples[(algorithm, size)]
                seen.add((algorithm, size))

                median = statistics.median(values)
                mean = sum(values) / len(values)
                ratio = outlier_ratio(values)

                # Flag rows where the mean is more than 2x the median, because
                # those are the rows a reader should not trust the mean on.
                flag = " ⚠" if ratio >= 2.0 else ""

                print(f"| {algorithm} | {size:,} | {fmt_ns(median)} | "
                      f"{fmt_ns(mean)} | {len(values)} | {ratio:.1f}x{flag} |")

    # Report anything in the CSV that the group list above did not cover, so a
    # newly added benchmark is never silently dropped from the documentation.
    leftover = sorted(set(samples) - seen)
    if leftover:
        print("\n#### Ungrouped (add these to G2_GROUPS)\n")
        for algorithm, size in leftover:
            values = samples[(algorithm, size)]
            print(f"| {algorithm} | {size:,} | "
                  f"{fmt_ns(statistics.median(values))} | | {len(values)} | |")


# Structures whose lookup cost is meant to stay flat (or nearly flat) as N
# grows. For these, a non-increasing line is the CORRECT result and must not be
# reported as a measurement failure.
FLAT_EXPECTED = {
    "Hash Table Search",       # O(1) average
    "Red-Black Tree Search",   # O(log n) - grows so slowly it looks flat here
    "B-Tree Search",           # O(log_T n) - likewise
}


def print_monotonicity_check(samples):
    """
    Check whether each algorithm's median time increases with input size.

    A complexity claim like "this is O(n log n)" implies bigger inputs take
    longer. If the measured medians go DOWN as N goes up for an algorithm that
    is supposed to grow, the measurement is too noisy to support any claim
    about the shape of the curve, and the document must say so rather than
    pretending the line is clean.

    Algorithms in FLAT_EXPECTED are judged differently: for those, staying flat
    IS the expected result, so we report the spread instead of demanding growth.
    """
    print("\n#### Growth of median runtime (does time increase with N?)\n")
    print("| Algorithm | Medians in N order | Verdict |")
    print("| --- | --- | --- |")

    algorithms = sorted({name for (name, _) in samples})

    for algorithm in algorithms:
        sizes = sorted(n for (name, n) in samples if name == algorithm)
        medians = [statistics.median(samples[(algorithm, n)]) for n in sizes]
        rendered = ", ".join(fmt_ns(m) for m in medians)

        if algorithm in FLAT_EXPECTED:
            # Ratio between the slowest and fastest median. Close to 1 means
            # the cost really is roughly independent of N, which is the claim.
            spread = max(medians) / min(medians) if min(medians) > 0 else float("inf")
            verdict = f"flat as expected ({spread:.1f}x spread across N)"
        else:
            # zip(medians, medians[1:]) pairs each value with the next one.
            is_monotonic = all(a <= b for a, b in zip(medians, medians[1:]))
            verdict = ("grows with N"
                       if is_monotonic
                       else "**not monotonic - too noisy to claim a curve**")

        print(f"| {algorithm} | {rendered} | {verdict} |")


def print_algo_tables(records):
    """
    Print the AlgorithmBenchmark tables.

    This benchmark also records comparison counts. Comparison counts are far
    more reliable evidence of complexity than wall-clock time, because they are
    deterministic -- they do not change when the machine is busy.
    """
    print("\n#### AlgorithmBenchmark results (median of 3, from performance_results.csv)\n")
    print("| Algorithm | N | Median runtime | Comparisons |")
    print("| --- | ---: | ---: | ---: |")

    for (algorithm, size) in sorted(records, key=lambda k: (k[0], k[1])):
        entry = records[(algorithm, size)]
        comparisons = f"{entry['comparisons']:,}" if entry["comparisons"] else "-"
        print(f"| {algorithm} | {size:,} | {fmt_ns(entry['timeNs'])} | {comparisons} |")


def main():
    if not os.path.exists(G2_CSV):
        raise SystemExit(
            f"Could not find {G2_CSV}.\n"
            "Run this script from the project root (the folder containing 'evidence/')."
        )

    samples, incorrect = load_g2(G2_CSV)

    total_rows = sum(len(v) for v in samples.values())
    print("=" * 70)
    print("G2 BENCHMARK  (evidence/performance/g2_benchmark_results.csv)")
    print("=" * 70)
    print(f"trial rows        : {total_rows}")
    print(f"algorithm/size    : {len(samples)} distinct combinations")
    print(f"incorrect results : {incorrect}")

    print_g2_tables(samples)
    print_monotonicity_check(samples)

    if os.path.exists(ALGO_CSV):
        records = load_algo(ALGO_CSV)
        print("\n" + "=" * 70)
        print("ALGORITHM BENCHMARK  (evidence/performance/performance_results.csv)")
        print("=" * 70)
        print(f"records: {len(records)}")
        print_algo_tables(records)
    else:
        print(f"\n(skipped {ALGO_CSV} - file not found)")


if __name__ == "__main__":
    main()
