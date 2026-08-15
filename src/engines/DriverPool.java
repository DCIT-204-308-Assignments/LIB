package engines;

import ds.CircularQueue;
import ds.DynamicArray;
import models.Resource;
import models.Location;
import models.RoadEdge;

public class DriverPool {
    private final CircularQueue<Resource> pool = new CircularQueue<>(64);

    public void rebuild(DynamicArray<Resource> riders) {
        // clear and enqueue available riders
        while (!pool.isEmpty()) pool.dequeue();
        for (Resource r : riders) {
            if ("AVAILABLE".equalsIgnoreCase(r.getAvailabilityStatus())) {
                pool.enqueue(r);
            }
        }
    }

    public Resource nextSuitable(models.Order order, DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
        int attempts = pool.size();
        for (int i = 0; i < attempts; i++) {
            Resource r = pool.dequeue();
            pool.enqueue(r); // rotate
            if (!"AVAILABLE".equalsIgnoreCase(r.getAvailabilityStatus())) continue;
            if (r.getCapacityKg() < order.getFoodWeightKg()) continue;
            // try route from rider home to pickup
            int riderLocId = r.getHomeLocationId();
            int pickupId = order.getPickupLocationId();
            // build a temporary graph for route computation
            ds.Graph graph = new ds.Graph(Math.max(1, locations.size()));
            for (Location loc : locations) graph.addLocation(loc);
            for (RoadEdge road : roads) graph.addRoad(road);
            RouteEngine.PathResult path = RouteEngine.dijkstra(graph, riderLocId, pickupId);
            if (path == null) continue;
            return r;
        }
        return null;
    }

    public void markBusy(int riderId) {
        // rotate pool until rider found and remove it temporarily by marking not available
        for (int i = 0; i < pool.size(); i++) {
            Resource r = pool.dequeue();
            if (r.getResourceId() == riderId) {
                r.setAvailabilityStatus("BUSY");
                pool.enqueue(r);
                return;
            }
            pool.enqueue(r);
        }
    }

    public void markAvailable(int riderId) {
        for (int i = 0; i < pool.size(); i++) {
            Resource r = pool.dequeue();
            if (r.getResourceId() == riderId) {
                r.setAvailabilityStatus("AVAILABLE");
                pool.enqueue(r);
                return;
            }
            pool.enqueue(r);
        }
    }
}
