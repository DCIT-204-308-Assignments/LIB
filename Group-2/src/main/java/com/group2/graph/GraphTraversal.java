package com.group2.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Breadth-first and depth-first traversal over a {@link Graph}. Both O(V + E).
 */
public final class GraphTraversal {

    private GraphTraversal() {
    }

    public static <T> List<T> bfs(Graph<T> graph, T start) {
        List<T> order = new ArrayList<>();
        if (!graph.hasVertex(start)) {
            return order;
        }
        Set<T> visited = new LinkedHashSet<>();
        Deque<T> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            T current = queue.poll();
            order.add(current);
            for (Edge<T> edge : graph.neighbors(current)) {
                if (visited.add(edge.to())) {
                    queue.add(edge.to());
                }
            }
        }
        return order;
    }

    public static <T> List<T> dfs(Graph<T> graph, T start) {
        List<T> order = new ArrayList<>();
        if (!graph.hasVertex(start)) {
            return order;
        }
        dfsVisit(graph, start, new LinkedHashSet<>(), order);
        return order;
    }

    private static <T> void dfsVisit(Graph<T> graph, T current, Set<T> visited, List<T> order) {
        visited.add(current);
        order.add(current);
        for (Edge<T> edge : graph.neighbors(current)) {
            if (!visited.contains(edge.to())) {
                dfsVisit(graph, edge.to(), visited, order);
            }
        }
    }
}
