package models;

public class ServiceRequest {
    private final int requestId;
    private final int sourceLocationId;
    private final int destLocationId;
    private final String category;
    private final int urgency; // 1 (low) to 5 (high)
    private final double timeSubmittedMin; // minutes from start of day
    private final double deadlineMin; // minutes from start of day
    private String status; // PENDING, ASSIGNED, DELIVERED
    private int assignedRiderId;
    private final double priority; // calculated priority weight
    private double deliveredTimeMin; // when delivered, minutes from start of day (-1 if not delivered)

    public ServiceRequest(int requestId, int sourceLocationId, int destLocationId,
                          String category, int urgency, double timeSubmittedMin,
                          double deadlineMin, String status, int assignedRiderId) {
        this.requestId = requestId;
        this.sourceLocationId = sourceLocationId;
        this.destLocationId = destLocationId;
        this.category = category;
        this.urgency = urgency;
        this.timeSubmittedMin = timeSubmittedMin;
        this.deadlineMin = deadlineMin;
        this.status = status;
        this.assignedRiderId = assignedRiderId;
        this.deliveredTimeMin = -1.0;
        // Calculated priority using student index number parts (22237205)
        // Formula: urgency * 2.22 + (1440 - deadlineMin) * 0.05
        this.priority = Math.round((urgency * 2.22 + (1440.0 - deadlineMin) * 0.05) * 100.0) / 100.0;
    }

    public ServiceRequest(int requestId, int sourceLocationId, int destLocationId,
                          String category, int urgency, double timeSubmittedMin,
                          double deadlineMin, String status, int assignedRiderId, double deliveredTimeMin) {
        this.requestId = requestId;
        this.sourceLocationId = sourceLocationId;
        this.destLocationId = destLocationId;
        this.category = category;
        this.urgency = urgency;
        this.timeSubmittedMin = timeSubmittedMin;
        this.deadlineMin = deadlineMin;
        this.status = status;
        this.assignedRiderId = assignedRiderId;
        this.deliveredTimeMin = deliveredTimeMin;
        this.priority = Math.round((urgency * 2.22 + (1440.0 - deadlineMin) * 0.05) * 100.0) / 100.0;
    }

    public int getRequestId() { return requestId; }
    public int getSourceLocationId() { return sourceLocationId; }
    public int getDestLocationId() { return destLocationId; }
    public String getCategory() { return category; }
    public int getUrgency() { return urgency; }
    public double getTimeSubmittedMin() { return timeSubmittedMin; }
    public double getDeadlineMin() { return deadlineMin; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAssignedRiderId() { return assignedRiderId; }
    public void setAssignedRiderId(int assignedRiderId) { this.assignedRiderId = assignedRiderId; }
    public double getPriority() { return priority; }
    public double getDeliveredTimeMin() { return deliveredTimeMin; }
    public void setDeliveredTimeMin(double t) { this.deliveredTimeMin = t; }

    public Order toOrder(int newOrderId) {
        return new Order(
            newOrderId,
            "Customer_" + requestId,
            "Campus Merchant",
            category != null ? category : "Package",
            1.5,
            sourceLocationId,
            destLocationId,
            timeSubmittedMin,
            deadlineMin,
            priority,
            Order.OrderState.CREATED.name(),
            assignedRiderId,
            0.0,
            0.0,
            "ANY"
        );
    }

    @Override
    public String toString() {
        return String.format("Request{id=%d, %s, urg=%d, priority=%.2f, status='%s'}",
                requestId, category, urgency, priority, status);
    }
}
