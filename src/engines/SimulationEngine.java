package engines;

import ds.DynamicArray;
import ds.Graph;
import models.Location;
import models.Order;
import models.RoadEdge;
import models.Resource;

import java.util.Random;

/**
 * Simulates a batch of campus delivery orders against the existing
 * DeliveryEngine / RouteEngine so the system's behaviour can be observed
 * (and measured, via MetricsEngine) without needing the Swing UI.
 */
public class SimulationEngine {

    public static class SimulationResult {
        public int ordersGenerated;
        public int ordersCompleted;
        public int ordersUnassigned;
        public final DynamicArray<Order> completedOrders = new DynamicArray<>();
        public final DynamicArray<Order> unassignedOrders = new DynamicArray<>();
    }

    /**
     * Generates {@code numOrders} random pickup/delivery pairs across the
     * given campus graph and runs each one through DeliveryEngine's real
     * assignment logic.
     *
     * @param randomSeed fixed seed for reproducible simulation runs
     */
    public static SimulationResult run(
            int numOrders,
            DynamicArray<Resource> riders,
            DynamicArray<Location> locations,
            DynamicArray<RoadEdge> roads,
            long randomSeed) {

        SimulationResult result = new SimulationResult();

        if (numOrders <= 0
                || riders == null || riders.isEmpty()
                || locations == null || locations.size() < 2
                || roads == null) {
            return result;
        }

        Random rng = new Random(randomSeed);
        Graph graph = buildGraph(locations, roads);

        for (int i = 0; i < numOrders; i++) {
            Location pickup = randomLocation(locations, rng);
            Location delivery = randomLocation(locations, rng);
            int attempts = 0;
            while (delivery.getLocationId() == pickup.getLocationId() && attempts < 10) {
                delivery = randomLocation(locations, rng);
                attempts++;
            }

            double weight = 0.5 + rng.nextDouble() * 2.5; // 0.5 - 3.0 kg
            Order order = new Order(
                    10_000 + i,
                    "SimCustomer" + i,
                    "SimVendor" + (i % 5),
                    "SimItem",
                    weight,
                    pickup.getLocationId(),
                    delivery.getLocationId(),
                    i * 2.0, // staggered creation time, minutes
                    Order.OrderState.CREATED.name(),
                    -1
            );

            result.ordersGenerated++;

            DeliveryEngine.AssignmentResult assignment =
                    DeliveryEngine.assignRider(order, riders, locations, roads);

            if (assignment == null || assignment.rider == null) {
                order.setStatus(Order.OrderState.QUEUED);
                result.ordersUnassigned++;
                result.unassignedOrders.add(order);
                continue;
            }

            Resource rider = assignment.rider;
            rider.assignOrder(order.getOrderId());

            RouteEngine.PathResult deliveryLeg = RouteEngine.dijkstra(
                    graph, order.getPickupLocationId(), order.getDeliveryLocationId());

            double deliveryDistanceKm = deliveryLeg != null ? deliveryLeg.totalDistanceKm : 0.0;
            double deliveryTimeMin = DeliveryEngine.estimateDeliveryDuration(order, assignment.distanceKm, rider)
                    + (deliveryLeg != null ? deliveryLeg.totalTimeMin : 0.0);

            order.setDistanceKm(assignment.distanceKm + deliveryDistanceKm);
            order.setEstimatedDeliveryTimeMin(deliveryTimeMin);
            order.setVehicleType(rider.getType());
            order.setAssignedRiderId(rider.getResourceId());
            order.setStatus(Order.OrderState.COMPLETED);

            rider.completeOrder(order.getDeliveryLocationId());

            result.ordersCompleted++;
            result.completedOrders.add(order);
        }

        return result;
    }

    private static Location randomLocation(DynamicArray<Location> locations, Random rng) {
        return locations.get(rng.nextInt(locations.size()));
    }

    private static Graph buildGraph(DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
        int maxId = 0;
        for (Location loc : locations) {
            if (loc != null) {
                maxId = Math.max(maxId, loc.getLocationId());
            }
        }

        Graph graph = new Graph(maxId);

        for (Location loc : locations) {
            if (loc != null) {
                graph.addLocation(loc);
            }
        }

        for (RoadEdge road : roads) {
            if (road != null) {
                graph.addRoad(road);
            }
        }

        return graph;
    }
}
