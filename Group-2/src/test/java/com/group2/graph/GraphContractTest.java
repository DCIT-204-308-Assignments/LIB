package com.group2.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shared behavioural contract that every {@link Graph} implementation must satisfy.
 * Each representation gets its own concrete subclass so failures are reported per structure.
 */
abstract class GraphContractTest {

    protected abstract Graph<String> createGraph(boolean directed);

    @Test
    void newGraph_isEmpty() {
        Graph<String> graph = createGraph(false);
        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
    }

    @Test
    void addVertex_registersVertexWithNoEdges() {
        Graph<String> graph = createGraph(false);
        graph.addVertex("A");
        assertTrue(graph.hasVertex("A"));
        assertEquals(1, graph.vertexCount());
        assertEquals(java.util.List.of(), graph.neighbors("A"));
    }

    @Test
    void addEdge_undirected_isTraversableBothWays() {
        Graph<String> graph = createGraph(false);
        graph.addEdge("A", "B", 5.0);

        assertTrue(graph.hasEdge("A", "B"));
        assertTrue(graph.hasEdge("B", "A"));
        assertEquals(5.0, graph.weight("A", "B"));
        assertEquals(5.0, graph.weight("B", "A"));
        assertEquals(1, graph.edgeCount());
        assertEquals(2, graph.vertexCount());
    }

    @Test
    void addEdge_directed_isOneWayOnly() {
        Graph<String> graph = createGraph(true);
        graph.addEdge("A", "B", 5.0);

        assertTrue(graph.hasEdge("A", "B"));
        assertFalse(graph.hasEdge("B", "A"));
        assertEquals(Double.POSITIVE_INFINITY, graph.weight("B", "A"));
    }

    @Test
    void hasEdge_missingEdge_returnsFalse() {
        Graph<String> graph = createGraph(false);
        graph.addVertex("A");
        graph.addVertex("B");
        assertFalse(graph.hasEdge("A", "B"));
        assertEquals(Double.POSITIVE_INFINITY, graph.weight("A", "B"));
    }

    @Test
    void neighbors_returnsAllOutgoingEdges() {
        Graph<String> graph = createGraph(true);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 2.0);

        var neighbors = graph.neighbors("A");
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.stream().anyMatch(e -> e.to().equals("B") && e.weight() == 1.0));
        assertTrue(neighbors.stream().anyMatch(e -> e.to().equals("C") && e.weight() == 2.0));
    }

    @Test
    void addEdge_updatesWeightOfExistingEdgeWithoutDoubleCountingEdgeCount() {
        Graph<String> graph = createGraph(true);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "B", 9.0);

        assertEquals(9.0, graph.weight("A", "B"));
        assertEquals(1, graph.edgeCount());
    }
}
