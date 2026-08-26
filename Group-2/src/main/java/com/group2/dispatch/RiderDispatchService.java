package com.group2.dispatch;

import com.group2.model.Rider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Dispatches riders to orders by priority. Available riders are ranked by a
 * weighted combination of distance, urgency and restaurant preparation time
 * (lower is better), using a {@link MinHeap} to always pick the best-ranked
 * candidate first. Unavailable riders are never dispatched.
 */
public final class RiderDispatchService {

    private static final double DISTANCE_WEIGHT = 1.0;
    private static final double PREP_TIME_WEIGHT = 0.5;
    private static final double URGENCY_WEIGHT = 2.0;

    private RiderDispatchService() {
    }

    public static double score(RiderCandidate candidate) {
        return candidate.distanceKm() * DISTANCE_WEIGHT
                + candidate.estimatedPrepTimeMinutes() * PREP_TIME_WEIGHT
                - candidate.urgency() * URGENCY_WEIGHT;
    }

    /** Picks the single best available rider for a dispatch, or empty if none are available. */
    public static Optional<Rider> dispatch(List<RiderCandidate> candidates) {
        List<Rider> ranked = dispatchOrder(candidates);
        return ranked.isEmpty() ? Optional.empty() : Optional.of(ranked.get(0));
    }

    /** Ranks all available riders from best to worst dispatch candidate. */
    public static List<Rider> dispatchOrder(List<RiderCandidate> candidates) {
        MinHeap<RiderCandidate> heap = new MinHeap<>(Comparator.comparingDouble(RiderDispatchService::score));
        for (RiderCandidate candidate : candidates) {
            if (candidate.rider().isAvailable()) {
                heap.insert(candidate);
            }
        }
        List<Rider> ranked = new ArrayList<>();
        while (!heap.isEmpty()) {
            ranked.add(heap.extractMin().rider());
        }
        return ranked;
    }
}
