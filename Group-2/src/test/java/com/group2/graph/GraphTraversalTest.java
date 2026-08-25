package com.group2.graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTraversalTest {

    private Graph<String> campusGraph() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addEdge("Gate", "Balme", 1);
        graph.addEdge("Gate", "Commonwealth", 1);
        graph.addEdge("Balme", "Legon Hall", 1);
        graph.addEdge("Commonwealth", "Legon Hall", 1);
        return graph;
    }

    @Test
    void bfs_visitsEveryReachableVertexOnce() {
        List<String> order = GraphTraversal.bfs(campusGraph(), "Gate");
        assertEquals(4, order.size());
        assertEquals("Gate", order.get(0));
        assertTrue(order.containsAll(List.of("Gate", "Balme", "Commonwealth", "Legon Hall")));
    }

    @Test
    void dfs_visitsEveryReachableVertexOnce() {
        List<String> order = GraphTraversal.dfs(campusGraph(), "Gate");
        assertEquals(4, order.size());
        assertEquals("Gate", order.get(0));
        assertTrue(order.containsAll(List.of("Gate", "Balme", "Commonwealth", "Legon Hall")));
    }

    @Test
    void bfs_unknownStart_returnsEmptyList() {
        assertEquals(List.of(), GraphTraversal.bfs(campusGraph(), "Nowhere"));
    }

    @Test
    void dfs_disconnectedVertex_isNotVisited() {
        Graph<String> graph = campusGraph();
        graph.addVertex("Isolated");
        assertTrue(GraphTraversal.dfs(graph, "Gate").stream().noneMatch("Isolated"::equals));
    }
}
