package com.group2.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Graph backed by an adjacency list: each vertex maps to its list of outgoing edges.
 * O(1) vertex insertion, O(1) average edge insertion, O(degree) edge lookup,
 * O(V + E) space. Preferred over {@link AdjacencyMatrixGraph} for sparse graphs
 * such as a campus road network.
 */
public class AdjacencyListGraph<T> implements Graph<T> {

    private final boolean directed;
    private final Map<T, List<Edge<T>>> adjacency = new LinkedHashMap<>();
    private int edgeCount;

    public AdjacencyListGraph() {
        this(false);
    }

    public AdjacencyListGraph(boolean directed) {
        this.directed = directed;
    }

    @Override
    public void addVertex(T vertex) {
        adjacency.putIfAbsent(vertex, new ArrayList<>());
    }

    @Override
    public void addEdge(T from, T to, double weight) {
        addVertex(from);
        addVertex(to);
        boolean isNewEdge = setEdge(adjacency.get(from), to, weight);
        if (!directed) {
            setEdge(adjacency.get(to), from, weight);
        }
        if (isNewEdge) {
            edgeCount++;
        }
    }

    /** Adds or overwrites the edge to {@code to} in {@code edges}. Returns true if it was newly added. */
    private boolean setEdge(List<Edge<T>> edges, T to, double weight) {
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).to().equals(to)) {
                edges.set(i, new Edge<>(to, weight));
                return false;
            }
        }
        edges.add(new Edge<>(to, weight));
        return true;
    }

    @Override
    public boolean hasVertex(T vertex) {
        return adjacency.containsKey(vertex);
    }

    @Override
    public boolean hasEdge(T from, T to) {
        List<Edge<T>> edges = adjacency.get(from);
        if (edges == null) {
            return false;
        }
        return edges.stream().anyMatch(edge -> edge.to().equals(to));
    }

    @Override
    public double weight(T from, T to) {
        List<Edge<T>> edges = adjacency.get(from);
        if (edges != null) {
            for (Edge<T> edge : edges) {
                if (edge.to().equals(to)) {
                    return edge.weight();
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public List<T> vertices() {
        return new ArrayList<>(adjacency.keySet());
    }

    @Override
    public List<Edge<T>> neighbors(T vertex) {
        List<Edge<T>> edges = adjacency.get(vertex);
        return edges == null ? List.of() : List.copyOf(edges);
    }

    @Override
    public int vertexCount() {
        return adjacency.size();
    }

    @Override
    public int edgeCount() {
        return edgeCount;
    }
}
