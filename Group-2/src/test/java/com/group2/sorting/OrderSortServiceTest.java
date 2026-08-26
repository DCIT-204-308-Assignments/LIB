package com.group2.sorting;

import com.group2.model.Order;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderSortServiceTest {

    private List<Order> sampleOrders() {
        return new ArrayList<>(List.of(
                new Order("O1", "C1", "R1", "REST1", List.of("F1"), 30.0, 40, 3000L, 2),
                new Order("O2", "C2", "R2", "REST2", List.of("F2"), 10.0, 15, 1000L, 3),
                new Order("O3", "C3", "R3", "REST3", List.of("F3"), 20.0, 25, 2000L, 1)
        ));
    }

    @Test
    void sortByPrice_ordersAscendingByTotalPrice() {
        List<Order> orders = sampleOrders();
        OrderSortService.sortByPrice(orders, MergeSort::sort);
        assertEquals(List.of("O2", "O3", "O1"), idsOf(orders));
    }

    @Test
    void sortByDeliveryTime_ordersAscendingByDeliveryMinutes() {
        List<Order> orders = sampleOrders();
        OrderSortService.sortByDeliveryTime(orders, MergeSort::sort);
        assertEquals(List.of("O2", "O3", "O1"), idsOf(orders));
    }

    @Test
    void sortByOrderTime_ordersAscendingByOrderTimestamp() {
        List<Order> orders = sampleOrders();
        OrderSortService.sortByOrderTime(orders, MergeSort::sort);
        assertEquals(List.of("O2", "O3", "O1"), idsOf(orders));
    }

    @Test
    void sortByPriority_ordersAscendingByPriority() {
        List<Order> orders = sampleOrders();
        OrderSortService.sortByPriority(orders, MergeSort::sort);
        assertEquals(List.of("O3", "O1", "O2"), idsOf(orders));
    }

    private List<String> idsOf(List<Order> orders) {
        return orders.stream().map(Order::getId).toList();
    }
}
