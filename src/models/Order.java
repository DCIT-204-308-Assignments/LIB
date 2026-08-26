package models;

public class Order {
    public enum OrderState {
        CREATED, QUEUED, SCHEDULED, ASSIGNED, PICKED_UP, IN_TRANSIT, COMPLETED, CANCELLED
    }

    private final int orderId;
    private final String customerName;
    private final String restaurant;
    private final String foodItem;
    private final double foodWeightKg;
    private final int pickupLocationId;
    private final int deliveryLocationId;
    private final double orderTimeMin; // creation time in min
    
    private double requestedDeliveryTimeMin;
    private double priority;
    private String status; // CREATED, QUEUED, SCHEDULED, ASSIGNED, PICKED_UP, IN_TRANSIT, COMPLETED, CANCELLED
    private int assignedRiderId;
    private double distanceKm;
    private double estimatedDeliveryTimeMin;
    private String vehicleType;

    /**
     * The ServiceRequest this order was created from, or -1 if it has none.
     *
     * <p>Without this link the two models could only be correlated by comparing
     * pickup id, delivery id and the food-item string, which silently picks the
     * wrong order whenever two customers order the same meal along the same
     * route. Kept out of the constructors so existing callers are unaffected.</p>
     */
    private int requestId = -1;

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
        this.requestedDeliveryTimeMin = orderTimeMin + 30.0;
        this.priority = 1.0;
        this.status = status != null ? status : OrderState.CREATED.name();
        this.assignedRiderId = assignedRiderId;
        this.distanceKm = 0.0;
        this.estimatedDeliveryTimeMin = 0.0;
        this.vehicleType = "ANY";
    }

    public Order(int orderId, String customerName, String restaurant, String foodItem, double foodWeightKg,
                 int pickupLocationId, int deliveryLocationId, double orderTimeMin, double requestedDeliveryTimeMin,
                 double priority, String status, int assignedRiderId, double distanceKm,
                 double estimatedDeliveryTimeMin, String vehicleType) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.restaurant = restaurant;
        this.foodItem = foodItem;
        this.foodWeightKg = foodWeightKg;
        this.pickupLocationId = pickupLocationId;
        this.deliveryLocationId = deliveryLocationId;
        this.orderTimeMin = orderTimeMin;
        this.requestedDeliveryTimeMin = requestedDeliveryTimeMin;
        this.priority = priority;
        this.status = status != null ? status : OrderState.CREATED.name();
        this.assignedRiderId = assignedRiderId;
        this.distanceKm = distanceKm;
        this.estimatedDeliveryTimeMin = estimatedDeliveryTimeMin;
        this.vehicleType = vehicleType;
    }

    public int getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getRestaurant() { return restaurant; }
    public String getFoodItem() { return foodItem; }
    public double getFoodWeightKg() { return foodWeightKg; }
    public int getPickupLocationId() { return pickupLocationId; }
    public int getDeliveryLocationId() { return deliveryLocationId; }
    public double getOrderTimeMin() { return orderTimeMin; }
    public double getRequestedDeliveryTimeMin() { return requestedDeliveryTimeMin; }
    public void setRequestedDeliveryTimeMin(double val) { this.requestedDeliveryTimeMin = val; }
    public double getPriority() { return priority; }
    public void setPriority(double priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setStatus(OrderState state) { this.status = state.name(); }
    public int getAssignedRiderId() { return assignedRiderId; }
    public void setAssignedRiderId(int riderId) { this.assignedRiderId = riderId; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getEstimatedDeliveryTimeMin() { return estimatedDeliveryTimeMin; }
    public void setEstimatedDeliveryTimeMin(double time) { this.estimatedDeliveryTimeMin = time; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    @Override
    public String toString() {
        return String.format("Order{id=%d, customer='%s', restaurant='%s', food='%s', pickup=%d, delivery=%d, status='%s', rider=%d}",
                orderId, customerName, restaurant, foodItem, pickupLocationId, deliveryLocationId, status, assignedRiderId);
    }
}
