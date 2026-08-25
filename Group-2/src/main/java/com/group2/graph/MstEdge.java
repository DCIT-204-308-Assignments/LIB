package com.group2.graph;

/** An edge selected into a minimum spanning tree by {@link Prim} or {@link Kruskal}. */
public record MstEdge<T>(T from, T to, double weight) {
}
