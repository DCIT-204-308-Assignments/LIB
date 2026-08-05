/**
 * UG Swift - Location model
 * Represents a single vertex in the University of Ghana campus graph.
 */
public class Location {
    private final int locationId;
    private final String name;
    private final String zone;
    private final String type;
    private final double latitude;
    private final double longitude;

    public Location(int locationId, String name, String zone, String type,
                     double latitude, double longitude) {
        this.locationId = locationId;
        this.name = name;
        this.zone = zone;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getLocationId() { return locationId; }
    public String getName() { return name; }
    public String getZone() { return zone; }
    public String getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() {
        return String.format("Location{id=%d, name='%s', zone='%s'}", locationId, name, zone);
    }
}
