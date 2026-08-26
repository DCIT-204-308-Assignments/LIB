package ds;

import models.Location;
import models.RoadEdge;

public class Graph {
    public static class Edge {
        public int to;
        public double weight;
        public double distanceKm;
        public double travelTimeMin;
        public String roadCondition;
        public String trafficLevel;

        public Edge(int to, double weight, double distanceKm, double travelTimeMin, String roadCondition, String trafficLevel) {
            this.to = to;
            this.weight = weight;
            this.distanceKm = distanceKm;
            this.travelTimeMin = travelTimeMin;
            this.roadCondition = roadCondition;
            this.trafficLevel = trafficLevel;
        }
    }

    private final int maxNodeId;
    private final double[][] adjMatrix;
    private final HashTable<Integer, LinkedList<Edge>> adjList;
    private final HashTable<Integer, Location> locations;

    public Graph(int maxNodeId) {
        if (maxNodeId < 0) {
            throw new IllegalArgumentException(
                    "maxNodeId cannot be negative"
            );
        }

        this.maxNodeId = maxNodeId;
        this.adjMatrix =
                new double[maxNodeId + 1][maxNodeId + 1];

        for (int i = 0; i <= maxNodeId; i++) {
            for (int j = 0; j <= maxNodeId; j++) {
                adjMatrix[i][j] =
                        (i == j)
                                ? 0.0
                                : Double.POSITIVE_INFINITY;
            }
        }

        int tableCapacity =
                Math.max(3, (maxNodeId + 1) * 2);

        this.adjList = new HashTable<>(tableCapacity);
        this.locations = new HashTable<>(tableCapacity);
    }

    public void addLocation(Location loc) {
        if (loc == null) {
            return;
        }

        int id = loc.getLocationId();

        if (!isValidNodeId(id)) {
            return;
        }

        locations.put(id, loc);

        if (adjList.get(id) == null) {
            adjList.put(id, new LinkedList<>());
        }
    }

    public Location getLocation(int id) {
        if (!isValidNodeId(id)) {
            return null;
        }

        return locations.get(id);
    }

    public void addRoad(RoadEdge road) {
        if (road == null) {
            return;
        }

        int u = road.getFromLocationId();
        int v = road.getToLocationId();

        if (!isValidNodeId(u) || !isValidNodeId(v)) {
            return;
        }

        if (locations.get(u) == null
                || locations.get(v) == null) {
            return;
        }

        double weight = road.getWeight();
        double distanceKm = road.getDistanceKm();
        double travelTimeMin = road.getTravelTimeMin();

        if (!isValidNonNegativeNumber(weight)
                || !isValidNonNegativeNumber(distanceKm)
                || !isValidNonNegativeNumber(travelTimeMin)) {
            return;
        }

        adjMatrix[u][v] = weight;

        if (!road.isOneWay()) {
            adjMatrix[v][u] = weight;
        }

        LinkedList<Edge> fromNeighbors = adjList.get(u);

        if (fromNeighbors == null) {
            fromNeighbors = new LinkedList<>();
            adjList.put(u, fromNeighbors);
        }

        fromNeighbors.addLast(
                new Edge(
                        v,
                        weight,
                        distanceKm,
                        travelTimeMin,
                        road.getRoadCondition(),
                        road.getTrafficLevel()
                )
        );

        if (!road.isOneWay()) {
            LinkedList<Edge> toNeighbors = adjList.get(v);

            if (toNeighbors == null) {
                toNeighbors = new LinkedList<>();
                adjList.put(v, toNeighbors);
            }

            toNeighbors.addLast(
                    new Edge(
                            u,
                            weight,
                            distanceKm,
                            travelTimeMin,
                            road.getRoadCondition(),
                            road.getTrafficLevel()
                    )
            );
        }
    }

    public double[][] getAdjacencyMatrix() {
        return adjMatrix;
    }

    public LinkedList<Edge> getNeighbors(int u) {
        if (!isValidNodeId(u)) {
            return null;
        }

        return adjList.get(u);
    }

    public int getMaxNodeId() {
        return maxNodeId;
    }

    public DynamicArray<Location> getAllLocations() {
        DynamicArray<Location> result = new DynamicArray<>();
        for (HashTable.Entry<Integer, Location> entry : locations.entries()) {
            result.add(entry.value);
        }
        return result;
    }

    private boolean isValidNodeId(int id) {
        return id >= 0 && id <= maxNodeId;
    }

    private boolean isValidNonNegativeNumber(double value) {
        return !Double.isNaN(value)
                && !Double.isInfinite(value)
                && value >= 0.0;
    }

}
