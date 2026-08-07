package engines;

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
        boolean[] visited = new boolean[requests.size()];
        int currentLoc = dispatchLocId;

        for (int step = 0; step < requests.size(); step++) {
            int nearestIdx = -1;
            double nearestDist = Double.MAX_VALUE;

            for (int i = 0; i < requests.size(); i++) {
                if (!visited[i]) {
                    ServiceRequest req = requests.get(i);
                    // Use Dijkstra to find distance from current location to the request's destination
                    RouteEngine.PathResult pathRes = RouteEngine.dijkstra(graph, currentLoc, req.getDestLocationId());
                    if (pathRes != null && pathRes.totalDistanceKm < nearestDist) {
                        nearestDist = pathRes.totalDistanceKm;
                        nearestIdx = i;
                    }
                }
            }

            if (nearestIdx == -1) {
                break; // No more reachable requests
            }

            visited[nearestIdx] = true;
            ServiceRequest selected = requests.get(nearestIdx);
            result.add(selected);
            currentLoc = selected.getDestLocationId(); // Move to destination
        }

        return result;
    }

    /**
     * Dynamic Programming Strategy: 0/1 Knapsack Solver for optimal request batching.
     * Fits requests into a rider's weight capacity (carrying limit) to maximize total priority.
     * Capacity is specified as an integer in grams or decigrams to make DP table indexes integer.
     * We will scale the weights (in kg) to integer hectograms (e.g., 2.5 kg -> 25 hectograms).
     */
    public static DynamicArray<ServiceRequest> dpKnapsackBatching(DynamicArray<ServiceRequest> requests, double capacityKg) {
        int n = requests.size();
        int W = (int) (capacityKg * 10.0); // capacity in hectograms (100g units)

        // Item weights in hectograms
        int[] w = new int[n];
        double[] v = new double[n]; // priority values
        for (int i = 0; i < n; i++) {
            // Assume default parcel weight based on category
            double weight = getWeightByCategory(requests.get(i).getCategory());
            w[i] = (int) Math.max(1, Math.round(weight * 10.0));
            v[i] = requests.get(i).getPriority();
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
