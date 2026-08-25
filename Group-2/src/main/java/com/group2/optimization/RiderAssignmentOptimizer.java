package com.group2.optimization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Greedy rider assignment: considers order/rider pairs in ascending distance order
 * and assigns each rider to at most one order, taking the closest pairing available
 * at each step. O(n log n) on the candidate pairs.
 */
public final class RiderAssignmentOptimizer {

    private RiderAssignmentOptimizer() {
    }

    /** Maps orderId to the assigned rider's id. */
    public static Map<String, String> assign(List<AssignmentCandidate> candidates) {
        List<AssignmentCandidate> sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator.comparingDouble(AssignmentCandidate::distanceKm));

        Map<String, String> assignments = new HashMap<>();
        Set<String> assignedOrders = new HashSet<>();
        Set<String> busyRiders = new HashSet<>();

        for (AssignmentCandidate candidate : sorted) {
            String riderId = candidate.rider().getId();
            if (assignedOrders.contains(candidate.orderId()) || busyRiders.contains(riderId)) {
                continue;
            }
            assignments.put(candidate.orderId(), riderId);
            assignedOrders.add(candidate.orderId());
            busyRiders.add(riderId);
        }
        return assignments;
    }
}
