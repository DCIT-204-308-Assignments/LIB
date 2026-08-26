package com.group2.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Kruskal's minimum spanning tree algorithm: edges considered in ascending weight
 * order, union-find rejecting any that would form a cycle. O(E log E). Assumes an
 * undirected, connected graph.
 */
public final class Kruskal {

    private Kruskal() {
    }

    public static <T> MinimumSpanningTree<T> minimumSpanningTree(Graph<T> graph) {
        List<T> vertices = graph.vertices();

        List<MstEdge<T>> candidateEdges = new ArrayList<>();
        for (T vertex : vertices) {
            for (Edge<T> edge : graph.neighbors(vertex)) {
                candidateEdges.add(new MstEdge<>(vertex, edge.to(), edge.weight()));
            }
        }
        candidateEdges.sort(Comparator.comparingDouble(MstEdge::weight));

        UnionFind<T> unionFind = new UnionFind<>(vertices);
        List<MstEdge<T>> mstEdges = new ArrayList<>();
        double totalWeight = 0;
        for (MstEdge<T> edge : candidateEdges) {
            if (mstEdges.size() == vertices.size() - 1) {
                break;
            }
            if (unionFind.union(edge.from(), edge.to())) {
                mstEdges.add(edge);
                totalWeight += edge.weight();
            }
        }
        return new MinimumSpanningTree<>(mstEdges, totalWeight);
    }
}
