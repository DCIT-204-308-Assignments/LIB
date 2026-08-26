package com.group2.dispatch;

import com.group2.model.Rider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiderDispatchServiceTest {

    @Test
    void dispatch_picksClosestAvailableRider() {
        Rider near = new Rider("R1", "Ama", true, 4.5);
        Rider far = new Rider("R2", "Kofi", true, 4.5);

        List<RiderCandidate> candidates = List.of(
                new RiderCandidate(near, 1.0, 1, 5),
                new RiderCandidate(far, 8.0, 1, 5)
        );

        Optional<Rider> dispatched = RiderDispatchService.dispatch(candidates);

        assertTrue(dispatched.isPresent());
        assertEquals("R1", dispatched.get().getId());
    }

    @Test
    void dispatch_ignoresUnavailableRiders() {
        Rider unavailable = new Rider("R1", "Ama", false, 4.5);
        Rider available = new Rider("R2", "Kofi", true, 4.5);

        List<RiderCandidate> candidates = List.of(
                new RiderCandidate(unavailable, 1.0, 1, 5),
                new RiderCandidate(available, 8.0, 1, 5)
        );

        Optional<Rider> dispatched = RiderDispatchService.dispatch(candidates);

        assertTrue(dispatched.isPresent());
        assertEquals("R2", dispatched.get().getId());
    }

    @Test
    void dispatch_noAvailableRiders_returnsEmpty() {
        Rider unavailable = new Rider("R1", "Ama", false, 4.5);
        List<RiderCandidate> candidates = List.of(new RiderCandidate(unavailable, 1.0, 1, 5));

        assertTrue(RiderDispatchService.dispatch(candidates).isEmpty());
    }

    @Test
    void dispatchOrder_higherUrgencyRanksBeforeLowerUrgencyAtEqualDistance() {
        Rider urgent = new Rider("R1", "Ama", true, 4.5);
        Rider routine = new Rider("R2", "Kofi", true, 4.5);

        List<RiderCandidate> candidates = List.of(
                new RiderCandidate(routine, 2.0, 1, 5),
                new RiderCandidate(urgent, 2.0, 9, 5)
        );

        List<Rider> ranked = RiderDispatchService.dispatchOrder(candidates);

        assertEquals(List.of("R1", "R2"), ranked.stream().map(Rider::getId).toList());
    }
}
