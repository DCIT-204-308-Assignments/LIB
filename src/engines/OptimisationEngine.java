package engines;

import models.Resource;
import ds.DynamicArray;
import ds.Graph;
import models.ServiceRequest;

public class OptimisationEngine {

    /**
     * Greedy Strategy: Nearest Neighbor Dispatch Routing.
     * Starts at dispatchLocationId and visits each request destination in greedy closest-first order.
     * Computes the total distance of the greedy path.
     */
    public static DynamicArray<ServiceRequest> greedyNearestNeighbor(Graph graph, int dispatchLocId, DynamicArray<ServiceRequest> requests) {
        DynamicArray<ServiceRequest> result = new DynamicArray<>();
        if (graph == null
                || requests == null
                || requests.isEmpty()
                || dispatchLocId < 0
                || dispatchLocId > graph.getMaxNodeId()
                || graph.getLocation(dispatchLocId) == null) {
            return result;
        }
        boolean[] visited = new boolean[requests.size()];
        int currentLoc = dispatchLocId;

        for (int step = 0; step < requests.size(); step++) {
            int nearestIdx = -1;
            double nearestDist = Double.MAX_VALUE;

            for (int i = 0; i < requests.size(); i++) {
                if (!visited[i]) {
                    ServiceRequest req = requests.get(i);

                    if (req == null) {
                        visited[i] = true;
                        continue;
                    }

                    int destinationId = req.getDestLocationId();

                    if (destinationId < 0
                            || destinationId > graph.getMaxNodeId()
                            || graph.getLocation(destinationId) == null) {
                        visited[i] = true;
                        continue;
                    }

                    RouteEngine.PathResult pathRes =
                            RouteEngine.dijkstra(
                                    graph,
                                    currentLoc,
                                    destinationId
                            );

                    if (pathRes != null
                            && Double.isFinite(pathRes.totalDistanceKm)
                            && pathRes.totalDistanceKm < nearestDist) {
                        nearestDist = pathRes.totalDistanceKm;
                        nearestIdx = i;
                    }
                }
            }

            if (nearestIdx == -1) {
                break;
            }

            visited[nearestIdx] = true;
            ServiceRequest selected = requests.get(nearestIdx);
            result.add(selected);
            currentLoc = selected.getDestLocationId(); // Move to destination
        }

        return result;
    }

        /**
     * Greedy Strategy:
     * selects the fastest available rider to reach a pickup location.
     * Uses Dijkstra travel time from each rider's current location.
     */
    public static Resource greedyFastestAvailableRider(
            Graph graph,
            int pickupLocationId,
            DynamicArray<Resource> riders,
            double requiredCapacityKg
    ) {
        if (graph == null
                || riders == null
                || riders.isEmpty()
                || pickupLocationId < 0
                || pickupLocationId > graph.getMaxNodeId()
                || graph.getLocation(pickupLocationId) == null
                || !Double.isFinite(requiredCapacityKg)
                || requiredCapacityKg < 0.0) {
            return null;
        }

        Resource bestRider = null;
        double bestTime = Double.POSITIVE_INFINITY;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (Resource rider : riders) {
            if (rider == null || !rider.isAvailable()) {
                continue;
            }

            double capacity = rider.getCapacityKg();

            if (!Double.isFinite(capacity)
                    || capacity < requiredCapacityKg) {
                continue;
            }

            int riderLocationId =
                    rider.getCurrentLocationId();

            if (riderLocationId < 0
                    || riderLocationId > graph.getMaxNodeId()
                    || graph.getLocation(riderLocationId) == null) {
                continue;
            }

            RouteEngine.PathResult path =
                    RouteEngine.dijkstra(
                            graph,
                            riderLocationId,
                            pickupLocationId
                    );

            if (path == null
                    || !Double.isFinite(path.totalTimeMin)
                    || path.totalTimeMin < 0.0
                    || !Double.isFinite(path.totalDistanceKm)
                    || path.totalDistanceKm < 0.0) {
                continue;
            }

            boolean faster =
                    path.totalTimeMin < bestTime;

            boolean sameTimeCloser =
                    Double.compare(
                            path.totalTimeMin,
                            bestTime
                    ) == 0
                            && path.totalDistanceKm
                            < bestDistance;

            boolean sameTimeAndDistanceLowerId =
                    Double.compare(
                            path.totalTimeMin,
                            bestTime
                    ) == 0
                            && Double.compare(
                                    path.totalDistanceKm,
                                    bestDistance
                            ) == 0
                            && (bestRider == null
                            || rider.getResourceId()
                            < bestRider.getResourceId());

            if (faster
                    || sameTimeCloser
                    || sameTimeAndDistanceLowerId) {

                bestRider = rider;
                bestTime = path.totalTimeMin;
                bestDistance = path.totalDistanceKm;
            }
        }

        return bestRider;
    }

    /**
     * Dynamic Programming Strategy: 0/1 Knapsack Solver for optimal request batching.
     * Fits requests into a rider's weight capacity (carrying limit) to maximize total priority.
     * Capacity is specified as an integer in grams or decigrams to make DP table indexes integer.
     * We will scale the weights (in kg) to integer hectograms (e.g., 2.5 kg -> 25 hectograms).
     */
    public static DynamicArray<ServiceRequest> dpKnapsackBatching(DynamicArray<ServiceRequest> requests, double capacityKg) {
        DynamicArray<ServiceRequest> emptyResult =
                new DynamicArray<>();

        if (requests == null
                || requests.isEmpty()
                || Double.isNaN(capacityKg)
                || Double.isInfinite(capacityKg)
                || capacityKg <= 0.0) {
            return emptyResult;
        }
        int n = requests.size();
        final int MAX_CAPACITY_UNITS = 10_000;

        double scaledCapacity = capacityKg * 10.0;

        if (scaledCapacity > MAX_CAPACITY_UNITS) {
            return emptyResult;
        }

        int W = (int) Math.floor(scaledCapacity);

        if (W <= 0) {
            return emptyResult;
        }

        // Item weights in hectograms
        int[] w = new int[n];
        double[] v = new double[n]; // priority values
        for (int i = 0; i < n; i++) {
            ServiceRequest request = requests.get(i);

            if (request == null) {
                w[i] = W + 1;
                v[i] = 0.0;
                continue;
            }

            double weight =
                    getWeightByCategory(request.getCategory());

            w[i] = (int) Math.max(
                    1,
                    Math.round(weight * 10.0)
            );

            double priority = request.getPriority();

            v[i] = Double.isFinite(priority)
                    ? priority
                    : 0.0;
        }

        // DP Table
        double[][] dp = new double[n + 1][W + 1];

        // Fill Table
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= W; j++) {
                if (w[i - 1] <= j) {
                    dp[i][j] = Math.max(v[i - 1] + dp[i - 1][j - w[i - 1]], dp[i - 1][j]);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }

        // Reconstruct selected items
        DynamicArray<ServiceRequest> selectedRequests = new DynamicArray<>();
        int resW = W;
        for (int i = n; i > 0; i--) {
            // If the value came from the item being included
            if (dp[i][resW] != dp[i - 1][resW]) {
                selectedRequests.add(0, requests.get(i - 1));
                resW -= w[i - 1];
            }
        }

        return selectedRequests;
    }

    private static double getWeightByCategory(String category) {
        if (category == null) return 1.0;
        String cat = category.toLowerCase();
        if (cat.contains("waakye") || cat.contains("jollof") || cat.contains("fufu") || cat.contains("red")) {
            return 1.2; // typical food pack weight
        } else if (cat.contains("pizza")) {
            return 0.8;
        } else if (cat.contains("groceries")) {
            return 5.0;
        } else if (cat.contains("pharmacy") || cat.contains("documents")) {
            return 0.3;
        } else {
            return 2.0; // default package weight
        }
    }
}
