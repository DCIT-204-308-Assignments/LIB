package com.group2.searching;

import com.group2.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSearchServiceTest {

    private List<Order> orders;

    @BeforeEach
    void setUp() {
        orders = List.of(
                new Order("O3", "C1", "R1", "REST1", List.of("F1", "F2"), 45.0, 35, 1000L, 1),
                new Order("O1", "C2", "R2", "REST2", List.of("F3"), 20.0, 20, 1001L, 2),
                new Order("O2", "C1", null, "REST1", List.of("F1"), 15.0, 25, 1002L, 3)
        );
    }

    @Test
    void searchByOrderId_findsOrder_whenSortedFirst() {
        List<Order> sorted = OrderSearchService.sortedById(orders);
        Order found = OrderSearchService.searchByOrderId(sorted, "O2");
        assertEquals("O2", found.getId());
    }

    @Test
    void searchByOrderId_returnsNull_whenIdNotPresent() {
        List<Order> sorted = OrderSearchService.sortedById(orders);
        assertNull(OrderSearchService.searchByOrderId(sorted, "O99"));
    }

    @Test
    void searchByCustomer_returnsAllOrdersForCustomer() {
        List<Order> result = OrderSearchService.searchByCustomer(orders, "C1");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getCustomerId().equals("C1")));
    }

    @Test
    void searchByRider_ignoresOrdersWithNoRiderAssigned() {
        List<Order> result = OrderSearchService.searchByRider(orders, "R2");
        assertEquals(1, result.size());
        assertEquals("O1", result.get(0).getId());
    }

    @Test
    void searchByRestaurant_returnsAllOrdersForRestaurant() {
        List<Order> result = OrderSearchService.searchByRestaurant(orders, "REST1");
        assertEquals(2, result.size());
    }

    @Test
    void searchByFoodItem_returnsOrdersContainingItem() {
        List<Order> result = OrderSearchService.searchByFoodItem(orders, "F1");
        assertEquals(2, result.size());
    }

    @Test
    void searchByFoodItem_returnsEmpty_whenItemNotOrdered() {
        assertTrue(OrderSearchService.searchByFoodItem(orders, "F99").isEmpty());
    }
}
