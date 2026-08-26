package com.group2.optimization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Greedy interval scheduling for delivery scheduling: given candidate delivery
 * windows for a single rider, selects the maximum-size set of non-overlapping
 * deliveries by always taking the one that finishes earliest next. O(n log n).
 */
public final class DeliveryScheduler {

    private DeliveryScheduler() {
    }

    public static List<DeliveryWindow> scheduleMaximum(List<DeliveryWindow> candidates) {
        List<DeliveryWindow> sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator.comparingDouble(DeliveryWindow::endTime));

        List<DeliveryWindow> selected = new ArrayList<>();
        double lastEnd = Double.NEGATIVE_INFINITY;
        for (DeliveryWindow window : sorted) {
            if (window.startTime() >= lastEnd) {
                selected.add(window);
                lastEnd = window.endTime();
            }
        }
        return selected;
    }
}
