package models;

public class Resource {
    private final int resourceId;
    private final String name;
    private final String type; // BICYCLE, MOTORBIKE
    private final int homeLocationId;
    private final double capacityKg; // carrying capacity
    private String availabilityStatus; // AVAILABLE, BUSY

    public Resource(int resourceId, String name, String type, int homeLocationId,
                    double capacityKg, String availabilityStatus) {
        this.resourceId = resourceId;
        this.name = name;
        this.type = type;
        this.homeLocationId = homeLocationId;
        this.capacityKg = capacityKg;
        this.availabilityStatus = availabilityStatus;
    }

    public int getResourceId() { return resourceId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getHomeLocationId() { return homeLocationId; }
    public double getCapacityKg() { return capacityKg; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String status) { this.availabilityStatus = status; }

    @Override
    public String toString() {
        return String.format("Rider{id=%d, name='%s', type='%s', home=%d, cap=%.1fkg, status='%s'}",
                resourceId, name, type, homeLocationId, capacityKg, availabilityStatus);
    }
}
