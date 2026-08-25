package com.group2.optimization;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchDeliveryOptimizerTest {

    @Test
    void selectBatch_maximizesValueWithinCapacity() {
        List<BatchItem> items = List.of(
                new BatchItem("O1", 2, 3),
                new BatchItem("O2", 3, 4),
                new BatchItem("O3", 4, 5),
                new BatchItem("O4", 5, 6)
        );

        BatchDeliveryOptimizer.Batch batch = BatchDeliveryOptimizer.selectBatch(items, 5);

        assertEquals(7.0, batch.totalValue());
        assertEquals(
                List.of("O2", "O1"),
                batch.items().stream().map(BatchItem::orderId).toList()
        );
    }

    @Test
    void selectBatch_zeroCapacity_selectsNothing() {
        List<BatchItem> items = List.of(new BatchItem("O1", 2, 3));
        BatchDeliveryOptimizer.Batch batch = BatchDeliveryOptimizer.selectBatch(items, 0);

        assertEquals(0.0, batch.totalValue());
        assertEquals(List.of(), batch.items());
    }

    @Test
    void selectBatch_capacityFitsEverything_selectsAll() {
        List<BatchItem> items = List.of(
                new BatchItem("O1", 1, 3),
                new BatchItem("O2", 1, 4)
        );
        BatchDeliveryOptimizer.Batch batch = BatchDeliveryOptimizer.selectBatch(items, 10);

        assertEquals(7.0, batch.totalValue());
        assertEquals(2, batch.items().size());
    }
}
