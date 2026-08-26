package com.group2.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Dijkstra's single-source shortest path algorithm. O((V + E) log V) with a
 * binary heap priority queue. Requires non-negative edge weights.
 */
public final class Dijkstra {

    private Dijkstra() {
    }

    public static <T> ShortestPaths<T> shortestPaths(Graph<T> graph, T source) {
        Map<T, Double> distances = new HashMap<>();
        Map<T, T> predecessors = new HashMap<>();
        for (T vertex : graph.vertices()) {
            distances.put(vertex, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);

        PriorityQueue<T> queue = new PriorityQueue<>((a, b) -> Double.compare(distances.get(a), distances.get(b)));
        queue.add(source);
        Set<T> settled = new HashSet<>();

        while (!queue.isEmpty()) {
            T current = queue.poll();
            if (!settled.add(current)) {
                continue;
            }
            for (Edge<T> edge : graph.neighbors(current)) {
                double candidate = distances.get(current) + edge.weight();
                if (candidate < distances.get(edge.to())) {
                    distances.put(edge.to(), candidate);
                    predecessors.put(edge.to(), current);
                    queue.add(edge.to());
                }
            }
        }
        return new ShortestPaths<>(source, distances, predecessors);
    }

    /** Distances and predecessor links computed from a single source, with path reconstruction. */
    public static final class ShortestPaths<T> {
        private final T source;
        private final Map<T, Double> distances;
        private final Map<T, T> predecessors;

        private ShortestPaths(T source, Map<T, Double> distances, Map<T, T> predecessors) {
            this.source = source;
            this.distances = distances;
            this.predecessors = predecessors;
        }

        public double distanceTo(T target) {
            return distances.getOrDefault(target, Double.POSITIVE_INFINITY);
        }

        /** Shortest path from the source to {@code target}, or empty if unreachable. */
        public List<T> pathTo(T target) {
            if (distanceTo(target) == Double.POSITIVE_INFINITY) {
                return List.of();
            }
            List<T> path = new ArrayList<>();
            T step = target;
            while (step != null && !step.equals(source)) {
                path.add(step);
                step = predecessors.get(step);
            }
            path.add(source);
            Collections.reverse(path);
            return path;
        }
    }
}
