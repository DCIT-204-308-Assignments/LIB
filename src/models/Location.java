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

    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's mean radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((R * c) * 1000.0) / 1000.0; // rounded to 3 decimal places
    }

    @Override
    public String toString() {
        return String.format("Location{id=%d, name='%s', zone='%s'}", locationId, name, zone);
    }
}
