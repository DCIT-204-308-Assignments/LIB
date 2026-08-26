package com.group2.sorting;

import com.group2.model.Order;

import java.util.Comparator;
import java.util.List;

/**
 * Sort operations over a list of {@link Order}s, as required by the project scope:
 * sort by price, delivery time, order time and priority. Caller picks the algorithm.
 */
public final class OrderSortService {

    private OrderSortService() {
    }

    public static void sortByPrice(List<Order> orders, Sorter<Order> sorter) {
        sorter.sort(orders, Comparator.comparingDouble(Order::getTotalPrice));
    }

    public static void sortByDeliveryTime(List<Order> orders, Sorter<Order> sorter) {
        sorter.sort(orders, Comparator.comparingInt(Order::getDeliveryTimeMinutes));
    }

    public static void sortByOrderTime(List<Order> orders, Sorter<Order> sorter) {
        sorter.sort(orders, Comparator.comparingLong(Order::getOrderTime));
    }

    public static void sortByPriority(List<Order> orders, Sorter<Order> sorter) {
        sorter.sort(orders, Comparator.comparingInt(Order::getPriority));
    }
}
