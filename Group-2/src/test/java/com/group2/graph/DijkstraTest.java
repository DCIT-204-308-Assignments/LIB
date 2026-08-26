package com.group2.graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DijkstraTest {

    @Test
    void shortestPaths_picksLowerWeightPathOverFewerHops() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addEdge("A", "B", 10);
        graph.addEdge("A", "C", 1);
        graph.addEdge("C", "B", 1);

        Dijkstra.ShortestPaths<String> paths = Dijkstra.shortestPaths(graph, "A");

        assertEquals(2.0, paths.distanceTo("B"));
        assertEquals(List.of("A", "C", "B"), paths.pathTo("B"));
    }

    @Test
    void distanceTo_source_isZero() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addEdge("A", "B", 5);
        Dijkstra.ShortestPaths<String> paths = Dijkstra.shortestPaths(graph, "A");
        assertEquals(0.0, paths.distanceTo("A"));
        assertEquals(List.of("A"), paths.pathTo("A"));
    }

    @Test
    void unreachableVertex_hasInfiniteDistanceAndEmptyPath() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addVertex("A");
        graph.addVertex("Isolated");
        Dijkstra.ShortestPaths<String> paths = Dijkstra.shortestPaths(graph, "A");
        assertEquals(Double.POSITIVE_INFINITY, paths.distanceTo("Isolated"));
        assertEquals(List.of(), paths.pathTo("Isolated"));
    }
}
