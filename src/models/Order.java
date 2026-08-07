package models;

public class Order {
    private final int orderId;
    private final String customerName;
    private final String restaurant;
    private final String foodItem;
    private final double foodWeightKg;
    private final int pickupLocationId;
    private final int deliveryLocationId;
    private final double orderTimeMin;
    private final String status;
    private final int assignedRiderId;

    public Order(int orderId, String customerName, String restaurant, String foodItem, double foodWeightKg,
                 int pickupLocationId, int deliveryLocationId, double orderTimeMin, String status, int assignedRiderId) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.restaurant = restaurant;
        this.foodItem = foodItem;
        this.foodWeightKg = foodWeightKg;
        this.pickupLocationId = pickupLocationId;
        this.deliveryLocationId = deliveryLocationId;
        this.orderTimeMin = orderTimeMin;
        this.status = status;
        this.assignedRiderId = assignedRiderId;
    }

    public int getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getRestaurant() { return restaurant; }
    public String getFoodItem() { return foodItem; }
    public double getFoodWeightKg() { return foodWeightKg; }
    public int getPickupLocationId() { return pickupLocationId; }
    public int getDeliveryLocationId() { return deliveryLocationId; }
    public double getOrderTimeMin() { return orderTimeMin; }
    public String getStatus() { return status; }
    public int getAssignedRiderId() { return assignedRiderId; }

    @Override
    public String toString() {
        return String.format("Order{id=%d, customer='%s', restaurant='%s', food='%s', pickup=%d, delivery=%d, status='%s', rider=%d}",
                orderId, customerName, restaurant, foodItem, pickupLocationId, deliveryLocationId, status, assignedRiderId);
    }
}
