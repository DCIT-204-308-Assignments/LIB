package models;

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

    public double distanceTo(Location other) {
        if (other == null) return 0.0;
        return haversineDistance(this.latitude, this.longitude, other.latitude, other.longitude);
    }

    public double distanceTo(double targetLat, double targetLon) {
        return haversineDistance(this.latitude, this.longitude, targetLat, targetLon);
    }

    /**
     * Great-circle distance in kilometres, rounded to three decimal places.
     *
     * <p>Delegates to {@link utils.GeoUtils#haversineKm}. The formula used to be
     * duplicated here in an atan2 form while GeoUtils used an asin form -
     * mathematically equivalent, but two copies of one formula is two places to
     * fix if it is ever wrong.</p>
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.round(utils.GeoUtils.haversineKm(lat1, lon1, lat2, lon2) * 1000.0) / 1000.0;
    }

    @Override
    public String toString() {
        return String.format("Location{id=%d, name='%s', zone='%s'}", locationId, name, zone);
    }
}
