package com.group2.routing;

import com.group2.graph.AdjacencyListGraph;
import com.group2.graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutingEngineTest {

    private Graph<String> campusGraph() {
        Graph<String> graph = new AdjacencyListGraph<>();
        graph.addEdge("Restaurant", "Junction", 3);
        graph.addEdge("Junction", "Hostel", 2);
        graph.addEdge("Restaurant", "Hostel", 8);
        return graph;
    }

    @Test
    void findRoute_prefersShorterMultiHopPathOverDirectEdge() {
        RoutingEngine<String> engine = new RoutingEngine<>(campusGraph());
        Route<String> route = engine.findRoute("Restaurant", "Hostel");

        assertEquals(List.of("Restaurant", "Junction", "Hostel"), route.path());
        assertEquals(5.0, route.totalDistance());
        assertTrue(route.isReachable());
    }

    @Test
    void findRoute_unknownVertex_isUnreachable() {
        RoutingEngine<String> engine = new RoutingEngine<>(campusGraph());
        Route<String> route = engine.findRoute("Restaurant", "Nowhere");

        assertFalse(route.isReachable());
        assertEquals(Double.POSITIVE_INFINITY, route.totalDistance());
    }
}
