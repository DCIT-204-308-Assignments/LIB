# UG Swift — Campus Delivery & Smart Service Operations Optimizer

## Complete Development Roadmap & Implementation Specification

UG Swift is a Java-based campus delivery and logistics optimization system developed as a Data Structures and Algorithms project.

The project is designed to demonstrate how **custom data structures, algorithms, graph processing, scheduling, indexing, optimization, database systems, and empirical performance analysis** can be combined to solve a real-world campus logistics problem.

> **Important:** The current repository is a skeleton implementation. The objective is to complete and integrate the existing architecture rather than unnecessarily rebuild the project.

---

# 1. Project Objective

The final system should be capable of receiving campus delivery requests, processing them through the appropriate data structures and algorithms, selecting the most suitable rider, calculating an efficient route, completing the delivery, and recording the results.

The system should demonstrate the complete relationship between:

```text
Data Structures
      ↓
Algorithms
      ↓
Models
      ↓
Engines
      ↓
Scheduling
      ↓
Optimization
      ↓
Campus Delivery
      ↓
Performance Analysis
```

The project should not simply function as a CRUD delivery application.

Its primary academic purpose is to demonstrate the **practical application and performance of Data Structures and Algorithms**.

---

# 2. Current Repository Structure

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
│   │
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
├── sqlite-jdbc-3.42.0.0.jar
├── ug_swift.db
├── Progress.md
└── README.md
```

---

# 3. Architecture

The intended architecture is:

```text
┌──────────────────────────────────────────────┐
│              APPLICATION LAYER               │
│ Main / UGSwiftApp / UGSwiftLauncher          │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│                   ENGINES                    │
│                                              │
│ IncomingOrderManager                         │
│ SchedulingEngine                             │
│ OptimisationEngine                           │
│ DriverPool                                   │
│ DeliveryEngine                               │
│ RouteEngine                                  │
│ SortingEngine                                │
│ IndexingEngine                               │
│ DatabaseManager                              │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│                    MODELS                    │
│                                              │
│ Order                                        │
│ ServiceRequest                               │
│ Resource                                     │
│ Location                                     │
│ RoadEdge                                     │
│ AlgorithmRun                                 │
│ AuditEvent                                   │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│              DATA STRUCTURES                 │
│                                              │
│ Graph │ MinHeap │ Queue │ Stack │ HashTable  │
│ BTree │ RedBlackTree │ BST │ LinkedList      │
│ Deque │ CircularQueue │ DynamicArray         │
│ DisjointSet                                  │
└──────────────────────────────────────────────┘
```

---

# 4. Core Delivery Workflow

A complete delivery should follow this pipeline:

```text
Service Request
      ↓
IncomingOrderManager
      ↓
Validate Request
      ↓
Create Order
      ↓
Calculate Distance
      ↓
SchedulingEngine
      ↓
OptimisationEngine
      ↓
Filter Eligible Riders
      ↓
Select Best Rider
      ↓
RouteEngine
      ↓
DeliveryEngine
      ↓
Pickup
      ↓
Transit
      ↓
Delivery
      ↓
Order Completed
      ↓
Rider Released
      ↓
Database Updated
      ↓
Audit Event Recorded
      ↓
Algorithm Performance Recorded
```

---

# 5. Data Structures

The project already contains a large collection of custom data structures.

Each one must be properly implemented, tested, and integrated into the system.

## Existing Structures

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

---

# 6. Data Structure Responsibility Map

| Data Structure  | Intended Use                          |
| --------------- | ------------------------------------- |
| `Graph`         | Campus road network                   |
| `MinHeap`       | Shortest-path and priority processing |
| `Queue`         | Incoming delivery requests            |
| `CircularQueue` | Round-robin operations                |
| `Deque`         | Flexible order processing             |
| `Stack`         | Scheduling history/backtracking       |
| `HashTable`     | Fast rider/order/resource lookup      |
| `BST`           | Ordered searchable records            |
| `BTree`         | Large-scale indexing                  |
| `RedBlackTree`  | Dynamically ordered records           |
| `LinkedList`    | Sequential records/history            |
| `DynamicArray`  | Dynamic collections                   |
| `DisjointSet`   | Network connectivity analysis         |

The final implementation may modify this mapping if a better algorithmic application is identified.

---

# 7. Data Structure Requirements

Every custom data structure must be tested for:

* Insertion
* Deletion
* Searching
* Traversal
* Empty states
* Duplicate values
* Boundary conditions
* Invalid operations
* Size tracking
* Correct output
* Expected complexity

The objective is not merely to have the classes compile.

The data structures must actually work.

---

# 8. Stack Integration

`src/ds/Stack.java` must be meaningfully integrated into `SchedulingEngine.java`.

Possible applications include:

* Scheduling history
* Backtracking scheduling decisions
* Temporary scheduling states
* Reversing scheduling operations
* Undoing a scheduling decision

The stack should have a legitimate algorithmic purpose.

It should not be added simply to satisfy the requirement that a Stack exists.

---

# 9. B-Tree Integration

`src/ds/BTree.java` must be integrated into the application.

A suitable use case is large-scale order indexing.

Example:

```text
Order ID
   ↓
B-Tree
   ↓
Order Record
```

The project should demonstrate why a B-Tree is appropriate for the selected operation.

---

# 10. Red-Black Tree Integration

`src/ds/RedBlackTree.java` must also be integrated into an actual engine.

Potential applications include:

* Active orders
* Time-based scheduling
* Ordered riders
* Dynamic priority records

The Red-Black Tree should demonstrate efficient:

```text
Insert
Search
Delete
```

operations while maintaining balance.

---

# 11. Order Model

`src/models/Order.java` requires a sufficiently detailed representation of a delivery.

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

---

# 12. Order Lifecycle

Orders should have clear states:

```text
CREATED
   ↓
QUEUED
   ↓
SCHEDULED
   ↓
ASSIGNED
   ↓
PICKED_UP
   ↓
IN_TRANSIT
   ↓
COMPLETED
```

Additional states:

```text
CANCELLED
FAILED
```

may be added where necessary.

---

# 13. Resource / Rider Model

`src/models/Resource.java` should properly represent delivery riders.

The system must support:

```text
BICYCLE
MOTORCYCLE
```

Each rider should have information such as:

```text
resourceId
name
vehicleType
currentLocation
availabilityStatus
currentOrder
completedDeliveries
```

---

# 14. Driver Pool

`src/engines/DriverPool.java` must become a proper rider management system.

It should support:

```text
Add Rider
Remove Rider
Find Rider
Update Location
Set Available
Set Busy
Get Available Riders
Get Riders by Vehicle Type
Find Nearest Rider
```

The Driver Pool should use appropriate custom data structures.

---

# 15. Rider Assignment

When a new order arrives, the system must determine the best rider.

The algorithm should:

```text
1. Get pickup location
2. Get delivery location
3. Calculate delivery distance
4. Determine eligible vehicle types
5. Retrieve available riders
6. Filter ineligible riders
7. Calculate rider-to-pickup distance
8. Estimate delivery time
9. Calculate assignment score
10. Select best rider
```

---

# 16. Bicycle and Motorcycle Optimization

The system contains both bicycle and motorcycle riders.

A configurable distance threshold should be implemented.

Example:

```text
MAX_BICYCLE_DISTANCE = 6 km
```

Rules:

```text
Distance ≤ 6 km
→ Bicycle and motorcycle riders may be considered

Distance > 6 km
→ Bicycle riders excluded
→ Motorcycle riders considered
```

This value should be configurable rather than hard-coded throughout the application.

---

# 17. Nearest Rider Algorithm

The system should calculate the distance between:

```text
Rider Current Location
        ↓
Pickup Location
```

A basic nearest-rider algorithm can evaluate every eligible rider.

However, the final optimization should consider more than distance alone.

---

# 18. Optimization Engine

`src/engines/OptimisationEngine.java` should be significantly improved.

The engine should determine:

```text
Which rider?
Which vehicle?
Which order?
Which route?
```

It should consider:

* Pickup distance
* Delivery distance
* Estimated delivery time
* Vehicle suitability
* Rider availability
* Rider workload
* Order priority
* Current rider location

---

# 19. Assignment Score

A scoring model can be introduced:

```text
Assignment Score =
    Pickup Distance
    + Estimated Delivery Time
    + Workload Penalty
    + Vehicle Penalty
    - Priority Benefit
```

Weights can be configurable.

The objective is to minimize delivery time while respecting operational constraints.

---

# 20. Incoming Order Manager

`src/engines/IncomingOrderManager.java` should become the entry point for new requests.

Expected workflow:

```text
ServiceRequest
      ↓
Validate
      ↓
Create Order
      ↓
Calculate Distance
      ↓
Determine Priority
      ↓
Queue
      ↓
Schedule
      ↓
Optimize
      ↓
Assign Rider
```

It should not simply append requests to a collection.

---

# 21. Scheduling Engine

`src/engines/SchedulingEngine.java` must perform actual scheduling.

It should handle:

* Incoming orders
* Priority
* Requested delivery time
* Rider availability
* Scheduling sequence
* Delivery deadlines
* Assignment decisions

A priority queue or `MinHeap` should be considered for order prioritization.

---

# 22. Priority Scheduling

The scheduling system should account for:

```text
Order Priority
+
Waiting Time
+
Requested Delivery Time
+
Delivery Constraints
```

An order should not necessarily be processed purely according to insertion order.

---

# 23. Route Engine

`src/engines/RouteEngine.java` should operate using:

```text
Location
RoadEdge
Graph
MinHeap
```

The road data already exists:

```text
data/locations.csv
data/roads.csv
```

The route engine should convert this information into a usable campus graph.

---

# 24. Shortest Path

A shortest-path algorithm such as Dijkstra's algorithm should be implemented using the graph and appropriate priority structure.

Expected flow:

```text
Campus Road Graph
       ↓
Dijkstra
       ↓
MinHeap
       ↓
Shortest Route
       ↓
Distance
       ↓
Estimated Travel Time
```

The algorithm execution should be recorded in `AlgorithmRun`.

---

# 25. Geographic Distance

For latitude/longitude calculations, the Haversine formula should be considered.

It can be used for:

* Rider-to-pickup distance
* Pickup-to-delivery distance
* Vehicle selection
* Delivery estimation

For actual road navigation, the campus graph should be preferred over straight-line geographic distance.

---

# 26. Delivery Engine

`src/engines/DeliveryEngine.java` should manage the complete delivery lifecycle.

```text
Order Assigned
      ↓
Rider Travels to Pickup
      ↓
Pickup
      ↓
Travel to Destination
      ↓
Delivery
      ↓
Complete Order
      ↓
Update Rider
```

The engine must update:

* Order status
* Rider status
* Rider location
* Delivery statistics
* Audit events

---

# 27. Rider Location Tracking

After completing a delivery, the rider's current location must become the delivery destination.

Example:

```text
Rider:
Location A

Pickup:
Location B

Delivery:
Location C

After completion:
Rider → Location C
```

This is critical for the next optimization decision.

---

# 28. Indexing Engine

`src/engines/IndexingEngine.java` should provide efficient lookup.

Possible indexes:

```text
Order ID
Resource ID
Location ID
Service Request ID
```

The engine should demonstrate practical applications of:

```text
HashTable
BST
BTree
RedBlackTree
```

---

# 29. Sorting Engine

`src/engines/SortingEngine.java` should provide meaningful sorting functionality.

Potential operations:

```text
Sort orders by priority
Sort orders by delivery time
Sort riders by distance
Sort riders by workload
Sort deliveries by completion time
```

Sorting algorithms should be benchmarked where applicable.

---

# 30. Database Manager

`src/engines/DatabaseManager.java` should handle persistence.

The database should store important information such as:

```text
Orders
Resources/Riders
Locations
Deliveries
Audit Events
Algorithm Runs
```

The database should complement the custom DSA implementations rather than replace them.

---

# 31. Algorithm Run Tracking

`src/models/AlgorithmRun.java` should record actual algorithm executions.

For example:

```text
Algorithm: Dijkstra
Input Size: 184 vertices
Execution Time: 1.42 ms
Operations: 782
Result: Route found
```

The same mechanism should be usable for:

* Searching
* Sorting
* Routing
* Scheduling
* Optimization

---

# 32. Audit Events

`src/models/AuditEvent.java` should record important events.

Examples:

```text
ORDER_CREATED
ORDER_QUEUED
ORDER_ASSIGNED
RIDER_STATUS_CHANGED
ORDER_PICKED_UP
ORDER_DELIVERED
ORDER_CANCELLED
ROUTE_CALCULATED
ALGORITHM_EXECUTED
```

This provides traceability.

---

# 33. Simulation Engine

### Recommended Addition

Create:

```text
src/engines/SimulationEngine.java
```

The Simulation Engine should allow the system to simulate realistic campus operations.

Example:

```text
100 Orders
20 Riders
10 Bicycles
10 Motorcycles
```

The simulation should generate:

* Incoming orders
* Different priorities
* Rider availability
* Deliveries
* Cancellations
* Rider movement
* Peak demand
* Multiple simultaneous orders

---

# 34. Metrics Engine

### Recommended Addition

Create:

```text
src/engines/MetricsEngine.java
```

It should track:

```text
Total Orders
Completed Orders
Cancelled Orders
Average Delivery Time
Average Pickup Distance
Average Delivery Distance
Rider Utilisation
Orders Per Rider
Bicycle Deliveries
Motorcycle Deliveries
Algorithm Execution Times
```

---

# 35. Benchmarking

The project should have a dedicated benchmark mode.

For example:

```text
100 records
1,000 records
10,000 records
100,000 records
```

Compare:

```text
Linear Search
vs
BST
vs
Red-Black Tree
vs
B-Tree
vs
HashTable
```

Where appropriate, compare:

```text
Sorting Algorithms
Route Algorithms
Scheduling Strategies
Optimization Strategies
```

Record:

```text
Input Size
Execution Time
Operations
Comparisons
```

---

# 36. Report Engine

### Recommended Addition

Create:

```text
src/engines/ReportEngine.java
```

The report engine could produce:

```text
UG SWIFT PERFORMANCE REPORT

Orders Processed:       1,000
Orders Completed:       987
Average Delivery Time:  14.3 min
Average Pickup Distance: 0.84 km

Bicycle Deliveries:     412
Motorcycle Deliveries:  575

Average Scheduling Time:  X ms
Average Optimization Time: X ms
Average Routing Time:    X ms
```

---

# 37. Route Caching

### Recommended Optimization

Introduce route caching using the custom `HashTable`.

Example:

```text
Location A → Location B
        ↓
     HashTable
        ↓
Cached Route
```

If the same route is requested repeatedly, the system can reuse the previous result.

This also provides another practical use for hashing.

---

# 38. Rider Workload Balancing

Optimization should not rely entirely on distance.

Example:

```text
Rider A → 10 deliveries
Rider B → 2 deliveries
Rider C → 1 delivery
```

Even if Rider A is closest, assigning another order to Rider A may produce poor overall performance.

The optimizer should therefore consider:

```text
Distance
+
Current Workload
+
Estimated Delivery Time
```

---

# 39. Cancellation and Reassignment

The system should support:

```text
Assigned Rider
      ↓
Rider Unavailable
      ↓
Order Reassignment
```

and:

```text
Order
 ↓
Cancelled
 ↓
Rider Released
 ↓
Next Order Scheduled
```

This is important for testing dynamic behavior.

---

# 40. Configuration

### Recommended Addition

Create:

```text
src/utils/Config.java
```

Centralize configuration such as:

```text
MAX_BICYCLE_DISTANCE
DEFAULT_BICYCLE_SPEED
DEFAULT_MOTORCYCLE_SPEED
MAX_ORDER_QUEUE_SIZE
DEFAULT_ORDER_PRIORITY
```

Avoid scattering hard-coded values throughout the codebase.

---

# 41. Enums

### Recommended Addition

Use Java enums for system states.

Potential enums:

```text
VehicleType
DriverStatus
OrderStatus
OrderPriority
DeliveryStatus
```

Example:

```java
VehicleType.BICYCLE
VehicleType.MOTORCYCLE
```

This is preferable to repeatedly using string values.

---

# 42. Assignment Result

### Recommended Addition

Create:

```text
src/models/AssignmentResult.java
```

The result should contain:

```text
Selected Rider
Vehicle Type
Pickup Distance
Delivery Distance
Estimated Delivery Time
Assignment Score
Algorithm Used
```

This allows the system to explain **why a rider was selected**.

---

# 43. DSA Trace / Demonstration Mode

### High-Priority Recommendation

Create a demonstration mode that explicitly shows how the DSA components are being used.

Example:

```text
NEW ORDER #1024
       ↓
Queue.enqueue()
       ↓
Priority = HIGH
       ↓
SchedulingEngine
       ↓
MinHeap.insert()
       ↓
RouteEngine
       ↓
Dijkstra()
       ↓
DriverPool
       ↓
HashTable lookup
       ↓
Nearest Rider
       ↓
OptimisationEngine
       ↓
Motorcycle selected
       ↓
DeliveryEngine
       ↓
ORDER COMPLETED
```

This feature is especially useful during project demonstrations.

It allows the examiner to see:

> This is where the Queue is used.

> This is where the MinHeap is used.

> This is where the Graph is used.

> This is where the HashTable is used.

> This is where the Red-Black Tree is used.

> This is where the optimization algorithm is executed.

---

# 44. End-to-End Testing

`UGSwiftTestSuite.java` must contain an actual end-to-end test.

Example:

```text
Create ServiceRequest
        ↓
Create Order
        ↓
Queue Order
        ↓
Schedule Order
        ↓
Calculate Route
        ↓
Find Eligible Riders
        ↓
Optimize Assignment
        ↓
Assign Rider
        ↓
Complete Delivery
        ↓
Update Rider
        ↓
Persist Database Record
        ↓
Record Audit Event
        ↓
Record AlgorithmRun
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

# 45. Required Test Scenarios

## Data Structure Tests

Test all:

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

---

## Engine Tests

Test:

```text
DriverPool
IncomingOrderManager
SchedulingEngine
OptimisationEngine
RouteEngine
DeliveryEngine
SortingEngine
IndexingEngine
DatabaseManager
```

---

## Vehicle Test

```text
Delivery Distance = 8 km

Bicycle = Available
Motorcycle = Available

Expected:
Bicycle rejected
Motorcycle selected
```

---

## Short-Distance Test

```text
Delivery Distance = 3 km

Bicycle = Available
Motorcycle = Available

Expected:
Both eligible
Best candidate selected
```

---

## Nearest-Rider Test

```text
Rider A = 1.2 km
Rider B = 3.4 km
Rider C = 0.8 km

Expected:
Rider C selected
```

provided all other constraints are satisfied.

---

## No-Rider Test

```text
All riders = BUSY

New order arrives

Expected:
Order remains queued
No invalid assignment occurs
```

---

## Multiple-Order Test

Example:

```text
10 Orders
5 Riders
```

Verify:

* Orders are scheduled correctly
* Riders are not double-assigned
* Priority is respected
* Vehicle restrictions work
* Riders become available after completion
* Orders eventually complete

---

# 46. Complexity Analysis

Every major algorithm must have documented complexity.

Examples:

| Operation             |       Complexity |
| --------------------- | ---------------: |
| Stack Push            |             O(1) |
| Stack Pop             |             O(1) |
| Queue Enqueue         |             O(1) |
| Queue Dequeue         |             O(1) |
| HashTable Search      |     O(1) average |
| BST Search            | O(log n) average |
| Red-Black Tree Search |         O(log n) |
| B-Tree Search         |         O(log n) |
| Heap Insert           |         O(log n) |
| Heap Remove           |         O(log n) |
| Dijkstra + MinHeap    | O((V + E) log V) |
| Linear Search         |             O(n) |

The documented complexity must reflect the actual implementation.

---

# 47. Performance Analysis

The project should provide empirical evidence.

For example:

```text
Dataset Size | HashTable | BST | RedBlackTree | BTree
-------------------------------------------------------
100
1,000
10,000
100,000
```

Measure:

```text
Execution Time
Number of Operations
Comparisons
Memory where practical
```

The goal is to compare theoretical complexity with actual observed performance.

---

# 48. Campus Map Integration

The existing:

```text
web/campus_map/
```

should visualize the actual campus data.

It should support visualization of:

* Campus locations
* Roads
* Pickup locations
* Delivery locations
* Rider locations
* Generated routes

The route displayed on the map should originate from the backend `RouteEngine` rather than implementing a separate routing algorithm in JavaScript.

---

# 49. Tools

The following tools should remain focused on their intended purposes:

```text
CampusMapLauncher.java
ExportLocations.java
ExportRoute.java
```

Routing logic should remain inside:

```text
RouteEngine.java
```

Avoid duplicating routing algorithms across tools.

---

# 50. Application Layer

The following should primarily coordinate the application:

```text
Main.java
UGSwiftApp.java
UGSwiftLauncher.java
```

They should not contain excessive business logic.

The preferred architecture is:

```text
Application
    ↓
Engines
    ↓
Models
    ↓
Data Structures
```

---

# 51. Repository Cleanup

Generated `.class` files should not normally be committed to the repository.

Add:

```gitignore
bin/
*.class
```

to `.gitignore`.

The source code in:

```text
src/
```

should remain the source of truth.

---

# 52. Final Definition of Done

## Data Structures

* [x] All custom data structures compile.
* [x] All custom data structures pass dedicated tests.
* [x] Stack is integrated into scheduling.
* [x] BTree is integrated into a real operation.
* [x] RedBlackTree is integrated into a real operation.
* [x] Graph is used by RouteEngine.
* [x] MinHeap is used for priority/shortest-path operations.
* [x] HashTable is used for efficient lookup.
* [x] Queue/Priority Queue is used for incoming/scheduled orders.

## Models

* [x] Order model is sufficiently detailed.
* [x] Resource model represents riders.
* [x] Bicycle and motorcycle riders are supported.
* [x] Rider availability is tracked.
* [x] Rider location is tracked.
* [x] Order lifecycle is tracked.
* [x] AlgorithmRun records algorithm performance.
* [x] AuditEvent records important system events.

## Engines

* [x] DriverPool works correctly.
* [x] IncomingOrderManager processes requests.
* [x] SchedulingEngine performs real scheduling.
* [x] OptimisationEngine performs real optimization.
* [x] DeliveryEngine manages complete deliveries.
* [x] RouteEngine calculates routes.
* [x] SortingEngine performs meaningful sorting.
* [x] IndexingEngine performs efficient lookup.
* [x] DatabaseManager persists required data.

## Optimization

* [x] Pickup distance is calculated.
* [x] Delivery distance is calculated.
* [x] Rider availability is considered.
* [x] Rider location is considered.
* [x] Vehicle type is considered.
* [x] Bicycle riders are excluded beyond 6 km.
* [x] Motorcycle riders are considered for long-distance deliveries.
* [x] Rider workload can influence assignment.
* [x] Assignment score is calculated.
* [x] Best eligible rider is selected.

## Testing

* [x] Data structure tests completed.
* [x] Engine tests completed.
* [x] Vehicle restriction tests completed.
* [x] Nearest-rider tests completed.
* [x] Multiple-order tests completed.
* [x] No-rider tests completed.
* [x] Cancellation/reassignment tests completed.
* [x] End-to-end delivery test completed.

## Performance

* [x] AlgorithmRun integrated.
* [x] Benchmarking implemented.
* [x] Algorithm execution times recorded.
* [x] Complexity documented.
* [x] Search algorithms compared.
* [x] Sorting algorithms compared.
* [x] Routing performance measured.
* [x] Scheduling performance measured.
* [x] Optimization performance measured.

## Optional Advanced Features

* [x] SimulationEngine (`src/engines/SimulationEngine.java`)
* [x] MetricsEngine (`src/engines/MetricsEngine.java`)
* [x] ReportEngine (`src/engines/ReportEngine.java`)
* [x] Route caching (`src/engines/RouteEngine.java`)
* [x] Rider workload balancing (`DeliveryEngine` & `OptimisationEngine`)
* [x] Cancellation/reassignment (`DeliveryEngine` & `UGSwiftApp`)
* [x] DSA Trace Mode (`UGSwiftTestSuite.java` & Panel 3 UI)
* [x] Benchmark Mode (`BenchmarkEngine` & `G2BenchmarkRunner`)
* [x] Config class (`src/utils/Config.java`)
* [x] Enums (`VehicleType.java`, `AuditEventType.java`, `OrderState`)
* [x] AssignmentResult (Scoring breakdown in `OptimisationEngine`)

---

# 53. Recommended Implementation Order

Do **not** attempt to implement everything simultaneously.

Follow this order:

```text
PHASE 1
Data Structures
      ↓
PHASE 2
Models
      ↓
PHASE 3
RouteEngine
      ↓
PHASE 4
DriverPool
      ↓
PHASE 5
IncomingOrderManager
      ↓
PHASE 6
SchedulingEngine
      ↓
PHASE 7
OptimisationEngine
      ↓
PHASE 8
DeliveryEngine
      ↓
PHASE 9
Database Integration
      ↓
PHASE 10
Testing
      ↓
PHASE 11
Benchmarking
      ↓
PHASE 12
Simulation
      ↓
PHASE 13
DSA Demonstration Mode
```

---

# 54. Final Goal

The finished UG Swift project should be able to demonstrate the following scenario:

```text
A student requests a delivery.

        ↓

The request enters the system.

        ↓

The Queue/Priority Queue stores the request.

        ↓

The Scheduling Engine determines its priority.

        ↓

The Route Engine calculates the delivery distance.

        ↓

The Driver Pool retrieves available riders.

        ↓

The Optimization Engine filters riders by:
- availability
- vehicle
- distance
- workload
- delivery time

        ↓

If the delivery is > 6 km,
bicycle riders are excluded.

        ↓

The nearest/best eligible rider is selected.

        ↓

The Route Engine calculates the optimal campus route.

        ↓

The Delivery Engine processes:
Pickup → Transit → Delivery.

        ↓

The rider's location is updated.

        ↓

The rider becomes available.

        ↓

The order is marked COMPLETED.

        ↓

The database is updated.

        ↓

An AuditEvent is recorded.

        ↓

AlgorithmRun records performance.

        ↓

Metrics are updated.
```

This is the standard the final implementation should aim for.

The project should ultimately demonstrate not only that **UG Swift works**, but also **why each Data Structure and Algorithm was selected, where it is used, how efficiently it performs, and how it contributes to solving the campus delivery problem.**
