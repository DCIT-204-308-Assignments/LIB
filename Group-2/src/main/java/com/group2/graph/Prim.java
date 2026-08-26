package com.group2.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Prim's minimum spanning tree algorithm, grown outward from a start vertex.
 * O(E log V) with a binary heap priority queue. Assumes an undirected, connected graph.
 */
public final class Prim {

    private Prim() {
    }

    public static <T> MinimumSpanningTree<T> minimumSpanningTree(Graph<T> graph, T start) {
        List<MstEdge<T>> mstEdges = new ArrayList<>();
        double totalWeight = 0;
        Set<T> visited = new HashSet<>();
        PriorityQueue<MstEdge<T>> frontier = new PriorityQueue<>((a, b) -> Double.compare(a.weight(), b.weight()));

        visited.add(start);
        enqueueFrontier(graph, start, visited, frontier);

        while (!frontier.isEmpty() && visited.size() < graph.vertexCount()) {
            MstEdge<T> next = frontier.poll();
            if (visited.contains(next.to())) {
                continue;
            }
            visited.add(next.to());
            mstEdges.add(next);
            totalWeight += next.weight();
            enqueueFrontier(graph, next.to(), visited, frontier);
        }
        return new MinimumSpanningTree<>(mstEdges, totalWeight);
    }

    private static <T> void enqueueFrontier(Graph<T> graph, T from, Set<T> visited, PriorityQueue<MstEdge<T>> frontier) {
        for (Edge<T> edge : graph.neighbors(from)) {
            if (!visited.contains(edge.to())) {
                frontier.add(new MstEdge<>(from, edge.to(), edge.weight()));
            }
        }
    }
}
