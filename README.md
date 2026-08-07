# UG SWIFT

This repository now contains a fuller Java desktop application for a campus logistics and routing system built around the UG Swift concept.

## What the project now includes

- A desktop UI for initializing the database, generating roads, computing routes, and running dispatch demos.
- A realistic campus road-network generator based on UG Legon location data.
- Graph-based routing and shortest-path logic using Dijkstra.
- Scheduling and optimisation engines for FIFO, priority, and round-robin dispatching.
- A SQLite-backed data layer for locations, roads, riders, requests, and audit history.
- A comprehensive DSA test suite covering custom data structures and algorithms.

## How to run it

Run the project directly with the standard Java entry point:

- Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName } | Set-Content sources.txt
- javac -cp ".;sqlite-jdbc-3.42.0.0.jar" -d bin @(Get-Content sources.txt)
- Remove-Item sources.txt
- java -cp "bin;sqlite-jdbc-3.42.0.0.jar" Main

## Main project areas

- [src/UGSwiftApp.java](src/UGSwiftApp.java) — interactive desktop UI.
- [src/UGSwiftLauncher.java](src/UGSwiftLauncher.java) — startup entry point.
- [src/RoadNetworkGenerator.java](src/RoadNetworkGenerator.java) — road network generation.
- [src/UGSwiftTestSuite.java](src/UGSwiftTestSuite.java) — DSA validation suite.
- [src/engines](src/engines) — route planning, scheduler, optimiser, and database logic.
- [src/models](src/models) — domain objects for locations, roads, and requests.

## Architecture mapping (data structures)

This section outlines recommended data structures for the main components of a campus food-delivery system. The table maps each component to a data structure and a short rationale — similar to production systems (Uber Eats, Bolt Food) but tailored for the campus delivery scope of this project.

| Component | Data Structure | Why |
|---|---|---|
| Registered users | Hash Table | Fast lookup by email, phone, or ID (O(1)) |
| Restaurants / Vendors | Hash Table + BST | Fast lookup by ID, and ordered/BST view for alphabetical browsing |
| Menu items | Hash Table | Retrieve food by ID quickly |
| Orders (active) | Doubly Linked List | Easy insertion/removal while orders change status |
| Incoming orders | FIFO Queue | Orders arrive in sequence; simple worker consumption model |
| Emergency / VIP orders | Priority Queue (Heap) | High-priority orders served first with logarithmic updates |
| Driver waiting list | Circular Queue | Fair round-robin rider assignment |
| Driver route history | Stack | Undo last navigation or audit previous stops |
| Live delivery route | Graph | Represents campus roads for routing algorithms |
| Nearby buildings | Adjacency List | Efficient sparse graph representation |
| Distance matrix | Adjacency Matrix | Useful for analytics and pairwise comparisons |
| Shortest path | Dijkstra | Fastest-route computation on weighted graphs |
| Campus exploration | BFS | Find reachable locations within N hops |
| Connectivity checking | DFS | Network/component analysis for graph integrity |
| Road network optimization | Prim / Kruskal | Build minimum spanning network for baseline routing |
| Location search | Red-Black Tree / BST | Ordered searching, range queries and prefix scans |
| Database indexing simulation | B-Tree | Mimics SQL index behavior for disk-based lookups |
| Driver availability | Hash Table | Instant rider lookup and status updates |
| Rider rankings | Heap | Highest-rated / closest rider retrieval |
| Customer search | Binary Search Tree | Ordered customer records for prefix/ordered queries |
| Search suggestions | Trie (optional) | Autocomplete for buildings / vendors |
| Undo cancelled orders | Stack | Reverse operations if an order is rolled back |
| Notifications waiting | Queue | FIFO message delivery to users / riders |
| Analytics | Dynamic Array | Efficient append and compact storage for time-series data |
| Connected campus zones | Disjoint Set | Detect connected components for Kruskal / clustering |

Guidance: the codebase already contains several custom DS implementations under `src/ds/`. For production-style behavior, use the existing implementations (Queue, Stack, Heap, Graph, etc.) and wire them into the engines (`engines/`) and UI (`UGSwiftApp.java`). I can implement specific integrations next (incoming FIFO + priority queue, driver circular queue, and persistent orders lifecycle).
