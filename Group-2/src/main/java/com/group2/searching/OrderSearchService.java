package com.group2.searching;

import com.group2.model.Order;

import java.util.Comparator;
import java.util.List;

/**
 * Search operations over a list of {@link Order}s, as required by the
 * project scope: search by restaurant, customer, rider, food item and order ID.
 */
public final class OrderSearchService {

    private OrderSearchService() {
    }

    /**
     * Finds an order by its ID using binary search.
     * {@code orders} must already be sorted by order ID (see {@link #sortedById(List)}).
     */
    public static Order searchByOrderId(List<Order> ordersSortedById, String orderId) {
        return BinarySearch.find(ordersSortedById, orderId, Order::getId);
    }

    /**
     * Finds all orders placed by a given customer using linear search.
     */
    public static List<Order> searchByCustomer(List<Order> orders, String customerId) {
        return LinearSearch.findAll(orders, order -> order.getCustomerId().equals(customerId));
    }

    /**
     * Finds all orders assigned to a given rider using linear search.
     */
    public static List<Order> searchByRider(List<Order> orders, String riderId) {
        return LinearSearch.findAll(orders, order -> order.getRiderId() != null && order.getRiderId().equals(riderId));
    }

    /**
     * Finds all orders placed at a given restaurant using linear search.
     */
    public static List<Order> searchByRestaurant(List<Order> orders, String restaurantId) {
        return LinearSearch.findAll(orders, order -> order.getRestaurantId().equals(restaurantId));
    }

    /**
     * Finds all orders that contain a given food item using linear search.
     */
    public static List<Order> searchByFoodItem(List<Order> orders, String foodItemId) {
        return LinearSearch.findAll(orders, order -> order.getFoodItemIds().contains(foodItemId));
    }

    /**
     * Returns a new list of {@code orders} sorted by order ID, a precondition for {@link #searchByOrderId}.
     */
    public static List<Order> sortedById(List<Order> orders) {
        return orders.stream().sorted(Comparator.comparing(Order::getId)).toList();
    }
}
