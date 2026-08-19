package engines;

import ds.DynamicArray;
import ds.Graph;
import ds.LinkedList;
import ds.MinHeap;
import ds.Queue;
import ds.Stack;
import ds.DisjointSet;
import models.Location;
import models.RoadEdge;

import java.util.Comparator;

public class RouteEngine {

    public static class PathResult {
        public DynamicArray<Integer> path;
        public double totalWeight;
        public double totalDistanceKm;
        public double totalTimeMin;

        public PathResult(
                DynamicArray<Integer> path,
                double totalWeight,
                double totalDistanceKm,
                double totalTimeMin
        ) {
            this.path = path;
            this.totalWeight = totalWeight;
            this.totalDistanceKm = totalDistanceKm;
            this.totalTimeMin = totalTimeMin;
        }
    }

    /**
     * BFS Reachability check:
     * returns list of location IDs reachable from startId.
     */
    public static DynamicArray<Integer> bfsReachable(
            Graph graph,
            int startId
    ) {
        DynamicArray<Integer> visitedList =
                new DynamicArray<>();

        if (!isValidLocation(graph, startId)) {
            return visitedList;
        }

        boolean[] visited =
                new boolean[graph.getMaxNodeId() + 1];

        Queue<Integer> queue = new Queue<>();

        queue.enqueue(startId);
        visited[startId] = true;

        while (!queue.isEmpty()) {
            int curr = queue.dequeue();
            visitedList.add(curr);

            LinkedList<Graph.Edge> neighbors =
                    graph.getNeighbors(curr);

            if (neighbors != null) {
                for (Graph.Edge edge : neighbors) {
                    if (edge == null
                            || edge.to < 0
                            || edge.to >= visited.length) {
                        continue;
                    }

                    if (!visited[edge.to]) {
                        visited[edge.to] = true;
                        queue.enqueue(edge.to);
                    }
                }
            }
        }

        return visitedList;
    }

    /**
     * DFS Traversal check:
     * returns list of location IDs visited during DFS
     * from startId.
     */
    public static DynamicArray<Integer> dfsTraversal(
            Graph graph,
            int startId
    ) {
        DynamicArray<Integer> visitedList =
                new DynamicArray<>();

        if (!isValidLocation(graph, startId)) {
            return visitedList;
        }

        boolean[] visited =
                new boolean[graph.getMaxNodeId() + 1];

        Stack<Integer> stack = new Stack<>();

        stack.push(startId);

        while (!stack.isEmpty()) {
            int curr = stack.pop();

            if (curr < 0 || curr >= visited.length) {
                continue;
            }

            if (!visited[curr]) {
                visited[curr] = true;
                visitedList.add(curr);

                LinkedList<Graph.Edge> neighbors =
                        graph.getNeighbors(curr);

                if (neighbors != null) {
                    for (Graph.Edge edge : neighbors) {
                        if (edge == null
                                || edge.to < 0
                                || edge.to >= visited.length) {
                            continue;
                        }

                        if (!visited[edge.to]) {
                            stack.push(edge.to);
                        }
                    }
                }
            }
        }

        return visitedList;
    }

    /**
     * Dijkstra's Shortest Path Algorithm:
     * finds the path with the minimum weight.
     * Uses our custom MinHeap.
     */
    public static PathResult dijkstra(
            Graph graph,
            int startId,
            int endId
    ) {
        if (!isValidLocation(graph, startId)
                || !isValidLocation(graph, endId)) {
            return null;
        }

        int maxId = graph.getMaxNodeId();

        double[] dist =
                new double[maxId + 1];

        double[] distanceKm =
                new double[maxId + 1];

        double[] travelTimeMin =
                new double[maxId + 1];

        int[] parent =
                new int[maxId + 1];

        boolean[] visited =
                new boolean[maxId + 1];

        for (int i = 0; i <= maxId; i++) {
            dist[i] = Double.POSITIVE_INFINITY;
            distanceKm[i] = 0.0;
            travelTimeMin[i] = 0.0;
            parent[i] = -1;
        }

        dist[startId] = 0.0;

        class HeapNode {
            final int id;
            final double d;

            HeapNode(int id, double d) {
                this.id = id;
                this.d = d;
            }
        }

        MinHeap<HeapNode> heap =
                new MinHeap<>(
                        maxId + 1,
                        Comparator.comparingDouble(a -> a.d)
                );

        heap.insert(
                new HeapNode(startId, 0.0)
        );

        while (!heap.isEmpty()) {
            HeapNode currNode =
                    heap.extractMin();

            int u = currNode.id;

            if (u < 0 || u > maxId) {
                continue;
            }

            if (visited[u]) {
                continue;
            }

            visited[u] = true;

            if (u == endId) {
                break;
            }

            LinkedList<Graph.Edge> neighbors =
                    graph.getNeighbors(u);

            if (neighbors != null) {
                for (Graph.Edge edge : neighbors) {
                    if (edge == null
                            || edge.to < 0
                            || edge.to > maxId) {
                        continue;
                    }

                    if (!Double.isFinite(edge.weight)
                            || edge.weight < 0.0) {
                        continue;
                    }

                    int v = edge.to;

                    if (graph.getLocation(v) == null) {
                        continue;
                    }

                    if (!visited[v]
                            && dist[u] + edge.weight < dist[v]) {

                        dist[v] =
                                dist[u] + edge.weight;

                        distanceKm[v] =
                                distanceKm[u]
                                        + edge.distanceKm;

                        travelTimeMin[v] =
                                travelTimeMin[u]
                                        + edge.travelTimeMin;

                        parent[v] = u;

                        heap.insert(
                                new HeapNode(
                                        v,
                                        dist[v]
                                )
                        );
                    }
                }
            }
        }

        if (dist[endId]
                == Double.POSITIVE_INFINITY) {
            return null;
        }

        DynamicArray<Integer> path =
                new DynamicArray<>();

        int temp = endId;

        while (temp != -1) {
            path.add(0, temp);
            temp = parent[temp];
        }

        return new PathResult(
                path,
                dist[endId],
                distanceKm[endId],
                travelTimeMin[endId]
        );
    }

    /**
     * Kruskal's Minimum Spanning Tree Algorithm:
     * returns the MST edges.
     * Uses custom DisjointSet and SortingEngine.
     */
    public static DynamicArray<RoadEdge> kruskalMST(
            DynamicArray<Location> locations,
            DynamicArray<RoadEdge> roads
    ) {
        DynamicArray<RoadEdge> mstEdges =
                new DynamicArray<>();

        if (locations == null
                || locations.isEmpty()) {
            return mstEdges;
        }

        int maxId = 0;

        for (Location loc : locations) {
            if (loc == null
                    || loc.getLocationId() < 0) {
                continue;
            }

            if (loc.getLocationId() > maxId) {
                maxId = loc.getLocationId();
            }
        }

        boolean[] validLocation =
                new boolean[maxId + 1];

        int validLocationCount = 0;

        for (Location loc : locations) {
            if (loc == null) {
                continue;
            }

            int id = loc.getLocationId();

            if (id < 0 || id > maxId) {
                continue;
            }

            if (!validLocation[id]) {
                validLocation[id] = true;
                validLocationCount++;
            }
        }

        if (validLocationCount <= 1
                || roads == null
                || roads.isEmpty()) {
            return mstEdges;
        }

        DisjointSet ds =
                new DisjointSet(maxId + 1);

        DynamicArray<RoadEdge> sortedRoads =
                new DynamicArray<>(roads.size());

        for (RoadEdge road : roads) {
            if (road == null) {
                continue;
            }

            int u =
                    road.getFromLocationId();

            int v =
                    road.getToLocationId();

            double weight =
                    road.getWeight();

            if (u < 0
                    || v < 0
                    || u > maxId
                    || v > maxId) {
                continue;
            }

            if (!validLocation[u]
                    || !validLocation[v]) {
                continue;
            }

            if (Double.isNaN(weight)
                    || Double.isInfinite(weight)
                    || weight < 0.0) {
                continue;
            }

            sortedRoads.add(road);
        }

        SortingEngine.quickSort(
                sortedRoads,
                Comparator.comparingDouble(
                        RoadEdge::getWeight
                )
        );

        for (RoadEdge road : sortedRoads) {
            int u =
                    road.getFromLocationId();

            int v =
                    road.getToLocationId();

            if (ds.union(u, v)) {
                mstEdges.add(road);

                if (mstEdges.size()
                        == validLocationCount - 1) {
                    break;
                }
            }
        }

        return mstEdges;
    }

    /**
     * Prim's Minimum Spanning Tree Algorithm:
     * returns the MST edges.
     */
    public static DynamicArray<RoadEdge> primMST(
            Graph graph
    ) {
        DynamicArray<RoadEdge> mstEdges =
                new DynamicArray<>();

        if (graph == null) {
            return mstEdges;
        }

        int maxId =
                graph.getMaxNodeId();

        boolean[] inMST =
                new boolean[maxId + 1];

        double[] key =
                new double[maxId + 1];

        int[] parent =
                new int[maxId + 1];

        Graph.Edge[] parentEdge =
                new Graph.Edge[maxId + 1];

        for (int i = 0; i <= maxId; i++) {
            key[i] =
                    Double.POSITIVE_INFINITY;

            parent[i] = -1;
        }

        DynamicArray<Location> allLocs =
                graph.getAllLocations();

        if (allLocs.isEmpty()) {
            return mstEdges;
        }

        int startId =
                allLocs.get(0).getLocationId();

        if (!isValidLocation(graph, startId)) {
            return mstEdges;
        }

        key[startId] = 0.0;

        class PrimNode {
            final int id;
            final double k;

            PrimNode(int id, double k) {
                this.id = id;
                this.k = k;
            }
        }

        MinHeap<PrimNode> heap =
                new MinHeap<>(
                        maxId + 1,
                        Comparator.comparingDouble(a -> a.k)
                );

        heap.insert(
                new PrimNode(startId, 0.0)
        );

        while (!heap.isEmpty()) {
            PrimNode curr =
                    heap.extractMin();

            int u = curr.id;

            if (u < 0 || u > maxId) {
                continue;
            }

            if (graph.getLocation(u) == null) {
                continue;
            }

            if (inMST[u]) {
                continue;
            }

            inMST[u] = true;

            if (parent[u] != -1
                    && parentEdge[u] != null) {

                Location parentLocation =
                        graph.getLocation(parent[u]);

                Location currentLocation =
                        graph.getLocation(u);

                if (parentLocation != null
                        && currentLocation != null) {

                    mstEdges.add(
                            new RoadEdge(
                                    mstEdges.size() + 1,
                                    parent[u],
                                    u,
                                    parentLocation.getName(),
                                    currentLocation.getName(),
                                    parentEdge[u]
                                            .distanceKm,
                                    parentEdge[u]
                                            .travelTimeMin,
                                    parentEdge[u]
                                            .trafficLevel,
                                    parentEdge[u]
                                            .roadCondition,
                                    1.0,
                                    false,
                                    parentEdge[u]
                                            .weight
                            )
                    );
                }
            }

            LinkedList<Graph.Edge> neighbors =
                    graph.getNeighbors(u);

            if (neighbors != null) {
                for (Graph.Edge edge : neighbors) {

                    if (edge == null
                            || edge.to < 0
                            || edge.to > maxId) {
                        continue;
                    }

                    int v = edge.to;

                    if (graph.getLocation(v) == null) {
                        continue;
                    }

                    if (!Double.isFinite(edge.weight)
                            || edge.weight < 0.0) {
                        continue;
                    }

                    if (!inMST[v]
                            && edge.weight < key[v]) {

                        key[v] =
                                edge.weight;

                        parent[v] =
                                u;

                        parentEdge[v] =
                                edge;

                        heap.insert(
                                new PrimNode(
                                        v,
                                        key[v]
                                )
                        );
                    }
                }
            }
        }

        return mstEdges;
    }

    private static boolean isValidLocation(
            Graph graph,
            int id
    ) {
        return graph != null
                && id >= 0
                && id <= graph.getMaxNodeId()
                && graph.getLocation(id) != null;
    }
}