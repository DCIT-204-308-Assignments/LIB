package com.group2.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KruskalTest {

    @Test
    void minimumSpanningTree_selectsCheapestConnectingEdges() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 2);
        graph.addEdge("A", "C", 5);

        MinimumSpanningTree<String> mst = Kruskal.minimumSpanningTree(graph);

        assertEquals(2, mst.edges().size());
        assertEquals(3.0, mst.totalWeight());
    }

    @Test
    void minimumSpanningTree_rejectsEdgeThatWouldFormACycle() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 1);

        MinimumSpanningTree<String> mst = Kruskal.minimumSpanningTree(graph);

        assertEquals(2, mst.edges().size());
        assertEquals(2.0, mst.totalWeight());
    }
}
