package com.group2.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Disjoint-set (union-find) with path compression and union by rank.
 * Near O(1) amortized find/union. Used by {@link Kruskal} to detect cycles.
 */
final class UnionFind<T> {

    private final Map<T, T> parent = new HashMap<>();
    private final Map<T, Integer> rank = new HashMap<>();

    UnionFind(List<T> elements) {
        for (T element : elements) {
            parent.put(element, element);
            rank.put(element, 0);
        }
    }

    T find(T element) {
        T root = parent.get(element);
        if (root.equals(element)) {
            return element;
        }
        root = find(root);
        parent.put(element, root);
        return root;
    }

    /** Merges the sets containing {@code a} and {@code b}. Returns false if they were already joined. */
    boolean union(T a, T b) {
        T rootA = find(a);
        T rootB = find(b);
        if (rootA.equals(rootB)) {
            return false;
        }
        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);
        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
        return true;
    }
}
