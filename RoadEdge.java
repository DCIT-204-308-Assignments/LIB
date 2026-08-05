/**
 * UG Swift - RoadEdge model
 * Represents a single weighted edge (road) between two campus locations.
 */
public class RoadEdge {
    private final int roadId;
    private final int fromLocationId;
    private final int toLocationId;
    private final String fromName;
    private final String toName;
    private final double distanceKm;
    private final double travelTimeMin;
    private final String trafficLevel;
    private final String roadCondition;
    private final double roadConditionWeight;
    private final boolean oneWay;
    private final double weight; // combined routing weight used by Dijkstra/Prim/Kruskal

    public RoadEdge(int roadId, int fromLocationId, int toLocationId, String fromName, String toName,
                     double distanceKm, double travelTimeMin, String trafficLevel, String roadCondition,
                     double roadConditionWeight, boolean oneWay, double weight) {
        this.roadId = roadId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.fromName = fromName;
        this.toName = toName;
        this.distanceKm = distanceKm;
        this.travelTimeMin = travelTimeMin;
        this.trafficLevel = trafficLevel;
        this.roadCondition = roadCondition;
        this.roadConditionWeight = roadConditionWeight;
        this.oneWay = oneWay;
        this.weight = weight;
    }

    public int getRoadId() { return roadId; }
    public int getFromLocationId() { return fromLocationId; }
    public int getToLocationId() { return toLocationId; }
    public String getFromName() { return fromName; }
    public String getToName() { return toName; }
    public double getDistanceKm() { return distanceKm; }
    public double getTravelTimeMin() { return travelTimeMin; }
    public String getTrafficLevel() { return trafficLevel; }
    public String getRoadCondition() { return roadCondition; }
    public double getRoadConditionWeight() { return roadConditionWeight; }
    public boolean isOneWay() { return oneWay; }
    public double getWeight() { return weight; }

    public String toCsvRow() {
        return roadId + "," + fromLocationId + "," + toLocationId + "," +
                escape(fromName) + "," + escape(toName) + "," +
                distanceKm + "," + travelTimeMin + "," + trafficLevel + "," +
                roadCondition + "," + roadConditionWeight + "," + oneWay + "," + weight;
    }

    private String escape(String s) {
        // Names contain no commas in this dataset, but guard anyway.
        return s.contains(",") ? "\"" + s + "\"" : s;
    }
}
