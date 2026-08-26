import os
import pandas as pd
import matplotlib.pyplot as plt

# Configure matplotlib aesthetic style
plt.style.use('seaborn-v0_8-whitegrid' if 'seaborn-v0_8-whitegrid' in plt.style.available else 'default')
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['font.size'] = 10
plt.rcParams['axes.titlesize'] = 12
plt.rcParams['axes.labelsize'] = 11
plt.rcParams['figure.titlesize'] = 14

CSV_PATH = 'evidence/performance/g2_benchmark_results.csv'
if not os.path.exists(CSV_PATH):
    CSV_PATH = 'g2_benchmark_results.csv'

OUTPUT_DIR = 'evidence/performance'
os.makedirs(OUTPUT_DIR, exist_ok=True)

df = pd.read_csv(CSV_PATH)

# Aggregate median timing (in nanoseconds) per algorithm and input_size
agg = df.groupby(['algorithm', 'input_size'])['execution_time_ns'].median().reset_index()

def format_y_ns_to_ms(ns_val):
    return ns_val / 1e6

def format_y_ns_to_us(ns_val):
    return ns_val / 1e3

# ── 1. Search Performance Graph ──────────────────────────────────────────
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
search_algos = ['Linear Search', 'Binary Search']
colors = {'Linear Search': '#e74c3c', 'Binary Search': '#2ecc71'}
markers = {'Linear Search': 'o', 'Binary Search': 's'}

for algo in search_algos:
    sub = agg[agg['algorithm'] == algo].sort_values('input_size')
    if not sub.empty:
        # Convert ns to microseconds for clarity
        y_us = sub['execution_time_ns'] / 1e3
        ax.plot(sub['input_size'], y_us, label=f"{algo}", color=colors[algo], 
                marker=markers[algo], linewidth=2.5, markersize=7)

ax.set_title('Searching Algorithm Performance: Linear Search vs Binary Search', fontweight='bold', pad=12)
ax.set_xlabel('Input Size (N items)', fontweight='bold')
ax.set_ylabel('Execution Time (Microseconds - µs)', fontweight='bold')
ax.set_yscale('log') # Use log scale because Linear O(N) vs Binary O(log N) scale drastically
ax.legend(frameon=True, facecolor='white', framealpha=0.9, loc='upper left')
ax.grid(True, which="both", ls="--", alpha=0.5)
plt.tight_layout()
search_fig_path = os.path.join(OUTPUT_DIR, 'search_performance.png')
plt.savefig(search_fig_path)
plt.close()
print(f"Saved: {search_fig_path}")

# ── 2. Sorting Performance Graph ──────────────────────────────────────────
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
sort_algos = ['Selection Sort', 'Insertion Sort', 'Merge Sort', 'Quick Sort']
colors_sort = {
    'Selection Sort': '#e74c3c', 
    'Insertion Sort': '#e67e22', 
    'Merge Sort': '#3498db', 
    'Quick Sort': '#2ecc71'
}
markers_sort = {'Selection Sort': 'o', 'Insertion Sort': '^', 'Merge Sort': 's', 'Quick Sort': 'D'}

for algo in sort_algos:
    sub = agg[agg['algorithm'] == algo].sort_values('input_size')
    if not sub.empty:
        # Convert ns to milliseconds
        y_ms = sub['execution_time_ns'] / 1e6
        ax.plot(sub['input_size'], y_ms, label=f"{algo}", color=colors_sort[algo], 
                marker=markers_sort[algo], linewidth=2.5, markersize=7)

ax.set_title('Sorting Algorithm Performance Comparison (O(N²) vs O(N log N))', fontweight='bold', pad=12)
ax.set_xlabel('Input Size (N items)', fontweight='bold')
ax.set_ylabel('Execution Time (Milliseconds - ms)', fontweight='bold')
ax.set_yscale('log') # Log scale prevents O(N log N) from being flattened by O(N²)
ax.legend(frameon=True, facecolor='white', framealpha=0.9, loc='upper left')
ax.grid(True, which="both", ls="--", alpha=0.5)
plt.tight_layout()
sort_fig_path = os.path.join(OUTPUT_DIR, 'sorting_performance.png')
plt.savefig(sort_fig_path)
plt.close()
print(f"Saved: {sort_fig_path}")

# ── 3. Indexing Performance Graph ─────────────────────────────────────────
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
index_algos = ['BST Search', 'Red-Black Tree Search', 'B-Tree Search', 'Hash Table Search']
colors_index = {
    'BST Search': '#9b59b6', 
    'Red-Black Tree Search': '#e74c3c', 
    'B-Tree Search': '#f39c12', 
    'Hash Table Search': '#1abc9c'
}
markers_index = {'BST Search': 'o', 'Red-Black Tree Search': 's', 'B-Tree Search': '^', 'Hash Table Search': 'D'}

for algo in index_algos:
    sub = agg[agg['algorithm'] == algo].sort_values('input_size')
    if not sub.empty:
        # Convert ns to microseconds
        y_us = sub['execution_time_ns'] / 1e3
        ax.plot(sub['input_size'], y_us, label=f"{algo}", color=colors_index[algo], 
                marker=markers_index[algo], linewidth=2.5, markersize=7)

ax.set_title('Indexing Data Structure Search Lookup Runtimes', fontweight='bold', pad=12)
ax.set_xlabel('Index Input Size (N records)', fontweight='bold')
ax.set_ylabel('Execution Time (Microseconds - µs)', fontweight='bold')
ax.set_yscale('log')
ax.legend(frameon=True, facecolor='white', framealpha=0.9, loc='upper left')
ax.grid(True, which="both", ls="--", alpha=0.5)
plt.tight_layout()
index_fig_path = os.path.join(OUTPUT_DIR, 'indexing_performance.png')
plt.savefig(index_fig_path)
plt.close()
print(f"Saved: {index_fig_path}")

# ── 4. Graph Performance Graph ───────────────────────────────────────────
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
graph_algos = ['BFS', 'DFS', 'Dijkstra']
colors_graph = {'BFS': '#3498db', 'DFS': '#9b59b6', 'Dijkstra': '#e74c3c'}
markers_graph = {'BFS': 'o', 'DFS': 's', 'Dijkstra': 'D'}

for algo in graph_algos:
    sub = agg[agg['algorithm'] == algo].sort_values('input_size')
    if not sub.empty:
        # Convert ns to milliseconds
        y_ms = sub['execution_time_ns'] / 1e6
        ax.plot(sub['input_size'], y_ms, label=f"{algo}", color=colors_graph[algo], 
                marker=markers_graph[algo], linewidth=2.5, markersize=7)

ax.set_title('Campus Network Graph Traversal & Shortest Path Runtimes', fontweight='bold', pad=12)
ax.set_xlabel('Graph Network Vertices (V locations)', fontweight='bold')
ax.set_ylabel('Execution Time (Milliseconds - ms)', fontweight='bold')
ax.legend(frameon=True, facecolor='white', framealpha=0.9, loc='upper left')
ax.grid(True, which="both", ls="--", alpha=0.5)
plt.tight_layout()
graph_fig_path = os.path.join(OUTPUT_DIR, 'graph_performance.png')
plt.savefig(graph_fig_path)
plt.close()
print(f"Saved: {graph_fig_path}")

# ── 5. MST Performance Graph ──────────────────────────────────────────────
fig, ax = plt.subplots(figsize=(8, 5), dpi=300)
mst_algos = ['Prim MST', 'Kruskal MST']
colors_mst = {'Prim MST': '#34495e', 'Kruskal MST': '#16a085'}
markers_mst = {'Prim MST': 'o', 'Kruskal MST': 's'}

for algo in mst_algos:
    sub = agg[agg['algorithm'] == algo].sort_values('input_size')
    if not sub.empty:
        # Convert ns to milliseconds
        y_ms = sub['execution_time_ns'] / 1e6
        ax.plot(sub['input_size'], y_ms, label=f"{algo}", color=colors_mst[algo], 
                marker=markers_mst[algo], linewidth=2.5, markersize=7)

ax.set_title('Minimum Spanning Tree (MST) Algorithm Runtime Comparison', fontweight='bold', pad=12)
ax.set_xlabel('Graph Network Vertices (V locations)', fontweight='bold')
ax.set_ylabel('Execution Time (Milliseconds - ms)', fontweight='bold')
ax.legend(frameon=True, facecolor='white', framealpha=0.9, loc='upper left')
ax.grid(True, which="both", ls="--", alpha=0.5)
plt.tight_layout()
mst_fig_path = os.path.join(OUTPUT_DIR, 'mst_performance.png')
plt.savefig(mst_fig_path)
plt.close()
print(f"Saved: {mst_fig_path}")

print("All performance graphs generated successfully!")