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
        this.maxNodeId = maxNodeId;
        this.adjMatrix = new double[maxNodeId + 1][maxNodeId + 1];
        // Initialize adjacency matrix with infinity (or 0 for self-loops)
        for (int i = 0; i <= maxNodeId; i++) {
            for (int j = 0; j <= maxNodeId; j++) {
                if (i == j) {
                    adjMatrix[i][j] = 0.0;
                } else {
                    adjMatrix[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
        this.adjList = new HashTable<>(maxNodeId * 2);
        this.locations = new HashTable<>(maxNodeId * 2);
    }

    public void addLocation(Location loc) {
        locations.put(loc.getLocationId(), loc);
        if (adjList.get(loc.getLocationId()) == null) {
            adjList.put(loc.getLocationId(), new LinkedList<>());
        }
    }

    public Location getLocation(int id) {
        return locations.get(id);
    }

    public void addRoad(RoadEdge road) {
        int u = road.getFromLocationId();
        int v = road.getToLocationId();
        double w = road.getWeight();

        // 1. Matrix representation
        adjMatrix[u][v] = w;
        if (!road.isOneWay()) {
            adjMatrix[v][u] = w;
        }

        // 2. Adjacency List representation
        if (adjList.get(u) == null) {
            adjList.put(u, new LinkedList<>());
        }
        adjList.get(u).addLast(new Edge(v, w, road.getDistanceKm(), road.getTravelTimeMin(), road.getRoadCondition(), road.getTrafficLevel()));

        if (!road.isOneWay()) {
            if (adjList.get(v) == null) {
                adjList.put(v, new LinkedList<>());
            }
            adjList.get(v).addLast(new Edge(u, w, road.getDistanceKm(), road.getTravelTimeMin(), road.getRoadCondition(), road.getTrafficLevel()));
        }
    }

    public double[][] getAdjacencyMatrix() {
        return adjMatrix;
    }

    public LinkedList<Edge> getNeighbors(int u) {
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
}
