package com.group2.graph;

import java.util.List;

/**
 * A weighted graph over vertices of type {@code T}, used to represent the
 * University of Ghana campus road network. Implemented as either an adjacency
 * list ({@link AdjacencyListGraph}) or an adjacency matrix ({@link AdjacencyMatrixGraph}).
 */
public interface Graph<T> {

    void addVertex(T vertex);

    void addEdge(T from, T to, double weight);

    boolean hasVertex(T vertex);

    boolean hasEdge(T from, T to);

    /** Weight of the edge from {@code from} to {@code to}, or {@link Double#POSITIVE_INFINITY} if none exists. */
    double weight(T from, T to);

    List<T> vertices();

    List<Edge<T>> neighbors(T vertex);

    int vertexCount();

    int edgeCount();
}
