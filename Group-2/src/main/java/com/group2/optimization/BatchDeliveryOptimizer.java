package com.group2.optimization;

import java.util.ArrayList;
import java.util.List;

/**
 * 0/1 knapsack via dynamic programming: picks the subset of orders that fits a
 * rider's carrying capacity while maximizing total value (e.g. delivery fee or
 * priority). O(n * capacity) time and space.
 */
public final class BatchDeliveryOptimizer {

    private BatchDeliveryOptimizer() {
    }

    public static Batch selectBatch(List<BatchItem> items, int capacity) {
        int n = items.size();
        double[][] table = buildTable(items, capacity);

        List<BatchItem> selected = new ArrayList<>();
        int remaining = capacity;
        for (int i = n; i > 0; i--) {
            if (table[i][remaining] != table[i - 1][remaining]) {
                BatchItem item = items.get(i - 1);
                selected.add(item);
                remaining -= item.weight();
            }
        }
        return new Batch(selected, table[n][capacity]);
    }

    private static double[][] buildTable(List<BatchItem> items, int capacity) {
        int n = items.size();
        double[][] table = new double[n + 1][capacity + 1];
        for (int i = 1; i <= n; i++) {
            BatchItem item = items.get(i - 1);
            for (int c = 0; c <= capacity; c++) {
                table[i][c] = table[i - 1][c];
                if (item.weight() <= c) {
                    table[i][c] = Math.max(table[i][c], table[i - 1][c - item.weight()] + item.value());
                }
            }
        }
        return table;
    }

    /** The selected orders and their combined value. */
    public record Batch(List<BatchItem> items, double totalValue) {
    }
}
