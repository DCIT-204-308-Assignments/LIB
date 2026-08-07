package engines;

import ds.DynamicArray;
import ds.Graph;
import models.Location;
import models.Order;
import models.Resource;
import models.RoadEdge;

public class DeliveryEngine {

    public static class AssignmentResult {
        public final Resource rider;
        public final double distanceKm;
        public final double estimatedTimeMin;

        public AssignmentResult(Resource rider, double distanceKm, double estimatedTimeMin) {
            this.rider = rider;
            this.distanceKm = distanceKm;
            this.estimatedTimeMin = estimatedTimeMin;
        }
    }

    public static AssignmentResult assignRider(Order order, DynamicArray<Resource> riders, DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
        if (riders == null || riders.isEmpty()) {
            return null;
        }

        Graph graph = buildGraph(locations, roads);
        Resource best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        double bestDistance = Double.POSITIVE_INFINITY;
        double bestTime = Double.POSITIVE_INFINITY;

        for (Resource rider : riders) {
            if (!"AVAILABLE".equalsIgnoreCase(rider.getAvailabilityStatus())) {
                continue;
            }
            if (rider.getCapacityKg() < order.getFoodWeightKg()) {
                continue;
            }

            Location riderLoc = findLocation(locations, rider.getHomeLocationId());
            Location pickup = findLocation(locations, order.getPickupLocationId());
            if (riderLoc == null || pickup == null) {
                continue;
            }

            RouteEngine.PathResult path = RouteEngine.dijkstra(graph, riderLoc.getLocationId(), pickup.getLocationId());
            if (path == null) {
                continue;
            }

            double score = scoreRider(rider, order, path.totalDistanceKm, path.totalTimeMin);
            if (score < bestScore) {
                bestScore = score;
                bestDistance = path.totalDistanceKm;
                bestTime = path.totalTimeMin;
                best = rider;
            }
        }

        if (best == null) {
            return null;
        }

        return new AssignmentResult(best, bestDistance, bestTime);
    }

    public static double estimateDeliveryDuration(Order order, double distanceKm, Resource rider) {
        double baseMinutes = Math.max(8.0, distanceKm * 6.0);
        if ("MOTORBIKE".equalsIgnoreCase(rider.getType())) {
            return Math.round((baseMinutes * 0.7) * 10.0) / 10.0;
        }
        return Math.round((baseMinutes * 1.05) * 10.0) / 10.0;
    }

    private static double scoreRider(Resource rider, Order order, double distanceKm, double travelTimeMin) {
        double score = distanceKm;
        boolean longTrip = distanceKm >= 2.0;

        if ("MOTORBIKE".equalsIgnoreCase(rider.getType())) {
            score -= longTrip ? 0.35 : 0.1;
        } else if ("BICYCLE".equalsIgnoreCase(rider.getType())) {
            score += longTrip ? 1.4 : 0.25;
        }

        if (order.getFoodWeightKg() > 1.4 && "BICYCLE".equalsIgnoreCase(rider.getType())) {
            score += 1.2;
        }

        if (order.getFoodWeightKg() > 1.8) {
            score += 0.25;
        }

        score += travelTimeMin / 20.0;
        return score;
    }

    private static Graph buildGraph(DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
        int maxId = 0;
        for (Location loc : locations) {
            maxId = Math.max(maxId, loc.getLocationId());
        }
        Graph graph = new Graph(maxId);
        for (Location loc : locations) {
            graph.addLocation(loc);
        }
        for (RoadEdge road : roads) {
            graph.addRoad(road);
        }
        return graph;
    }

    private static Location findLocation(DynamicArray<Location> locations, int id) {
        for (Location loc : locations) {
            if (loc.getLocationId() == id) {
                return loc;
            }
        }
        return null;
    }
}
