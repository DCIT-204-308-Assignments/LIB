package engines;

import ds.CircularQueue;
import ds.DynamicArray;
import ds.HashTable;
import models.Resource;
import models.Location;
import models.RoadEdge;

public class DriverPool {
    // Source of truth for every rider, keyed by resourceId -> O(1) lookup.
    private HashTable<Integer, Resource> allRiders = new HashTable<>();
    // Round-robin rotation pool used by nextSuitable(); riders sit here whether
    // AVAILABLE or BUSY - nextSuitable() skips busy ones while rotating.
    private final CircularQueue<Resource> pool = new CircularQueue<>(64);

    public void rebuild(DynamicArray<Resource> riders) {
        allRiders = new HashTable<>();
        while (!pool.isEmpty()) pool.dequeue();
        for (Resource r : riders) {
            allRiders.put(r.getResourceId(), r);
            if (r.isAvailable()) {
                pool.enqueue(r);
            }
        }
    }

    /**
     * Adds a single rider to the pool without rebuilding everything.
     */
    public void addDriver(Resource r) {
        allRiders.put(r.getResourceId(), r);
        if (r.isAvailable()) {
            pool.enqueue(r);
        }
    }

    /**
     * Removes a rider entirely (e.g. they leave the platform). Returns the
     * removed Resource, or null if no rider with that id existed.
     */
    public Resource removeDriver(int riderId) {
        Resource removed = allRiders.remove(riderId);
        if (removed != null) {
            int count = pool.size();
            for (int i = 0; i < count; i++) {
                Resource r = pool.dequeue();
                if (r.getResourceId() != riderId) {
                    pool.enqueue(r);
                }
            }
        }
        return removed;
    }

    /**
     * O(1) lookup by rider id via the HashTable, instead of scanning the pool.
     */
    public Resource findDriver(int riderId) {
        return allRiders.get(riderId);
    }

    /**
     * Updates where a rider actually is right now.
     */
    public boolean updateDriverLocation(int riderId, int newLocationId) {
        Resource r = allRiders.get(riderId);
        if (r == null) return false;
        r.setCurrentLocationId(newLocationId);
        return true;
    }

    public boolean setDriverAvailable(int riderId) {
        Resource r = allRiders.get(riderId);
        if (r == null) return false;
        boolean wasAlreadyAvailable = r.isAvailable();
        r.setAvailabilityStatus(Resource.STATUS_AVAILABLE);
        if (!wasAlreadyAvailable) {
            // Rider wasn't in the rotation pool (started BUSY/OFFLINE at
            // rebuild time, or was excluded elsewhere) - add them now.
            pool.enqueue(r);
        }
        return true;
    }

    public boolean setDriverBusy(int riderId) {
        Resource r = allRiders.get(riderId);
        if (r == null) return false;
        r.setAvailabilityStatus(Resource.STATUS_BUSY);
        return true;
    }

    // Kept for backward compatibility with any existing callers.
    public void markBusy(int riderId) { setDriverBusy(riderId); }
    public void markAvailable(int riderId) { setDriverAvailable(riderId); }

    public DynamicArray<Resource> getAvailableDrivers() {
        DynamicArray<Resource> result = new DynamicArray<>();
        for (HashTable.Entry<Integer, Resource> entry : allRiders.entries()) {
            if (entry.value.isAvailable()) {
                result.add(entry.value);
            }
        }
        return result;
    }

    public DynamicArray<Resource> getDriversByVehicleType(String type) {
        DynamicArray<Resource> result = new DynamicArray<>();
        for (HashTable.Entry<Integer, Resource> entry : allRiders.entries()) {
            if (type.equalsIgnoreCase(entry.value.getType())) {
                result.add(entry.value);
            }
        }
        return result;
    }

    /**
     * Finds the AVAILABLE rider whose current location is closest (by actual
     * road-network distance) to the given location.
     */
    public Resource findNearestDriver(int locationId, DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
        ds.Graph graph = new ds.Graph(Math.max(1, locations.size()));
        for (Location loc : locations) graph.addLocation(loc);
        for (RoadEdge road : roads) graph.addRoad(road);

        Resource nearest = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (HashTable.Entry<Integer, Resource> entry : allRiders.entries()) {
            Resource r = entry.value;
            if (!r.isAvailable()) continue;

            RouteEngine.PathResult path = RouteEngine.dijkstraCached(graph, r.getCurrentLocationId(), locationId);
            if (path == null) continue;

            if (path.totalDistanceKm < bestDistance) {
                bestDistance = path.totalDistanceKm;
                nearest = r;
            }
        }
        return nearest;
    }

    public Resource nextSuitable(models.Order order, DynamicArray<Location> locations, DynamicArray<RoadEdge> roads) {
        int attempts = pool.size();
        for (int i = 0; i < attempts; i++) {
            Resource r = pool.dequeue();
            pool.enqueue(r); // rotate
            if (!r.isAvailable()) continue;
            if (r.getCapacityKg() < order.getFoodWeightKg()) continue;
            // Route from the rider's ACTUAL current location, not their home
            // (bug fix: previously used getHomeLocationId(), ignoring moves).
            int riderLocId = r.getCurrentLocationId();
            int pickupId = order.getPickupLocationId();
            ds.Graph graph = new ds.Graph(Math.max(1, locations.size()));
            for (Location loc : locations) graph.addLocation(loc);
            for (RoadEdge road : roads) graph.addRoad(road);
            RouteEngine.PathResult path = RouteEngine.dijkstraCached(graph, riderLocId, pickupId);
            if (path == null) continue;
            return r;
        }
        return null;
    }
}