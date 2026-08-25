package com.group2.optimization;

import com.group2.model.Rider;

/** A possible pairing of an order with a rider, at the given distance apart. */
public record AssignmentCandidate(String orderId, Rider rider, double distanceKm) {
}
