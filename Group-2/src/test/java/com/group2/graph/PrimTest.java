package com.group2.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimTest {

    @Test
    void minimumSpanningTree_selectsCheapestConnectingEdges() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 2);
        graph.addEdge("A", "C", 5);

        MinimumSpanningTree<String> mst = Prim.minimumSpanningTree(graph, "A");

        assertEquals(2, mst.edges().size());
        assertEquals(3.0, mst.totalWeight());
    }

    @Test
    void minimumSpanningTree_singleVertex_hasNoEdges() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addVertex("A");
        MinimumSpanningTree<String> mst = Prim.minimumSpanningTree(graph, "A");
        assertEquals(0, mst.edges().size());
        assertEquals(0.0, mst.totalWeight());
    }
}
