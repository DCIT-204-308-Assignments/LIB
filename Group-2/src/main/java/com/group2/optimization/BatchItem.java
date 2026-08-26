package com.group2.optimization;

/** An order considered for batching: {@code weight} is its size/load, {@code value} its priority or fee. */
public record BatchItem(String orderId, int weight, double value) {
}
