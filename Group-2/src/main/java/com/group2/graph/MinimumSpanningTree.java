package com.group2.graph;

import java.util.List;

/** Result of a minimum spanning tree computation: the edges chosen and their total weight. */
public record MinimumSpanningTree<T>(List<MstEdge<T>> edges, double totalWeight) {
}
