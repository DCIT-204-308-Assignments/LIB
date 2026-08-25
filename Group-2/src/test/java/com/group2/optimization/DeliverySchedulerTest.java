package com.group2.optimization;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliverySchedulerTest {

    @Test
    void scheduleMaximum_selectsMostNonOverlappingDeliveries() {
        List<DeliveryWindow> candidates = List.of(
                new DeliveryWindow("O1", 1, 3),
                new DeliveryWindow("O2", 2, 5),
                new DeliveryWindow("O3", 4, 6),
                new DeliveryWindow("O4", 6, 8)
        );

        List<DeliveryWindow> selected = DeliveryScheduler.scheduleMaximum(candidates);

        assertEquals(List.of("O1", "O3", "O4"), selected.stream().map(DeliveryWindow::orderId).toList());
    }

    @Test
    void scheduleMaximum_noOverlaps_selectsAll() {
        List<DeliveryWindow> candidates = List.of(
                new DeliveryWindow("O1", 1, 2),
                new DeliveryWindow("O2", 3, 4)
        );

        List<DeliveryWindow> selected = DeliveryScheduler.scheduleMaximum(candidates);

        assertEquals(2, selected.size());
    }

    @Test
    void scheduleMaximum_emptyInput_returnsEmpty() {
        assertEquals(List.of(), DeliveryScheduler.scheduleMaximum(List.of()));
    }
}
