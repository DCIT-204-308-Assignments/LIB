import csv
from pathlib import Path

import matplotlib.pyplot as plt


CSV_FILE = Path("evidence/performance/performance_results.csv")
OUTPUT_DIR = Path("evidence/performance/graphs")


def load_results():
    rows = []

    with CSV_FILE.open(newline="", encoding="utf-8") as file:
        reader = csv.DictReader(file)

        for row in reader:
            rows.append(
                {
                    "algorithm": row["algorithmName"],
                    "input_size": int(row["inputSize"]),
                    "time_ns": int(row["timeNs"]),
                    "comparisons": int(row["comparisonsCount"]),
                }
            )

    return rows


def plot_algorithms(rows, algorithms, filename, title):
    plt.figure(figsize=(10, 6))

    for algorithm in algorithms:
        selected = [
            row
            for row in rows
            if row["algorithm"] == algorithm
        ]

        selected.sort(
            key=lambda row: row["input_size"]
        )

        x = [
            row["input_size"]
            for row in selected
        ]

        y = [
            row["time_ns"] / 1_000_000
            for row in selected
        ]

        plt.plot(
            x,
            y,
            marker="o",
            label=algorithm,
        )

    plt.xlabel("Input size")
    plt.ylabel("Execution time (milliseconds)")
    plt.title(title)
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()

    output = OUTPUT_DIR / filename
    plt.savefig(output, dpi=200)
    plt.close()

    print(f"Created {output}")


def plot_search_comparisons(rows):
    algorithms = [
        "Linear Search",
        "Binary Search",
    ]

    plt.figure(figsize=(10, 6))

    for algorithm in algorithms:
        selected = [
            row
            for row in rows
            if row["algorithm"] == algorithm
        ]

        selected.sort(
            key=lambda row: row["input_size"]
        )

        x = [
            row["input_size"]
            for row in selected
        ]

        y = [
            row["comparisons"]
            for row in selected
        ]

        plt.plot(
            x,
            y,
            marker="o",
            label=algorithm,
        )

    plt.xlabel("Input size")
    plt.ylabel("Recorded comparisons")
    plt.title("Searching: Recorded Comparison Counts")
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()

    output = OUTPUT_DIR / "search_comparisons.png"

    plt.savefig(output, dpi=200)
    plt.close()

    print(f"Created {output}")


def main():
    if not CSV_FILE.exists():
        raise FileNotFoundError(
            f"Benchmark CSV not found: {CSV_FILE}"
        )

    OUTPUT_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    rows = load_results()

    plot_algorithms(
        rows,
        [
            "Selection Sort",
            "Insertion Sort",
            "Merge Sort",
            "Quick Sort",
        ],
        "sorting_runtime.png",
        "Sorting Algorithms: Runtime Growth",
    )

    plot_algorithms(
        rows,
        [
            "Linear Search",
            "Binary Search",
        ],
        "search_runtime.png",
        "Searching Algorithms: Runtime Growth",
    )

    plot_search_comparisons(rows)

    plot_algorithms(
        rows,
        [
            "BFS",
            "DFS",
            "Dijkstra",
            "Prim",
            "Kruskal",
        ],
        "graph_runtime.png",
        "Graph Algorithms: Runtime Growth",
    )

    plot_algorithms(
        rows,
        [
            "Greedy Nearest Neighbor",
            "Dynamic Programming Knapsack",
        ],
        "optimisation_runtime.png",
        "Optimisation Algorithms: Runtime Growth",
    )

    plot_algorithms(
        rows,
        [
            "Brute Force Batching",
        ],
        "brute_force_runtime.png",
        "Brute Force Batching: Exponential Runtime Growth",
    )

    print()
    print("Performance graphs generated successfully.")


if __name__ == "__main__":
    main()