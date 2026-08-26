import java.awt.GraphicsEnvironment;

import ds.DynamicArray;
import ds.Graph;
import engines.DatabaseManager;
import engines.RouteEngine;
import models.Location;
import models.RoadEdge;
import models.ServiceRequest;

public class UGSwiftLauncher {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            runConsoleMode();
        } else {
            javax.swing.SwingUtilities.invokeLater(() -> {
                UGSwiftApp app = new UGSwiftApp();
                app.setVisible(true);
            });
        }
    }

    private static void runConsoleMode() {
        System.out.println("UG Swift is running in console mode.");
        try {
            String dataDir = resolveDataDir();
            DatabaseManager.initializeDatabase(dataDir + "locations.csv", dataDir + "roads.csv");
            DynamicArray<Location> locations = DatabaseManager.loadLocations();
            DynamicArray<RoadEdge> roads = DatabaseManager.loadRoads();
            DynamicArray<ServiceRequest> requests = DatabaseManager.loadServiceRequests();

            System.out.printf("Loaded %d locations, %d roads, %d service requests.%n", locations.size(), roads.size(), requests.size());

            if (locations.size() >= 2) {
                Graph graph = buildGraph(locations, roads);
                RouteEngine.PathResult result = RouteEngine.dijkstra(graph, locations.get(0).getLocationId(), locations.get(1).getLocationId());
                if (result != null) {
                    System.out.printf("Sample route: %s -> %s | distance=%.3f km | time=%.2f min%n",
                            locations.get(0).getName(), locations.get(1).getName(), result.totalDistanceKm, result.totalTimeMin);
                }
            }

            System.out.println("Use the Swing UI on a desktop machine for the full interactive experience.");
        } catch (Exception ex) {
            System.err.println("Console startup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String resolveDataDir() {
        java.io.File cwd = new java.io.File(System.getProperty("user.dir"));
        java.io.File[] candidates = {
                new java.io.File(cwd, "data"),
                new java.io.File(cwd, "src/data"),
                new java.io.File(cwd.getParentFile(), "data"),
                new java.io.File("data")
        };
        for (java.io.File candidate : candidates) {
            if (candidate.exists() && candidate.isDirectory()) {
                return candidate.getPath() + java.io.File.separator;
            }
        }
        return "";
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
}
