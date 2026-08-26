# UG Swift — Codebase Completion & DSA Implementation Specification

## 1. Project Overview

UG Swift is a Java-based campus delivery and logistics optimization system designed to demonstrate practical applications of Data Structures and Algorithms.

The current repository is a **skeleton implementation with many of the required classes already present**. The objective is not to rebuild the project from scratch, but to complete, integrate, test, and optimize the existing architecture.

The repository already contains:

```text
UG-Swift/
│
├── bin/
│   ├── ds/
│   ├── engines/
│   ├── models/
│   ├── utils/
│   └── *.class
│
├── data/
│   ├── locations.csv
│   └── roads.csv
│
├── src/
│   ├── ds/
│   │   ├── BST.java
│   │   ├── BTree.java
│   │   ├── CircularQueue.java
│   │   ├── Deque.java
│   │   ├── DisjointSet.java
│   │   ├── DynamicArray.java
│   │   ├── Graph.java
│   │   ├── HashTable.java
│   │   ├── LinkedList.java
│   │   ├── MinHeap.java
│   │   ├── Queue.java
│   │   ├── RedBlackTree.java
│   │   └── Stack.java
│   │
│   ├── engines/
│   │   ├── DatabaseManager.java
│   │   ├── DeliveryEngine.java
│   │   ├── DriverPool.java
│   │   ├── IncomingOrderManager.java
│   │   ├── IndexingEngine.java
│   │   ├── OptimisationEngine.java
│   │   ├── RouteEngine.java
│   │   ├── SchedulingEngine.java
│   │   └── SortingEngine.java
│   │
│   ├── models/
│   │   ├── AlgorithmRun.java
│   │   ├── AuditEvent.java
│   │   ├── Location.java
│   │   ├── Order.java
│   │   ├── Resource.java
│   │   ├── RoadEdge.java
│   │   └── ServiceRequest.java
│   │
│   ├── tools/
│   │   ├── CampusMapLauncher.java
│   │   ├── ExportLocations.java
│   │   └── ExportRoute.java
│   │
│   ├── utils/
│   │
│   ├── Main.java
│   ├── RoadNetworkGenerator.java
│   ├── SeedDB.java
│   ├── UGSwiftApp.java
│   ├── UGSwiftLauncher.java
│   └── UGSwiftTestSuite.java
│
├── web/
│   └── campus_map/
│       ├── index.html
│       ├── locations.js
│       ├── script.js
│       └── style.css
│
├── data/
├── sqlite-jdbc-3.42.0.0.jar
├── ug_swift.db
├── Progress.md
└── README.md
```

---

# 2. Overall Objective

The final system must demonstrate a complete pipeline:

```text
Campus Location/Road Data
          ↓
        Graph
          ↓
     Route Engine
          ↓
   Incoming Order
          ↓
   Order Management
          ↓
   Scheduling Engine
          ↓
  Optimisation Engine
          ↓
      Driver Pool
          ↓
    Delivery Engine
          ↓
     Completed Order
          ↓
   Database / Audit
          ↓
 Algorithm Performance
```

The key objective is to ensure that the existing data structures in `src/ds` are **actually used by the engines in `src/engines`**.

Simply having a `BTree.java`, `Stack.java`, or `RedBlackTree.java` file is not enough.

---

# 3. Priority 1 — Complete the Data Structures

The following data structures already exist:

```text
BST
BTree
CircularQueue
Deque
DisjointSet
DynamicArray
Graph
HashTable
LinkedList
MinHeap
Queue
RedBlackTree
Stack
```

Each implementation must be reviewed for:

* Correctness
* Edge cases
* Empty-state behavior
* Duplicate handling
* Searching
* Insertion
* Deletion
* Traversal
* Size tracking
* Performance
* Integration with the engines

## Required

The data structures must not exist only as standalone demonstrations.

They must be connected to actual system operations.

---

# 4. Data Structure Usage Map

The following is the intended mapping.

| Data Structure   | Primary System Usage                              |
| ---------------- | ------------------------------------------------- |
| `Graph`          | Campus road network                               |
| `MinHeap`        | Shortest path / priority processing               |
| `Priority Queue` | Order scheduling                                  |
| `Queue`          | Incoming delivery requests                        |
| `CircularQueue`  | Rotating/round-robin operations where appropriate |
| `Deque`          | Flexible scheduling/order processing              |
| `Stack`          | Scheduling/backtracking/history                   |
| `HashTable`      | Fast rider/order/resource lookup                  |
| `BST`            | Ordered searchable records                        |
| `BTree`          | Large-scale order/index lookup                    |
| `RedBlackTree`   | Dynamically ordered records                       |
| `LinkedList`     | Sequential history/records                        |
| `DynamicArray`   | Dynamic collections                               |
| `DisjointSet`    | Connectivity/network analysis                     |

The final implementation may adjust this mapping where the existing architecture suggests a better use, but every major DSA implementation should have a justified purpose.

---

# 5. Priority 2 — `models/` Overhaul

The models currently provide the foundation for the system.

The following classes require review:

```text
AlgorithmRun.java
AuditEvent.java
Location.java
Order.java
Resource.java
RoadEdge.java
ServiceRequest.java
```

---

## 5.1 `Location.java`

`Location` should represent a physical point on the University of Ghana campus.

It should provide enough information for:

* Mapping
* Distance calculation
* Route calculation
* Rider location tracking
* Pickup location
* Delivery location

Potential information:

```text
id
name
latitude
longitude
type
```

---

# 6. `Order.java` Overhaul

The current Order model needs to be sufficiently detailed for scheduling and optimization.

An order should contain information such as:

```text
orderId
customer/requester
pickupLocation
deliveryLocation
creationTime
requestedDeliveryTime
priority
status
distance
estimatedDeliveryTime
assignedResource
vehicleType
```

The exact field names should follow the existing project conventions.

## Required Order States

At minimum:

```text
CREATED
QUEUED
SCHEDULED
ASSIGNED
PICKED_UP
IN_TRANSIT
COMPLETED
CANCELLED
```

The state must be updated throughout the delivery lifecycle.

---

# 7. `Resource.java` — Rider Model

Resources should support the project's delivery riders.

At minimum, the system must distinguish:

```text
BICYCLE
MOTORCYCLE
```

Each rider/resource should have:

```text
resourceId
name
vehicleType
currentLocation
availability
currentOrder
completedDeliveries
```

The resource must be capable of changing location as deliveries progress.

---

# 8. `ServiceRequest.java`

`ServiceRequest` should represent an incoming request before or during conversion into an operational order.

The flow should be:

```text
ServiceRequest
      ↓
IncomingOrderManager
      ↓
Order
      ↓
Scheduling
      ↓
Optimisation
      ↓
Delivery
```

The distinction between `ServiceRequest` and `Order` should be clear.

---

# 9. `AlgorithmRun.java`

`AlgorithmRun` must be used to record actual algorithm executions.

It should capture information such as:

```text
algorithmName
inputSize
startTime
endTime
executionTime
result
operations
comparisons
status
```

Where appropriate, additional metrics should be recorded.

This is important because the project is not only expected to implement algorithms but also demonstrate **empirical analysis**.

---

# 10. Priority 3 — `DriverPool.java`

`DriverPool.java` needs significant improvement.

The Driver Pool must maintain all active delivery resources and support efficient lookup.

## Required operations

```text
addDriver()
removeDriver()
findDriver()
updateDriverLocation()
setDriverAvailable()
setDriverBusy()
getAvailableDrivers()
getDriversByVehicleType()
findNearestDriver()
```

The implementation should use the project's custom data structures where appropriate.

---

# 11. Rider Assignment

The Driver Pool and Optimisation Engine must work together.

When an order arrives, the system should:

```text
1. Get pickup location
2. Get delivery location
3. Calculate delivery distance
4. Determine acceptable vehicle types
5. Retrieve available riders
6. Calculate rider-to-pickup distance
7. Estimate delivery time
8. Score eligible riders
9. Select the optimal rider
10. Assign rider
```

---

# 12. Bicycle vs Motorcycle Logic

The system contains:

```text
Bicycle Riders
Motorcycle Riders
```

The vehicle must be selected intelligently.

## Required rule

A configurable threshold should be implemented.

Example:

```text
MAX_BICYCLE_DISTANCE = 6.0 km
```

Then:

```text
IF delivery distance <= 6 km
    Bicycle and Motorcycle riders can be considered

IF delivery distance > 6 km
    Bicycle riders are excluded
    Motorcycle riders are considered
```

This should be implemented inside the optimization/assignment logic rather than scattered throughout the application.

---

# 13. Nearest Rider Algorithm

The system needs a proper algorithm to map an order to the rider closest to its pickup point.

For every eligible rider:

```text
distance =
    distance(rider.currentLocation, order.pickupLocation)
```

The system then selects the best candidate.

A basic implementation is:

```text
bestDistance = infinity

for each available rider:
    if rider is eligible:
        calculate distance

        if distance < bestDistance:
            bestDistance = distance
            bestDriver = rider
```

However, the final Optimisation Engine should consider more than distance.

---

# 14. Optimisation Score

The optimization engine should be capable of considering:

```text
Pickup Distance
+
Delivery Distance
+
Estimated Delivery Time
+
Vehicle Suitability
+
Rider Availability
+
Order Priority
```

A scoring model can be introduced.

For example:

```text
score =
    w1 * pickupDistance
  + w2 * estimatedDeliveryTime
  + w3 * workload
  + w4 * vehiclePenalty
  - w5 * priority
```

The weights should be configurable.

The objective is to minimize delivery time while respecting constraints.

---

# 15. Priority 4 — `IncomingOrderManager.java`

This class currently does not properly handle delivery requests.

It should become the main gateway for incoming requests.

Required flow:

```text
ServiceRequest received
        ↓
Validate request
        ↓
Create Order
        ↓
Calculate distance
        ↓
Determine priority
        ↓
Queue/Schedule order
        ↓
Find suitable rider
        ↓
Assign rider
        ↓
Send to DeliveryEngine
```

The manager should not simply store incoming requests.

---

# 16. Priority 5 — `SchedulingEngine.java`

This is one of the most important components requiring implementation.

The Scheduling Engine must perform actual scheduling rather than acting as a placeholder.

It should manage:

* Incoming orders
* Order priority
* Rider availability
* Scheduling decisions
* Delivery time
* Assignment sequence

---

# 17. Stack Integration in Scheduling Engine

A proper `Stack` implementation already exists:

```text
src/ds/Stack.java
```

It must be properly integrated into the Scheduling Engine.

Possible uses include:

* Backtracking scheduling decisions
* Maintaining scheduling history
* Undoing/revisiting decisions
* Temporary scheduling states

The stack must have an actual algorithmic purpose.

Do not simply instantiate a stack to satisfy a requirement.

---

# 18. Priority Queue / MinHeap Scheduling

The scheduling engine should use an appropriate priority-based structure.

For example:

```text
Priority Queue
      ↓
High Priority Order
      ↓
Earlier Requested Time
      ↓
Shortest/Most Suitable Delivery
```

The exact priority rules should be documented.

The system must prevent an arbitrary insertion order from becoming the scheduling order unless that is intentionally part of the algorithm.

---

# 19. Priority 6 — B-Tree Integration

`src/ds/BTree.java` already exists.

It must be reviewed and properly integrated.

Potential application:

```text
Order Index
```

Example:

```text
Order ID
   ↓
B-Tree
   ↓
Fast ordered lookup
```

The project should demonstrate why a B-Tree is appropriate for the selected use case.

---

# 20. Priority 7 — Red-Black Tree Integration

`src/ds/RedBlackTree.java` also exists.

It must be integrated into an actual engine.

Potential applications:

```text
Active Orders
Ordered Riders
Time-based Scheduling
Dynamic Priority Records
```

The Red-Black Tree should support efficient:

```text
Insert
Search
Delete
```

while maintaining balance.

---

# 21. Priority 8 — `RouteEngine.java`

The Route Engine should operate on:

```text
Location
RoadEdge
Graph
MinHeap
```

The road data is already available:

```text
data/locations.csv
data/roads.csv
```

The engine should construct and operate on the campus graph.

Conceptually:

```text
locations.csv
     ↓
Location objects
     ↓
Graph vertices

roads.csv
     ↓
RoadEdge objects
     ↓
Graph edges
```

---

# 22. Shortest Path

The Route Engine should provide a shortest-path algorithm such as Dijkstra's algorithm where appropriate.

Expected structure:

```text
Graph
  ↓
MinHeap
  ↓
Dijkstra
  ↓
Shortest Path
  ↓
Estimated Distance
```

The algorithm should record its execution through `AlgorithmRun`.

---

# 23. Geographic Distance

Where latitude and longitude are used, the system should calculate geographic distance correctly.

The Haversine formula should be considered for direct geographic distance:

```text
a =
sin²(Δφ/2)
+
cos(φ1)cos(φ2)sin²(Δλ/2)

c = 2 atan2(√a, √(1-a))

d = R × c
```

This can be used for:

* Rider-to-pickup distance
* Pickup-to-delivery distance
* Distance estimation
* Vehicle selection

Road-network distance should be used where actual campus routing is required.

---

# 24. Priority 9 — `DeliveryEngine.java`

The Delivery Engine should control the actual delivery lifecycle.

Required flow:

```text
Order
 ↓
Assigned Rider
 ↓
Travel to Pickup
 ↓
Pickup
 ↓
Travel to Destination
 ↓
Delivery
 ↓
Complete
 ↓
Rider Available
```

The engine must update:

```text
Order status
Rider status
Rider location
Delivery statistics
Audit events
```

---

# 25. Rider Location Updates

After completing a delivery, the rider's location must become the delivery destination.

Example:

```text
Before:
Rider → Location A

Pickup → Location B
Delivery → Location C

After completion:
Rider → Location C
```

This is important because the next order should calculate distance from the rider's **actual current location**.

---

# 26. Priority 10 — `OptimisationEngine.java`

The existing optimization engine should be overhauled.

It should become the main decision-making component for:

```text
Which rider?
Which vehicle?
Which order?
Which route?
```

It should use information from:

```text
DriverPool
RouteEngine
SchedulingEngine
Order
Location
```

---

# 27. Suggested Optimization Pipeline

```text
New Order
   ↓
Calculate Delivery Distance
   ↓
Check Vehicle Constraints
   ↓
Get Available Riders
   ↓
Filter Ineligible Riders
   ↓
Calculate Rider → Pickup Distance
   ↓
Estimate Delivery Time
   ↓
Calculate Assignment Score
   ↓
Select Best Rider
   ↓
Assign Delivery
```

---

# 28. Priority 11 — `IndexingEngine.java`

The Indexing Engine should demonstrate the practical use of the project's search structures.

Possible indexes:

```text
Order ID
Driver ID
Location ID
Service Request ID
```

Potential structures:

```text
HashTable
BTree
RedBlackTree
BST
```

The engine should provide efficient lookup operations rather than repeatedly scanning every record.

---

# 29. Priority 12 — `SortingEngine.java`

The Sorting Engine should provide meaningful sorting operations.

Potential sorting targets:

```text
Orders by priority
Orders by delivery time
Drivers by distance
Drivers by workload
Deliveries by completion time
```

Where appropriate, the engine should expose algorithm performance.

Example:

```text
Algorithm: Merge Sort
Input Size: 1000
Execution Time: X ms
Comparisons: Y
```

---

# 30. Database Integration

The existing:

```text
DatabaseManager.java
SeedDB.java
ug_swift.db
```

should be integrated with the operational engines.

The database should persist important information such as:

```text
Orders
Resources/Riders
Locations
Deliveries
Audit Events
Algorithm Runs
```

The database should not become a replacement for the DSA structures.

The custom data structures should still be used for in-memory algorithmic operations.

---

# 31. Audit Events

`AuditEvent.java` should record important system events.

Examples:

```text
ORDER_CREATED
ORDER_ASSIGNED
RIDER_STATUS_CHANGED
ORDER_PICKED_UP
ORDER_DELIVERED
ORDER_CANCELLED
ROUTE_CALCULATED
ALGORITHM_EXECUTED
```

This provides traceability during demonstrations and testing.

---

# 32. Algorithm Run Model

Every major algorithm should be measurable.

Examples:

```text
Dijkstra
Nearest Rider
Sorting
Searching
Scheduling
Optimization
```

The system should capture:

```text
Algorithm Name
Input Size
Execution Time
Number of Operations
Result
Timestamp
```

This allows the project to demonstrate empirical algorithm analysis.

---

# 33. Test Suite Overhaul

`UGSwiftTestSuite.java` currently needs to move beyond generic tests.

It should test the actual implementation.

## Data Structure Tests

Test:

```text
Stack
Queue
CircularQueue
Deque
LinkedList
DynamicArray
HashTable
BST
BTree
RedBlackTree
MinHeap
Graph
DisjointSet
```

Each should test:

* Normal operations
* Empty operations
* Boundary cases
* Duplicate values
* Invalid operations
* Correct output
* Size/state changes

---

# 34. Engine Tests

The test suite should test:

```text
DriverPool
IncomingOrderManager
SchedulingEngine
OptimisationEngine
RouteEngine
DeliveryEngine
SortingEngine
IndexingEngine
```

Tests must verify actual behavior.

---

# 35. End-to-End Test

At least one test should execute the complete delivery lifecycle.

Example:

```text
Create Order
     ↓
IncomingOrderManager
     ↓
Calculate Distance
     ↓
SchedulingEngine
     ↓
OptimisationEngine
     ↓
Select Rider
     ↓
DeliveryEngine
     ↓
Complete Delivery
     ↓
Update Rider
     ↓
Persist Result
```

Expected result:

```text
Order = COMPLETED
Rider = AVAILABLE
Rider Location = Delivery Location
Database = Updated
Audit Event = Recorded
Algorithm Run = Recorded
```

---

# 36. Critical Test — 6 km Vehicle Rule

The following test must exist.

### Input

```text
Delivery distance = 8 km
Bicycle rider = available
Motorcycle rider = available
```

### Expected

```text
Bicycle = rejected
Motorcycle = eligible
Motorcycle = selected
```

---

# 37. Critical Test — Short Distance

### Input

```text
Delivery distance = 3 km
Bicycle rider = available
Motorcycle rider = available
```

### Expected

```text
Both vehicles are eligible.

The optimization algorithm selects the rider
with the best assignment score.
```

---

# 38. Critical Test — Nearest Rider

Example:

```text
Rider A → 1.2 km from pickup
Rider B → 3.4 km from pickup
Rider C → 0.8 km from pickup
```

Expected:

```text
Rider C
```

provided Rider C satisfies all other constraints.

---

# 39. Critical Test — No Available Rider

If all riders are busy:

```text
New Order
    ↓
No eligible rider
    ↓
Order remains queued/scheduled
```

The application must not crash or assign the order to a busy rider.

---

# 40. Critical Test — Multiple Orders

The system should be tested with multiple simultaneous orders.

Example:

```text
10 Orders
5 Riders
```

The system must:

* Queue orders correctly
* Apply priorities
* Avoid double assignment
* Assign eligible riders
* Update rider states
* Complete deliveries
* Release riders for subsequent jobs

---

# 41. `web/campus_map`

The campus map should remain integrated with the backend data.

The map currently contains:

```text
index.html
locations.js
script.js
style.css
```

The map should be capable of visualizing:

* Campus locations
* Roads
* Pickup points
* Delivery points
* Rider locations
* Routes

Where possible, the route generated by `RouteEngine` should correspond to the route visualized on the map.

---

# 42. Tools

The existing tools:

```text
CampusMapLauncher.java
ExportLocations.java
ExportRoute.java
```

should be reviewed to ensure they work with the completed route and location system.

They should not contain duplicate routing logic.

The actual route calculation should remain inside `RouteEngine`.

---

# 43. `Main.java`, `UGSwiftApp.java`, and Launchers

The application entry points should primarily coordinate the system rather than contain large amounts of business logic.

The architecture should remain:

```text
Application Layer
      ↓
Engines
      ↓
Models
      ↓
Data Structures
```

Business logic should not be unnecessarily placed inside:

```text
Main.java
UGSwiftApp.java
UGSwiftLauncher.java
```

---

# 44. Code Quality Requirements

The final implementation should:

* Use meaningful method names
* Avoid duplicated algorithms
* Avoid unnecessary global variables
* Avoid hard-coded values where configuration is appropriate
* Handle exceptions properly
* Document complex algorithms
* Keep models separate from engines
* Keep DSA implementations separate from business logic
* Avoid unnecessary third-party dependencies

---

# 45. Complexity Documentation

Every important algorithm should have documented complexity.

Examples:

| Operation             |  Expected Complexity |
| --------------------- | -------------------: |
| Hash Table Search     |         Average O(1) |
| BST Search            |     O(log n) average |
| Red-Black Tree Search |             O(log n) |
| B-Tree Search         |             O(log n) |
| Heap Insert           |             O(log n) |
| Heap Remove           |             O(log n) |
| Stack Push            |                 O(1) |
| Stack Pop             |                 O(1) |
| Queue Enqueue         |                 O(1) |
| Queue Dequeue         |                 O(1) |
| Dijkstra with MinHeap |     O((V + E) log V) |
| Linear Rider Search   |                 O(n) |
| Sorting               | Depends on algorithm |

The actual complexity should match the implementation.

---

# 46. Definition of Done

The project should not be considered complete simply because it compiles.

**Status legend**

| Mark | Meaning |
| --- | --- |
| `[x]` | Done, and reachable from the running application |
| `[~]` | Implemented and often unit-tested, but **not wired into the running app**, or only partly done |
| `[ ]` | Not implemented |

> Verified against commit `8e0718a` on a clean seeded database
> (95 locations, 382 roads, 30 riders, 300 requests) with `UGSwiftTestSuite`
> reporting 198 assertions passed, 0 failed.
> The `[~]` marks matter: a class that compiles and passes tests but that no
> code path ever calls does not demonstrate the requirement it was written for.

## Data Structures

* [x] All DSA implementations compile and behave correctly. *(198 assertions pass)*
* [~] Stack is integrated into scheduling. *(`SchedulingEngine.dispatchUrgentOverride` uses `Stack`, but that method has no call sites)*
* [~] BTree is integrated into a meaningful operation. *(used by `IndexingEngine`, which the app never calls)*
* [~] RedBlackTree is integrated into a meaningful operation. *(same: `IndexingEngine` only)*
* [x] Graph is used by the Route Engine.
* [x] MinHeap is used where priority processing is required. *(Dijkstra frontier, `IncomingOrderManager`, `DeliveryEngine` candidate ranking)*
* [x] HashTable is used for efficient lookup. *(`Graph` adjacency, `DriverPool.allRiders`)*
* [x] Queue/Priority Queue is used for incoming/scheduled orders. *(`IncomingOrderManager`)*

## Models

* [x] `Order` contains sufficient delivery information.
* [x] `Resource` properly represents riders.
* [x] Bicycle and motorcycle riders are distinguished.
* [x] Rider availability is tracked.
* [ ] Rider location is tracked. *(`setCurrentLocationId` is only reached from `SimulationEngine` and the tests; the live app never moves a rider, and `resources` has no `currentLocationId` column)*
* [ ] Order lifecycle is tracked. *(`UGSwiftApp` creates orders with the string `"PENDING"`, which is not an `OrderState`; PICKED_UP / IN_TRANSIT / COMPLETED never occur in the app)*
* [~] `AlgorithmRun` records algorithm execution. *(`AlgorithmBenchmark` and `BenchmarkEngine` persist runs; the live app records nothing)*
* [ ] `AuditEvent` records important system events. *(`DatabaseManager.addAuditEvent` has zero call sites; `audit_events` is empty)*

## Engines

* [x] `DriverPool` properly manages riders. *(all nine operations from section 10 exist)*
* [ ] `IncomingOrderManager` properly handles requests. *(stores only; does not validate, create an Order, compute distance, or determine priority as section 15 requires)*
* [~] `SchedulingEngine` performs actual scheduling. *(four strategies implemented and unit-tested, but `runDispatch` is hardcoded to `"Nearest Rider"`, so none are reachable)*
* [~] `OptimisationEngine` performs actual optimization. *(greedy + DP + brute force implemented and tested; zero call sites from the app)*
* [ ] `DeliveryEngine` handles the complete delivery lifecycle. *(performs rider selection only; no pickup, transit, or completion stage)*
* [x] `RouteEngine` calculates routes. *(Dijkstra, used on every order)*
* [x] `SortingEngine` performs meaningful sorting.
* [~] `IndexingEngine` provides efficient lookup. *(implemented and unit-tested; zero call sites from the app)*
* [~] `DatabaseManager` persists important information. *(locations, roads, riders, requests and algorithm runs persist; **orders and audit events do not** - there is no `orders` table)*

## Optimization

* [x] Rider-to-pickup distance is calculated.
* [x] Delivery distance is calculated. *(computed once per order for the bicycle range check)*
* [x] Vehicle type is considered.
* [x] Bicycle riders are excluded beyond the configured 6 km threshold. *(`Config.MAX_BICYCLE_DISTANCE_KM`, enforced in `DeliveryEngine.assignRider`)*
* [x] Motorcycle riders are considered for long-distance deliveries.
* [x] Rider availability is considered.
* [~] Current rider location is considered. *(`DeliveryEngine` reads `getCurrentLocationId()`, but `UGSwiftApp.placeOrder` routes from `getHomeLocationId()`, and the value never changes anyway - see "Rider location is tracked")*
* [x] A clear assignment algorithm exists. *(`DeliveryEngine.scoreRider`)*
* [~] The algorithm can select the optimal eligible rider. *(it can, but `DriverPool.nextSuitable` is tried first and usually returns a merely acceptable rider before the scored path runs)*
* [ ] Order priority influences the assignment score. *(section 14 requires a priority term; `scoreRider` never reads `order.getPriority()`)*

## Testing

* [x] All major data structures have dedicated tests.
* [~] All major engines have dedicated tests. *(DriverPool, Scheduling, Optimisation, Indexing, Delivery, IncomingOrder and Graph are covered; `SimulationEngine`, `MetricsEngine`, `BenchmarkEngine`, `ReportEngine` and `DatabaseManager` are not)*
* [x] Edge cases are tested. *(23 `[BOUNDARY]` and 22 `[INVALID]` assertions)*
* [ ] Multiple orders are tested. *(no test places several orders and checks that riders are not double-assigned)*
* [x] Multiple riders are tested.
* [~] Vehicle restrictions are tested. *(the soft scoring preference is tested; the hard 6 km cutoff required by section 36 is not)*
* [x] No-rider scenarios are tested. *(`assignRider returns null when every candidate rider is unavailable`)*
* [ ] End-to-end delivery is tested. *(section 35 requires request through to completion, rider release and persistence; no such test exists)*
* [x] Algorithm performance is measured. *(`AlgorithmBenchmark` and `G2BenchmarkRunner`; see `docs/G2_PERFORMANCE_ANALYSIS.md`)*

## Summary

| Status | Count |
| --- | ---: |
| `[x]` done | 24 |
| `[~]` implemented but not wired / partial | 12 |
| `[ ]` not implemented | 8 |
| **Total** | **44** |

The dominant theme is `[~]`: most of the remaining work is **connecting existing,
already-tested code to the running application**, not writing new algorithms.

---

# 47. Final Architecture

The completed architecture should ultimately resemble:

```text
                         UG SWIFT
                            │
                    ┌───────┴───────┐
                    │ Application   │
                    │ Main / UI     │
                    └───────┬───────┘
                            │
                    ┌───────▼───────┐
                    │    ENGINES    │
                    ├───────────────┤
                    │ IncomingOrder │
                    │ Scheduling    │
                    │ Optimisation  │
                    │ DriverPool    │
                    │ Delivery      │
                    │ Route         │
                    │ Sorting       │
                    │ Indexing      │
                    │ Database      │
                    └───────┬───────┘
                            │
                    ┌───────▼───────┐
                    │    MODELS     │
                    ├───────────────┤
                    │ Order         │
                    │ Resource      │
                    │ Location      │
                    │ ServiceReq.   │
                    │ RoadEdge      │
                    │ AlgorithmRun  │
                    │ AuditEvent    │
                    └───────┬───────┘
                            │
                    ┌───────▼───────┐
                    │ DATA STRUCTURES│
                    ├───────────────┤
                    │ Graph         │
                    │ MinHeap       │
                    │ Queue         │
                    │ Stack         │
                    │ HashTable     │
                    │ BTree         │
                    │ RedBlackTree  │
                    │ BST           │
                    │ LinkedList    │
                    │ Deque         │
                    │ CircularQueue │
                    │ DynamicArray  │
                    │ DisjointSet   │
                    └───────────────┘
```

---

# 48. Final Goal

The final UG Swift system should demonstrate that the project is not merely a Java application with several data structure classes attached to it.

It should demonstrate a genuine relationship between:

```text
DATA STRUCTURES
       ↓
ALGORITHMS
       ↓
MODELS
       ↓
ENGINES
       ↓
OPTIMISATION
       ↓
REAL-WORLD CAMPUS DELIVERY
```

A delivery request should be able to enter the system, move through the appropriate data structures and algorithms, be assigned to the most suitable rider, have its route calculated, be delivered, and have the complete operation recorded and measured.

The existing codebase should therefore be **completed and integrated rather than rewritten unnecessarily**.
