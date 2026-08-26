package com.group2.model;

import java.util.List;

public class Order {

    private final String id;
    private final String customerId;
    private final String riderId;
    private final String restaurantId;
    private final List<String> foodItemIds;
    private final double totalPrice;
    private final int deliveryTimeMinutes;
    private final long orderTime;
    private final int priority;

    public Order(String id, String customerId, String riderId, String restaurantId,
                 List<String> foodItemIds, double totalPrice, int deliveryTimeMinutes,
                 long orderTime, int priority) {
        this.id = id;
        this.customerId = customerId;
        this.riderId = riderId;
        this.restaurantId = restaurantId;
        this.foodItemIds = foodItemIds;
        this.totalPrice = totalPrice;
        this.deliveryTimeMinutes = deliveryTimeMinutes;
        this.orderTime = orderTime;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public List<String> getFoodItemIds() {
        return foodItemIds;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public int getDeliveryTimeMinutes() {
        return deliveryTimeMinutes;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', customerId='" + customerId + "', riderId='" + riderId
                + "', restaurantId='" + restaurantId + "', foodItemIds=" + foodItemIds
                + ", totalPrice=" + totalPrice + ", deliveryTimeMinutes=" + deliveryTimeMinutes
                + ", orderTime=" + orderTime + ", priority=" + priority + "}";
    }
}
