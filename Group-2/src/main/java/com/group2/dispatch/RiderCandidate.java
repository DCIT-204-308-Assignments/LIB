package com.group2.dispatch;

import com.group2.model.Rider;

/**
 * A rider being considered for a dispatch, along with the factors that determine
 * dispatch priority: distance to the order, its urgency, and the restaurant's
 * estimated preparation time.
 */
public record RiderCandidate(Rider rider, double distanceKm, int urgency, int estimatedPrepTimeMinutes) {
}
