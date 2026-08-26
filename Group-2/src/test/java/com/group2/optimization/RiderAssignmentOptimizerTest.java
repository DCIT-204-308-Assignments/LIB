package com.group2.optimization;

import com.group2.model.Rider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RiderAssignmentOptimizerTest {

    @Test
    void assign_prefersClosestPairingsAndAssignsEachRiderOnce() {
        Rider r1 = new Rider("R1", "Ama", true, 4.5);
        Rider r2 = new Rider("R2", "Kofi", true, 4.0);

        List<AssignmentCandidate> candidates = List.of(
                new AssignmentCandidate("O1", r1, 1.0),
                new AssignmentCandidate("O1", r2, 2.0),
                new AssignmentCandidate("O2", r1, 0.5),
                new AssignmentCandidate("O2", r2, 3.0)
        );

        Map<String, String> assignments = RiderAssignmentOptimizer.assign(candidates);

        assertEquals("R1", assignments.get("O2"));
        assertEquals("R2", assignments.get("O1"));
        assertEquals(2, assignments.size());
    }

    @Test
    void assign_moreOrdersThanRiders_leavesSomeOrdersUnassigned() {
        Rider r1 = new Rider("R1", "Ama", true, 4.5);

        List<AssignmentCandidate> candidates = List.of(
                new AssignmentCandidate("O1", r1, 1.0),
                new AssignmentCandidate("O2", r1, 2.0)
        );

        Map<String, String> assignments = RiderAssignmentOptimizer.assign(candidates);

        assertEquals(1, assignments.size());
        assertEquals("R1", assignments.get("O1"));
        assertFalse(assignments.containsKey("O2"));
    }
}
