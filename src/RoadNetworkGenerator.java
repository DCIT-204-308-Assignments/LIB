import java.io.*;
import java.nio.file.*;
import java.util.*;
import models.Location;
import models.RoadEdge;
import utils.GeoUtils;

/**
 * UG Swift - Road Network Generator (Java) — v2
 * ------------------------------------------------------
 * Reads locations.csv built directly from the REAL UG Legon Leaflet map
 * (95 real buildings/halls/services with real coordinates — no fabricated
 * junction names this time, since the source map only has building markers).
 *
 * Because the location names are no longer guaranteed to match any
 * hand-written "cross zone link" list, connectivity is no longer wired
 * through named junctions. Instead it is GUARANTEED ALGORITHMICALLY using
 * Prim's Minimum Spanning Tree over the full location set — which is also
 * one of the graph algorithms the DCIT 204/308 brief explicitly requires
 * students to implement, so this generator doubles as a working Prim's demo.
 *
 * Layers, in order:
 *   1. Zone spanning chain (nearest-neighbour within each zone) — realistic
 *      local road pattern inside Academic District / Residential Area /
 *      Services & Facilities.
 *   2. Prim's MST over ALL 95 locations by haversine distance — guarantees
 *      the WHOLE graph is one connected component, with no reliance on
 *      hand-named junctions.
 *   3. K-nearest-neighbour mesh: every node gets edges to its K closest
 *      neighbours — realistic road density / alternate routes.
 *   4. Extra short-hop shortcut edges under a distance threshold, added
 *      until the target edge count is reached.
 *   5. Attributes (traffic, road condition, one-way, weight) assigned with a
 *      Random seeded by the student index number (22237205).
 *   6. Final BFS reachability check proves the produced graph is connected
 *      before anything is written to disk.
 */
public class RoadNetworkGenerator {

    private static final long INDEX_NUMBER_SEED = 22237205L;
    private static final int TARGET_EDGE_COUNT = 340; // scaled up for 95 real nodes (was 260 for 73 synthetic nodes)
    private static final int K_NEAREST = 6;
    private static final double SHORTCUT_THRESHOLD_KM = 0.35; // campus buildings are close together in the real data

    private static final String[] TRAFFIC_LEVELS = {"LOW", "MEDIUM", "HIGH"};
    private static final double[] TRAFFIC_PROB = {0.5, 0.35, 0.15};
    private static final double[] TRAFFIC_WEIGHT = {1.0, 1.2, 1.5};

    private static final String[] ROAD_CONDITIONS = {"GOOD", "FAIR", "POOR"};
    private static final double[] CONDITION_PROB = {0.55, 0.35, 0.10};
    private static final double[] CONDITION_WEIGHT = {1.0, 1.15, 1.35};

    public static void main(String[] args) throws IOException {
        String inputPath = args.length > 0 ? args[0] : "locations.csv";
        String outputPath = args.length > 1 ? args[1] : "roads.csv";

        List<Location> locations = readLocations(inputPath);
        Map<Integer, Location> idToLoc = new HashMap<>();
        for (Location loc : locations) idToLoc.put(loc.getLocationId(), loc);

        Map<String, List<Integer>> zones = new LinkedHashMap<>();
        for (Location loc : locations) {
            zones.computeIfAbsent(loc.getZone(), z -> new ArrayList<>()).add(loc.getLocationId());
        }

        Set<Long> edgeSet = new LinkedHashSet<>();

        // 1. Nearest-neighbour spanning chain within each zone
        for (List<Integer> ids : zones.values()) {
            List<Integer> remaining = new ArrayList<>(ids);
            int current = remaining.remove(0);
            while (!remaining.isEmpty()) {
                int nearest = -1;
                double best = Double.MAX_VALUE;
                for (int candidate : remaining) {
                    double d = distance(idToLoc.get(current), idToLoc.get(candidate));
                    if (d < best) {
                        best = d;
                        nearest = candidate;
                    }
                }
                addEdge(edgeSet, current, nearest);
                remaining.remove(Integer.valueOf(nearest));
                current = nearest;
            }
        }

        // 2. Prim's MST across ALL locations -> guarantees full-graph connectivity
        List<long[]> mstEdges = primMST(locations, idToLoc);
        for (long[] e : mstEdges) {
            addEdge(edgeSet, (int) e[0], (int) e[1]);
        }
        System.out.println("Prim's MST edges added: " + mstEdges.size() + " (guarantees connectivity)");

        // 3. K-nearest-neighbour mesh
        for (Location loc : locations) {
            List<Map.Entry<Integer, Double>> ranked = new ArrayList<>();
            for (Location other : locations) {
                if (other.getLocationId() == loc.getLocationId()) continue;
                ranked.add(new AbstractMap.SimpleEntry<>(other.getLocationId(), distance(loc, other)));
            }
            ranked.sort(Comparator.comparingDouble(Map.Entry::getValue));
            int added = 0;
            for (Map.Entry<Integer, Double> entry : ranked) {
                if (added >= K_NEAREST) break;
                addEdge(edgeSet, loc.getLocationId(), entry.getKey());
                added++;
            }
        }

        // 4. Extra short-hop shortcuts under distance threshold, closest-first, up to target
        List<double[]> candidatePairs = new ArrayList<>();
        for (int i = 0; i < locations.size(); i++) {
            for (int j = i + 1; j < locations.size(); j++) {
                Location a = locations.get(i);
                Location b = locations.get(j);
                double d = distance(a, b);
                if (d < SHORTCUT_THRESHOLD_KM) {
                    candidatePairs.add(new double[]{d, a.getLocationId(), b.getLocationId()});
                }
            }
        }
        candidatePairs.sort(Comparator.comparingDouble(p -> p[0]));
        for (double[] pair : candidatePairs) {
            if (edgeSet.size() >= TARGET_EDGE_COUNT) break;
            addEdge(edgeSet, (int) pair[1], (int) pair[2]);
        }

        // Widen threshold once more if still short of target (keeps graph plausible, avoids random noise edges)
        if (edgeSet.size() < TARGET_EDGE_COUNT) {
            List<double[]> wider = new ArrayList<>();
            for (int i = 0; i < locations.size(); i++) {
                for (int j = i + 1; j < locations.size(); j++) {
                    Location a = locations.get(i);
                    Location b = locations.get(j);
                    double d = distance(a, b);
                    if (d >= SHORTCUT_THRESHOLD_KM && d < SHORTCUT_THRESHOLD_KM * 2) {
                        wider.add(new double[]{d, a.getLocationId(), b.getLocationId()});
                    }
                }
            }
            wider.sort(Comparator.comparingDouble(p -> p[0]));
            for (double[] pair : wider) {
                if (edgeSet.size() >= TARGET_EDGE_COUNT) break;
                addEdge(edgeSet, (int) pair[1], (int) pair[2]);
            }
        }

        // 5. Attach road attributes
        Random attrRandom = new Random(INDEX_NUMBER_SEED);
        List<RoadEdge> roads = new ArrayList<>();
        List<Long> sortedEdges = new ArrayList<>(edgeSet);
        Collections.sort(sortedEdges);
        int roadId = 1;
        for (long key : sortedEdges) {
            int a = (int) (key >> 32);
            int b = (int) (key & 0xFFFFFFFFL);
            Location locA = idToLoc.get(a);
            Location locB = idToLoc.get(b);

            double straight = distance(locA, locB);
            double inflate = 1.20 + attrRandom.nextDouble() * 0.25; // 1.20 - 1.45x (dense real campus, less winding needed)
            double distanceKm = round(straight * inflate, 3);

            String traffic = weightedChoice(TRAFFIC_LEVELS, TRAFFIC_PROB, attrRandom);
            String condition = weightedChoice(ROAD_CONDITIONS, CONDITION_PROB, attrRandom);
            double conditionWeight = CONDITION_WEIGHT[indexOf(ROAD_CONDITIONS, condition)];
            double trafficWeight = TRAFFIC_WEIGHT[indexOf(TRAFFIC_LEVELS, traffic)];

            double baseSpeedKmh = 12.0 / trafficWeight;
            double travelTimeMin = round((distanceKm / baseSpeedKmh) * 60, 2);
            boolean oneWay = attrRandom.nextDouble() < 0.08;
            double weight = round(distanceKm * conditionWeight * trafficWeight, 4);

            roads.add(new RoadEdge(roadId++, a, b, locA.getName(), locB.getName(),
                    distanceKm, travelTimeMin, traffic, condition, conditionWeight, oneWay, weight));
        }

        // 6. Connectivity check (BFS) before writing anything
        boolean connected = isConnected(locations, roads);
        System.out.println("Locations: " + locations.size());
        System.out.println("Roads generated: " + roads.size());
        System.out.println("Graph connected: " + connected);
        if (!connected) {
            throw new IllegalStateException("Generated road graph is NOT fully connected — aborting write.");
        }

        writeRoadsCsv(outputPath, roads);
        System.out.println("Wrote " + outputPath);
    }

    // ---- Prim's MST ----------------------------------------------------

    /** Classic O(n^2) Prim's algorithm. Returns MST edges as [fromId, toId]. */
    private static List<long[]> primMST(List<Location> locations, Map<Integer, Location> idToLoc) {
        int n = locations.size();
        boolean[] inMst = new boolean[n];
        double[] minDist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(minDist, Double.MAX_VALUE);
        Arrays.fill(parent, -1);
        minDist[0] = 0;

        List<long[]> mstEdges = new ArrayList<>();

        for (int count = 0; count < n; count++) {
            int u = -1;
            double best = Double.MAX_VALUE;
            for (int v = 0; v < n; v++) {
                if (!inMst[v] && minDist[v] < best) {
                    best = minDist[v];
                    u = v;
                }
            }
            if (u == -1) break; // shouldn't happen on a complete distance graph
            inMst[u] = true;
            if (parent[u] != -1) {
                mstEdges.add(new long[]{
                        locations.get(parent[u]).getLocationId(),
                        locations.get(u).getLocationId()
                });
            }
            for (int v = 0; v < n; v++) {
                if (!inMst[v]) {
                    double d = distance(locations.get(u), locations.get(v));
                    if (d < minDist[v]) {
                        minDist[v] = d;
                        parent[v] = u;
                    }
                }
            }
        }
        return mstEdges;
    }

    // ---- helpers -----------------------------------------------------

    private static boolean addEdge(Set<Long> edgeSet, int a, int b) {
        if (a == b) return false;
        int lo = Math.min(a, b);
        int hi = Math.max(a, b);
        long key = ((long) lo << 32) | (hi & 0xFFFFFFFFL);
        return edgeSet.add(key);
    }

    private static double distance(Location a, Location b) {
        return GeoUtils.haversineKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
    }

    private static double round(double val, int places) {
        double factor = Math.pow(10, places);
        return Math.round(val * factor) / factor;
    }

    private static String weightedChoice(String[] options, double[] probs, Random rnd) {
        double r = rnd.nextDouble();
        double cumulative = 0;
        for (int i = 0; i < options.length; i++) {
            cumulative += probs[i];
            if (r <= cumulative) return options[i];
        }
        return options[options.length - 1];
    }

    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return -1;
    }

    private static boolean isConnected(List<Location> locations, List<RoadEdge> roads) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (Location loc : locations) adj.put(loc.getLocationId(), new ArrayList<>());
        for (RoadEdge e : roads) {
            adj.get(e.getFromLocationId()).add(e.getToLocationId());
            if (!e.isOneWay()) {
                adj.get(e.getToLocationId()).add(e.getFromLocationId());
            }
        }
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> queue = new ArrayDeque<>();
        int start = locations.get(0).getLocationId();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            for (int nb : adj.get(cur)) {
                if (!visited.contains(nb)) {
                    visited.add(nb);
                    queue.add(nb);
                }
            }
        }
        if (visited.size() != locations.size()) {
            List<String> unreachable = new ArrayList<>();
            for (Location loc : locations) {
                if (!visited.contains(loc.getLocationId())) unreachable.add(loc.getName());
            }
            System.err.println("Unreachable: " + unreachable);
            return false;
        }
        return true;
    }

    private static List<Location> readLocations(String path) throws IOException {
        List<Location> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String header = reader.readLine(); // locationId,name,zone,type,latitude,longitude[,sourceSlug]
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = parseCsvLine(line);
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                String zone = parts[2].trim();
                String type = parts[3].trim();
                double lat = Double.parseDouble(parts[4].trim());
                double lon = Double.parseDouble(parts[5].trim());
                result.add(new Location(id, name, zone, type, lat, lon));
            }
        }
        return result;
    }

    /**
     * Minimal quote-aware CSV line splitter — handles fields wrapped in double
     * quotes that contain commas (e.g. "Institute of Statistical, Social and
     * Economic Research(ISSER)"), which a naive line.split(",") would corrupt.
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    private static void writeRoadsCsv(String path, List<RoadEdge> roads) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
            writer.write("roadId,fromLocationId,toLocationId,fromName,toName,distanceKm,travelTimeMin,trafficLevel,roadCondition,roadConditionWeight,isOneWay,weight");
            writer.newLine();
            for (RoadEdge r : roads) {
                writer.write(r.toCsvRow());
                writer.newLine();
            }
        }
    }
}
