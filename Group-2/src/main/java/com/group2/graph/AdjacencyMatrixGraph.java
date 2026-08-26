package com.group2.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Graph backed by an adjacency matrix, growing the matrix as vertices are added.
 * O(1) edge lookup, O(V^2) space. Preferred over {@link AdjacencyListGraph} for
 * dense graphs where most vertex pairs are connected.
 */
public class AdjacencyMatrixGraph<T> implements Graph<T> {

    private final boolean directed;
    private final Map<T, Integer> indexOf = new LinkedHashMap<>();
    private final List<T> vertexList = new ArrayList<>();
    private double[][] matrix = new double[0][0];
    private int edgeCount;

    public AdjacencyMatrixGraph() {
        this(false);
    }

    public AdjacencyMatrixGraph(boolean directed) {
        this.directed = directed;
    }

    @Override
    public void addVertex(T vertex) {
        if (indexOf.containsKey(vertex)) {
            return;
        }
        indexOf.put(vertex, vertexList.size());
        vertexList.add(vertex);
        grow();
    }

    private void grow() {
        int n = vertexList.size();
        double[][] resized = new double[n][n];
        for (double[] row : resized) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, resized[i], 0, matrix[i].length);
        }
        matrix = resized;
    }

    @Override
    public void addEdge(T from, T to, double weight) {
        addVertex(from);
        addVertex(to);
        int i = indexOf.get(from);
        int j = indexOf.get(to);
        if (matrix[i][j] == Double.POSITIVE_INFINITY) {
            edgeCount++;
        }
        matrix[i][j] = weight;
        if (!directed) {
            matrix[j][i] = weight;
        }
    }

    @Override
    public boolean hasVertex(T vertex) {
        return indexOf.containsKey(vertex);
    }

    @Override
    public boolean hasEdge(T from, T to) {
        return weight(from, to) != Double.POSITIVE_INFINITY;
    }

    @Override
    public double weight(T from, T to) {
        Integer i = indexOf.get(from);
        Integer j = indexOf.get(to);
        if (i == null || j == null) {
            return Double.POSITIVE_INFINITY;
        }
        return matrix[i][j];
    }

    @Override
    public List<T> vertices() {
        return List.copyOf(vertexList);
    }

    @Override
    public List<Edge<T>> neighbors(T vertex) {
        Integer i = indexOf.get(vertex);
        if (i == null) {
            return List.of();
        }
        List<Edge<T>> result = new ArrayList<>();
        for (int j = 0; j < vertexList.size(); j++) {
            if (matrix[i][j] != Double.POSITIVE_INFINITY) {
                result.add(new Edge<>(vertexList.get(j), matrix[i][j]));
            }
        }
        return result;
    }

    @Override
    public int vertexCount() {
        return vertexList.size();
    }

    @Override
    public int edgeCount() {
        return edgeCount;
    }
}
