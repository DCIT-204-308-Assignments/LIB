package com.group2.routing;

import com.group2.graph.Dijkstra;
import com.group2.graph.Graph;

import java.util.List;

/**
 * Finds the shortest delivery route between two points on the campus road network,
 * using Dijkstra's algorithm over a {@link Graph}.
 */
public final class RoutingEngine<T> {

    private final Graph<T> campusGraph;

    public RoutingEngine(Graph<T> campusGraph) {
        this.campusGraph = campusGraph;
    }

    public Route<T> findRoute(T from, T to) {
        if (!campusGraph.hasVertex(from) || !campusGraph.hasVertex(to)) {
            return new Route<>(List.of(), Double.POSITIVE_INFINITY);
        }
        Dijkstra.ShortestPaths<T> shortestPaths = Dijkstra.shortestPaths(campusGraph, from);
        double distance = shortestPaths.distanceTo(to);
        if (distance == Double.POSITIVE_INFINITY) {
            return new Route<>(List.of(), Double.POSITIVE_INFINITY);
        }
        return new Route<>(shortestPaths.pathTo(to), distance);
    }
}
