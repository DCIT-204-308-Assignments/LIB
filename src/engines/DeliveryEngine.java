package engines;

import ds.DynamicArray;
import ds.Graph;
import ds.MinHeap;
import models.Location;
import models.Order;
import models.Resource;
import models.RoadEdge;
import utils.Config;

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

    private static class RiderCandidate {
        final Resource rider;
        final double distanceKm;
        final double travelTimeMin;
        final double score;

        RiderCandidate(Resource rider, double distanceKm, double travelTimeMin, double score) {
            this.rider = rider;
            this.distanceKm = distanceKm;
            this.travelTimeMin = travelTimeMin;
            this.score = score;
        }
    }


    public static AssignmentResult assignRider(
        Order order,
        DynamicArray<Resource> riders,
        DynamicArray<Location> locations,
        DynamicArray<RoadEdge> roads) {

    if (order == null || riders == null || locations == null || roads == null) {
        return null;
    }

    if (riders.isEmpty() || locations.isEmpty()) {
        return null;
    }

    double orderWeight = order.getFoodWeightKg();

    if (orderWeight <= 0
            || Double.isNaN(orderWeight)
            || Double.isInfinite(orderWeight)) {
        return null;
    }

    Location pickup = findLocation(locations, order.getPickupLocationId());

    if (pickup == null) {
        return null;
    }

    Graph graph = buildGraph(locations, roads);

    // The 6km bicycle cutoff must be judged against the actual delivery leg
    // (pickup -> delivery), not the rider's distance to the pickup point -
    // a rider stationed right next to the pickup could still face an 8km+
    // drop-off. Computed once per order since it does not depend on the rider.
    Location delivery = findLocation(locations, order.getDeliveryLocationId());
    double deliveryDistanceKm = 0.0;
    if (delivery != null) {
        RouteEngine.PathResult deliveryPath = RouteEngine.dijkstraCached(
                graph,
                pickup.getLocationId(),
                delivery.getLocationId()
        );
        if (deliveryPath != null && !Double.isNaN(deliveryPath.totalDistanceKm)
                && !Double.isInfinite(deliveryPath.totalDistanceKm)) {
            deliveryDistanceKm = deliveryPath.totalDistanceKm;
        }
    }

    MinHeap<RiderCandidate> candidateHeap = new MinHeap<>(
        Math.max(1, riders.size()),
        (a, b) -> {
            int byScore = Double.compare(a.score, b.score);
            if (byScore != 0) {
                return byScore;
            }

            int byDistance = Double.compare(a.distanceKm, b.distanceKm);
            if (byDistance != 0) {
                return byDistance;
            }

            return Integer.compare(
                    a.rider.getResourceId(),
                    b.rider.getResourceId()
            );
        }
    );

    for (Resource rider : riders) {

        if (rider == null) {
            continue;
        }
        
        if (!rider.isAvailable()) {
            continue;
        }

        double capacity = rider.getCapacityKg();

        if (capacity <= 0
                || Double.isNaN(capacity)
                || Double.isInfinite(capacity)
                || capacity < orderWeight) {
            continue;
        }

        int riderLocationId = rider.getCurrentLocationId();

        Location riderLocation = findLocation(locations, riderLocationId);

        if (riderLocation == null) {
            continue;
        }

        RouteEngine.PathResult path = RouteEngine.dijkstra(
                graph,
                riderLocationId,
                pickup.getLocationId()
        );

        if (path == null
                || Double.isNaN(path.totalDistanceKm)
                || Double.isInfinite(path.totalDistanceKm)
                || Double.isNaN(path.totalTimeMin)
                || Double.isInfinite(path.totalTimeMin)) {
            continue;
        }
        if (Resource.BICYCLE.equalsIgnoreCase(rider.getType())
                && deliveryDistanceKm > Config.MAX_BICYCLE_DISTANCE_KM) {
            // Bicycle riders are not eligible for deliveries beyond the configured range.
            continue;
        }


        double score = scoreRider(
                rider,
                order,
                path.totalDistanceKm,
                path.totalTimeMin
        );

        if (Double.isNaN(score) || Double.isInfinite(score)) {
            continue;
        }

        candidateHeap.insert(
                new RiderCandidate(
                        rider,
                        path.totalDistanceKm,
                        path.totalTimeMin,
                        score
                )
        );
    }

    if (candidateHeap.isEmpty()) {
        return null;
    }

    RiderCandidate best = candidateHeap.extractMin();

    return new AssignmentResult(
            best.rider,
            best.distanceKm,
            best.travelTimeMin
    );
}

    

    public static double estimateDeliveryDuration(Order order, double distanceKm, Resource rider) {
        double baseMinutes = Math.max(8.0, distanceKm * 6.0);
        if ("MOTORBIKE".equalsIgnoreCase(rider.getType())
            || "MOTORCYCLE".equalsIgnoreCase(rider.getType())) {
            return Math.round((baseMinutes * 0.7) * 10.0) / 10.0;
        }
        return Math.round((baseMinutes * 1.05) * 10.0) / 10.0;
    }

    private static double scoreRider(Resource rider, Order order, double distanceKm, double travelTimeMin) {
        double score = distanceKm;
        boolean longTrip = distanceKm >= 2.0;

        if ("MOTORBIKE".equalsIgnoreCase(rider.getType())
            || "MOTORCYCLE".equalsIgnoreCase(rider.getType())) {
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
        score += rider.getCompletedDeliveries() * Config.WORKLOAD_WEIGHT;
        return score;
    }

    private static Graph buildGraph(DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
        int maxId = 0;

        for (Location loc : locations) {
            if (loc != null && loc.getLocationId() >= 0) {
                maxId = Math.max(maxId, loc.getLocationId());
            }
        }

        Graph graph = new Graph(maxId);

        for (Location loc : locations) {
            if (loc != null && loc.getLocationId() >= 0) {
                graph.addLocation(loc);
            }
        }

        for (RoadEdge road : roads) {
            if (road == null) {
                continue;
            }

            int from = road.getFromLocationId();
            int to = road.getToLocationId();

            if (from < 0 || to < 0 || from > maxId || to > maxId) {
                continue;
            }

            if (graph.getLocation(from) == null || graph.getLocation(to) == null) {
                continue;
            }

            graph.addRoad(road);
        }

        return graph;
    }

    private static Location findLocation(DynamicArray<Location> locations, int id) {
        if (locations == null || id < 0) {
            return null;
        }

        for (Location loc : locations) {
            if (loc != null && loc.getLocationId() == id) {
                return loc;
            }
        }

        return null;
    }


    public static AssignmentResult cancelAndReassign(
            Order order,
            Resource currentRider,
            DynamicArray<Resource> riders,
            DynamicArray<Location> locations,
            DynamicArray<RoadEdge> roads) {

        if (order == null) {
            return null;
        }

        if (currentRider != null) {
            currentRider.setCurrentOrderId(-1);
            currentRider.setAvailabilityStatus(Resource.STATUS_AVAILABLE);
        }

        order.setAssignedRiderId(-1);
        order.setStatus(Order.OrderState.QUEUED);

        AssignmentResult result = assignRider(order, riders, locations, roads);

        if (result != null && result.rider != null) {
            order.setAssignedRiderId(result.rider.getResourceId());
            order.setStatus(Order.OrderState.ASSIGNED);
            result.rider.setCurrentOrderId(order.getOrderId());
            result.rider.setAvailabilityStatus(Resource.STATUS_BUSY);
        } else {
            order.setStatus(Order.OrderState.CANCELLED);
        }

        return result;
    }


}
    