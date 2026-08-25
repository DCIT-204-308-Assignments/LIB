package com.group2.graph;

/**
 * A directed, weighted edge to {@code to}. Held inside the source vertex's
 * adjacency list/row, so the source is implicit.
 */
public record Edge<T>(T to, double weight) {
}
