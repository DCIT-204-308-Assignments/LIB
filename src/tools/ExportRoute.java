package tools;

import ds.Graph;
import models.Location;
import engines.DatabaseManager;
import engines.RouteEngine;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class ExportRoute {

    public static Path exportRoute(int fromLocationId, int toLocationId) throws Exception {
        var locs = DatabaseManager.loadLocations();
        var roads = DatabaseManager.loadRoads();

        // Determine max node id
        int maxId = 0;
        for (int i = 0; i < locs.size(); i++) {
            Location l = locs.get(i);
            if (l.getLocationId() > maxId) maxId = l.getLocationId();
        }

        Graph graph = new Graph(maxId);
        for (int i = 0; i < locs.size(); i++) {
            graph.addLocation(locs.get(i));
        }
        for (int i = 0; i < roads.size(); i++) {
            graph.addRoad(roads.get(i));
        }

        RouteEngine.PathResult res = RouteEngine.dijkstra(graph, fromLocationId, toLocationId);
        Path out = Path.of("web/campus_map/route.json");
        try (PrintWriter pw = new PrintWriter(new FileWriter(out.toFile()))) {
            if (res == null || res.path == null || res.path.size() == 0) {
                pw.println("null");
                return out;
            }
            pw.println("[");
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < res.path.size(); i++) {
                int nid = res.path.get(i);
                Location loc = null;
                for (int j = 0; j < locs.size(); j++) {
                    if (locs.get(j).getLocationId() == nid) { loc = locs.get(j); break; }
                }
                if (loc != null) {
                    lines.add(String.format("  { \"lat\": %f, \"lon\": %f }", loc.getLatitude(), loc.getLongitude()));
                }
            }
            pw.println(String.join(",\n", lines));
            pw.println("]");
        }
        return out;
    }
}
