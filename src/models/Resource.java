package models;

public class Resource {
    public static final String BICYCLE = "BICYCLE";
    public static final String MOTORCYCLE = "MOTORCYCLE";
    public static final String MOTORBIKE = "MOTORBIKE";

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_BUSY = "BUSY";
    public static final String STATUS_OFFLINE = "OFFLINE";

    private final int resourceId;
    private final String name;
    private final String type; // BICYCLE, MOTORCYCLE / MOTORBIKE
    private final int homeLocationId;
    private int currentLocationId;
    private final double capacityKg;
    private String availabilityStatus; // AVAILABLE, BUSY, OFFLINE
    private int currentOrderId;
    private int completedDeliveries;

    public Resource(int resourceId, String name, String type, int homeLocationId,
                    double capacityKg, String availabilityStatus) {
        this.resourceId = resourceId;
        this.name = name;
        this.type = normalizeType(type);
        this.homeLocationId = homeLocationId;
        this.currentLocationId = homeLocationId;
        this.capacityKg = capacityKg;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : STATUS_AVAILABLE;
        this.currentOrderId = -1;
        this.completedDeliveries = 0;
    }

    public Resource(int resourceId, String name, String type, int homeLocationId, int currentLocationId,
                    double capacityKg, String availabilityStatus, int currentOrderId, int completedDeliveries) {
        this.resourceId = resourceId;
        this.name = name;
        this.type = normalizeType(type);
        this.homeLocationId = homeLocationId;
        this.currentLocationId = currentLocationId;
        this.capacityKg = capacityKg;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : STATUS_AVAILABLE;
        this.currentOrderId = currentOrderId;
        this.completedDeliveries = completedDeliveries;
    }

    private static String normalizeType(String type) {
        if (type == null) return MOTORCYCLE;
        String t = type.toUpperCase().trim();
        if (t.contains("BIKE") || t.contains("CYCLE")) {
            if (t.contains("MOTOR") || t.contains("MOTO")) return MOTORCYCLE;
            return BICYCLE;
        }
        return t;
    }

    public int getResourceId() { return resourceId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getVehicleType() { return type; }
    public int getHomeLocationId() { return homeLocationId; }
    public int getCurrentLocationId() { return currentLocationId; }
    public void setCurrentLocationId(int locationId) { this.currentLocationId = locationId; }
    public double getCapacityKg() { return capacityKg; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String status) { this.availabilityStatus = status; }
    public boolean isAvailable() { return STATUS_AVAILABLE.equalsIgnoreCase(availabilityStatus); }

    public int getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(int orderId) { this.currentOrderId = orderId; }
    public int getCompletedDeliveries() { return completedDeliveries; }
    public void incrementCompletedDeliveries() { this.completedDeliveries++; }

    public void assignOrder(int orderId) {
        this.currentOrderId = orderId;
        this.availabilityStatus = STATUS_BUSY;
    }

    public void completeOrder(int newLocationId) {
        this.currentOrderId = -1;
        this.currentLocationId = newLocationId;
        this.availabilityStatus = STATUS_AVAILABLE;
        this.completedDeliveries++;
    }

    @Override
    public String toString() {
        return String.format("Rider{id=%d, name='%s', type='%s', currentLoc=%d, cap=%.1fkg, status='%s', completed=%d}",
                resourceId, name, type, currentLocationId, capacityKg, availabilityStatus, completedDeliveries);
    }
}
